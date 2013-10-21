package com.kentivo.mdm.logic

import scala.language.postfixOps
import com.kentivo.mdm.domain.Mutation
import com.kentivo.mdm.domain.Item
import com.busymachines.commons
import com.busymachines.commons.implicits._
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyScope
import java.util.Locale
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.collection.concurrent.TrieMap
import com.kentivo.mdm.domain.PropertyValue
import com.busymachines.commons.domain.UnitOfMeasure
import com.kentivo.mdm.db.ItemDao
import scala.concurrent.ExecutionContext
import com.busymachines.commons.domain.Id
import com.busymachines.commons.dao.SearchCriteria
import com.busymachines.commons.dao.DaoCache
import com.busymachines.commons.dao.VersionConflictException
import com.busymachines.commons.dao.RetryVersionConflict
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import com.busymachines.commons.dao.RootDaoMutator
import com.busymachines.commons.dao.Versioned
import com.kentivo.mdm.domain.Repository
import com.busymachines.commons.dao.Page
import com.kentivo.mdm.domain.Channel
import com.kentivo.mdm.domain.ItemMetadata
import org.joda.time.DateTime
import com.kentivo.mdm.domain.ItemMetadata

class Mutator (val repositoryId : Id[Repository], val itemDao : ItemDao, val mutations : MutationManager, val mutation: Mutation, end : () => Unit, timeout : Duration)(implicit ec: ExecutionContext) {

  def endMutation = end()
  
  def setValue(itemId : Id[Item], propertyId : Id[Property], value : Option[String], channel : Option[Id[Channel]], locale : Option[Locale]) =
    setValues(itemId, (propertyId, value, channel, locale))
    
  def setValues(itemId : Id[Item], changes : (Id[Property], Option[String], Option[Id[Channel]], Option[Locale])*) = {
    modifyItem(itemId) { item =>
      var resultData = item.data
      var shouldUpdate : Boolean = false
      val now = DateTime.now
      for ((propertyId, value, channel, locale) <- changes) {
        val values = resultData.get(propertyId).getOrElse(Nil)
        var maxMutationTime = new DateTime(0l)
        var maxPriority : Int = Int.MinValue
        var effectiveValue: Option[PropertyValue] = None
        var lastSourceMutationTime = maxMutationTime
        var lastSourceValue: Option[PropertyValue] = None
        values.foreach { value =>
          val valueMutation = mutations.get(value.mutation)
          val mutationTime = value.mutationTime
          val priority = valueMutation.priority
          if (value.mutation != mutation.id && channel == value.channel && locale == value.locale) {
            if (priority >= maxPriority && mutationTime.isAfter(maxMutationTime)) {
              maxPriority = priority
              maxMutationTime = mutationTime
              effectiveValue = Some(value)
            }
            if (valueMutation.source ==  mutation.source &&  mutationTime.isAfter(lastSourceMutationTime)) {
              lastSourceMutationTime = mutationTime
              lastSourceValue = Some(value)
            }
          }
        }
        
        (effectiveValue, lastSourceValue) match {
          case (Some(effective), Some(lastSource)) if effective.value == value && lastSource.value == value => 
          case _ => 
            shouldUpdate = true
            resultData += (propertyId -> (values :+ PropertyValue(mutation.id, now, value, channel, locale)))
        }
      }
      if (shouldUpdate) (true, item.copy(data = resultData), item)
      else (false, item, item)
    }
  }
  
  /**
   * Modifies the metadata for given item. This operation will never update an existing ItemMetadata instance.
   * Instead, it will create a new one, which is the instance that is returned. Note
   * that this instance is not necessary the effective metadata instance, since this 
   * depends on the priority of the current mutation.
   * This function recognizes when the modification does not cause any changes. It is a no-op when the resulting 
   * metadata instance equals the effective one AND equals the last one from the same source, which must exist in this case. 
   * When there are no changes, the function returns the last metadata from the same source. 
   */
  def modifyMetadata(itemId : Id[Item])(modify : ItemMetadata => ItemMetadata) : ItemMetadata = {

    modifyItem(itemId) { item =>
      // find the effective metadata based on priority and mutation time
      var maxMutationTime = new DateTime(0l)
      var maxPriority : Int = Int.MinValue
      var effectiveMetadata: Option[ItemMetadata] = None
      var lastSourceMutationTime = maxMutationTime
      var lastSourceMetadata: Option[ItemMetadata] = None
      item.metadata.foreach { metadata =>
        val metadataMutation = mutations.get(metadata.mutation)
        val mutationTime = metadata.mutationTime
        val priority = metadataMutation.priority
        if (metadata.mutation != mutation.id) {
          if (priority >= maxPriority && mutationTime.isAfter(maxMutationTime)) {
            maxPriority = priority
            maxMutationTime = mutationTime
            effectiveMetadata = Some(metadata)
          }
          if (metadataMutation.source ==  mutation.source &&  mutationTime.isAfter(lastSourceMutationTime)) {
            lastSourceMutationTime = mutationTime
            lastSourceMetadata = Some(metadata)
          }
        }
      }
      // set mutation time and create new metadata if necessary
      val now = DateTime.now
      val original = effectiveMetadata.
        map(_.copy(mutation = mutation.id, mutationTime = now)).
        getOrElse(ItemMetadata(mutation = mutation.id, mutationTime = now))

      // Modify metadata and check whether there are changes
      val modified = modify(original)
      val (shouldUpdate, result) = lastSourceMetadata match {
        case Some(sourceMetadata) 
          if modified == original && 
            (sourceMetadata.eq(effectiveMetadata.getOrElse(original)) || 
            modified == sourceMetadata.copy(mutation = mutation.id, mutationTime = now)) => (false, sourceMetadata)
        case None => (true, modified)
      }
        
      // remove old metadata with same mutation, add the new metadata
      val resultingItem = shouldUpdate match {
        case true => item.copy(metadata = item.metadata.filterNot(_.mutation.id == mutation.id) :+ modified)
        case false => item
      }
      
      (shouldUpdate, resultingItem, modified)
    }
  }

  def modifyProperty(itemId: Id[Item], propertyId: Id[Property])(modify: Property => Property): ItemMetadata = {
    modifyMetadata(itemId) { metadata =>
      metadata.copy(properties = metadata.properties.modify(_.id == propertyId, Property(propertyId), modify)._1)
    }
  }
  
  private def modifyItem[A](itemId : Id[Item])(modify : Item => (Boolean, Item, A)) : A = {
    RetryVersionConflict(10) {
      // Make sure item has been created.
      val Versioned(item, version) = Await.result(itemDao.retrieve(itemId), timeout).getOrElse(Versioned(new Item(repository = repositoryId), 1))
      // Modify the item
      val (shouldUpdate, modified, result) = modify(item)
      // Update 
      if (shouldUpdate) {
        Await.result(itemDao.update(Versioned(modified, version), false), timeout)
      }
      // return result
      result
    }
  }
}
