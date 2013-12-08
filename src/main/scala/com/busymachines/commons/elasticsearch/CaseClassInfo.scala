package com.busymachines.commons.elasticsearch

import java.lang.reflect.Modifier
import scala.reflect.ClassTag
import scala.language.existentials 

case class CaseClassField(name: String, clazz : Class[_], default: Option[() => Any])

object CaseClassFields {

  def of[T](implicit ct : ClassTag[T]) : Map[String, CaseClassField] = 
    extractFieldNames(ct, true).groupBy(_.name).mapValues(_.head)

    
  private def extractFieldNames(classManifest: ClassTag[_], allowOptionalFields: Boolean): Array[CaseClassField] = {
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
        fields.zip(applyDefaultMethods).map { case (f, m) => CaseClassField(f.getName, f.getType, m) }
      } else {
        fields.map { f => CaseClassField(f.getName, f.getType, None) }
      }
    } catch {
      case ex : Throwable => throw new RuntimeException("Cannot automatically determine case class field names and order " +
        "for '" + clazz.getName + "', please use the 'jsonFormat' overload with explicit field name specification", ex)
    }
  }
}