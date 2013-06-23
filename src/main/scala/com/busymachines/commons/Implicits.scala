package com.busymachines.commons

import com.typesafe.config.Config
import spray.json.JsValue
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import spray.json.JsObject
import scala.concurrent.ExecutionContext

package object implicits extends CommonImplicits

trait CommonImplicits {
  implicit def toOption[A](a: A) = Option(a)
  implicit def RichConfig(config : Config) = new RichConfig(config)
  implicit def RichJsValue(value : JsValue) = new RichJsValue(value)
  implicit def RichSeq[A](seq : Seq[A]) = new RichSeq[A](seq)
  implicit def RichString(s : String) = new RichString(s)
  implicit def RichFunction[A, B](f : A => Option[B]) = new RichFunction(f)
}