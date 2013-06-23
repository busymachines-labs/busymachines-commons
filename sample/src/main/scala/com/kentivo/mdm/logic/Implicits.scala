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
    def property(property: Id[Property]): Option[(Item, Property)] =
      item.properties.find(_.id == property).map(item -> _)
  
    def propertyValue(property: Id[Property]): Option[(Item, Property, PropertyValue)] = {
      this.property(property) flatMap {
        case (item, property) =>
          item.values.find(value => value.property == property).map((item, property, _))
      }
    }
  }
}

package object implicits extends Implicits {
    val base64 = BaseEncoding.base64
} 
