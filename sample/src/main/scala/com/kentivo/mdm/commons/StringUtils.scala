//package com.kentivo.mdm.commons
//
//import scala.util.Random
//
//object StringUtils {
//
//  def randomAlpha(length: Int): String = {
//    val chars = ('a' to 'z') ++ ('A' to 'Z')
//    randomStringFromCharList(length, chars)
//  }
//
//  def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
//    val sb = new StringBuilder
//    for (i <- 1 to length) {
//      val randomNum = util.Random.nextInt(chars.length)
//      sb.append(chars(randomNum))
//    }
//    sb.toString
//  }
//
//}