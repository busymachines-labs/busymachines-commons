package com.busymachines.commons.spray

import _root_.spray.json.{JsValue, JsObject}
import scala.reflect.ClassTag

trait ProductJsonFormats {
  type Fields = Seq[ProductField]
  type JFmt[A] = ProductFieldJsonFormat[A]

  def productFormat1[P <: Product :ClassTag, F1 :JFmt](construct: (F1) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      Nil)
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value)
    )
  }

  def productFormat2[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt](construct: (F1, F2) => P) = new ProductJsonFormat[P] {
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

  def productFormat3[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt](construct: (F1, F2, F3) => P) = new ProductJsonFormat[P] {
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

  def productFormat4[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt](construct: (F1, F2, F3, F4) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      Nil))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value)
    )
  }
  
  def productFormat5[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt, F5 :JFmt](construct: (F1, F2, F3, F4, F5) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      Nil)))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value)
    )
  }
  
  def productFormat6[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt, F5 :JFmt, F6 :JFmt](construct: (F1, F2, F3, F4, F5, F6) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      Nil))))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value)
    )
  }
  
  def productFormat7[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt, F5 :JFmt, F6 :JFmt, F7 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      Nil)))))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value)
    )
  }
  
  def productFormat8[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt, F5 :JFmt, F6 :JFmt, F7 :JFmt, F8 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      Nil))))))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value)
    )
  }
  
  def productFormat9[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt, F5 :JFmt, F6 :JFmt, F7 :JFmt, F8 :JFmt, F9 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8, F9) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      write[F9](fields(8), p, 8,
      Nil)))))))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value),
      read[F9](fields(8), value)
    )
  }
  
  def productFormat10[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt, F5 :JFmt, F6 :JFmt, F7 :JFmt, F8 :JFmt, F9 :JFmt, F10 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8, F9, F10) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      write[F9](fields(8), p, 8,
      write[F10](fields(9), p, 9,
      Nil))))))))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value),
      read[F9](fields(8), value),
      read[F10](fields(9), value)
    )
  }
  
  def productFormat11[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt, F4 :JFmt, F5 :JFmt, F6 :JFmt, F7 :JFmt, F8 :JFmt, F9 :JFmt, F10 :JFmt, F11 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      write[F9](fields(8), p, 8,
      write[F10](fields(9), p, 9,
      write[F11](fields(10), p, 10,
      Nil)))))))))))
    )
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value),
      read[F9](fields(8), value),
      read[F10](fields(9), value),
      read[F11](fields(10), value)
    )
  }
  
  def productFormat12[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt,F4 :JFmt,F5 :JFmt,F6 :JFmt,F7 :JFmt,F8 :JFmt,F9 :JFmt,F10 :JFmt,F11 :JFmt,F12 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      write[F9](fields(8), p, 8,
      write[F10](fields(9), p, 9,
      write[F11](fields(10), p, 10,
      write[F12](fields(11), p, 11,
      Nil))))))))))))
    )
    
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value),
      read[F9](fields(8), value),
      read[F10](fields(9), value),
      read[F11](fields(10), value),
      read[F12](fields(11), value)
    )
  }
  
  def productFormat13[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt,F4 :JFmt,F5 :JFmt,F6 :JFmt,F7 :JFmt,F8 :JFmt,F9 :JFmt,F10 :JFmt,F11 :JFmt, F12 :JFmt, F13 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      write[F9](fields(8), p, 8,
      write[F10](fields(9), p, 9,
      write[F11](fields(10), p, 10,
      write[F12](fields(11), p, 11,
      write[F13](fields(12), p, 12,
      Nil)))))))))))))
    )
    
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value),
      read[F9](fields(8), value),
      read[F10](fields(9), value),
      read[F11](fields(10), value),
      read[F12](fields(11), value),
      read[F13](fields(12), value)      
    )
  }  
  
  def productFormat14[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt,F4 :JFmt,F5 :JFmt,F6 :JFmt,F7 :JFmt,F8 :JFmt,F9 :JFmt,F10 :JFmt,F11 :JFmt, F12 :JFmt, F13 :JFmt, F14 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      write[F9](fields(8), p, 8,
      write[F10](fields(9), p, 9,
      write[F11](fields(10), p, 10,
      write[F12](fields(11), p, 11,
      write[F13](fields(12), p, 12,
      write[F14](fields(13), p, 13,
      Nil))))))))))))))
    )
    
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value),
      read[F9](fields(8), value),
      read[F10](fields(9), value),
      read[F11](fields(10), value),
      read[F12](fields(11), value),
      read[F13](fields(12), value),
      read[F14](fields(13), value)            
    )
  }
  
  def productFormat15[P <: Product :ClassTag, F1 :JFmt, F2 :JFmt, F3 :JFmt,F4 :JFmt,F5 :JFmt,F6 :JFmt,F7 :JFmt,F8 :JFmt,F9 :JFmt,F10 :JFmt,F11 :JFmt, F12 :JFmt, F13 :JFmt, F14 :JFmt, F15 :JFmt](construct: (F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15) => P) = new ProductJsonFormat[P] {
    def write(fields: Fields, p: P) = JsObject(
      write[F1](fields(0), p, 0,
      write[F2](fields(1), p, 1,
      write[F3](fields(2), p, 2,
      write[F4](fields(3), p, 3,
      write[F5](fields(4), p, 4,
      write[F6](fields(5), p, 5,
      write[F7](fields(6), p, 6,
      write[F8](fields(7), p, 7,
      write[F9](fields(8), p, 8,
      write[F10](fields(9), p, 9,
      write[F11](fields(10), p, 10,
      write[F12](fields(11), p, 11,
      write[F13](fields(12), p, 12,
      write[F14](fields(13), p, 13,
      write[F15](fields(14), p, 14,
      Nil)))))))))))))))
    )
    
    def read(fields: Fields, value: JsValue) = construct(
      read[F1](fields(0), value),
      read[F2](fields(1), value),
      read[F3](fields(2), value),
      read[F4](fields(3), value),
      read[F5](fields(4), value),
      read[F6](fields(5), value),
      read[F7](fields(6), value),
      read[F8](fields(7), value),
      read[F9](fields(8), value),
      read[F10](fields(9), value),
      read[F11](fields(10), value),
      read[F12](fields(11), value),
      read[F13](fields(12), value),
      read[F14](fields(13), value),            
      read[F15](fields(14), value)            
    )
  }

}
