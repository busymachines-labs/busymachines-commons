package com.kentivo.mdm.db
import spray.json.JsObject
import spray.json.JsTrue
import spray.json.JsString
import spray.json.DefaultJsonProtocol
import spray.json.JsValue
import scala.Array.canBuildFrom
import com.busymachines.commons

//trait DefaultESMappings extends DefaultJsonProtocol {
//  
//  val analyzed = "index" -> JsString("analyzed")
//  val notAnalyzed = "index" -> JsString("not_analyzed")
//  val stored = "store" -> JsString("yes") 
//   
//  def mappingFor[A: ClassManifest](f : Mapping[_]*) = new Mapping[A] {
//    def apply(name : String) = {
//    val manifest = classManifest[A]
//    val fieldNames = extractFieldNames(manifest).zip(f)
//    name -> JsObject(manifest.erasure.getSimpleName.capitalize -> JsObject(
//        "_all" -> JsObject("enabled" -> JsTrue),
//        "_source" -> JsObject("enabled" -> JsTrue),
//        "store" -> JsTrue,
//        "properties" -> JsObject(fieldNames.toList.map { 
//          case (fieldName, mapping) => mapping(fieldName)
//          })))
//    }
//  }
//
//  
//  def mapping[A](f : (String, JsValue)*) = new Mapping[A] {
//    def apply(name: String) = name -> JsObject(f:_*)
//  }  
//  
//  implicit def idMapping[A](implicit a : Mapping[A]) = new Mapping[Id[A]] {
//    def apply(name : String) = (if (name == "id") "_id" else "name") -> 
//      JsObject("type" -> JsString("String"), stored, notAnalyzed)
//  }
//  implicit def optionMapping[A](implicit a : Mapping[A]) = mapping[Option[A]]("type" -> JsTrue)
//  implicit val booleanMapping = mapping[Boolean]("type" -> JsString("boolean"), stored, notAnalyzed)
//  implicit val stringMapping = mapping[String]("type" -> JsString("string"), stored, analyzed)
//  implicit def enumMapping[E <: Enumeration] = mapping[E#Value]("type" -> JsString("string"), stored, notAnalyzed)
//
//  implicit def mappingFor1[T <: Product :ClassManifest, A](construct : (A) => T)(implicit a : Mapping[A]) = mappingFor[T](a)
//  implicit def mappingFor2[T <: Product :ClassManifest, A, B](construct : (A, B) => T)(implicit a : Mapping[A], b : Mapping[B]) = mappingFor[T](a, b)
//  implicit def mappingFor3[T <: Product :ClassManifest, A, B, C](construct : (A, B, C) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C]) = mappingFor[T](a, b, c)
//  implicit def mappingFor4[T <: Product :ClassManifest, A, B, C, D](construct : (A, B, C, D) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D]) = mappingFor[T](a, b, c, d)
//  implicit def mappingFor5[T <: Product :ClassManifest, A, B, C, D, E](construct : (A, B, C, D, E) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E]) = mappingFor[T](a, b, c, d, e)
//  implicit def mappingFor5[T <: Product :ClassManifest, A, B, C, D, E, F](construct : (A, B, C, D, E, F) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F]) = mappingFor[T](a, b, c, d, e, f)
//  implicit def mappingFor6[T <: Product :ClassManifest, A, B, C, D, E, F, G](construct : (A, B, C, D, E, F, G) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G]) = mappingFor[T](a, b, c, d, e, f, g)
//  implicit def mappingFor7[T <: Product :ClassManifest, A, B, C, D, E, F, G, H](construct : (A, B, C, D, E, F, G, H) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H]) = mappingFor[T](a, b, c, d, e, f, g, h)
//  implicit def mappingFor8[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I](construct : (A, B, C, D, E, F, G, H, I) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I]) = mappingFor[T](a, b, c, d, e, f, g, h, i)
//  implicit def mappingFor9[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J](construct : (A, B, C, D, E, F, G, H, I, J) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j)
//  implicit def mappingFor10[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K](construct : (A, B, C, D, E, F, G, H, I, J, K) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k)
//  implicit def mappingFor11[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L](construct : (A, B, C, D, E, F, G, H, I, J, K, L) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l)
//  implicit def mappingFor12[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m)
//  implicit def mappingFor13[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n)
//  implicit def mappingFor14[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)
//  implicit def mappingFor15[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)
//  implicit def mappingFor16[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)
//  implicit def mappingFor17[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)
//  implicit def mappingFor18[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)
//  implicit def mappingFor19[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S], u : Mapping[U]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u)
//  implicit def mappingFor20[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S], u : Mapping[U], v : Mapping[V]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v)
//  implicit def mappingFor21[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V, W](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V, W) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S], u : Mapping[U], v : Mapping[V], w : Mapping[W]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v, w)

  
//  implicit def productMapping[A <: Product](implicit a : Mapping[A]) = new Mapping[A] {
//    def apply(name : String) = name -> JsObject("type" -> JsString("nested"), stored, "properties" -> )
//  }
//}

//trait Mapping[A] extends Function[String, (String, JsObject)] 
//
//object Mapping extends DefaultJsonProtocol {
  
//  def mappingFor[T <: Product : ClassManifest](implicit mapping : ESProductMapping[T, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _]) = {
//    val manifest = classManifest[T]
//    JsObject(mapping(manifest.erasure.getSimpleName.capitalize))
//  }
  
//  def mappingFor[T :ClassManifest](implicit mapping : Mapping[T]) = mapping(classManifest[T].erasure.getSimpleName.capitalize) ._2)))
//  }
//  
//  def mappingFor1[T <: Product :ClassManifest, A](construct : (A) => T)(implicit a : Mapping[A]) = mappingFor[T](a)
//  def mappingFor2[T <: Product :ClassManifest, A, B](construct : (A, B) => T)(implicit a : Mapping[A], b : Mapping[B]) = mappingFor[T](a, b)
//  def mappingFor3[T <: Product :ClassManifest, A, B, C](construct : (A, B, C) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C]) = mappingFor[T](a, b, c)
//  def mappingFor4[T <: Product :ClassManifest, A, B, C, D](construct : (A, B, C, D) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D]) = mappingFor[T](a, b, c, d)
//  def mappingFor5[T <: Product :ClassManifest, A, B, C, D, E](construct : (A, B, C, D, E) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E]) = mappingFor[T](a, b, c, d, e)
//  def mappingFor5[T <: Product :ClassManifest, A, B, C, D, E, F](construct : (A, B, C, D, E, F) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F]) = mappingFor[T](a, b, c, d, e, f)
//  def mappingFor6[T <: Product :ClassManifest, A, B, C, D, E, F, G](construct : (A, B, C, D, E, F, G) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G]) = mappingFor[T](a, b, c, d, e, f, g)
//  def mappingFor7[T <: Product :ClassManifest, A, B, C, D, E, F, G, H](construct : (A, B, C, D, E, F, G, H) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H]) = mappingFor[T](a, b, c, d, e, f, g, h)
//  def mappingFor8[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I](construct : (A, B, C, D, E, F, G, H, I) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I]) = mappingFor[T](a, b, c, d, e, f, g, h, i)
//  def mappingFor9[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J](construct : (A, B, C, D, E, F, G, H, I, J) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j)
//  def mappingFor10[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K](construct : (A, B, C, D, E, F, G, H, I, J, K) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k)
//  def mappingFor11[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L](construct : (A, B, C, D, E, F, G, H, I, J, K, L) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l)
//  def mappingFor12[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m)
//  def mappingFor13[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n)
//  def mappingFor14[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)
//  def mappingFor15[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)
//  def mappingFor16[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)
//  def mappingFor17[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)
//  def mappingFor18[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)
//  def mappingFor19[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S], u : Mapping[U]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u)
//  def mappingFor20[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S], u : Mapping[U], v : Mapping[V]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v)
//  def mappingFor21[T <: Product :ClassManifest, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V, W](construct : (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, U, V, W) => T)(implicit a : Mapping[A], b : Mapping[B], c : Mapping[C], d : Mapping[D], e : Mapping[E], f : Mapping[F], g : Mapping[G], h : Mapping[H], i : Mapping[I], j : Mapping[J], k : Mapping[K], l : Mapping[L], m : Mapping[M], n : Mapping[N], o : Mapping[O], p : Mapping[P], q : Mapping[Q], r : Mapping[R], s : Mapping[S], u : Mapping[U], v : Mapping[V], w : Mapping[W]) = mappingFor[T](a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, u, v, w)


  
  
//})))
//  }
//}
//
