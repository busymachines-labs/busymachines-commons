package com.busymachines.commons.elasticsearch

import java.util.UUID

import org.elasticsearch.action.bulk.{BulkItemResponse, BulkResponse}
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.search.fetch.source.FetchSourceContext

import scala.collection.JavaConversions.{asScalaBuffer, asScalaSet}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Try, Failure, Success}

import org.elasticsearch.action.delete.{DeleteRequestBuilder, DeleteRequest}
import org.elasticsearch.action.get.{MultiGetRequest, MultiGetRequestBuilder, GetRequest}
import org.elasticsearch.action.index.{IndexRequestBuilder, IndexRequest}
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.index.engine.VersionConflictEngineException
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import org.elasticsearch.search.facet.{FacetBuilder, FacetBuilders}
import org.elasticsearch.search.facet.histogram.HistogramFacet
import org.elasticsearch.search.facet.terms.TermsFacet
import org.elasticsearch.transport.RemoteTransportException

import com.busymachines.commons.CommonException
import com.busymachines.commons.dao._
import com.busymachines.commons.logging.Logging
import com.busymachines.commons.util.JsonParser
import scala.collection.JavaConversions._

import spray.json.{JsNumber, JsObject, JsString, JsValue}

/**
 * Collection of documents represented by type.
 */
class ESCollection[T](val index: ESIndex, val typeName: String, val mapping: ESMapping[T])(implicit ec: ExecutionContext) extends Logging {
  def this(index: ESIndex, esType: ESType[T])(implicit ec: ExecutionContext) =
    this(index, esType.name, esType.mapping)

  private val EsIDField: String = "_id"

  implicit val client = index.client
  val indexName = index.indexName
  val eventName = mapping.mappingName + "_" + indexName + "_" + typeName
  val all: SearchCriteria[T] = ESSearchCriteria.All[T]()

  val defaultSort: SearchSort = ESSearchSort.asc(EsIDField)

  // Add mapping.
  index.onInitialize { () => index.addMapping(typeName, mapping) }


  object versioned {
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

    def find(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, invalidDocument: (Throwable, JsValue, mutable.Buffer[Versioned[T]]) => Unit = logInvalidDocument): Future[List[Versioned[T]]] =
      search(criteria, page, sort, Seq.empty, invalidDocument).map(result => result.result)

    def findSingle(criteria: SearchCriteria[T], onMany: List[Versioned[T]] => Versioned[T] = _ => throw new MoreThanOneResultException): Future[Option[Versioned[T]]] = {
      search(criteria).map {
        case VersionedSearchResult(Nil, _, _) => None
        case VersionedSearchResult(first :: Nil, _, _) => Some(first)
        case VersionedSearchResult(many, _, _) =>
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

    //TODO Removed count because it's based on query not on filters and for now we support just filters
    /*def count(criteria: SearchCriteria[T]):Future[Long] = criteria match {
      case criteria: ESSearchCriteria[T] =>
        client.execute(client.javaClient.prepareCount(indexName)
          .setTypes(typeName)
          .setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), criteria.toFilter))
          .request).map(_.getCount)

      case _ =>
        throw new Exception("Expected ElasticSearch search criteria")
    }*/

    def search(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty, invalidDocument: (Throwable, JsValue, mutable.Buffer[Versioned[T]]) => Unit = logInvalidDocument): Future[VersionedSearchResult[T]] = {
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
          } yield VersionedSearchResult(builder.result(), Some(result.getHits.getTotalHits), if (result.getFacets != null) convertESFacetResponse(facets, result) else Map.empty)

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

    def create(entity: T, refresh: Boolean, ttl: Option[Duration] = None): Future[Versioned[T]] = {
      val json = mapping.jsonFormat.write(entity)
      val id: String = getIdFromJson(json)
      val request = new IndexRequest(indexName, typeName)
        .id(id)
        .create(true)
        .source(json.toString)
        .refresh(refresh)
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
        logger.error(s"Create ${indexName }/${typeName }/${entity.toString } failed :\n${XContentHelper.convertToJson(request.source, true, true) }", e)
        throw e
      })
    }

    def retrieveOrCreate(id: String, refresh: Boolean)(_create: => T): Future[Versioned[T]] = {
      retrieve(id) flatMap {
        case Some(entity) => Future.successful(entity)
        case None => create(_create, refresh)
      }
    }

    def retrieveOrCreateAndModify(id: String, refresh: Boolean)(_create: => T)(_modify: T => T): Future[Versioned[T]] = {
      RetryVersionConflictAsync(10) {
        retrieve(id).flatMap {
          case None => create(_modify(_create), refresh)
          case Some(Versioned(entity, version)) =>
            update(Versioned(_modify(entity), version), refresh)
        }
      }
    }

    def retrieveOrCreateAndModifyOptionally(id: String, refresh: Boolean)(_create: => T)(_modify: T => Option[T]): Future[Versioned[T]] = {
      RetryVersionConflictAsync(10) {
        retrieve(id).flatMap {
          case None =>
            val created: T = _create
            create(_modify(created).getOrElse(created), refresh)
          case Some(Versioned(entity, version)) =>
            _modify(entity) match {
              case Some(newEntity) => update(Versioned(newEntity, version), refresh)
              case None => Future.successful(Versioned(entity, version))
            }
        }
      }
    }

    def modify(id: String, refresh: Boolean)(modify: Versioned[T] => T): Future[Versioned[T]] = {
      RetryVersionConflictAsync(10) {
        retrieve(id).flatMap {
          case None => throw new IdNotFoundException(id.toString, typeName)
          case Some(v@Versioned(entity, version)) =>
            update(Versioned(modify(v), version), refresh)
        }
      }
    }

    def modifyOptionally(id: String, refresh: Boolean)(modify: Versioned[T] => Option[Versioned[T]]): Future[Versioned[T]] = {
      RetryVersionConflictAsync(10) {
        retrieve(id).flatMap {
          case None => throw new IdNotFoundException(id.toString, typeName)
          case Some(v@Versioned(entity, version)) =>
            modify(v) match {
              case Some(Versioned(newEntity, newVersion)) => update(Versioned(newEntity, newVersion), refresh)
              case None => Future.successful(Versioned(entity, version))
            }
        }
      }
    }

    private def createUpdateRequest(entity: Versioned[T], refresh: Boolean): IndexRequest = {
      val newJson = mapping.jsonFormat.write(entity.entity)

      val id: String = getIdFromJson(newJson)

      val request = new IndexRequest(indexName, typeName)
        .refresh(refresh)
        .id(id)
        .source(newJson.toString)
        .version(entity.version)
      request
    }

    /**
     * @throws VersionConflictException
     */
    def update(entity: Versioned[T], refresh: Boolean): Future[Versioned[T]] = {
      val request: IndexRequest = createUpdateRequest(entity, refresh)

      // Call synchronously, useful for debugging: proper stack trace is reported. TODO make config flag.
      //      val response = client.execute(IndexAction.INSTANCE, request).get
      //      Future.successful(Versioned(entity, response.getVersion.toString))
      client.execute(request).map(response => Versioned(entity.entity, response.getVersion))
        .andThen {
        case Success(v) =>
        //        index.eventBus.publish (ESRootDaoMutationEvent (eventName, id))
      }
        .recover(convertException { e =>
        logger.trace(s"Create $indexName/$typeName/$entity failed :\n${XContentHelper.convertToJson(request.source, true, true) }", e)
        throw e
      })
    }

    def delete(id: String, refresh: Boolean): Future[Long] = {
      val request = new DeleteRequest(indexName, typeName, id.toString).refresh(refresh)
      client.execute(request).map {
        response =>
          if (!response.isFound) {
            throw new IdNotFoundException(id, typeName)
          } else {
            //          index.eventBus.publish (ESRootDaoMutationEvent (eventName, id))
            response.getVersion
          }
      }
    }

    def bulkCreate(toCreate: Seq[T], refreshAfterBulk: Boolean = false)(idOfEntity: T => String): Future[Seq[Try[Versioned[T]]]] = {
      val bulkRequest = client.javaClient.prepareBulk()
      toCreate.foreach { entity: T =>
        val request = client.javaClient.prepareIndex(indexName, typeName).setSource(mapping.jsonFormat.write(entity).toString()).setRefresh(false)
        bulkRequest.add(request.setId(idOfEntity(entity)))
      }
      bulkRequest.setRefresh(refreshAfterBulk)
      org.scalastuff.esclient.ActionMagnet.bulkAction.execute(client.javaClient, bulkRequest.request()) map { bulkResponse: BulkResponse =>
        processBulkResponse(bulkResponse) { response =>
          if (response.isFailed) {
            bulkResponseException(response)
          } else {
            Success(Versioned(response.getResponse, response.getVersion))
          }
        }
      }
    }

    //TODO: the fact that update extracts a
    def bulkUpdate(toUpdate: Seq[Versioned[T]], refreshAfterBulk: Boolean = false)(idOfEntity: T => String): Future[Seq[Try[Versioned[T]]]] = {
      val bulkRequest = client.javaClient.prepareBulk()
      toUpdate.foreach { entity =>
        val request = createUpdateRequest(entity, false)
        bulkRequest.add(request)
      }
      bulkRequest.setRefresh(refreshAfterBulk)
      org.scalastuff.esclient.ActionMagnet.bulkAction.execute(client.javaClient, bulkRequest.request()) map { bulkResponse: BulkResponse =>
        processBulkResponse(bulkResponse) { response =>
          if (response.isFailed) {
            bulkResponseException(response)
          } else {
            Success(Versioned(response.getResponse, response.getVersion))
          }
        }
      }
    }
  }

  def retrieve(id: String): Future[Option[T]] =
    versioned.retrieve(id).map(_.map(_.entity))


  def retrieve(ids: Seq[String]): Future[List[T]] =
    versioned.retrieve(ids).map(_.map(_.entity))


  @deprecated("Use scan", "0.6")
  def retrieveAll: Future[List[Versioned[T]]] =
    versioned.search(all, page = Page.all).map(_.result)

  def scan(criteria: SearchCriteria[T], duration: FiniteDuration, batchSize: Int): Iterator[T] =
    new ScrollIterator[T](col = this, criteria = criteria, duration = duration, size = batchSize)

  def find(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, invalidDocument: (Throwable, JsValue, mutable.Buffer[Versioned[T]]) => Unit = logInvalidDocument): Future[List[T]] =
    versioned.find(criteria, page, sort, invalidDocument).map(_.map(_.entity))

  def findSingle(criteria: SearchCriteria[T], onMany: List[Versioned[T]] => Versioned[T] = _ => throw new MoreThanOneResultException): Future[Option[T]] =
    versioned.findSingle(criteria, onMany).map(_.map(_.entity))

  def search(criteria: SearchCriteria[T], page: Page = Page.first, sort: SearchSort = defaultSort, facets: Seq[Facet] = Seq.empty, invalidDocument: (Throwable, JsValue, mutable.Buffer[Versioned[T]]) => Unit = logInvalidDocument): Future[SearchResult[T]] =
    versioned.search(criteria, page, sort, facets, invalidDocument).map(result => SearchResult(result.result.map(_.entity), result.totalCount, result.facets))

  private def processBulkResponse[A](bulkResponse: BulkResponse)(processResponseItem: BulkItemResponse => Try[A]): Seq[Try[A]] = {
    val responseIterator = bulkResponse.iterator()
    val result = ListBuffer[Try[A]]()
    while (responseIterator.hasNext) {
      result += processResponseItem(responseIterator.next())
    }
    result.toSeq
  }

  private def bulkResponseException[A](response: BulkItemResponse): Try[A] = {
    Try(throw new CommonException(
      message = response.getFailureMessage,
      _errorId = Some("BulkRequestAction"),
      parameters = Map("id" -> response.getFailure.getId, "index" -> response.getFailure.getIndex, "type" -> response.getFailure.getType),
      cause = None))
  }

  def bulkCreate(toCreate: Seq[T], refreshAfterBulk: Boolean = false)(idOfEntity: T => String): Future[Seq[Try[T]]] = {
    versioned.bulkCreate(toCreate, refreshAfterBulk)(idOfEntity).map(_.map(_.map(_.entity)))
  }

  def bulkDelete(idsToDelete: Seq[String]): Future[Seq[Try[Unit]]] = {
    val bulkRequest = client.javaClient.prepareBulk()
    idsToDelete.foreach(id => bulkRequest.add(client.javaClient.prepareDelete().setId(id).request()))
    org.scalastuff.esclient.ActionMagnet.bulkAction.execute(client.javaClient, bulkRequest.request()) map { bulkResponse: BulkResponse =>
      processBulkResponse(bulkResponse) { response =>
        if (response.isFailed) {
          bulkResponseException(response)
        } else {
          Success({})
        }
      }
    }
  }

  def exists(ids: Seq[String]): Future[Map[String, Boolean]] = {
    val requestBuilder = new MultiGetRequestBuilder(client.javaClient)
    for (id <- ids) requestBuilder.add(new MultiGetRequest.Item(this.indexName, this.typeName, id).fields(EsIDField))

    org.scalastuff.esclient.ActionMagnet.multiGetAction.execute(client.javaClient, requestBuilder.request()) map { responses =>
      responses.map(r => r.getId -> r.getResponse.isExists).toMap
    }
  }

  def create(entity: T, refresh: Boolean, ttl: Option[Duration] = None): Future[T] =
    versioned.create(entity, refresh, ttl).map(_.entity)

  def retrieveOrCreate(id: String, refresh: Boolean)(create: => T): Future[T] =
    versioned.retrieveOrCreate(id, refresh)(create).map(_.entity)

  def retrieveOrCreateAndModify(id: String, refresh: Boolean)(create: => T)(modify: T => T): Future[T] =
    versioned.retrieveOrCreateAndModify(id, refresh)(create)(modify).map(_.entity)

  def retrieveOrCreateAndModifyOptionally(id: String, refresh: Boolean)(create: => T)(modify: T => Option[T]): Future[T] =
    versioned.retrieveOrCreateAndModifyOptionally(id, refresh)(create)(modify).map(_.entity)

  def modify(id: String, refresh: Boolean)(modify: T => T): Future[T] =
    versioned.modify(id, refresh)(v => modify(v.entity)).map(_.entity)

  def modifyOptionally(id: String, refresh: Boolean)(modify: T => Option[T]): Future[T] =
    versioned.modifyOptionally(id, refresh)(v => modify(v.entity).map(Versioned(_, v.version))).map(_.entity)

  /**
   * @throws VersionConflictException
   */
  def update(entity: Versioned[T], refresh: Boolean): Future[T] =
    versioned.update(entity, refresh).map(_.entity)

  def delete(id: String, refresh: Boolean): Future[Unit] =
    versioned.delete(id, refresh).map(_ => Unit)

  def reindexAll() = {
    index.refresh()
  }

  def onChange(f: String => Unit) {
    //    index.eventBus.subscribe {
    //      case ESRootDaoMutationEvent (n, id) if n == eventName =>
    //        f (id)
    //    }
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
      val facet = facets.collectFirst { case x if x.name == entry.getKey => x }.getOrElse(throw new CommonException(message = s"The ES response contains facet ${entry.getKey } that were not requested", cause = None, parameters = Map.empty))
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
          val exc = new CommonException(message = s"Only script histogram facets are supported for now. Unknown facet:${entry.getValue() }", cause = None, parameters = Map.empty)
          logger.error("Only ES facets supported for now. Retrieved: {}", exc)
          throw exc
        }
      }
    }.toMap

  private def logInvalidDocument(t: Throwable, json: JsValue, result: mutable.Buffer[Versioned[T]]) =
    logger.error(s"Fetched invalid $typeName: ${t.getMessage }: $json (document skipped)", t)

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
        fields.get(EsIDField) match {
          case Some(JsString(id)) => id.toString
          case Some(JsNumber(id)) => id.toString
          case _ => UUID.randomUUID.toString
        }
      case _ => UUID.randomUUID.toString
    }
}
