package com.kentivo.mdm.logic

import com.kentivo.mdm.domain.Repository
import com.busymachines.commons
import com.kentivo.mdm.commons.implicits._
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Mutation
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyScope
import com.kentivo.mdm.domain.PropertyValue
import java.util.Locale
import scala.concurrent.Future
import scala.collection.mutable
import com.kentivo.mdm.db.ItemDaoFilter

trait RepositoryView {

  val repository: Repository

  def findItems(itemIds : Seq[Id[Item]]) : Seq[Item]
  def searchItems(filters : ItemDaoFilter*) : Seq[Item]
  
  def findItem(itemId : Id[Item]) : Option[Item] = 
    findItems(Seq(itemId)).headOption
  
  def parentExtent(item : Item) : List[Item] = 
    parentExtent(Seq(item)).head
    
  def parentExtent(items : Seq[Item]) : Seq[List[Item]] = {
    def extent(item : Item, found : Map[Id[Item], Item]) : List[(Id[Item], Option[Item])] =
      (item.id, Some(item)) :: (item.id, Some(item)) :: item.parents.flatMap(id => found.get(id) match {
        case Some(item) => extent(item, found)
        case None => List(id -> None)
      })
    def findParentExtent(items : Seq[Item], found : Map[Id[Item], Item]) : Seq[List[Item]] = {
      val result = items.map(extent(_, found))
      val missing = result.flatten.collect { case (id, None) => id }
      if (missing.nonEmpty) {
        val foundItems = findItems(missing).map(item => item.id -> item).toMap
        if (foundItems.isEmpty) result.map(_.collect { case (_, Some(item)) => item }) 
        else findParentExtent(items, found ++ foundItems)
      } else {
        result.map(_.map(_._2.get))
      }
    }
    val itemIds = items.flatMap(item => item.parents ++ item.knownParentExtent)
    val foundItems = findItems(itemIds).map(item => item.id -> item).toMap
    findParentExtent(items, foundItems)
  } 
  
  def property(item : Item, property: Id[Property]): Option[(Item, Property)] =
    parentExtent(item).collectFirst((item: Item) => item.properties.find(_.id == property).map(item -> _))

  def propertyValue(item: Item, property: Id[Property]): Option[(Item, Property, PropertyValue)] = {
    this.property(item, property) flatMap {
      case (item, property) =>
        item.values.find(value => value.property == property).map((item, property, _))
    }
  }

}