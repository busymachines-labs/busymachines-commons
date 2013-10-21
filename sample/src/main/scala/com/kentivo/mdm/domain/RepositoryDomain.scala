package com.kentivo.mdm.domain

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import scala.concurrent.duration.Deadline
import com.busymachines.commons.domain.HasId
import org.joda.time.DateTime
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.UnitOfMeasure
import scala.actors.OutputChannel

// Synchronizing an effective repository:
//1. get lastSynchronizationTime
//2. determine newSynchronizationTime
//3. query all items where mutationTime > lastSynchronizationTime
//4. query all invalid mutations where endTime == None || endTime > newSynchronizationTime
//5. for item <- items
//6.   filter invalid mutations from item
//7.   calculate effective item
//8. store newSynchronizationTime as lastSynchronizationTime


//repository locales: nl, nl_be, nl_nl
//webshop channel locales: nl_be, nl_nl
//german webshop (no locales)
//german north webshop (no locales, parent: german webshop)
//german south webshop (no locales, parent: german webshop)
//
//fallback tree:
//
//default
//  dutch
//    dutch (be)
//    dutch (nl)
//  webshop
//    dutch (be)
//    dutch (nl)
//    german webshop
//      german north webshop
//      german south webshop

// import: 
// Determine whether there was a change by comparing the effective value caused by THE SANE import source only. Only store when changed.

// default properties:
// - parents
// - deleted

/**
 * Main unit of data. Contains meta-data, data, history, effective repositories, etc.
 */
case class Repository(
  id: Id[Repository] = Id.generate,
  
  /**
   * Owner party of the repository and all related data (items, effective repositories)
   */
  owner: Id[Party],
  
  /**
   * Name of this repository (multi-lingual).
   */
  name: Map[Option[Locale], String] = Map.empty,
  
  /**
   * The current effective repository. It will be synchronised on the fly. 
   */
  current : EffectiveRepository,
  
  /**
   * A copy of the repositories on a specific moment in time. It will be synchronised on-demand.
   * Examples: preview, production
   */
  snapshots : Map[String, EffectiveRepository],
  
  /**
   * Locales used by the repository.
   */
  locales : List[Locale],
  
  sources : List[Source] = Nil,
  
  sourcePriorities: Map[(Id[Source], List[DateTime]), Int] = Map.empty
)

case class Source(
  id : Id[Source],
  priority : Int
) extends HasId[Source]

/**
 * A repository can store variations of data and meta-data that are channel-specific.
 * An example of a channel can be a SAP system (for exporting) or a web-shop. 
 */
case class Channel(
  id : Id[Channel],
  
  /**
   * Channels can have parents defining the fallback tree.
   */
  parent : Option[Id[Channel]],
  
  /**
   * Locales used by this channel. Not necessarily a sub-set or a super-set of the
   * repository's locales. 
   */
  locales : List[Locale]
) extends HasId[Channel]
  
case class Item(
  id: Id[Item] = Id.generate,
  repository: Id[Repository],
  metadata: List[ItemMetadata] = Nil,
  data: Map[Id[Property], List[PropertyValue]] = Map.empty
) extends HasId[Item] 

case class ItemMetadata(
  mutation : Id[Mutation],
  mutationTime : DateTime,
  properties: List[Property] = Nil,
  propertyGroups: List[PropertyGroup] = Nil, // must be concatenated with parent extent
  excludedPropertyGroups: Option[List[Id[PropertyGroup]]] = None, // entire list is either inherited or redefined
  isCategory: Boolean = false,
  rules: List[ItemRule] = Nil) extends HasId[ItemMetadata] 

case class Property(
  id: Id[Property] = Id.generate,
  name: Map[Option[Locale], String] = Map.empty,
  scope : PropertyScope.Value = PropertyScope.Item,
  `type`: PropertyType.Value = PropertyType.String,
  unitOfMeasure: Map[Option[Locale], UnitOfMeasure] = Map.empty, // only if type == Real
  precision: Option[Int] = None, // only if type == Real, number of digits after the . 
  itemValueBase : Option[Id[Item]] = None, // only if type == Item
  enumValues : Map[String, Map[Option[Locale], String]] = Map.empty, // only if type == Enum
  baseItem : Option[Id[Item]] = None, // only if type == Item
  baseItemIsCategoryConstraint : Option[Boolean] = None, // only if type == Item, true: must be category, false: may not be a category
  mandatory: Boolean = false,
  multiValue : Boolean = false,
  multiLingual: Boolean = false,
  public: Boolean = false, // visible by managed parties
  propertyGroups : List[Id[PropertyGroup]] = Nil, 
  rules: List[PropertyRule] = List.empty) extends HasId[Property]

case class PropertyGroup(
  id : Id[PropertyGroup] = Id.generate,
  name : Map[Option[Locale], String] = Map.empty,
  properties : List[Id[Property]] = List.empty)

object PropertyType extends Enumeration {
  val String = Value("string")
  val Int = Value("int")
  val Real = Value("real")
  val Boolean = Value("boolean")
  val DateTime = Value("datetime")
  val Duration = Value("duration")
  val Media = Value("media")
  val Item = Value("item")
  val Property = Value("property")
}

object PropertyScope extends Enumeration {
  val Category = Value("category")
  val Item = Value("item")
  val Instance = Value("instance")
  val Dynamic = Value("dynamic")
}

case class PropertyValue(
  mutation: Id[Mutation],
  mutationTime : DateTime,
  value: Option[String], // None has special meaning in effective calculation
  channel : Option[Id[Channel]] = None,
  locale: Option[Locale] = None)

/**
 * An effective repository is derived from a 'master' repository and
 * contains only the effective values for a particular locale and and channel.
 * All relevant mutations and rules have been applied.
 * An effective repository contains effective items for all relevant channels and
 * locales.
 */   
case class EffectiveRepository(
  id : Id[EffectiveRepository] = Id.generate,
  
  /**
   * The effective repository will contain data from mutations that have an
   * end-time smaller than the last synchronisation time.
   */
  lastSynchronizationTime : DateTime,
  
  sourcePriorities: Map[Id[Source], Int],
  
  effectiveMetadata: Map[Id[Item], EffectiveItemMetadata]
) extends HasId[EffectiveRepository]
  
/**
 * An effective item contains the 'flat' view of an item for a specific channel and locale.
 */ 
case class EffectiveItem(
  id: Id[EffectiveItem] = Id.generate,
  /**
   * Origin item.
   */
  item: Id[Item],
  
  /**
   * Repository this item belongs to.
   */
  repository: Id[EffectiveRepository],
  parentExtent: List[Id[Item]] = Nil,
  locale : Option[Locale],
  channel : Option[Id[Channel]],
  values : Map[Id[Property], String]
) extends HasId[EffectiveItem]

case class EffectiveItemMetadata(
  properties: List[Property] = Nil,
  propertyGroups: List[PropertyGroup] = Nil, // must be concatenated with parent extent
  excludedPropertyGroups: Option[List[Id[PropertyGroup]]] = None, // entire list is either inherited or redefined
  isCategory: Boolean = false) extends HasId[ItemMetadata] 

case class Mutation(
  id: Id[Mutation] = Id.generate,
  source: Id[Source],
  description: String,
  startTime: DateTime,
  endTime: Option[DateTime] = None,
  user: Option[Id[User]] = None,
  priority: Int) extends HasId[Mutation]


object Item2 {
  val dateTimeFormat = {
    val df = new SimpleDateFormat("dd-MM-yyyy HH:mm")
    df.setTimeZone(TimeZone.getTimeZone("UTC"))
    df
  }
  def parseValue(value: String, `type`: PropertyType.Value): Option[Any] = {
    `type` match {
      case PropertyType.String => Some(value)
      case PropertyType.Int => try {
        Some(java.lang.Long.parseLong(value))
      } catch {
        case _: java.lang.NumberFormatException => None
      }
      case PropertyType.Real => try {
        Some(java.lang.Double.parseDouble(value))
      } catch {
        case _: java.lang.NumberFormatException => None
      }
      case PropertyType.DateTime => try {
        Some(dateTimeFormat.parse(value))
      } catch {
        case _: Throwable => None
      }
      case _ => None
    }
  }
  def toString(value: Any, `type`: PropertyType.Value): String =
    `type` match {
      case PropertyType.DateTime =>
        if (value.isInstanceOf[Deadline]) {
          dateTimeFormat.format(value.asInstanceOf[Deadline].time.length)
        } else {
          value.toString
        }
      case _ => value.toString
    }

  def convertValue(value: Any, fromType: PropertyType.Value, toType: PropertyType.Value): Option[Any] =
    parseValue(toString(value, fromType), toType)
}
