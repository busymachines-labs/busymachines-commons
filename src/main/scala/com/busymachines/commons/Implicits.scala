package com.busymachines.commons

import com.typesafe.config.Config
import _root_.spray.json.JsValue
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import _root_.spray.json.JsObject
import scala.concurrent.ExecutionContext
import java.net.URL
import scala.xml.factory.XMLLoader
import scala.xml.Elem
import scala.collection.{Map, Iterable}
import scala.AnyVal

package object implicits extends CommonImplicits

trait CommonImplicits {
  implicit def toOption[A](a: A) = Option(a)
  implicit def richConfig(config : Config) = new RichConfig(config)
  implicit def richJsValue(value : JsValue) = new RichJsValue(value)
  implicit def richSeq[A](seq : Seq[A]) = new RichSeq[A](seq)
  implicit def richIterableMap[K, V, I <: Iterable[V]](map : Map[K, I]) = new RichIterableMap[K, V, I](map)
  implicit def richListMap[K, V](map : Map[K, List[V]]) = new RichIterableMap[K, V, List[V]](map)
  implicit def richStringSeq(seq : Seq[String]) = new RichStringSeq(seq)
  implicit def richString(s : String) = new RichString(s)
  implicit def richByteArray(bytes : Array[Byte]) = new RichByteArray(bytes)
  implicit def richUrl(url : URL) = new RichUrl(url)
  implicit def richXml(xml : XMLLoader[Elem]) = new RichXml(xml)
  implicit def richFunction[A, B](f : A => Option[B]) = new RichFunction(f)
  implicit def richFuture[A](f : Future[A]) = new RichFuture[A](f)
  implicit def richFutureType[A](f : Future.type) = new RichFutureType(f)
  implicit def richAny[A](a : A) = new RichAny[A](a)
  implicit def convertToUnit(f : Future[_])(implicit ec : ExecutionContext) : Future[Unit] = f.map(_ => Unit)
}