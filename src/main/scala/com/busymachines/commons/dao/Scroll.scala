package com.busymachines.commons.dao

import java.util.concurrent.TimeUnit

import com.busymachines.commons.domain.{HasId, Id}
import com.busymachines.commons.elasticsearch.{ESCollection, ESSearchCriteria}
import com.busymachines.commons.util.JsonParser
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.scalastuff.esclient.ESClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{FiniteDuration}

import scala.concurrent.duration.DurationInt
import com.busymachines.commons.Implicits._

/**
 * Created by alex on 25.06.2014.
 */

//TODO Think about a reactive approach
private[commons] class ScrollIterator[A] (col: ESCollection[A], criteria: SearchCriteria[A], duration: FiniteDuration = 5 minutes, size: Int = 100)(implicit client: ESClient, ex: ExecutionContext) extends Iterator[A] {

  private def getScrollBatch: Future[(StringBuilder, Iterator[A])] = prepareScroll flatMap fetch map { case (it, id) =>(new StringBuilder (id), it)}

  private def prepareScroll: Future[String] =
    criteria match {
      case crit: ESSearchCriteria[A] =>

        val request = client.javaClient.prepareSearch (col.indexName)
          .setTypes (col.typeName)
          .setQuery (QueryBuilders.filteredQuery (QueryBuilders.matchAllQuery (), crit.toFilter))
          .setSearchType (SearchType.SCAN)
          .setSize (size)
          .setScroll (new TimeValue (duration.toSeconds, TimeUnit.SECONDS))
        client.execute (request.request).map (r => r.getScrollId)
      case _ => throw new Exception ("Expected ElasticSearch search criteria")
    }

  private def fetch (scrollId: String): Future[(Iterator[A], String)] = {
    val request = client.javaClient.prepareSearchScroll(scrollId) setScroll (new TimeValue (duration.toSeconds, TimeUnit.SECONDS))

    client.execute (request.request).map { result =>
      (result.getHits.hits.toIterator.map { hit =>
        val json = JsonParser.parse (hit.sourceAsString)
        col.mapping.jsonFormat.read (json)
      }, result.getScrollId)
    }
  }

  private lazy val (scrollToUse, it) = getScrollBatch.await

  override def hasNext: Boolean = it.hasNext match {
    case true => true
    case false =>
      val (newIt, newScroll) = fetch (scrollToUse.toString).await
      it ++ newIt
      scrollToUse.clear
      scrollToUse.append (newScroll)
      it.hasNext //TODO Close scroller
  }

  override def next (): A = it.next

}

