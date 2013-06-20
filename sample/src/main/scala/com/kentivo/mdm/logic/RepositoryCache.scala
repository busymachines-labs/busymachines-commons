package com.kentivo.mdm.logic

import com.kentivo.mdm.domain.Repository
import com.kentivo.mdm.domain.Item
import com.busymachines.commons
import com.kentivo.mdm.commons.implicits._
import com.kentivo.mdm.db.ItemDao
import com.kentivo.mdm.domain.Mutation
import com.kentivo.mdm.db.MutationDao
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.collection.concurrent.TrieMap
import scala.concurrent.Await
import scala.concurrent.duration._
import com.kentivo.mdm.db.ItemDaoFilter
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.SearchCriteria

class RepositoryCache(val repository : Repository, itemDao : ItemDao) extends RepositoryView {
  
  private val _itemCache = TrieMap[Id[Item], Item]()
  private val _searchCache = TrieMap[Seq[ItemDaoFilter], List[Item]]()
  
  def findItems(itemIds : Seq[Id[Item]]) : Seq[Item] = {
    val cachedItems = itemIds.map(id => id -> _itemCache.get(id))
    val missingItemIds = cachedItems.collect { case (id, None) => id }
    val readItems = missingItemIds match {
      case Seq() => Seq()
      case ids => Await.result(itemDao.getItems(ids), 10 seconds)
    }
    val result = cachedItems.collect { case (_, Some(item)) => item } ++ readItems
    _itemCache ++= result.map(item => item.id -> item)
    result
  }

  def searchItems(criteria : SearchCriteria[Item]) : Seq[Item] = {
    _searchCache.getOrElseUpdate(filters.toSeq, {
      val items = Await.result(itemDao.searchItems(filters:_*), 10 seconds)
      val cachedItems = items.map(item => (item -> _itemCache.get(item.id)))
      _itemCache ++= cachedItems.collect { case (item, None) => item.id -> item } 
      cachedItems.collect { 
        case (item, None) => item 
        case (_, Some(item)) => item 
      }
    })
  }

}