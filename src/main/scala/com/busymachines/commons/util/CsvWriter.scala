package com.busymachines.commons.util

import java.io.Writer

/**
 * CSV writer compatible with RFC4180
 * @see http://tools.ietf.org/html/rfc4180
 * @author Ruud Diterwich
 */
class CsvWriter(writer: Writer, separator: Char = ',') {

  private var separatorPending = false

  def write(value: String) {
    if (separatorPending) {
      writer.append(separator)
    }
    separatorPending = true
    if (value.contains("\n") || value.contains("\t") || value.contains("\"") || value.contains(separator)) {
      writer.append("\"")
      writer.append(value.replaceAll("\"", "\"\""))
      writer.append("\"")
    } else {
      writer.append(value)
    }
  }

  def newline() {
    writer.append("\n")
    separatorPending = false
  }

  def writeLine(values: String*) {
    values.foreach(write)
    newline()
  }
}
