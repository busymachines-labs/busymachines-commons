package com.busymachines.commons.localisation

import com.busymachines.commons.{EnumValue, Enum}


/**
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 11.07.2014.
 */
trait Language extends EnumValue[Language]
object Language extends Enum[Language] {
  case class Value(value: Int, name: String) extends Val with NextId with Language
  val AMHARIC = Value(1, "am")
  val ARABIC = Value(2, "ar")
  val BELARUSIAN = Value(3, "be")
  val BULGARIAN = Value(4, "bg")
  val BOSNIAN = Value(5, "bs")
  val CATALAN = Value(6, "ca")
  val CZECH = Value(7, "cs")
  val DANISH = Value(8, "da")
  val GERMAN = Value(9, "de")
  val GREEK = Value(10, "el")
  val ENGLISH = Value(11, "en")
  val SPANISH = Value(12, "es")
  val ESTONIAN = Value(13, "et")
  val PERSIAN = Value(14, "fa")
  val FINNISH = Value(15, "fi")
  val FAROESE = Value(16, "fo")
  val FRENCH = Value(17, "fr")
  val HEBREW = Value(18, "he")
  val HINDI = Value(19, "hi")
  val CROATIAN = Value(20, "hr")
  val HUNGARIAN = Value(21, "hu")
  val INDONESIAN = Value(22, "id")
  val ICELANDIC = Value(23, "is")
  val ITALIAN = Value(24, "it")
  val JAPANESE = Value(25, "ja")
  val KOREAN = Value(26, "ko")
  val LUXEMBOURGISH = Value(27, "lb")
  val LATVIAN = Value(28, "lv")
  val LITUANIAN = Value(29, "lt")
  val MACEDONIAN = Value(30, "mk")
  val MARATHI = Value(31, "mr")
  val MALAY = Value(32, "ms")
  val DUTCH = Value(33, "nl")
  val NORVEGIAN_NYNORSK = Value(34, "nn")
  val NORVEGIAN_BOKMAL = Value(35, "nb")
  val POLISH = Value(36, "pl")
  val PORTUGUESE = Value(37, "pt")
  val ROMANIAN = Value(38, "ro")
  val RUSSIAN = Value(39, "ru")
  val SLOVAK = Value(40, "sk")
  val SLOVENIAN = Value(41, "sl")
  val SERBIAN = Value(42, "sr")
  val ALBANIAN = Value(43, "sq")
  val SWEDISH = Value(44, "sv")
  val TAGALOG = Value(45, "tl")
  val TAMIL = Value(46, "ta")
  val TURKISH = Value(47, "tr")
  val UKRAINIAN = Value(48, "uk")
  val VIETNAMESE = Value(49, "vi")
  val CHINESE = Value(50, "zh")
}