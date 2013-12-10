package com.busymachines.commons

import scala.util.parsing.combinator._
import scala.language.postfixOps
import scala.collection.mutable.ArrayBuilder
import scala.collection.mutable.ArrayBuffer
import java.io.Reader
import java.io.BufferedReader

/**
 * CSV parser compatible with RFC4180
 * @see http://tools.ietf.org/html/rfc4180
 */
object CsvParser {
  
  def parse(reader : Reader) : Array[Array[String]] = {
    val bufferedReader = new BufferedReader(reader)
    val result = ArrayBuffer[Array[String]]()
    var currentLine = ArrayBuffer[String]()
    var currentField = new StringBuilder
    var peek = bufferedReader.read
    var current = bufferedReader.read
    def goNext { current = peek; peek = bufferedReader.read; if (current == '\r') goNext }
    def skipWhitespace = while (current == ' ' || current == '\t') goNext
    while (current != -1) {
      currentLine.clear
      skipWhitespace
      while (current != '\n') {
        currentField.clear
        if (current == '"') {
          goNext
          while (current != -1 && (current != '"' || peek == '"')) {
            if (current == '"') goNext
            currentField append current.asInstanceOf[Char]
            goNext
          }
          if (current == '"') goNext
          skipWhitespace
        }
        else {
          while (current != -1 && current != ',' && current != '\n') {
            currentField append current.asInstanceOf[Char]
            goNext
          }
        }
        if (current == ',') goNext
        skipWhitespace
        currentLine += currentField.toString.trim
      }
      if (current == '\n') goNext
      if (!currentLine.isEmpty) 
        result += currentLine.toArray
    }
    result.toArray
  }
}