package com.busymachines.commons.elasticsearch

import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.common.base.Charsets

class RichUpdateRequest(val request : UpdateRequest) extends AnyVal {
//  def source(source : String) =
//    request.source(new BytesArray(source.getBytes(Charsets.UTF_8)))
}