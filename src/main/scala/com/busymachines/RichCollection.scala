package com.busymachines

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.generic.CanBuildFrom

/**
 * Created by ruud on 21/01/14.
 */
class RichCollection[A, C[A] <: Iterable[A]](val collection: C[A]) {

  def mapWhere(pred: A => Boolean)(fn: A => A)(implicit cbf: CanBuildFrom[C[A], A, C[A]]): C[A] = {
    val builder = cbf()
    builder.sizeHint(collection.size)
    collection.foreach(a => builder.+=(if (pred(a)) fn(a) else a))
    builder.result
  }

}
