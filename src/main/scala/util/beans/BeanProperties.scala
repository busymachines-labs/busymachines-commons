package util.beans

import scala.language.higherKinds
import scala.language.experimental.macros
import scala.collection.generic.CanBuildFrom
import scala.reflect.macros.Context

trait Copier[A, B] {
  def copy(a: A): B
}

trait Updater[A, B] {
  def update(delta: A): B => B
}

trait StandardCopiers {

  implicit def optionCopier[A, B](implicit copier: Copier[A, B]) = new Copier[Option[A], Option[B]] {
    def copy(a: Option[A]): Option[B] = a.map(copier.copy)
  }

  implicit def collectionCopier[A, B, C[A] <: Iterable[A]](implicit
    copier: Copier[A, B],
    cbf: CanBuildFrom[C[B], B, C[B]]
  ): Copier[C[A], C[B]] = new Copier[C[A], C[B]] {
    def copy(in: C[A]): C[B] = {
      def builder = { // extracted to keep method size under 35 bytes, so that it can be JIT-inlined
        val b = cbf()
        b.sizeHint(in)
        b
      }
      val b = builder
      for (x <- in) b += copier.copy(x)
      b.result()
    }

  }

}

object BeanProperties {

  /**
   * Make a copy of one bean to another with matching property names (and types).
   *
   * Implicit conversions are applied where necessary
   *
   * @param a
   * @tparam A
   * @tparam B
   * @return
   */
  def copy[A, B](a: A, props: Pair[String, Any]*): B = macro copyImpl[A, B]

  def copyImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context)(a: c.Expr[A], props: c.Expr[Pair[String, Any]]*): c.Expr[B] = {
    import c.universe._

    val ctorB: MethodSymbol = weakTypeOf[B].declaration(nme.CONSTRUCTOR) match {
      case NoSymbol =>
        c.abort(c.enclosingPosition, "No constructor found")
      case s =>
        s.asMethod
    }

    val stringProps: Map[String, Tree] =
      (for (prop <- props)
      yield {
        prop.tree match {
          case q"${_}(${Literal(Constant(propName: String))}).->[${_}](${propValue: Tree})" =>
            (propName, propValue)
          case q"(${propName: String}, ${propValue: Tree})" =>
            (propName, propValue)
        }
      }).toMap

    val paramss: List[Option[Tree]] =
      ctorB.paramss.head map { param =>
        val paramName = param.name
        stringProps.get(paramName.encoded) match {
          case Some(propValue) =>
            Some(AssignOrNamedArg(Ident(paramName), propValue))
          case None =>
            weakTypeOf[A].member(paramName) match {
              case NoSymbol =>

                if (!param.asTerm.isParamWithDefault)
                  c.abort(c.enclosingPosition, s"Cannot copy: property ${weakTypeOf[A]}.${paramName} not found and no default value provided")
                else
                  None // property is not found, use default value


              case accessor =>
                val found = accessor.asMethod.returnType
                val required = param.typeSignature
                //println(s"found: ${found}, required: $required, equal: ${found == required}")
                if (found <:< required)
                  Some(AssignOrNamedArg(Ident(paramName), q"$a.$accessor"))
                else {
                  val implicitlyConvertedArgument: Tree =
                    c.inferImplicitView(Select(a.tree, accessor), found, required) match {
                      case EmptyTree =>
                        val copierType = appliedType(weakTypeOf[Copier[A, B]].typeConstructor, List(found, required))
                        c.inferImplicitValue(copierType) match {
                          case EmptyTree =>
                            c.abort(c.enclosingPosition, s"Cannot copy: no implicit conversion found from $found to $required")
                          case copier =>
                            AssignOrNamedArg(Ident(paramName), q"$copier.copy($a.$accessor)")
                        }
                      case convert =>
                        AssignOrNamedArg(Ident(paramName), q"$convert($a.$accessor)")
                    }
                  Some(implicitlyConvertedArgument)
                }
            }
        }
      }

    val params = paramss.flatMap(_.toList)
    val bType = weakTypeOf[B].typeSymbol

    c.Expr(q"new $bType(..$params)")
  }


  def copier[A, B](props: Pair[String, A => Any]*): Copier[A, B] = macro copierImpl[A, B]

  def copierImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context)(props: c.Expr[Pair[String, A => Any]]*): c.Expr[Copier[A, B]] = {
    import c.universe._

    val params: List[Tree] =
      for (prop <- props.toList)
      yield {
        prop.tree match {
          case q"${_}(${Literal(Constant(propName: String))}).->[${_}](${propValue: Tree})" =>
            q"($propName, $propValue(a))"
          case q"(${propName: String}, ${propValue: Tree})" =>
            q"($propName, $propValue(a))"
        }
      }

    val aType = weakTypeOf[A].typeSymbol
    val bType = weakTypeOf[B].typeSymbol
    c.Expr[Copier[A, B]](
      q"""new util.beans.Copier[$aType, $bType] {
           def copy(a: $aType): $bType = util.beans.BeanProperties.copy[$aType, $bType](a, ..$params)
         }""")
  }

  /**
   * Creates an update function which produces copy of an object where properties are replaced with
   * values from provided delta object.
   *
   * @param delta
   * @tparam A
   * @tparam B
   * @return
   */
  def update[A, B](delta: A): B => B = macro updateImpl[A, B]

  def updateImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context)(delta: c.Expr[A]): c.Expr[B => B] = {
    import c.universe._

    val bTypeSym = weakTypeOf[B].typeSymbol

    val copyMethod: MethodSymbol = weakTypeOf[B].declaration(stringToTermName("copy")) match {
      case NoSymbol =>
        c.abort(c.enclosingPosition, s"Cannot create update function: no copy method found. Is $bTypeSym a case class?")
      case s =>
        s.asMethod
    }

    val deltaType = weakTypeOf[A]

    val paramss: List[Option[Tree]] =
      for (param <- copyMethod.paramss.head)
      yield {
        val paramName = param.name

        deltaType.member(paramName) match {
          case NoSymbol =>
            None

          case accessor =>
            val found = accessor.asMethod.returnType
            val required = param.typeSignature
            //println(s"found: ${found}, required: $required, equal: ${found == required}")
            if (found <:< required)
              Some(AssignOrNamedArg(Ident(paramName), q"$delta.$accessor"))
            else {
              val implicitlyConvertedArgument: Tree =
                c.inferImplicitView(Select(delta.tree, accessor), found, required) match {
                  case EmptyTree =>
                    val copierType = appliedType(weakTypeOf[Copier[_, _]].typeConstructor, List(found, required))
                    c.inferImplicitValue(copierType) match {
                      case EmptyTree =>
                        val updaterType = appliedType(weakTypeOf[Updater[_, _]].typeConstructor, List(found, required))
                        c.inferImplicitValue(updaterType) match {
                          case EmptyTree =>
                            c.abort(c.enclosingPosition, s"Cannot copy: no implicit Updater, Copier or conversion found from $found to $required")
                          case updater =>
                            AssignOrNamedArg(Ident(paramName), q"$updater.update($delta.$accessor)(obj.${paramName.toTermName})")
                        }
                      case copier =>
                        AssignOrNamedArg(Ident(paramName), q"$copier.copy($delta.$accessor)")
                    }
                  case convert =>
                    AssignOrNamedArg(Ident(paramName), q"$convert($delta.$accessor)")
                }
              Some(implicitlyConvertedArgument)
            }

        }
      }

    val params: List[Tree] = paramss.map(_.toList).flatten
    c.Expr[B => B](q"{obj: $bTypeSym => obj.copy(..$params)}")
  }

  def updater[A, B]: Updater[A, B] = macro updaterImpl[A, B]

  def updaterImpl[A: c.WeakTypeTag, B: c.WeakTypeTag](c: Context): c.Expr[Updater[A, B]] = {
    import c.universe._

    val aType = weakTypeOf[A].typeSymbol
    val bType = weakTypeOf[B].typeSymbol
    c.Expr[Updater[A, B]](
      q"""new util.beans.Updater[$aType, $bType] {
           def update(delta: $aType): $bType => $bType = util.beans.BeanProperties.update[$aType, $bType](delta)
         }""")
  }
}