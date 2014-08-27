package com.busymachines.commons.elasticsearch

import java.util.UUID

import scala.collection.JavaConversions.{asScalaBuffer, asScalaSet}
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Success

import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.index.engine.VersionConflictEngineException
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import org.elasticsearch.search.facet.{FacetBuilder, FacetBuilders}
import org.elasticsearch.search.facet.histogram.HistogramFacet
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.transport.RemoteTransportException

import com.busymachines.commons.CommonException
import com.busymachines.commons.dao.{Facet, HistogramFacetValue, IdNotFoundException, MoreThanOneResultException, Page, RetryVersionConflictAsync, ScrollIterator, SearchCriteria, SearchResult, SearchSort, TermFacetValue, VersionConflictException, Versioned}
import com.busymachines.commons.logging.Logging
import com.busymachines.commons.util.JsonParser

import spray.json.{JsNumber, JsObject, JsString, JsValue}

/**
 * Collection of documents represented by type.
 */
class ESCollection[T](val index: ESIndex, val typeName: String, val mapping: ESMapping[T])(implicit ec: ExecutionContext) extends Logging {
  def this(index: ESIndex, esType: ESType[T])(implicit ec: ExecutionContext) =
    this(index, esType.name, esType.mapping)

  implicit val client = index.client
  val indexName = index.indexName
  val eventName = mapping.mappingName + "_" + indexName + "_" + typeName
  val all: SearchCriteria[T] = ESSearchCriteria.All[T]()
  val defaultSort: SearchSort = ESSearchSort.asc("_id")

  // Add mapping.
  index.onInitialize { () => index.addMapping(typeName, mapping) }

  def retrieve(id: String): Future[Option[Versioned[T]]] = {
    val request = new GetRequest(indexName, typeName, id.toString)
    client.execute(request) map {
      case response if !response.isExists =>
        None
      case response if response.isSourceEmpty =>
        logger.error(s"No source available for $typeName with id $id", "typeName" -> typeName, "id" -> id)
        None
      case response =>
        val json = JsonParser.parse(response.getSourceAsString)
        Some(Versioned(mapping.jsonFormat.read(json), response.getVersion))
    }
  }

  def retrieve(ids: Seq[String]): Future[List[Versioned[T]]] =
    search(new ESSearchCriteria[T] {
      def toFilter = FilterBuilders.idsFilter(typeName).addIds(ids: _*)

      def prepend[A0](path: ESPath[A0, T]) = ???
    }, page = Page.all).map(_.result)

  def retrieveAll: Future[List[Versioned[T]]] =
    search(all, page = Page.all).map(_.result)

  def scan(criteria: SearchCriteria[T], duration: FiniteDuration, batchSize: Int): Iterator[T] =
    new ScrollIterator[T](col = this, criteria = criteria, duration = duration, size = batchSize)

  private def logInvalidDocument(t: Throwable, json: JsValue, result: mutable.Buffer[Versioned[T]]) =
    logger.error(s"Fetched invalid $typeName: ${t.getMessage}: $json (document skipped)", t)

  def search(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty, invalidDocument: (Throwable, JsValue, mutable.Buffer[Versioned[T]]) => Unit = logInvalidDocument): Future[SearchResult[T]] = {
    criteria match {
      case criteria: ESSearchCriteria[T] =>

        var request =
          client.javaClient.prepareSearch(indexName)
            .setTypes(typeName)
            .setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), criteria.toFilter))
            .setSearchType(SearchType.DFS_QUERY_AND_FETCH)
            .setFrom(page.from)
            .setSize(page.size)
            .setVersion(true)

        // Sorting
        sort match {
          case esSearchOrder: ESSearchSort =>
            request = request.addSort(esSearchOrder.field, esSearchOrder.order)
        }

        // Faceting
        val requestFacets = toESFacets(facets)
        for (facet <- requestFacets) {
          request = request.addFacet(facet._2)
        }

        //        println(request.toString)

        for {
          result <- client.execute(request.request)
          hits = result.getHits.hits
          builder = new mutable.ListBuffer[Versioned[T]]
          _ = for {
            hit <- hits
            json = JsonParser.parse(hit.sourceAsString)
            _ = try {
              builder += Versioned(mapping.jsonFormat.read(json), hit.version)
            } catch {
              case t: Throwable =>
                invalidDocument(t, json, builder)
            }
          } yield ""
        } yield SearchResult(builder.result(), Some(result.getHits.getTotalHits), if (result.getFacets != null) convertESFacetResponse(facets, result) else Map.empty)

      //        client.execute (request.request).map { result =>
      //          SearchResult (result.getHits.hits.toList.map { hit =>
      //            val json = JsonParser.parse(hit.sourceAsString)
      //            Versioned(mapping.jsonFormat.read(json), hit.version)
      //          }, Some (result.getHits.getTotalHits),
      //            if (result.getFacets != null) convertESFacetResponse (facets, result) else Map.empty)
      //        }
      case _ =>
        throw new Exception("Expected ElasticSearch search criteria")
    }
  }

  def searchSingle(criteria: SearchCriteria[T], onMany: List[Versioned[T]] => Versioned[T] = _ => throw new MoreThanOneResultException): Future[Option[Versioned[T]]] = {
    search(criteria).map {
      case SearchResult(Nil, _, _) => None
      case SearchResult(first :: Nil, _, _) => Some(first)
      case SearchResult(many, _, _) =>
        try {
          Some(onMany(many))
        } catch {
          case t: MoreThanOneResultException =>
            val exc = new MoreThanOneResultException(s"Search criteria $criteria returned more than one result and should return at most one result. Database probably inconsistent. The returned results were :$many")
            logger.error("More than one result retrieved for criteria", exc)
            throw exc
        }
    }
  }

  def bulk(list: Seq[T]): Unit = {
    val bulkRequest = client.javaClient.prepareBulk()
    list.foreach(o => bulkRequest.add(client.javaClient.prepareIndex(indexName, typeName).setSource(mapping.jsonFormat.write(o).toString)))
    bulkRequest.execute()
  }

  def create(entity: T, refreshAfterMutation: Boolean, ttl: Option[Duration] = None): Future[Versioned[T]] = {
    val json = mapping.jsonFormat.write(entity)
    val id: String = getIdFromJson(json)
    val request = new IndexRequest(indexName, typeName)
      .id(id)
      .create(true)
      .source(json.toString)
      .refresh(refreshAfterMutation)
      .ttl(ttl.map(_.toMillis).map(new java.lang.Long(_)).getOrElse(null))

    // Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.
    //      val response = client.execute(IndexAction.INSTANCE, request).get
    //      Future.successful(Versioned(entity, response.getVersion.toString))

    client.execute(request).map(response => Versioned(entity, response.getVersion))
      .andThen {
        case Success(v) =>
        //        index.eventBus.publish (ESRootDaoMutationEvent (eventName, id))
      }
      .recover(convertException { e =>
        logger.error(s"Create ${indexName}/${typeName}/${entity.toString} failed :\n${XContentHelper.convertToJson(request.source, true, true)}", e)
        throw e
      })
  }

  def getOrCreate(id: String, refreshAfterMutation: Boolean)(_create: => T): Future[Versioned[T]] = {
    retrieve(id) flatMap {
      case Some(entity) => Future.successful(entity)
      case None => create(_create, refreshAfterMutation)
    }
  }

  def getOrCreateAndModify(id: String, refreshAfterMutation: Boolean)(_create: => T)(_modify: T => T): Future[Versioned[T]] = {
    RetryVersionConflictAsync(10) {
      retrieve(id).flatMap {
        case None => create(_modify(_create), refreshAfterMutation)
        case Some(Versioned(entity, version)) =>
          update(Versioned(_modify(entity), version), refreshAfterMutation)
      }
    }
  }

  def getOrCreateAndModifyOptionally(id: String, refreshAfterMutation: Boolean)(_create: => T)(_modify: T => Option[T]): Future[Versioned[T]] = {
    RetryVersionConflictAsync(10) {
      retrieve(id).flatMap {
        case None =>
          val created: T = _create
          create(_modify(created).getOrElse(created), refreshAfterMutation)
        case Some(Versioned(entity, version)) =>
          _modify(entity) match {
            case Some(newEntity) => update(Versioned(newEntity, version), refreshAfterMutation)
            case None => Future.successful(Versioned(entity, version))
          }
      }
    }
  }

  def modify(id: String, refreshAfterMutation: Boolean)(modify: T => T): Future[Versioned[T]] = {
    RetryVersionConflictAsync(10) {
      retrieve(id).flatMap {
        case None => throw new IdNotFoundException(id.toString, typeName)
        case Some(Versioned(entity, version)) =>
          update(Versioned(modify(entity), version), refreshAfterMutation)
      }
    }
  }

  def modifyOptionally(id: String, refreshAfterMutation: Boolean)(modify: T => Option[T]): Future[Versioned[T]] = {
    RetryVersionConflictAsync(10) {
      retrieve(id).flatMap {
        case None => throw new IdNotFoundException(id.toString, typeName)
        case Some(Versioned(entity, version)) =>
          modify(entity) match {
            case Some(newEntity) => update(Versioned(newEntity, version), refreshAfterMutation)
            case None => Future.successful(Versioned(entity, version))
          }
      }
    }
  }

  /**
   * @throws VersionConflictException
   */
  def update(entity: Versioned[T], refreshAfterMutation: Boolean): Future[Versioned[T]] = {
    val newJson = mapping.jsonFormat.write(entity.entity)

    val id: String = getIdFromJson(newJson)

    val request = new IndexRequest(indexName, typeName)
      .refresh(refreshAfterMutation)
      .id(id)
      .source(newJson.toString)
      .version(entity.version)

    // Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.
    //      val response = client.execute(IndexAction.INSTANCE, request).get
    //      Future.successful(Versioned(entity, response.getVersion.toString))
    client.execute(request).map(response => Versioned(entity.entity, response.getVersion))
      .andThen {
        case Success(v) =>
        //        index.eventBus.publish (ESRootDaoMutationEvent (eventName, id))
      }
      .recover(convertException { e =>
        logger.trace(s"Create $indexName/$typeName/$entity failed :\n${XContentHelper.convertToJson(request.source, true, true)}", e)
        throw e
      })
  }

  def delete(id: String, refreshAfterMutation: Boolean): Future[Unit] = {
    val request = new DeleteRequest(indexName, typeName, id.toString).refresh(refreshAfterMutation)
    client.execute(request) map {
      response =>
        if (!response.isFound) {
          throw new IdNotFoundException(id, typeName)
        } else {
          //          index.eventBus.publish (ESRootDaoMutationEvent (eventName, id))
        }
    }
  }

  def reindexAll() = {
    index.refresh
  }

  def onChange(f: String => Unit) {
    //    index.eventBus.subscribe {
    //      case ESRootDaoMutationEvent (n, id) if n == eventName =>
    //        f (id)
    //    }
  }

  /**
   * Escapes a string special ES characters as specified here : {@link http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#_reserved_characters}
   */
  private[elasticsearch] def escapeQueryText(s: String): String = {
    var b0: StringBuilder = null
    def b = {
      if (b0 == null) b0 = new StringBuilder; b0
    }
    var p = 0
    val l = s.length
    while (p < l) {
      val c = s.charAt(p)
      p += 1
      c match {
        case '+' => b.append("\\+")
        case '-' => b.append("\\-")
        case '!' => b.append("\\!")
        case '(' => b.append("\\(")
        case ')' => b.append("\\)")
        case '{' => b.append("\\{")
        case '}' => b.append("\\}")
        case '[' => b.append("\\[")
        case ']' => b.append("\\]")
        case '^' => b.append("\\^")
        case '\"' => b.append("\\\"")
        case '~' => b.append("\\~")
        case '*' => b.append("\\*")
        case '?' => b.append("\\?")
        case ':' => b.append("\\:")
        case '\\' => b.append("\\\\")
        case ' ' => b.append("\\ ")
        case '&' if p < l && s.charAt(p) == '&' => b.append("\\&")
        case '|' if p < l && s.charAt(p) == '|' => b.append("\\|")
        case c => b.append(c)
      }
    }
    if (b0 == null) s
    else b0.toString
  }

  private def toESFacets(facets: Seq[Facet]): Map[Facet, FacetBuilder] =
    facets.map {
      case historyFacet: ESHistoryFacet =>
        (historyFacet.keyScript, historyFacet.valueScript, historyFacet.interval) match {
          case (Some(keyScript), Some(valueScript), Some(interval)) =>
            historyFacet -> FacetBuilders.histogramScriptFacet(historyFacet.name).facetFilter(historyFacet.searchCriteria.asInstanceOf[ESSearchCriteria[_]].toFilter).keyScript(keyScript).valueScript(valueScript).interval(interval)
          case _ => {
            val exc = new CommonException(message = s"Only script histogram facets are supported for now", cause = None, parameters = Map.empty)
            logger.error("Only ES facets supported for now. Retrieved: {}", exc)
            throw exc

          }
        }

      // TODO : Investigate if this is generic enough : http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-facets.html#_all_nested_matching_root_documents
      case termFacet: ESTermFacet =>
        val fieldList = termFacet.fields.map(_.toString)
        val firstFacetField = fieldList.head
        val pathComponents = firstFacetField.split("\\.").toList
        val facetbuilder = pathComponents.size match {
          case s if s <= 1 => FacetBuilders.termsFacet(termFacet.name)
          case s if s > 1 => FacetBuilders.termsFacet(termFacet.name).nested(pathComponents.head)
        }

        termFacet -> facetbuilder.size(termFacet.size).facetFilter(termFacet.searchCriteria.asInstanceOf[ESSearchCriteria[_]].toFilter).fields(fieldList: _*)
      case _ => throw new Exception(s"Unknown facet type")
    }.toMap

  private def convertESFacetResponse(facets: Seq[Facet], response: org.elasticsearch.action.search.SearchResponse) =
    response.getFacets.facetsAsMap.entrySet.map { entry =>
      val facet = facets.collectFirst { case x if x.name == entry.getKey => x }.getOrElse(throw new CommonException(message = s"The ES response contains facet ${entry.getKey} that were not requested", cause = None, parameters = Map.empty))
      entry.getValue match {
        case histogramFacet: HistogramFacet =>
          facet.name -> histogramFacet.getEntries.map(histogramEntry =>
            HistogramFacetValue(key = histogramEntry.getKey,
              count = histogramEntry.getCount,
              min = histogramEntry.getMin,
              max = histogramEntry.getMax,
              total = histogramEntry.getTotal,
              total_count = histogramEntry.getTotalCount,
              mean = histogramEntry.getMean)).toList
        case termFacet: TermsFacet =>
          facet.name -> termFacet.getEntries.map(termEntry => TermFacetValue(termEntry.getTerm.string, termEntry.getCount)).toList
        case _ => {
          val exc = new CommonException(message = s"Only script histogram facets are supported for now. Unknown facet:${entry.getValue()}", cause = None, parameters = Map.empty)
          logger.error("Only ES facets supported for now. Retrieved: {}", exc)
          throw exc
        }
      }
    }.toMap

  private def convertException(f: Throwable => Versioned[T]): PartialFunction[Throwable, Versioned[T]] = {
    case t: Throwable =>
      val cause = t match {
        case t: RemoteTransportException => t.getCause
        case t2 => t2
      }
      val converted = cause match {
        case t: VersionConflictEngineException => new VersionConflictException(t)
        case t2 => t2
      }
      f(converted)
  }

  private def getIdFromJson(json: JsValue): String =
    json match {
      case JsObject(fields) =>
        fields.get("_id") match {
          case Some(JsString(id)) => id.toString
          case Some(JsNumber(id)) => id.toString
          case _ => UUID.randomUUID.toString
        }
      case _ => UUID.randomUUID.toString
    }
}
