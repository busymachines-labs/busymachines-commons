package com.busymachines.commons.mail.model

import javax.mail.Address
import org.joda.time.DateTime
import com.busymachines.commons.domain.MimeType
import com.busymachines.commons.domain.Media

case class MailMessage(
  messageNumber:Int,
  from: List[Address] = Nil,
  to: List[Address] = Nil,
  cc: List[Address] = Nil,
  sendDate: Option[DateTime] = None,
  subject: Option[String] = None,
  contentType: Option[MimeType] = None,
  content: Option[Array[Byte]] = None,
  attachments: List[Media] = Nil)