package com.busymachines.commons.logging

object LogHelper {

  def toOption[T](value: T): Option[T] = if (value == null) None else Some(value)
  def toList(value: String, separator: String = ","): Seq[String] = if (value == null) Nil else value.split(separator)

  def stackTracesToString(traces: Seq[StackTraceElement]) =
    (traces map { case trace => s"${trace.getClassName()}.${trace.getMethodName()}(${trace.getFileName()}: ${trace.getLineNumber()})" }) mkString ("\\n\\t")
}