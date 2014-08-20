package com.busymachines.commons.elasticsearch

import scala.language.postfixOps
import org.scalatest.FlatSpec
import com.busymachines.commons.logging.Logging
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
//import scala.concurrent.ExecutionContext.Implicits.global
//import com.busymachines.commons.Implicits._
//import com.busymachines.commons.testing.EmptyESTestIndex
//import org.joda.time.DateTime
//import org.joda.time.DateTimeZone
//import com.busymachines.commons.dao.TermFacetValue
//import com.busymachines.commons.event.DoNothingEventSystem
//import com.busymachines.commons.domain.GeoPoint
//import com.busymachines.commons.elasticsearch.ESTermFacet

@RunWith(classOf[JUnitRunner])
class ItemDaoFacetsTests extends FlatSpec with Logging {

//
//  val esIndex = EmptyESTestIndex(getClass, new DoNothingEventSystem)
//  val dao = new ESRootDao[Item](esIndex, ESType("item", ItemMapping))
//
//  val now = DateTime.now(DateTimeZone.UTC)
//  val geoPoint = GeoPoint(10, 10)
//
//  val _nocrit = ESSearchCriteria.All[Item]
//  val _priceSaleFacet = ESTermFacet("priceSale", _nocrit, Path(ItemMapping.priceSale :: Nil) :: Nil, 10)
//
//  val item1 = Item(name = "Sample Item 1", priceSale = 0.01, priceNormal = 1.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
//  val item2 = Item(name = "Sample Item 2", priceSale = 0.02, priceNormal = 4.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
//  val item3 = Item(name = "Sample item 3", priceSale = 6.0, priceNormal = 4.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
//  val item4 = Item(name = "Sample item 4", priceSale = 10.1, priceNormal = 2.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
//  val item5 = Item(name = "Sample item 5", priceSale = 11.1, priceNormal = 4.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
//  val item6 = Item(name = "Sample item 6", priceSale = 6.0, priceNormal = 3.0, validUntil = now, location = geoPoint, properties = Property(name = "Property3") :: Property(name = "Property4") :: Nil)
//
//  dao.create(item1, true).await
//  dao.create(item2, true).await
//  dao.create(item3, true).await
//  dao.create(item4, true).await
//  dao.create(item5, true).await
//  dao.create(item6, true).await
//
//  "OrderDaoF" should "search with facets on all documents w/o filter"  in {
//
//    val _priceNormalFacet = ESTermFacet("priceNormal", _nocrit, Path(ItemMapping.priceNormal :: Nil) :: Nil, 10)
//
//    val _response = dao.search(criteria = _nocrit, facets = _priceNormalFacet :: Nil).await
//    val _facet=_response.facets("priceNormal")
//
//    assert(_facet.count(p => true) == 4)
//
//    assert(_facet(0).asInstanceOf[TermFacetValue].value == "4.0")
//    assert(_facet(0).asInstanceOf[TermFacetValue].count == 3)
//
//    assert(_facet(1).asInstanceOf[TermFacetValue].value == "3.0")
//    assert(_facet(1).asInstanceOf[TermFacetValue].count == 1)
//
//    assert(_facet(2).asInstanceOf[TermFacetValue].value == "2.0")
//    assert(_facet(2).asInstanceOf[TermFacetValue].count == 1)
//
//    assert(_facet(3).asInstanceOf[TermFacetValue].value == "1.0")
//    assert(_facet(3).asInstanceOf[TermFacetValue].count == 1)
//
//
//  }
//
//  it should "search with facets on all documents with filter" in {
//
//    val _crit = ItemMapping.id in item1.id::item2.id::item3.id::Nil
//    val _priceNormalFacet = ESTermFacet("priceNormal", _crit, Path(ItemMapping.priceNormal :: Nil) :: Nil, 10)
//
//    val _response = dao.search(criteria = _nocrit, facets = _priceNormalFacet :: Nil).await
//    val _facet=_response.facets("priceNormal")
//
//    assert(_facet.count(p => true) == 2)
//
//    assert(_facet(0).asInstanceOf[TermFacetValue].value == "4.0")
//    assert(_facet(0).asInstanceOf[TermFacetValue].count == 2)
//
//    assert(_facet(1).asInstanceOf[TermFacetValue].value == "1.0")
//    assert(_facet(1).asInstanceOf[TermFacetValue].count == 1)
//
//  }
//

}
