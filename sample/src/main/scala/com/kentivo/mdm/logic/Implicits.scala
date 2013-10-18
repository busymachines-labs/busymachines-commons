package com.kentivo.mdm.logic

import com.busymachines.commons.domain.Id
import com.google.common.io.BaseEncoding
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyValue

trait Implicits {

  implicit class RichString(s : String) {
    def decodeBase64 = implicits.base64.decode(s)
  }
  
  implicit class RichByteArray(bytes : Array[Byte]) {
    def encodeBase64 = implicits.base64.encode(bytes)
  }
  
  implicit class RichItem(item : Item) {
    def property(property: Id[Property]): Option[Property] =
      item.definition.properties.find(_.id == property)
  
    def value(property: Id[Property]): Option[PropertyValue] = 
      item.values.get(property)
  }
}

package object implicits extends Implicits {
    val base64 = BaseEncoding.base64
} 
