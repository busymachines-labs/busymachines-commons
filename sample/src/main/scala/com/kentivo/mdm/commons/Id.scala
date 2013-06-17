//package com.kentivo.mdm.commons
//
//import java.nio.ByteBuffer.allocate
//import java.nio.ByteBuffer.wrap
//
//import com.eaio.uuid.UUID
//import com.google.common.io.BaseEncoding
//
//trait HasId[A] {
//  def id: Id[A]
//}
//
//object Id {
//  def generate[A]: Id[A] =
//    apply[A](new UUID)
//
//  def apply[A](id: UUID): Id[A] =
//    apply[A](allocate(16).putLong(id.getTime).putLong(id.getClockSeqAndNode).array)
//
//  def apply[A](id: Array[Byte]): Id[A] =
//    new Id[A](encoding.encode(id))
//
//  private val encoding = BaseEncoding.base64Url.omitPadding
//}
//
//case class Id[A](id: String) {
//  override def toString = id
//}