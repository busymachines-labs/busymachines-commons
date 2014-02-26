package com.busymachines.commons

import java.net.URL
import java.util.Locale

import scala.collection.generic.CanBuildFrom
import scala.concurrent.ExecutionContext
import scala.xml.Elem
import scala.xml.factory.XMLLoader

import com.busymachines.commons.domain.CommonJsonFormats
import com.typesafe.config.Config

import scala.concurrent.Future
import _root_.spray.json.JsValue

trait Implicits extends CommonJsonFormats {
  implicit def toOption[A](a: A) = Option(a)
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
  implicit def richXml(xml : XMLLoader[Elem]) = new RichXml(xml)
  implicit def richOption[A](option : Option[A]) = new RichOption(option)
  implicit def richFunction[A, B](f : A => Option[B]) = new RichFunction(f)
  implicit def richFuture[A](f : Future[A]) = new RichFuture[A](f)
  implicit def richFutureType[A](f : Future.type) = new RichFutureType(f)
  implicit def richLocale(a : Locale) = new RichLocale(a)
  implicit def richAny[A](a : A) = new RichAny[A](a)
  implicit def convertToUnit(f : Future[_])(implicit ec : ExecutionContext) : Future[Unit] = f.map(_ => Unit)
}

package object implicits extends Implicits

