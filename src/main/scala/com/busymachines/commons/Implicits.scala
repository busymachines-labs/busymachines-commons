package com.busymachines.commons

import java.io.InputStream
import java.net.URL
import java.util.Locale
import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.xml.Elem
import scala.xml.factory.XMLLoader
import org.joda.time.ReadablePartial
import com.busymachines.commons.dao.Versioned
import com.busymachines.commons.implicits._
import com.typesafe.config.Config
import implicits.RichAny
import implicits.RichByteArray
import implicits.RichConfig
import implicits.RichDouble
import implicits.RichFunction
import implicits.RichFuture
import implicits.RichFutureType
import implicits.RichInputStream
import implicits.RichIterable
import implicits.RichIterableMap
import implicits.RichJsValue
import implicits.RichLocale
import implicits.RichOption
import implicits.RichSeq
import implicits.RichString
import implicits.RichStringCollection
import implicits.RichUrl
import implicits.RichXml
import implicits.JodaImplicits
import scala.concurrent.Future
import _root_.spray.json.JsValue
import com.busymachines.commons.domain.CommonDomainJsonFormats
import com.busymachines.commons.logging.LoggingJsonFormats

object Implicits extends CommonJsonFormats with CommonDomainJsonFormats with ExtensionsImplicits with JodaImplicits with LoggingJsonFormats {
  implicit def richConfig(config : Config) = new RichConfig(config)
  implicit def richCommonConfigType[A <: CommonConfig](f : String => A) = new RichCommonConfigType[A](f)
  implicit def richJsValue(value : JsValue) = new RichJsValue(value)
  implicit def richIterable[A, C[A] <: Iterable[A]](collection : C[A]) = new RichIterable[A, C](collection)
  implicit def richStringCollection[C <: Iterable[String]](collection : C)(implicit cbf: CanBuildFrom[C, String, C]) = new RichStringCollection[C](collection)
  implicit def richSeq[A](seq : Seq[A]) = new RichSeq[A](seq)
  implicit def richIterableMap[K, V, I <: Iterable[V]](map : Map[K, I]) = new RichIterableMap[K, V, I](map)
  implicit def richListMap[K, V](map : Map[K, List[V]]) = new RichIterableMap[K, V, List[V]](map)
  implicit def richDouble(value : Double) = new RichDouble(value)
  implicit def richString(s : String) = new RichString(s)
  implicit def richByteArray(bytes : Array[Byte]) = new RichByteArray(bytes)
  implicit def richUrl(url : URL) = new RichUrl(url)
  implicit def richInputStream(is : InputStream) = new RichInputStream(is)
  implicit def richXml(xml : XMLLoader[Elem]) = new RichXml(xml)
  implicit def richOption[A](option : Option[A]) = new RichOption(option)
  implicit def richFunction[A, B](f : A => Option[B]) = new RichFunction(f)
  implicit def richFuture[A](f : Future[A]) = new RichFuture[A](f)
  implicit def richFutureType[A](f : Future.type) = new RichFutureType(f)
  implicit def richLocale(a : Locale) = new RichLocale(a)
  implicit def richAny[A](a : A) = new RichAny[A](a)
  implicit def convertToUnit(f : Future[_])(implicit ec : ExecutionContext) : Future[Unit] = f.map(_ => Unit)

  implicit def richVersionedFuture[A](future: Future[Versioned[A]])(implicit ec: ExecutionContext) = future.map(_.entity)
  implicit def richVersionedListFuture[A](future: Future[List[Versioned[A]]])(implicit ec: ExecutionContext) = future.map(_.map(_.entity))
  implicit def richVersionedSeqFuture[A](future: Future[Seq[Versioned[A]]])(implicit ec: ExecutionContext) = future.map(_.map(_.entity))
  implicit def richVersionedOptionFuture[A](future: Future[Option[Versioned[A]]])(implicit ec: ExecutionContext) = future.map(_.map(_.entity))

}

