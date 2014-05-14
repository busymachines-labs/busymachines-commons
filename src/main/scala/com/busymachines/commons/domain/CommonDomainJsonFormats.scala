package com.busymachines.commons.domain

import java.util.Currency
import java.util.Locale
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._
import com.busymachines.commons.dao.SearchResult
import com.busymachines.commons.dao.Versioned
import scala.concurrent.duration.FiniteDuration
import org.joda.time.LocalDate
import com.busymachines.commons.dao.TermFacetValue
import com.busymachines.commons.dao.HistogramFacetValue
import com.busymachines.commons.spray.ProductFormatsInstances
import com.busymachines.commons.AbstractEnum
import com.busymachines.commons.CommonEnum
import com.busymachines.commons.EnumValue
import com.busymachines.commons.implicits.CommonJsonFormats
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

trait CommonDomainJsonFormats { this: CommonJsonFormats =>

  implicit val geoPointFormat = format3(GeoPoint)

  implicit val unitOfMeasureFormat = stringFormat("UnitOfMeasure", s => UnitOfMeasure(s))


  implicit def idFormat[A] = stringWrapperFormat(Id[A])
  implicit def mimeTypeFormat = stringWrapperFormat(MimeType)
  implicit val mediaFormat = format4(Media)
  implicit val moneyFormat = format2(Money)

  implicit val termFacetValueFormat = format2(TermFacetValue)
  implicit val histogramValueFormat = format7(HistogramFacetValue)

  implicit def versionedFormat[T <: HasId[T]](implicit tFormat: JsonFormat[T]) = format2(Versioned[T])

  class SearchResultFormat[T <: HasId[T]](fieldName: String)(implicit tFormat: JsonFormat[T]) extends RootJsonWriter[SearchResult[T]] {
    def write(result: SearchResult[T]) = {
      val jsonResult = JsArray(result.result.map(_.entity).map(tFormat.write))
      val jsonFacetResult = JsObject(result.facets.map(facet => facet._1 -> JsArray(facet._2.map {
        case v: HistogramFacetValue => histogramValueFormat.write(v)
        case v: TermFacetValue => termFacetValueFormat.write(v)
        })))
      result.totalCount match {
        case Some(totalCount) => JsObject(fieldName -> jsonResult, "totalCount" -> JsNumber(totalCount), "facets" -> jsonFacetResult)
        case None => JsObject(fieldName -> jsonResult)
      }
    }
  }

  implicit val sequenceFormat = format2(Sequence)
}
