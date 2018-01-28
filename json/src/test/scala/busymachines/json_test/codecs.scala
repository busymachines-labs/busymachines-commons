package busymachines.json_test

/**
  *
  * Compile time optimization for JSON codec tests, every bit helps
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
object codecs {
  import busymachines.json._

  val `OutdoorMelons.Color.codec`:            Codec[OutdoorMelons.Color] = derive.codec[OutdoorMelons.Color]
  val `OutdoorMelons.Color.enumerationCodec`: Codec[OutdoorMelons.Color] = derive.enumerationCodec[OutdoorMelons.Color]

}
