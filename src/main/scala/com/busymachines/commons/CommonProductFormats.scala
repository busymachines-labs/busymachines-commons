package com.busymachines.commons

import _root_.spray.json._
import java.lang.reflect.Modifier
import scala.reflect.ClassTag
import scala.reflect.classTag

trait CommonProductFormats { self: StandardFormats =>

  type JFmt[T] = JsonFormat[T]
  case class Field(name: String, getDefault: Option[() => Any])
  implicit def toField(s : String) = Field(s, None)
  implicit def toField(s : (String, () => Any)) = Field(s._1, Some(s._2))

  def jsonFormat0[T <: Product :ClassTag](construct: () => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    jsonFormat(construct)
  }
  def jsonFormat[T <: Product](construct: () => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject()
    def read(value: JsValue) = construct()
  }

  def jsonFormat1[A :JFmt, T <: Product :ClassTag](construct: A => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a)
  }
  def jsonFormat[A :JFmt, T <: Product](construct: A => T, a : Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0)
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a)
    )
  }

  def jsonFormat2Ext[A :JFmt, B: JFmt, T <: Product :ClassTag](construct: (A, B, Extensions) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, _) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat((a: A, b: B) => construct(a, b, Extensions.empty), a, b)
    new RootJsonFormat[T] {
      def write(p: T) =
        JsObject(
          Extensions.write(p.productElement(2).asInstanceOf[Extensions]) ++
          productElement2Field[A](a.name, p, 0,
            productElement2Field[B](b.name, p, 1)))

      def read(value: JsValue) = construct(
        fromField[A](value, a),
        fromField[B](value, b),
        Extensions.read[T](value))
    }
  }

  def jsonFormat2[A :JFmt, B :JFmt, T <: Product :ClassTag](construct: (A, B) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b)
  }

  def jsonFormat[A :JFmt, B :JFmt, T <: Product](construct: (A, B) => T, a : Field, b: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b)
    )
  }

  def jsonFormat3[A :JFmt, B :JFmt, C :JFmt, T <: Product :ClassTag](construct: (A, B, C) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, T <: Product](construct: (A, B, C) => T,
                                                    a : Field, b: Field, c: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2)))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c)
    )
  }

  def jsonFormat4[A :JFmt, B :JFmt, C :JFmt, D :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, T <: Product](construct: (A, B, C, D) => T,
                                                           a : Field, b: Field, c: Field, d: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d)
    )
  }

  def jsonFormat5[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, T <: Product](construct: (A, B, C, D, E) => T,
                                                                  a : Field, b: Field, c: Field, d: Field, e: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3,
              productElement2Field[E](e.name, p, 4)))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e)
    )
  }

  def jsonFormat6[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, T <: Product](construct: (A, B, C, D, E, F) => T,
                                                                         a : Field, b: Field, c: Field, d: Field, e: Field, f: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3,
              productElement2Field[E](e.name, p, 4,
                productElement2Field[F](f.name, p, 5))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f)
    )
  }

  def jsonFormat7[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, T <: Product](construct: (A, B, C, D, E, F, G) => T,
                                                                                a : Field, b: Field, c: Field, d: Field, e: Field, f: Field, g: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3,
              productElement2Field[E](e.name, p, 4,
                productElement2Field[F](f.name, p, 5,
                  productElement2Field[G](g.name, p, 6)))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g)
    )
  }

  def jsonFormat8[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H) => T,
   a : Field, b: Field, c: Field, d: Field, e: Field, f: Field, g: Field, h: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3,
              productElement2Field[E](e.name, p, 4,
                productElement2Field[F](f.name, p, 5,
                  productElement2Field[G](g.name, p, 6,
                    productElement2Field[H](h.name, p, 7))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h)
    )
  }

  def jsonFormat9[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H, I) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H, I) => T, a : Field, b: Field, c: Field, d: Field, e: Field, f: Field,
   g: Field, h: Field, i: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3,
              productElement2Field[E](e.name, p, 4,
                productElement2Field[F](f.name, p, 5,
                  productElement2Field[G](g.name, p, 6,
                    productElement2Field[H](h.name, p, 7,
                      productElement2Field[I](i.name, p, 8)))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i)
    )
  }

  def jsonFormat10[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H, I, J) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H, I, J) => T, a : Field, b: Field, c: Field, d: Field, e: Field,
   f: Field, g: Field, h: Field, i: Field, j: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3,
              productElement2Field[E](e.name, p, 4,
                productElement2Field[F](f.name, p, 5,
                  productElement2Field[G](g.name, p, 6,
                    productElement2Field[H](h.name, p, 7,
                      productElement2Field[I](i.name, p, 8,
                        productElement2Field[J](j.name, p, 9))))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j)
    )
  }

  def jsonFormat11[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H, I, J, K) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H, I, J, K) => T, a : Field, b: Field, c: Field, d: Field, e: Field,
   f: Field, g: Field, h: Field, i: Field, j: Field, k: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p, 0,
        productElement2Field[B](b.name, p, 1,
          productElement2Field[C](c.name, p, 2,
            productElement2Field[D](d.name, p, 3,
              productElement2Field[E](e.name, p, 4,
                productElement2Field[F](f.name, p, 5,
                  productElement2Field[G](g.name, p, 6,
                    productElement2Field[H](h.name, p, 7,
                      productElement2Field[I](i.name, p, 8,
                        productElement2Field[J](j.name, p, 9,
                          productElement2Field[K](k.name, p, 10)))))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k)
    )
  }

  def jsonFormat12[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L: JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L) => T, a : Field, b: Field, c: Field, d: Field, e: Field,
   f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p,  0,
        productElement2Field[B](b.name, p,  1,
          productElement2Field[C](c.name, p,  2,
            productElement2Field[D](d.name, p,  3,
              productElement2Field[E](e.name, p,  4,
                productElement2Field[F](f.name, p,  5,
                  productElement2Field[G](g.name, p,  6,
                    productElement2Field[H](h.name, p,  7,
                      productElement2Field[I](i.name, p,  8,
                        productElement2Field[J](j.name, p,  9,
                          productElement2Field[K](k.name, p, 10,
                            productElement2Field[L](l.name, p, 11))))))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l)
    )
  }

  def jsonFormat13[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L: JFmt, M :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L, M) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L :JFmt, M :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L, M) => T, a : Field, b: Field, c: Field, d: Field, e: Field,
   f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p,  0,
        productElement2Field[B](b.name, p,  1,
          productElement2Field[C](c.name, p,  2,
            productElement2Field[D](d.name, p,  3,
              productElement2Field[E](e.name, p,  4,
                productElement2Field[F](f.name, p,  5,
                  productElement2Field[G](g.name, p,  6,
                    productElement2Field[H](h.name, p,  7,
                      productElement2Field[I](i.name, p,  8,
                        productElement2Field[J](j.name, p,  9,
                          productElement2Field[K](k.name, p, 10,
                            productElement2Field[L](l.name, p, 11,
                              productElement2Field[M](m.name, p, 12)))))))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m)
    )
  }

  def jsonFormat14[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L: JFmt, M :JFmt, N :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L :JFmt, M :JFmt, N :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => T, a : Field, b: Field, c: Field, d: Field,
   e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field,
   n: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p,  0,
        productElement2Field[B](b.name, p,  1,
          productElement2Field[C](c.name, p,  2,
            productElement2Field[D](d.name, p,  3,
              productElement2Field[E](e.name, p,  4,
                productElement2Field[F](f.name, p,  5,
                  productElement2Field[G](g.name, p,  6,
                    productElement2Field[H](h.name, p,  7,
                      productElement2Field[I](i.name, p,  8,
                        productElement2Field[J](j.name, p,  9,
                          productElement2Field[K](k.name, p, 10,
                            productElement2Field[L](l.name, p, 11,
                              productElement2Field[M](m.name, p, 12,
                                productElement2Field[N](n.name, p, 13))))))))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n)
    )
  }

  def jsonFormat15[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L: JFmt, M :JFmt, N :JFmt, O :JFmt, T <: Product :ClassTag]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)
  }
  def jsonFormat[A :JFmt, B :JFmt, C :JFmt, D :JFmt, E :JFmt, F :JFmt, G :JFmt, H :JFmt, I :JFmt, J :JFmt, K :JFmt, L :JFmt, M :JFmt, N :JFmt, O :JFmt, T <: Product]
  (construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => T, a : Field, b: Field, c: Field, d: Field,
   e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
   o: Field): RootJsonFormat[T] = new RootJsonFormat[T]{
    def write(p: T) = JsObject(
      productElement2Field[A](a.name, p,  0,
        productElement2Field[B](b.name, p,  1,
          productElement2Field[C](c.name, p,  2,
            productElement2Field[D](d.name, p,  3,
              productElement2Field[E](e.name, p,  4,
                productElement2Field[F](f.name, p,  5,
                  productElement2Field[G](g.name, p,  6,
                    productElement2Field[H](h.name, p,  7,
                      productElement2Field[I](i.name, p,  8,
                        productElement2Field[J](j.name, p,  9,
                          productElement2Field[K](k.name, p, 10,
                            productElement2Field[L](l.name, p, 11,
                              productElement2Field[M](m.name, p, 12,
                                productElement2Field[N](n.name, p, 13,
                                  productElement2Field[O](o.name, p, 14)))))))))))))))
    )
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o)
    )
  }

  def jsonFormat16[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)
  }
  def jsonFormat[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => T, a: Field, b: Field, c: Field, d: Field,
                                                                                                                                               e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
                                                                                                                                               o: Field, p: Field): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a.name, pw, 0,
        productElement2Field[B](b.name, pw, 1,
          productElement2Field[C](c.name, pw, 2,
            productElement2Field[D](d.name, pw, 3,
              productElement2Field[E](e.name, pw, 4,
                productElement2Field[F](f.name, pw, 5,
                  productElement2Field[G](g.name, pw, 6,
                    productElement2Field[H](h.name, pw, 7,
                      productElement2Field[I](i.name, pw, 8,
                        productElement2Field[J](j.name, pw, 9,
                          productElement2Field[K](k.name, pw, 10,
                            productElement2Field[L](l.name, pw, 11,
                              productElement2Field[M](m.name, pw, 12,
                                productElement2Field[N](n.name, pw, 13,
                                  productElement2Field[O](o.name, pw, 14,
                                    productElement2Field[P](p.name, pw, 15)))))))))))))))))
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o),
      fromField[P](value, p))
  }

  def jsonFormat17[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)
  }
  def jsonFormat[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => T, a: Field, b: Field, c: Field, d: Field,
                                                                                                                                                      e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
                                                                                                                                                      o: Field, p: Field, q: Field): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a.name, pw, 0,
        productElement2Field[B](b.name, pw, 1,
          productElement2Field[C](c.name, pw, 2,
            productElement2Field[D](d.name, pw, 3,
              productElement2Field[E](e.name, pw, 4,
                productElement2Field[F](f.name, pw, 5,
                  productElement2Field[G](g.name, pw, 6,
                    productElement2Field[H](h.name, pw, 7,
                      productElement2Field[I](i.name, pw, 8,
                        productElement2Field[J](j.name, pw, 9,
                          productElement2Field[K](k.name, pw, 10,
                            productElement2Field[L](l.name, pw, 11,
                              productElement2Field[M](m.name, pw, 12,
                                productElement2Field[N](n.name, pw, 13,
                                  productElement2Field[O](o.name, pw, 14,
                                    productElement2Field[P](p.name, pw, 15,
                                      productElement2Field[Q](q.name, pw, 16))))))))))))))))))

    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o),
      fromField[P](value, p),
      fromField[Q](value, q))
  }

  def jsonFormat18[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)
  }
  def jsonFormat[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => T, a: Field, b: Field, c: Field, d: Field,
                                                                                                                                                             e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
                                                                                                                                                             o: Field, p: Field, q: Field, r: Field): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a.name, pw, 0,
        productElement2Field[B](b.name, pw, 1,
          productElement2Field[C](c.name, pw, 2,
            productElement2Field[D](d.name, pw, 3,
              productElement2Field[E](e.name, pw, 4,
                productElement2Field[F](f.name, pw, 5,
                  productElement2Field[G](g.name, pw, 6,
                    productElement2Field[H](h.name, pw, 7,
                      productElement2Field[I](i.name, pw, 8,
                        productElement2Field[J](j.name, pw, 9,
                          productElement2Field[K](k.name, pw, 10,
                            productElement2Field[L](l.name, pw, 11,
                              productElement2Field[M](m.name, pw, 12,
                                productElement2Field[N](n.name, pw, 13,
                                  productElement2Field[O](o.name, pw, 14,
                                    productElement2Field[P](p.name, pw, 15,
                                      productElement2Field[Q](q.name, pw, 16,
                                        productElement2Field[R](r.name, pw, 17)))))))))))))))))))

    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o),
      fromField[P](value, p),
      fromField[Q](value, q),
      fromField[R](value, r))
  }

  def jsonFormat19[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)
  }

  def jsonFormat[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => T, a: Field, b: Field, c: Field, d: Field,
                                                                                                                                                                    e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
                                                                                                                                                                    o: Field, p: Field, q: Field, r: Field, s: Field): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a.name, pw, 0,
        productElement2Field[B](b.name, pw, 1,
          productElement2Field[C](c.name, pw, 2,
            productElement2Field[D](d.name, pw, 3,
              productElement2Field[E](e.name, pw, 4,
                productElement2Field[F](f.name, pw, 5,
                  productElement2Field[G](g.name, pw, 6,
                    productElement2Field[H](h.name, pw, 7,
                      productElement2Field[I](i.name, pw, 8,
                        productElement2Field[J](j.name, pw, 9,
                          productElement2Field[K](k.name, pw, 10,
                            productElement2Field[L](l.name, pw, 11,
                              productElement2Field[M](m.name, pw, 12,
                                productElement2Field[N](n.name, pw, 13,
                                  productElement2Field[O](o.name, pw, 14,
                                    productElement2Field[P](p.name, pw, 15,
                                      productElement2Field[Q](q.name, pw, 16,
                                        productElement2Field[R](r.name, pw, 17,
                                          productElement2Field[S](s.name, pw, 18))))))))))))))))))))
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o),
      fromField[P](value, p),
      fromField[Q](value, q),
      fromField[R](value, r),
      fromField[S](value, s))
  }

  def jsonFormat20[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, U: JFmt, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u)
  }

  def jsonFormat[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, U: JFmt, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U) => T, a: Field, b: Field, c: Field, d: Field,
                                                                                                                                                                           e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
                                                                                                                                                                           o: Field, p: Field, q: Field, r: Field, s: Field, u: Field): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a.name, pw, 0,
        productElement2Field[B](b.name, pw, 1,
          productElement2Field[C](c.name, pw, 2,
            productElement2Field[D](d.name, pw, 3,
              productElement2Field[E](e.name, pw, 4,
                productElement2Field[F](f.name, pw, 5,
                  productElement2Field[G](g.name, pw, 6,
                    productElement2Field[H](h.name, pw, 7,
                      productElement2Field[I](i.name, pw, 8,
                        productElement2Field[J](j.name, pw, 9,
                          productElement2Field[K](k.name, pw, 10,
                            productElement2Field[L](l.name, pw, 11,
                              productElement2Field[M](m.name, pw, 12,
                                productElement2Field[N](n.name, pw, 13,
                                  productElement2Field[O](o.name, pw, 14,
                                    productElement2Field[P](p.name, pw, 15,
                                      productElement2Field[Q](q.name, pw, 16,
                                        productElement2Field[R](r.name, pw, 17,
                                          productElement2Field[S](s.name, pw, 18,
                                            productElement2Field[U](u.name, pw, 19)))))))))))))))))))))
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o),
      fromField[P](value, p),
      fromField[Q](value, q),
      fromField[R](value, r),
      fromField[S](value, s),
      fromField[U](value, u))
  }

  def jsonFormat21[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, U: JFmt, V: JFmt, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v)
  }

  def jsonFormat[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, U: JFmt, V: JFmt, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V) => T, a: Field, b: Field, c: Field, d: Field,
                                                                                                                                                                                  e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
                                                                                                                                                                                  o: Field, p: Field, q: Field, r: Field, s: Field, u: Field, v: Field): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a.name, pw, 0,
        productElement2Field[B](b.name, pw, 1,
          productElement2Field[C](c.name, pw, 2,
            productElement2Field[D](d.name, pw, 3,
              productElement2Field[E](e.name, pw, 4,
                productElement2Field[F](f.name, pw, 5,
                  productElement2Field[G](g.name, pw, 6,
                    productElement2Field[H](h.name, pw, 7,
                      productElement2Field[I](i.name, pw, 8,
                        productElement2Field[J](j.name, pw, 9,
                          productElement2Field[K](k.name, pw, 10,
                            productElement2Field[L](l.name, pw, 11,
                              productElement2Field[M](m.name, pw, 12,
                                productElement2Field[N](n.name, pw, 13,
                                  productElement2Field[O](o.name, pw, 14,
                                    productElement2Field[P](p.name, pw, 15,
                                      productElement2Field[Q](q.name, pw, 16,
                                        productElement2Field[R](r.name, pw, 17,
                                          productElement2Field[S](s.name, pw, 18,
                                            productElement2Field[U](u.name, pw, 19,
                                              productElement2Field[V](v.name, pw, 20))))))))))))))))))))))
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o),
      fromField[P](value, p),
      fromField[Q](value, q),
      fromField[R](value, r),
      fromField[S](value, s),
      fromField[U](value, u),
      fromField[V](value, v))
  }

  def jsonFormat22[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, U: JFmt, V: JFmt, W: JFmt, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V, W) => T, allowOptionalFields: Boolean = false): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v, w) = extractFieldNames(classTag[T], allowOptionalFields)
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v, w)
  }

  def jsonFormat[A: JFmt, B: JFmt, C: JFmt, D: JFmt, E: JFmt, F: JFmt, G: JFmt, H: JFmt, I: JFmt, J: JFmt, K: JFmt, L: JFmt, M: JFmt, N: JFmt, O: JFmt, P: JFmt, Q: JFmt, R: JFmt, S: JFmt, U: JFmt, V: JFmt, W: JFmt, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V, W) => T, a: Field, b: Field, c: Field, d: Field,
                                                                                                                                                                                         e: Field, f: Field, g: Field, h: Field, i: Field, j: Field, k: Field, l: Field, m: Field, n: Field,
                                                                                                                                                                                         o: Field, p: Field, q: Field, r: Field, s: Field, u: Field, v: Field, w: Field): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a.name, pw, 0,
        productElement2Field[B](b.name, pw, 1,
          productElement2Field[C](c.name, pw, 2,
            productElement2Field[D](d.name, pw, 3,
              productElement2Field[E](e.name, pw, 4,
                productElement2Field[F](f.name, pw, 5,
                  productElement2Field[G](g.name, pw, 6,
                    productElement2Field[H](h.name, pw, 7,
                      productElement2Field[I](i.name, pw, 8,
                        productElement2Field[J](j.name, pw, 9,
                          productElement2Field[K](k.name, pw, 10,
                            productElement2Field[L](l.name, pw, 11,
                              productElement2Field[M](m.name, pw, 12,
                                productElement2Field[N](n.name, pw, 13,
                                  productElement2Field[O](o.name, pw, 14,
                                    productElement2Field[P](p.name, pw, 15,
                                      productElement2Field[Q](q.name, pw, 16,
                                        productElement2Field[R](r.name, pw, 17,
                                          productElement2Field[S](s.name, pw, 18,
                                            productElement2Field[U](u.name, pw, 19,
                                              productElement2Field[V](v.name, pw, 20,
                                                productElement2Field[W](w.name, pw, 21)))))))))))))))))))))))
    def read(value: JsValue) = construct(
      fromField[A](value, a),
      fromField[B](value, b),
      fromField[C](value, c),
      fromField[D](value, d),
      fromField[E](value, e),
      fromField[F](value, f),
      fromField[G](value, g),
      fromField[H](value, h),
      fromField[I](value, i),
      fromField[J](value, j),
      fromField[K](value, k),
      fromField[L](value, l),
      fromField[M](value, m),
      fromField[N](value, n),
      fromField[O](value, o),
      fromField[P](value, p),
      fromField[Q](value, q),
      fromField[R](value, r),
      fromField[S](value, s),
      fromField[U](value, u),
      fromField[V](value, v),
      fromField[W](value, w))
  }

  private def fromField[T](value: JsValue, fieldAndDefault: Field)(implicit reader: JsonReader[T]) = {
    value match {
      case x: JsObject =>
        x.fields.get(fieldAndDefault.name) match {
          case Some(value) =>
            reader.read(value)
          case None => fieldAndDefault.getDefault match {
            case Some(defarg) =>
              defarg().asInstanceOf[T]
            case None =>
              if (reader.isInstanceOf[OptionFormat[_]]) None.asInstanceOf[T]
              else deserializationError("Object is missing required member '" + fieldAndDefault.name + "'")
          }
        }
      case _ => deserializationError("Object expected")
    }
  }

  protected def productElement2Field[T](fieldName: String, p: Product, ix: Int, rest: List[JsField] = Nil)
                                       (implicit writer: JsonWriter[T]): List[JsField] = {
    val value = p.productElement(ix).asInstanceOf[T]
    writer match {
      case _: OptionFormat[_] if value == None => rest
      case _ => (fieldName, writer.write(value)) :: rest
    }
  }

  protected def extractFieldNames(classManifest: ClassTag[_], allowOptionalFields: Boolean): Array[Field] = {
    val clazz = classManifest.runtimeClass
    try {
      // Need companion class for default arguments.
      lazy val companionClass = Class.forName(clazz.getName + "$")
      lazy val moduleField =
        try { companionClass.getField("MODULE$") }
        catch { case e : Throwable => throw new RuntimeException("Can't deserialize default arguments of nested case classes", e) }
      lazy val companionObj = moduleField.get(null)
      // copy methods have the form copy$default$N(), we need to sort them in order, but must account for the fact
      // that lexical sorting of ...8(), ...9(), ...10() is not correct, so we extract N and sort by N.toInt
      val copyDefaultMethods = clazz.getMethods.filter(_.getName.startsWith("copy$default$")).sortBy(
        _.getName.drop("copy$default$".length).takeWhile(_ != '(').toInt)
      val fields = clazz.getDeclaredFields.filterNot(f => f.getName.startsWith("$") || Modifier.isTransient(f.getModifiers))
      if (copyDefaultMethods.length != fields.length)
        sys.error("Case class " + clazz.getName + " declares additional fields")
      lazy val applyDefaultMethods = copyDefaultMethods.map { method =>
        try {
          val defmeth = companionClass.getMethod("apply" + method.getName.drop("copy".size))
          Some(() => defmeth.invoke(companionObj))}
        catch { case e : Throwable => None }
      }
      if (fields.zip(copyDefaultMethods).exists { case (f, m) => f.getType != m.getReturnType })
        sys.error("Cannot determine field order of case class " + clazz.getName)
      if (allowOptionalFields) {
        fields.zip(applyDefaultMethods).map { case (f, m) => Field(f.getName, m) }
      } else {
        fields.map { f => Field(f.getName, None) }
      }
    } catch {
      case ex : Throwable => throw new RuntimeException("Cannot automatically determine case class field names and order " +
        "for '" + clazz.getName + "', please use the 'jsonFormat' overload with explicit field name specification", ex)
    }
  }
}
