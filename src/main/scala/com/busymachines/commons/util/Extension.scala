package com.busymachines.commons.util

import java.util.concurrent.atomic.AtomicBoolean

import _root_.spray.json._
import com.busymachines.commons.logging.Logging
import com.busymachines.commons.spray.json.{DefaultProductFieldFormat, ProductField, ProductFieldFormat, ProductFormat}

import scala.collection.concurrent.TrieMap
import scala.reflect.ClassTag

object Extensions {
  val emptyInstance = new Extensions[Any](Map.empty)

  private val extensionsUsed = new AtomicBoolean(false)
  private val registeredExtensions = new TrieMap[Extension[_, _], Extension[_, _]]

  /**
   * Registered extensions mapped by the ClassTag of the parent type.
   */
  private lazy val extensions: Map[ClassTag[_], Seq[Extension[_, _]]] = {
    extensionsUsed.set(true)
    registeredExtensions.keys.groupBy(_.parentClass).mapValues(_.toSeq)
  }

  /**
   * Registers an extension. Extensions should be registered before any of the extension functionality is used,
   * in particular before any case classes with extensions are convert to or from json.
   */
  private[commons] def register(ext: Extension[_, _]) {
//    if (extensionsUsed.get) throw new IllegalStateException(s"Extension $ext was registered after extensions were used. Try to register it earlier.")
    registeredExtensions.put(ext, ext)
  }

  /**
   * Empty extensions container, cast to parent type P.
   * To be used as the default value for the extensions field of a parent case class.
   */
  def empty[P] = emptyInstance.asInstanceOf[Extensions[P]]

  /**
   * Returns the registered extensions of parent type P.
   */
  def registeredExtensionsOf[P :ClassTag] : Seq[Extension[P, _]] =
    extensions.getOrElse(scala.reflect.classTag[P], Nil).asInstanceOf[Seq[Extension[P, _]]]

  /**
   * Implicitly converts an extension instance to an extensions container.
   */
  implicit def toExtensions[P, A](instance: A)(implicit e: Extension[P, A]) =
    new Extensions[P](Map(e -> instance))
}

/**
 * Container for extension instances. The parent case class should have a field of this type. There can only be
 * 1 instance per extension type.
 */
class Extensions[P](val map: Map[Extension[P, _], _]) {

  /**
   * Gets the extension instance for given type A.
   */
  def apply[A](implicit e: Extension[P, A]) : A =
    map.getOrElse(e, e.empty).asInstanceOf[A]

  /**
   * Transforms an extension instance of type A and returns a new extensions container that contains the result.
   */
  def copy[A](f: A => A)(implicit e: Extension[P, A]) =
    new Extensions(map + (e -> f(this(e))))

  /**
   * Prints the extension instances.
   */
  override def toString = "Extensions(" + map.values.mkString(",") + ")"
}

/**
 * Defines that parent class P can contain an extension instance of type A.
 * Extensions should be registered during application startup using #register().
 * Instances of this class should be marked implicit.
 */
class Extension[P, A](val getExt: P => Extensions[P], val cp: (P, Extensions[P]) => P)(implicit val parentClass: ClassTag[P], val extensionClass: ClassTag[A], val format: ProductFormat[A]) extends Logging {

  private val registered = new AtomicBoolean(false)

  val jsonNames: Set[String] = format.fields.map { field =>
    field.format match {
      case DefaultProductFieldFormat(jsonName, default, format) => jsonName.getOrElse(field.name)
      case _ => field.name
    }
  }.toSet

  /**
   * Registers an extension. Extensions should be registered before any of the extension functionality is used,
   * in particular before any case classes with extensions are convert to or from json.
   * Registering an extension too late will result in an exception being thrown.
   */
  def register() {
    logger.debug(s"Registered $this")
    registered.set(true)
    Extensions.register(this)
  }

  def checkRegistered =
    if (registered.get) this
    else throw new Exception(s"Extension $this was not registered")

  def apply(p: P) = getExt(p)(this)

  override def toString = s"Extension[${parentClass.runtimeClass.getSimpleName},${extensionClass.runtimeClass.getSimpleName}]"

  private[commons] lazy val empty = format.read(JsObject())
}

class ExtensionsProductFieldFormat[P: ClassTag] extends ProductFieldFormat[Extensions[P]] {
  def write(extensions: Extensions[P]): JsValue = JsNull // TODO throw new IllegalStateException
  def read(value: JsValue): Extensions[P] = Extensions.empty // TODO throw new IllegalStateException
  override def write(field: ProductField, extensions: Extensions[P], rest: List[JsField]) =
    extensions.map.toList.flatMap {
      case (e, a) =>
        formatOf(e.checkRegistered).asInstanceOf[JsonFormat[Any]].write(a.asInstanceOf[Any]) match {
          case JsObject(fields) => fields.toList
          case value => Nil
        }
    } ++ rest

  override def read(field: ProductField, obj: JsObject): Extensions[P] =
    Extensions.registeredExtensionsOf[P].flatMap {
      ext =>
        if (!obj.fields.exists(f => ext.jsonNames.contains(f._1))) Seq.empty
        else Seq(ext.asInstanceOf[Extension[P, Product]] -> formatOf(ext).read(obj))
    } match {
      case Seq() => Extensions.empty[P]
      case instances => new Extensions[P](instances.toMap)
    }
  protected def formatOf[A](ext: Extension[P, A]) = ext.format
}

/**
 * Json format for extensions. Extensions can only be used as fields withing a ProductJsonFormat.
 */
trait ExtensionsImplicits {
  implicit def extensionFormat[P :ClassTag] = new ExtensionsProductFieldFormat[P]
  implicit class RichProductWithExtensions[P](p: P) {
    def copyExt[A <: Product](cp: A => A)(implicit e: Extension[P, A]) =
      e.cp(p, e.getExt(p).copy(cp))
  }
  implicit class ConcatExtension[A](val a: A) {
    def & [P, A2](a2: A2)(implicit ext1: Extension[P, A], ext2: Extension[P, A2]) =
      new Extensions[P](Map(ext1 -> a, ext2 -> a2))
  }
}

