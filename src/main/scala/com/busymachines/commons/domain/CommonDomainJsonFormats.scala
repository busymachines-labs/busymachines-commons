package com.busymachines.commons.domain

import com.busymachines.commons.dao._
import com.busymachines.commons.implicits.CommonJsonFormats
import spray.json.JsArray
import spray.json.JsNumber
import spray.json.JsObject
import spray.json.JsonFormat
import spray.json.RootJsonWriter

trait CommonDomainJsonFormats {
  this: CommonJsonFormats =>

  implicit val geoPointFormat = format3(GeoPoint)

  implicit val unitOfMeasureFormat = stringFormat("UnitOfMeasure", s => UnitOfMeasure(s))

  implicit val mimeTypeFormat = stringFormat("MimeType", MimeType)

  implicit def idFormat[A] = stringWrapperFormat(Id[A])

  implicit val moneyFormat = format2(Money)

  implicit val termFacetValueFormat = format2(TermFacetValue)
  implicit val histogramValueFormat = format7(HistogramFacetValue)

  implicit def versionedFormat[T](implicit tFormat: JsonFormat[T]) = format2(Versioned[T])

  class SearchResultFormat[T](fieldName: String)(implicit tFormat: JsonFormat[T]) extends RootJsonWriter[SearchResult[T]] {
    def write(result: SearchResult[T]) = {
      val jsonResult = JsArray(result.result.map(tFormat.write))
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

  class VersionedSearchResultFormat[T](fieldName: String)(implicit tFormat: JsonFormat[T]) extends RootJsonWriter[VersionedSearchResult[T]] {
    def write(result: VersionedSearchResult[T]) = {
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

  implicit val countryJsonFormat = enumFormat(Country)

  implicit val languageJsonFormat = enumFormat(Language)
}
