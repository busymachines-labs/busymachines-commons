package com.busymachines.commons.spray

import _root_.spray.json.{JsValue, JsObject}
import scala.reflect.ClassTag

trait ProductJsonFormats {
  type Fields = Seq[ProductField]
  type JFmt[A] = ProductFieldJsonFormat[A]

  def format1[P <: Product :ClassTag, F1 :JFmt](construct: (F1) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      Nil)
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value)
    )
  }

  def format2[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt](construct: (F1, F2) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      Nil))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value)
    )
  }

  def format3[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt](construct: (F1, F2, F3) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      Nil)))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value)
    )
  }
}
