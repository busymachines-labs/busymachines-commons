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

/**
 * Main unit of data. Contains meta-data, data, history, effective repositories, etc.
 */
case class Repository(
  id: Id[Repository] = Id.generate,
  owner: Id[Party],
  name: Map[Locale, String] = Map.empty,
  main : EffectiveRepository,
  secundairy : Map[String, EffectiveRepository],
  /**
   * Locales used by the repository.
   */
  locales : List[Locale])

/**
 * An effective repository is derived from a 'master' repository and
 * contains only the effective values for a particular locale and and channel.
 * All relevant mutations and rules have been applied.
 * The main effective repository will be synchronised always and on the fly, the
 * secondary ones are synchronised on-demand only, like "publish". 
 */   
case class EffectiveRepository(
  id : Id[Repository] = Id.generate,
  lastSynchronizationTime : DateTime
)
  
/**
 * A repository can store variations of data and meta-data that are channel-specific.
 * An example of a channel can be a SAP system (for exporting) or a web-shop. 
 */
case class Channel(
  /**
   * Locales used by this channel. Not necessarily a sub-set or a super-set of the
   * repository's locales. 
   */
  locales : List[Locale]
)
  
case class Item(
  id: Id[Item] = Id.generate,
  repository: Id[Repository],
  lastMutationTime : DateTime, // maximum of ItemDefinition.mutationTime and all values.mutationTime
  currentDefinition : ItemDefinition,
  definitions: List[ItemDefinition],
  values: Map[Id[Property], PropertyValue] = Map.empty
) extends HasId[Item] 

case class ItemDefinition(
  mutation : Id[Mutation],
  mutationTime : DateTime,
  parents: List[Id[Item]] = Nil,
  properties: List[Property] = Nil,
  propertyGroups: List[PropertyGroup] = Nil,
  isCategory: Boolean = false,
  isDeleted: Boolean = false,
  groups : List[PropertyGroup] = Nil,
  rules: List[ItemRule] = Nil) extends HasId[ItemDefinition] 

// immutable
case class EffectiveItem(
  id: Id[EffectiveItem] = Id.generate,
  item: Id[Item],
  repository: Id[Repository],
  definition : ItemDefinition,
  knownParentExtent: List[Id[Item]] = Nil,
  locale : Locale,
  channel : Option[Id[Channel]],
  values : Map[Id[Property], String]
) extends HasId[EffectiveItem]

case class Property(
  id: Id[Property] = Id.generate,
  name: Map[Locale, String] = Map.empty,
  scope : PropertyScope.Value = PropertyScope.Item,
  `type`: PropertyType.Value = PropertyType.String,
  unitOfMeasure: Map[Locale, UnitOfMeasure] = Map.empty, // only if type == Real
  precision: Option[Int] = None, // only if type == Real, number of digits after the . 
  itemValueBase : Option[Id[Item]] = None, // only if type == Item
  enumValues : Map[String, Map[Locale, String]] = Map.empty, // only if type == Enum
  baseItem : Option[Id[Item]] = None, // only if type == Item
  baseItemIsCategoryConstraint : Option[Boolean] = None, // only if type == Item, true: must be category, false: may not be a category
  mandatory: Boolean = false,
  multiValue : Boolean = false,
  multiLingual: Boolean = false,
  public: Boolean = false, // visible by managed parties
  groups : List[Id[PropertyGroup]] = Nil,
  rules: List[PropertyRule] = List.empty) extends HasId[Property]

case class PropertyGroup(
  id : Id[PropertyGroup] = Id.generate,
  name : Map[Locale, String] = Map.empty,
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
  value: String,
  channel : Option[Channel],
  locale: Option[Locale] = None)

case class Mutation(
  description: String,
  startTime: DateTime,
  endTime: DateTime,
  id: Id[Mutation] = Id.generate,
  user: Option[Id[User]] = None,
  source: Option[Id[Source]] = None) extends HasId[Mutation]


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
