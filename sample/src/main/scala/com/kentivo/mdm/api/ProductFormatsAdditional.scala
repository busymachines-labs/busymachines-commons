package spray.json

trait ProductFormatsAdditional extends ProductFormats {
  this: StandardFormats =>

  def jsonFormat16[A: JF, B: JF, C: JF, D: JF, E: JF, F: JF, G: JF, H: JF, I: JF, J: JF, K: JF, L: JF, M: JF, N: JF, O: JF, P: JF, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => T): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p) = extractFieldNames(scala.reflect.classTag[T])
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)
  }
  def jsonFormat[A: JF, B: JF, C: JF, D: JF, E: JF, F: JF, G: JF, H: JF, I: JF, J: JF, K: JF, L: JF, M: JF, N: JF, O: JF, P: JF, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => T, a: String, b: String, c: String, d: String,
    e: String, f: String, g: String, h: String, i: String, j: String, k: String, l: String, m: String, n: String,
    o: String, p: String): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a, pw, 0,
        productElement2Field[B](b, pw, 1,
          productElement2Field[C](c, pw, 2,
            productElement2Field[D](d, pw, 3,
              productElement2Field[E](e, pw, 4,
                productElement2Field[F](f, pw, 5,
                  productElement2Field[G](g, pw, 6,
                    productElement2Field[H](h, pw, 7,
                      productElement2Field[I](i, pw, 8,
                        productElement2Field[J](j, pw, 9,
                          productElement2Field[K](k, pw, 10,
                            productElement2Field[L](l, pw, 11,
                              productElement2Field[M](m, pw, 12,
                                productElement2Field[N](n, pw, 13,
                                  productElement2Field[O](o, pw, 14,
                                    productElement2Field[P](p, pw, 15)))))))))))))))))
    def read(value: JsValue) = construct(
      fromFieldCustom[A](value, a),
      fromFieldCustom[B](value, b),
      fromFieldCustom[C](value, c),
      fromFieldCustom[D](value, d),
      fromFieldCustom[E](value, e),
      fromFieldCustom[F](value, f),
      fromFieldCustom[G](value, g),
      fromFieldCustom[H](value, h),
      fromFieldCustom[I](value, i),
      fromFieldCustom[J](value, j),
      fromFieldCustom[K](value, k),
      fromFieldCustom[L](value, l),
      fromFieldCustom[M](value, m),
      fromFieldCustom[N](value, n),
      fromFieldCustom[O](value, o),
      fromFieldCustom[P](value, p))
  }

  def jsonFormat18[A: JF, B: JF, C: JF, D: JF, E: JF, F: JF, G: JF, H: JF, I: JF, J: JF, K: JF, L: JF, M: JF, N: JF, O: JF, P: JF, Q: JF, R: JF, T <: Product: scala.reflect.ClassTag](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => T): RootJsonFormat[T] = {
    val Array(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r) = extractFieldNames(scala.reflect.classTag[T])
    jsonFormat(construct, a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)
  }
  def jsonFormat[A: JF, B: JF, C: JF, D: JF, E: JF, F: JF, G: JF, H: JF, I: JF, J: JF, K: JF, L: JF, M: JF, N: JF, O: JF, P: JF, Q: JF, R: JF, T <: Product](construct: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => T, a: String, b: String, c: String, d: String,
    e: String, f: String, g: String, h: String, i: String, j: String, k: String, l: String, m: String, n: String,
    o: String, p: String, q: String, r: String): RootJsonFormat[T] = new RootJsonFormat[T] {
    def write(pw: T) = JsObject(
      productElement2Field[A](a, pw, 0,
        productElement2Field[B](b, pw, 1,
          productElement2Field[C](c, pw, 2,
            productElement2Field[D](d, pw, 3,
              productElement2Field[E](e, pw, 4,
                productElement2Field[F](f, pw, 5,
                  productElement2Field[G](g, pw, 6,
                    productElement2Field[H](h, pw, 7,
                      productElement2Field[I](i, pw, 8,
                        productElement2Field[J](j, pw, 9,
                          productElement2Field[K](k, pw, 10,
                            productElement2Field[L](l, pw, 11,
                              productElement2Field[M](m, pw, 12,
                                productElement2Field[N](n, pw, 13,
                                  productElement2Field[O](o, pw, 14,
                                    productElement2Field[P](p, pw, 15,
                                      productElement2Field[Q](q, pw, 16,
                                        productElement2Field[R](r, pw, 17)))))))))))))))))))

    def read(value: JsValue) = construct(
      fromFieldCustom[A](value, a),
      fromFieldCustom[B](value, b),
      fromFieldCustom[C](value, c),
      fromFieldCustom[D](value, d),
      fromFieldCustom[E](value, e),
      fromFieldCustom[F](value, f),
      fromFieldCustom[G](value, g),
      fromFieldCustom[H](value, h),
      fromFieldCustom[I](value, i),
      fromFieldCustom[J](value, j),
      fromFieldCustom[K](value, k),
      fromFieldCustom[L](value, l),
      fromFieldCustom[M](value, m),
      fromFieldCustom[N](value, n),
      fromFieldCustom[O](value, o),
      fromFieldCustom[P](value, p),
      fromFieldCustom[Q](value, q),
      fromFieldCustom[R](value, r))
  }

  private def fromFieldCustom[T](value: JsValue, fieldName: String)(implicit reader: JsonReader[T]) = {
    value match {
      case x: JsObject =>
        var fieldFound = false
        try {
          val fieldValue = x.fields(fieldName)
          fieldFound = true
          reader.read(fieldValue)
        } catch {
          case e: NoSuchElementException if !fieldFound =>
            if (reader.isInstanceOf[OptionFormat[_]]) None.asInstanceOf[T]
            else deserializationError("Object is missing required member '" + fieldName + "'", e)
        }
      case _ => deserializationError("Object expected")
    }
  }

}