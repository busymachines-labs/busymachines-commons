package com.busymachines.commons.util

import java.io.{OutputStream, InputStream}
import java.util.Properties
import javax.activation.{DataHandler, DataSource}
import javax.mail.internet.{MimeBodyPart, MimeMultipart, InternetAddress, MimeMessage}
import javax.mail._
import com.busymachines.commons.domain.{MimeTypes, MimeType}
import com.busymachines.commons.CommonConfig

class MailConfig(baseName: String) extends CommonConfig(baseName) {
  def host = string("host")
  def port = int("port")
  def auth = boolean("auth")
  def ssl = boolean("ssl")
  def starttls = boolean("starttls")
  def userName = string("userName")
  def password = string("password")
}

case class MailMessage(
  subject: String,
  text: String,
  from: String,
  mimeType: MimeType = MimeTypes.text,
  to: List[String] = Nil,
  cc: List[String] = Nil,
  bcc: List[String] = Nil,
  attachments: List[(String, MimeType, InputStream)] = Nil)

object Mail {

  def send(config: MailConfig, message: MailMessage) = {

    val props = new Properties()
    if (config.auth)
      props.put("mail.smtp.auth", String.valueOf(config.auth))
    if (config.ssl) {
      props.put("mail.smtp.socketFactory.port", String.valueOf(config.port))
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    }
    if (config.starttls)
      props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", config.host)
    props.put("mail.smtp.port", String.valueOf(config.port))

    val session = Session.getInstance(props,
      new Authenticator() {
        override def getPasswordAuthentication =
          new PasswordAuthentication(config.userName, config.password)
      })

    val messageToSend = new MimeMessage(session)
    messageToSend.setFrom(new InternetAddress(message.from))
    messageToSend.setRecipients(Message.RecipientType.TO, message.to.mkString(","))
    messageToSend.setRecipients(Message.RecipientType.CC, message.cc.mkString(","))
    messageToSend.setRecipients(Message.RecipientType.BCC, message.bcc.mkString(","))
    messageToSend.setSubject(message.subject)

    if (message.attachments.isEmpty)
      if (message.mimeType == MimeTypes.text)
        messageToSend.setText(message.text)
      else
        messageToSend.setContent(message.text, "text/html")
    else {
      val multipart = new MimeMultipart
      val bodyPart = new MimeBodyPart
      if (message.mimeType == MimeTypes.text)
        bodyPart.setText(message.text)
      else
        bodyPart.setContent(message.text, "text/html")
      multipart.addBodyPart(bodyPart)
      for ((fileName, mimeType, is) <- message.attachments) {
        val part = new MimeBodyPart
        val source = new DataSource {
          def getName: String = fileName

          def getOutputStream: OutputStream = null

          def getContentType: String = mimeType.value

          def getInputStream: InputStream = is
        }
        bodyPart.setDataHandler(new DataHandler(source))
        bodyPart.setFileName(fileName)
        multipart.addBodyPart(bodyPart)
      }
      messageToSend.setContent(multipart)
    }

    val transport = session.getTransport("smtp")
    try {
      transport.connect(config.host, config.port, config.userName, config.password)
      transport.sendMessage(messageToSend, messageToSend.getAllRecipients)
    } finally {
      transport.close()
    }
  }
}
