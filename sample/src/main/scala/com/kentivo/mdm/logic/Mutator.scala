package com.kentivo.mdm.logic

import com.kentivo.mdm.domain.Mutation
import com.kentivo.mdm.domain.Item
import com.busymachines.commons
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyScope
import java.util.Locale
import scala.collection.concurrent.TrieMap
import com.kentivo.mdm.domain.PropertyValue
import com.kentivo.mdm.domain.Unit
import com.kentivo.mdm.db.ItemDao
import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.RootMutator
import com.busymachines.commons.dao.SearchCriteria
import scala.concurrent.duration.Duration

class Mutator(val view: RepositoryView, val itemDao : ItemDao, val mutation: Mutation)(implicit ec: ExecutionContext) extends RepositoryView {

  private val mutator = new RootMutator[Item](itemDao)
  
  val repository = view.repository

  def newItem = Item(repository.id, mutation.id)
  
  def findItems(itemIds : Seq[Id[Item]], timeout: Duration) = 
    mutator.retrieve(itemIds, timeout)
      itemIds.flatMap(id => _changedItems.get(id).map(Some(_)).getOrElse(view.findItem(id)))

  def searchItems(criteria : SearchCriteria[Item], timeout : Duration) : Seq[Item] = 
    mutator.search(criteria, timeout)
    val items = view.searchItems(criteria)
    items.map(item => _changedItems.get(item.id).getOrElse(item))
  }
    
  def getChangedItem(id: Id[Item]): Option[Item] =
    _changedItems.get(id)

  def getOrCreateItem(id: Id[Item]): Item = {
    _changedItems.getOrElse(id, view.findItem(id) match {
      case Some(item) => item
      case None => Item(view.repository.id, mutation.id, id)
    })
  }

  def modifyItem(itemId: Id[Item], modify: Item => Item): Item = {
    val item = getOrCreateItem(itemId)
    val modItem = modify(item)
    if (item != modItem) {
      _changedItems += (itemId -> modItem)
    }
    modItem
  }
  
  def modifyProperty(itemId: Id[Item], propertyId: Id[Property], modify: Property => Property): Property = {
    val item = getOrCreateItem(itemId)
    val (properties, property, changed) = Mutator.modify[Property](item.properties, _.id == propertyId, Property(repository.id, mutation.id, propertyId), modify)
    if (changed) {
      val modItem = item.copy(properties = properties)
      _changedItems += (itemId -> modItem)
    }
    property
  }

  def modifyValues(itemId: Id[Item], propertyId: Id[Property], modify: List[PropertyValue] => List[PropertyValue]) : Item = {
    val item = getOrCreateItem(itemId)
    val (values, otherValues) = item.values.partition(_.property == propertyId)
    val newValues = otherValues ++ modify(values) 
    if (item.values != newValues) {
      val modItem = item.copy(values = newValues)
      _changedItems += (itemId -> modItem)
      modItem
    } else {
      item
    }
  }
  
  def setValues(itemId: Id[Item], propertyId: Id[Property], values: Seq[(Locale, String)]) : Item =
    modifyValues(itemId, propertyId, _ => values.toList.map { 
      case (locale, value) => PropertyValue(propertyId, mutation.id, value, Some(locale))
    })
  
  def setValue(itemId: Id[Item], propertyId: Id[Property], value : Option[String], locale : Option[Locale] = None, unit : Option[Unit] = None) : Item = {
    val item = getOrCreateItem(itemId)
    val (newValues, _, changed) = value match {
      case Some(value) => 
        Mutator.modify[PropertyValue](item.values, _.locale == locale, PropertyValue(propertyId, mutation.id, value, locale, unit))
      case None =>
        val newValues = item.values.filterNot(_.locale == locale)
        (newValues, Unit, item.values != newValues)
    }
    if (changed) {
      val modItem = item.copy(values = newValues)
      _changedItems += (itemId -> modItem)
      modItem
    }
    else item
  }
  
  def flush(implicit ec : ExecutionContext) {
    mutator.write(10 seconds, true);
    itemDao.storeItems(changedItems.values.toSeq).map(_.filter(_._2 != None).map(_._1))
    _changedItems.clear
  }
}

object Mutator {
  def modify[A](seq: Seq[A], matches : A => Boolean, newA: => A, modify: A => A = (a : A) => a): (List[A], A, Boolean) = {
    var found: Option[A] = None
    var changed = false
    val newSeq = seq.toList.map {
      case a if matches(a) =>
        val modA = modify(a)
        found = Some(modA);
        changed = a != modA
        newA
      case a =>
        a
    }
    found match {
      case Some(a) =>
        (newSeq, a, changed)
      case None =>
        val modA = modify(newA)
        (newSeq :+ modA, modA, true)
    }
  }
}
