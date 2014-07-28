package com.busymachines.commons.elasticsearch.tools

import java.util.concurrent.TimeUnit
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.common.unit.TimeValue
import org.scalastuff.json.spray.SprayJsonParser
import spray.json.JsObject
import com.busymachines.commons.elasticsearch.ESClient

object ESToolHelper {

  def iterateAll(client: ESClient, index: String, f: (String, JsObject, Long, Long) => Unit) = {

    val response = client.javaClient.prepareSearch(index)
      .setSearchType(SearchType.SCAN)
      .setScroll(new TimeValue(5, TimeUnit.MINUTES))
      .setSize(100)
      .execute.actionGet

    val totalHits = response.getHits.getTotalHits
    val jsonParser = new SprayJsonParser
    var scrollId: Option[String] = Some(response.getScrollId)
    var currentHit = 0

    while (scrollId.isDefined) {
      val response =
        client.javaClient.prepareSearchScroll(scrollId.get)
          .setScroll(new TimeValue(5, TimeUnit.MINUTES))
          .execute().actionGet()

      for (hit <- response.getHits.hits) {
        jsonParser.parse(hit.sourceAsString) match {
          case obj: JsObject =>
            currentHit += 1
            f(hit.`type`, obj, currentHit, totalHits)
          case _ =>
        }
      }
      scrollId =
        if (response.getHits.getHits.length == 0) None
        else Some(response.getScrollId)
    }
  }
}
