package com.busymachines.commons.mail

import java.util.Properties

import com.busymachines.commons.Logging
import com.busymachines.commons.mail.model.MailMessage
import javax.mail.Session

/**
 * Created by paul on 2/6/14.
 */
class OutgoingMailBox(mailConfig: OutgoingMailConfig) extends Logging {

  val session = Session.getInstance(serverProperties)

  private def serverProperties: Properties = {

    val properties = new Properties

    if (mailConfig.ssl) {
      properties.put("mail.smtp.socketFactory.port", String.valueOf(mailConfig.port))
      properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    }
    if (mailConfig.starttls) {
      properties.put("mail.smtp.starttls.enable", String.valueOf(mailConfig.starttls))
    }
    properties.put("mail.smtp.auth", String.valueOf(mailConfig.auth))
    properties.put("mail.smtp.host", mailConfig.host)
    properties.put("mail.smtp.port", String.valueOf(mailConfig.port))

    println(properties)
    properties
  }

  def send(message: MailMessage) = {
    val messageToSend = MailBox.mailMessageToMessage(session,message)
    val transport = session.getTransport("smtp")
    try {
      transport.connect(mailConfig.userName, mailConfig.password)
      transport.sendMessage(messageToSend, messageToSend.getAllRecipients)
    } finally {
      transport.close()
    }
  }


}