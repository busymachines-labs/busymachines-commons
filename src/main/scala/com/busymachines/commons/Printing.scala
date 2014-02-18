package com.busymachines.commons

import com.busymachines.commons.domain.MimeType
import com.busymachines.commons.domain.MimeTypes
import java.net.Socket
import java.io.DataOutputStream
import javax.print.SimpleDoc
import javax.print.attribute.HashPrintRequestAttributeSet
import javax.print.PrintServiceLookup
import java.io.ByteArrayInputStream
import javax.print.attribute.HashDocAttributeSet
import javax.print.attribute.HashPrintServiceAttributeSet
import javax.print.DocFlavor
import javax.print.attribute.standard.PrinterName

object Printing extends Logging {

  def print(printer: String, data: (MimeType, Array[Byte])): Unit =
    print(printer, data._1, data._2)

  def print(printer: String, mimeType: MimeType, data: Array[Byte]) {
    mimeType match {
      case MimeTypes.pdf => printPdf(printer, data)
      case MimeTypes.zpl => printZpl(printer, data)
      case _ =>
    }
  }

  def printPdf(printer: String, data: Array[Byte]) {
    val is = new ByteArrayInputStream(data)

    val flavor = DocFlavor.INPUT_STREAM.AUTOSENSE
    val aset = new HashPrintServiceAttributeSet
    aset.add(new PrinterName(printer, null))
    val services = PrintServiceLookup.lookupPrintServices(
      flavor, aset)

    if (services.length != 0) {
      //print using default
      val job = services(0).createPrintJob()
      val docAset = new HashDocAttributeSet()
      val mydoc = new SimpleDoc(is, flavor, docAset)
      val pras = new HashPrintRequestAttributeSet()
      job.print(mydoc, pras)
    } else {
      error("Couldn't find printer: " + printer)
    }
  }

  def printZpl(printer: String, data: Array[Byte]) {
    val clientSocket = new Socket(printer, 9100)
    val outToServer = new DataOutputStream(clientSocket.getOutputStream())
    outToServer.writeBytes("^XA")
    outToServer.writeBytes(new String(data))
    outToServer.writeBytes("^XZ")
    clientSocket.close();
  }
}