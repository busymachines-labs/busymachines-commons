//import com.busymachines.prefab.party.domain.Address
//import scala.collection.concurrent.TrieMap
//import spray.json.JsObject
//import com.busymachines.commons.Extensions
//import com.busymachines.commons.ExtensionFor
//import com.busymachines.commons.elasticsearch.CaseClassField
//import com.busymachines.commons.elasticsearch.CaseClassFields
//import spray.json.JsTrue
//import scala.concurrent.duration.Duration
//import spray.json.JsNumber
//
//package com.busymachines.commons.elasticsearch2 {
//
//import scala.reflect.ClassTag
//import scala.reflect.classTag
//import spray.json.{JsString, JsonFormat}
//import spray.json.JsValue
//import org.joda.time.DateTime
//import com.busymachines.commons.implicits._
//
///**
// * Base class for mapping objects.
// */
//abstract class ESMapping[A :ClassTag] {
//
//  private var _fields = Map[String, ESField[A, _]]()
//  private val caseClassFields : Map[String, CaseClassField] = CaseClassFields.of[A]
//
//  lazy val fieldsByName = _fields
//  lazy val fieldsByPropertyName = _fields.values.groupBy(_.propertyName).mapValues(_.head)
//  lazy val fields = _fields.values
//  lazy val mappingName = classTag[A].runtimeClass.getName.stripSuffix("$")
//
//  // Mapping options
//  protected var ttl : Option[Duration] = None // enable ttl without a default ttl value: Some(Duration.Inf)
//
//  // Field types
//  private implicit def fromSringTuple(t: (String, String)) = Seq(new ESFieldOption(t._1, JsString(t._2)))
//  protected object String extends FieldType[String]("type" -> "string")
//  protected object Float extends FieldType[Float]("type" -> "float")
//  protected object Double extends FieldType[Double]("type" -> "double")
//  protected object Byte extends FieldType[Byte]("type" -> "byte")
//  protected object Short extends FieldType[Short]("type" -> "short")
//  protected object Integer extends FieldType[Int]("type" -> "integer")
//  protected object Long extends FieldType[Long]("type" -> "long")
//  protected object TokenCount extends FieldType[Int]("type" -> "token_count")
//  protected object Date extends FieldType[DateTime]("type" -> "date")
//  protected object Boolean extends FieldType[Boolean]("type" -> "boolean")
//  protected object Binary extends FieldType[Array[Byte]]("type" -> "binary")
//
//  protected def Object[B :ClassTag :JsonFormat](mapping: ESMapping[B]) =
//    new FieldType[B]("type" -> "object", mapping)
//
//  protected def Nested[B :ClassTag :JsonFormat](mapping: ESMapping[B]) =
//    new FieldType[B]("type" -> "nested", mapping)
//
//  // Field options
//  protected object Analyzed extends ESFieldOption("index", JsString("analyzed"))
//  protected object NotAnalyzed extends ESFieldOption("index", JsString("not_analyzed"))
//
//  private[elasticsearch2] class FieldType[T :ClassTag :JsonFormat](options: Seq[ESFieldOption], childMapping: Option[ESMapping[T]] = None) {
//    def as[T2](implicit jsonFormat: JsonFormat[T2], classTag: ClassTag[T2]) =
//      if (childMapping.isDefined) throw new Exception("Can't convert a field with a child mapping to another type")
//      else new FieldType[T2](options, None)
//    def ::(name: String) = new Field(name, name, options, childMapping)
//    def ::(name: (String, String)) = new Field(name._1, name._2, options, childMapping)
//    class Field(name: String, propertyName: String, options: Seq[ESFieldOption], childMapping: Option[ESMapping[T]])
//      extends ESField[A, T](name, propertyName, options, childMapping)(implicitly[ClassTag[T]], implicitly[JsonFormat[T]]) {
//      _fields += (name -> this)
//      def & (option: ESFieldOption) = new Field(name, propertyName, options :+ option, childMapping)
//    }
//  }
//
//  def toMappingDefinition: JsObject = {
//    // Check mapping
//    checkValid.toSeq.isEmptyOrElse(errors => throw new Exception(s"Mapping errors found:\n  ${errors.mkString("\n  ")}"))
//
//    JsObject(
//      "_all" -> JsObject("enabled" -> JsTrue) ::
//      "_source" -> JsObject("enabled" -> JsTrue) ::
//      "store" -> JsTrue ::
//      "properties" -> toProperties ::
//      ttl.toList.map {
//        case ttl if ttl.isFinite => "_ttl" -> JsObject("enabled" -> JsTrue, "default" -> JsNumber(ttl.toMillis))
//        case ttl => "_ttl" -> JsObject("enabled" -> JsTrue)
//      })
//  }
//
//  private def toProperties : JsObject =
//    JsObject(_fields.values.map(f =>
//      f.name -> JsObject(
//        f.options.map(o => o.name -> o.value) ++
//        f.childMapping.flatMap("properties" -> _.toProperties) toMap)).toMap)
//
//  private def checkValid : Iterable[String] = {
//    // All properties should have a mapping field
//    caseClassFields.keys.flatMap { propertyName =>
//      if (fieldsByPropertyName.contains(propertyName)) Seq.empty
//      else Seq(s"No field defined for 'propertyName' in mapping $mappingName")
//    } ++
//    // All options should be unique
//    fields.flatMap(f => f.options.groupBy(_.name).flatMap {
//      case (name, values) if values.size > 1 => Seq(s"Mapping $mappingName.${f.name} contains incompatible options: " + values.mkString(", "))
//      case _ => Seq.empty
//    }) ++
//    // Recurse
//    fields.flatMap(_.childMapping.map(_.checkValid).getOrElse(Nil))
//  }
//}
//
//case class ESField[+A, T] protected (name: String, propertyName: String, options: Seq[ESFieldOption], childMapping: Option[ESMapping[T]])(implicit val classTag: ClassTag[T], val jsonFormat: JsonFormat[T])
//
//object ESField {
//  implicit def toPath[A, T](field: ESField[A, T]) = ESPath[A, T](Seq(field))
//  implicit def toPathExt[A, T](field: ESField[_ <: ExtensionFor[A], T]) = ESPath[A, T](Seq(field))
//}
//
//case class ESFieldOption(name: String, value: JsValue)
//
//case class ESPath[A, T](fields: Seq[ESField[_, _]]) {
//  def /[T2, P <% ESPath[T, T2]](path: P) = ESPath[A, T2](fields ++ path.fields)
//  def ++[T2] (other : ESPath[T, T2]) = ESPath[A, T2](fields ++ other.fields)
//
//  def equ(value: T) = ESCriteria.Equ(this, value)
//
//  override def toString = fields.map(_.name).mkString(".")
//}
//
//
//
//}
//package test {
//import com.busymachines.commons.elasticsearch2._
//import com.busymachines.prefab.party.domain.Party
//import com.busymachines.prefab.party.implicits._
//import spray.json.JsValue
//import com.busymachines.commons.implicits._
//
//case class ThingBox(things: List[Thing])
//case class Thing(name: String, extensions: Extensions)
//case class BigThing(size: Int) extends ExtensionFor[Thing]
//
//object Formats {
//implicit val thingFormat = jsonFormat1Ext(Thing)
//implicit val thingBoxFormat = jsonFormat1(ThingBox)
//implicit val bigThingFormat = jsonFormat1(BigThing)
//}
//import Formats._
//
//object ThingBoxMapping extends ESMapping[ThingBox] {
//  val things = "things" :: Nested(ThingMapping)
//}
//
//object ThingMapping extends ESMapping[Thing] {
//  val name = "name" :: String & Analyzed
//}
//
//object BigThingMapping extends ESMapping[BigThing] {
//  val size = "size" :: Integer & Analyzed
//}
//
//  object App {
//    val p1: ESField[BigThing, Int] = BigThingMapping.size
//    val p2: ESPath[BigThing, Int] = BigThingMapping.size
//    val p3: ESPath[ThingBox, String] = ThingBoxMapping.things / ThingMapping.name
//    val p4: ESPath[Thing, Int] = BigThingMapping.size
//    val p5: ESPath[ThingBox, Int] = ThingBoxMapping.things / BigThingMapping.size
//  }
//
//object AddressMapping extends ESMapping[Address] {
//    val f1 = "id" :: String
//}
//}