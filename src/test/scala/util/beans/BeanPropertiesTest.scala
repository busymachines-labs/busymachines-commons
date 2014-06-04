package util.beans

import org.specs2.mutable.Specification
import scala.collection.generic.CanBuildFrom

class BeanPropertiesTest extends Specification {

  val copierAB = BeanProperties.copier[Abean, Bbean]
  val copierCB = BeanProperties.copier[Cbean, Bbean]

  "BeanProperties copier" should {
    "... copy all properties from A to B" in {
      copierAB.copy(Abean("aaa")) === Bbean("aaa")
      copierCB.copy(Cbean("aaa", 0)) === Bbean("aaa")
    }

    "... take extra parameters" in {
      def copyWith(a: Abean, i: Int) = {
        BeanProperties.copy[Abean, Cbean](a, "i" -> i*2)
      }

      copyWith(Abean("bbb"), 10) === Cbean("bbb", 20)
    }

    "... extra parameters override properties in supplied object" in {
      def copyWith(a: Abean, s: String) = {
        BeanProperties.copy[Abean, Bbean](a, "s" -> s"overridden by: $s")
      }

      copyWith(Abean("ccc"), "aaa") === Bbean("overridden by: aaa")
    }

    "... use implicit conversions for deep copy" in {
      implicit def a2b(a: Abean): Bbean = BeanProperties.copy[Abean, Bbean](a)

      val copier = BeanProperties.copier[SupAbean, SupBbean]

      copier.copy(SupAbean(Abean("eee"))) === SupBbean(Bbean("eee"))
    }

    "... use implicit Copier for deep copy" in {
      implicit val a2bCopier = BeanProperties.copier[Abean, Bbean]

      val copier = BeanProperties.copier[SupAbean, SupBbean]

      copier.copy(SupAbean(Abean("eee"))) === SupBbean(Bbean("eee"))
    }

    "... copy collections" in {
      val seq = Seq(Abean("1"), Abean("2"))

      A2BCopier.a2b(seq) === Seq(Bbean("1"), Bbean("2"))
    }
  }

  "BeanProperties updater" should {
    "... update intersection of properties from A to B" in {
      val updaterAC = BeanProperties.updater[Abean, Cbean]
      val updaterCA = BeanProperties.updater[Cbean, Abean]

      updaterAC.update(Abean("xxx"))(Cbean("yyy", 11)) === Cbean("xxx", 11)
      updaterCA.update(Cbean("zzz", 12))(Abean("w")) === Abean("zzz")
    }

    "... use implicit conversions for deep update" in {
      implicit def a2b(a: Abean): Bbean = BeanProperties.copy[Abean, Bbean](a)

      val updater = BeanProperties.updater[SupAbean, SupCbean]

      updater.update(SupAbean(Abean("eee")))(SupCbean(Bbean("a"), 12)) === SupCbean(Bbean("eee"), 12)
    }

    "... use implicit Copier for deep update" in {
      implicit val a2bCopier = BeanProperties.copier[Abean, Bbean]

      val updater = BeanProperties.updater[SupAbean, SupCbean]

      updater.update(SupAbean(Abean("eee")))(SupCbean(Bbean("b"), 13)) === SupCbean(Bbean("eee"), 13)
    }

    "... use implicit Updater for deep update" in {
      implicit val a2cUpdater = BeanProperties.updater[Abean, Cbean]

      a2cUpdater.update(Abean("x"))(Cbean("y", 999)) === Cbean("x", 999)

      val updater = BeanProperties.updater[SupAbean, SupDbean]

      updater.update(SupAbean(Abean("eee")))(SupDbean(Cbean("c", 15), 14)) === SupDbean(Cbean("eee", 15), 14)
    }
  }
}

case class Abean(s: String)

case class Bbean(s: String)

case class Cbean(s: String, i: Int)

case class SupAbean(sub: Abean)

case class SupBbean(sub: Bbean)

case class SupCbean(sub: Bbean, j: Long)

case class SupDbean(sub: Cbean, j: Long)

object A2BCopier extends StandardCopiers {
  implicit val a2bCopier = BeanProperties.copier[Abean, Bbean]

  def a2b(seq: Seq[Abean]): Seq[Bbean] = implicitly[Copier[Seq[Abean], Seq[Bbean]]].copy(seq)
}