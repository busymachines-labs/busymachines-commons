package com.busymachines.commons.util

import java.io.{BufferedReader, Reader}

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
 * CSV parser compatible with RFC4180
 * @see http://tools.ietf.org/html/rfc4180
 * @author Ruud Diterwich
 */
object CsvParser {
  
  def parse(reader : Reader, separator: Char = ',') : Array[Array[String]] = {
    val bufferedReader = new BufferedReader(reader)
    try {
      val result = ArrayBuffer[Array[String]]()
      var currentLine = ArrayBuffer[String]()
      val currentField = new StringBuilder
      var current = bufferedReader.read
      var peek = bufferedReader.read
      def goNext() { current = peek; peek = bufferedReader.read; if (current == '\r') if (peek == '\n') goNext() else current = '\n' }
      def skipWhitespace() = while (current == ' ' || current == '\t') goNext()
      while (current != -1) {
        currentLine.clear()
        skipWhitespace()
        while (current != '\n' && current != -1) {
          currentField.clear()
          if (current == '"') {
            goNext()
            while (current != -1 && (current != '"' || peek == '"')) {
              if (current == '"') goNext()
              currentField append current.asInstanceOf[Char]
              goNext()
            }
            if (current == '"') goNext()
            skipWhitespace()
          }
          else {
            while (current != -1 && current != separator && current != '\n') {
              currentField append current.asInstanceOf[Char]
              goNext()
            }
          }
          if (current == separator) goNext()
          skipWhitespace()
          currentLine += currentField.toString.trim
        }
        if (current == '\n') goNext()
        if (!currentLine.isEmpty)
          result += currentLine.toArray
      }
      result.toArray
    } finally {
      bufferedReader.close()
    }
  }
}