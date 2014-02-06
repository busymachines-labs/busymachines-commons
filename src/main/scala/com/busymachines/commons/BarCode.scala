package com.busymachines.commons

import java.awt.image.BufferedImage
import com.google.zxing.client.j2se.{MatrixToImageConfig, MatrixToImageWriter}
import com.google.zxing.oned.{Code39Writer, EAN13Writer}
import com.google.zxing.BarcodeFormat
import java.io.ByteArrayOutputStream

/**
 * Only EAN13 supported now, pls add other formats too.
 */
object BarCode {
  class ImageFormat(val format: String)
  val JPG = new ImageFormat("JPEG")
  val BMP = new ImageFormat("BMP")
  val PNG = new ImageFormat("PNG")

  def apply(code: String) = {
    if (code.length == 13) new BarCodeEAN13(code)
    else throw new Exception("Unrecognized bar code format: " + code)
  }

  def apply(code: String, format: String) = {
    if (format.contains("39")) new BarCode39(code)
    else if (format.contains("13")) new BarCodeEAN13(code)
    else throw new Exception("Unrecognized bar code format: " + format)
  }

  def ean13(code: String) =
    new BarCodeEAN13(code)

  def code39(code: String) =
    new BarCode39(code)

  def ean13(value : Long): BarCodeEAN13 = {
    import Character.digit
    val str = f"$value%012d"
    def sumOf(indexes: Int*): Int =
      indexes.foldLeft(0)((s, i) => s + digit(str.charAt(i), 10))
    val sum: Int =
      sumOf(1, 3, 5, 7, 9, 11) * 3 +
        sumOf(0, 2, 4, 6, 8, 10)
    val checksum: Int = (10 - sum % 10) % 10
    new BarCodeEAN13(str.substring(0, 12) + checksum)
  }
}

trait BarCode {
  def code: String
  def toImage(width: Int, height: Int): BufferedImage
  def toImage(width: Int, height: Int, format: BarCode.ImageFormat): Array[Byte]
  def toImage(width: Int, height: Int, format: String): Array[Byte] =
    toImage(width, height, format.toLowerCase match {
      case "jpg" => BarCode.JPG
      case "jpeg" => BarCode.JPG
      case "png" => BarCode.PNG
      case "bmp" => BarCode.BMP
    })
  def toJPG(width: Int, height: Int): Array[Byte] = toImage(width, height, BarCode.JPG)
  def toPNG(width: Int, height: Int): Array[Byte] = toImage(width, height, BarCode.PNG)
  def toBMP(width: Int, height: Int): Array[Byte] = toImage(width, height, BarCode.BMP)
}

class BarCodeEAN13(val code: String) extends BarCode {
  def toImage(width: Int, height: Int): BufferedImage =
    MatrixToImageWriter.toBufferedImage(
      new EAN13Writer().encode(code, BarcodeFormat.EAN_13, width, height),
      new MatrixToImageConfig)

  def toImage(width: Int, height: Int, format: BarCode.ImageFormat): Array[Byte] = {
    val o = new ByteArrayOutputStream()
    MatrixToImageWriter.writeToStream(
      new EAN13Writer().encode(code, BarcodeFormat.EAN_13, width, height),
      format.format,
      o)
    o.toByteArray
  }

}

class BarCode39(val code: String) extends BarCode {
  def toImage(width: Int, height: Int): BufferedImage =
    MatrixToImageWriter.toBufferedImage(
      new Code39Writer().encode(code, BarcodeFormat.CODE_39, width, height),
      new MatrixToImageConfig)

  def toImage(width: Int, height: Int, format: BarCode.ImageFormat): Array[Byte] = {
    val o = new ByteArrayOutputStream()
    MatrixToImageWriter.writeToStream(
      new Code39Writer().encode(code, BarcodeFormat.CODE_39, width, height),
      format.format,
      o)
    o.toByteArray
  }

}
