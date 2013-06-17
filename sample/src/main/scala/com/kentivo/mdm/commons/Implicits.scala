//package com.kentivo.mdm.commons
//
//import java.security.MessageDigest
//import com.typesafe.config.Config
//import spray.json.JsValue
//
//package object implicits {
//  implicit def toOption[A](a: A) = Option(a)
//  implicit class RichConfig(val config: Config) extends AnyVal {
//    def getOptionalInt(path: String) = ConfigUtils.getOptionalInt(config, path)
//    def mkString(sep: String) = ConfigUtils.mkString(config, sep)
//    def toSeq: Seq[String] = ConfigUtils.toSeq(config)
//  }
//  implicit class RichString(val s: String) extends AnyVal {
//    def sha256Hash: Array[Byte] = {
//      val digest = MessageDigest.getInstance("SHA-256")
//      digest.update(s.getBytes("UTF-8"))
//      digest.digest
//    }
//  }
//  implicit class RichJsValue(val value: JsValue) extends AnyVal {
//    def recurse = JsonUtils.recurse(value) _
//  }
//  implicit class ESRichJsValue(value: JsValue) {
//    def withESIds: JsValue = JsonUtils.recurse(value) {
//      case ("id", value) => ("_id", value)
//    }
//    def withoutESIds: JsValue = JsonUtils.recurse(value) {
//      case ("_id", value) => ("id", value)
//    }
//  }
//  implicit class DroppedFunction[A, B](f: A => Option[B]) extends PartialFunction[A, B] {
//    private[this] var arg: Option[A] = None
//    private[this] var result: Option[B] = None
//    private[this] def cache(a: A) {
//      if (Some(a) != arg) {
//        arg = Some(a)
//        result = f(a)
//      }
//    }
//    def isDefinedAt(a: A) = {
//      cache(a)
//      result.isDefined
//    }
//    def apply(a: A) = {
//      cache(a)
//      result.get
//    }
//  }
//}