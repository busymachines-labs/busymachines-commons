package com.busymachines.commons

import com.typesafe.config.Config
import spray.json.JsValue
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import spray.json.JsObject
import scala.concurrent.ExecutionContext

package object implicits {
  implicit def toOption[A](a: A) = Option(a)
  implicit class RichConfig(val config: Config) extends AnyVal {
    def getOptionalInt(path: String) = ConfigUtils.getOptionalInt(config, path)
    def mkString(sep: String) = ConfigUtils.mkString(config, sep)
    def toSeq: Seq[String] = ConfigUtils.toSeq(config)
  }
  implicit class RichJsValue(val value: JsValue) extends AnyVal {
    def generateIds: JsValue = JsonUtils.replaceWithGeneratedIds(value)
  }
  implicit class RichFuture(f : Future[_]) {
    def mapAs[T](value : T)(implicit ec : ExecutionContext) = f.map(_ => value)
  }
}