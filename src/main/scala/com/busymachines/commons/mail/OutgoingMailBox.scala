package com.busymachines.commons.mail

import com.busymachines.commons.Logging
import java.util.Properties
import javax.mail.{Authenticator, PasswordAuthentication, Session}
import com.busymachines.commons.mail.model.MailMessage
import javax.mail.internet.MimeBodyPart
import javax.mail.Message.RecipientType
import org.joda.time.DateTime
import javax.activation.DataHandler

class SpecificAuthenticator(userName: String, password: String) extends Authenticator {
  override def getPasswordAuthentication: PasswordAuthentication = {
    new PasswordAuthentication(userName, password)
  }
}

/**
 * Created by paul on 2/6/14.
 */
class OutgoingMailBox(mailConfig: OutgoingMailConfig) extends Logging {

  val session = Session.getDefaultInstance(serverProperties,
    new SpecificAuthenticator(mailConfig.userName, mailConfig.password))

  private def serverProperties: Properties = {

    val properties = new Properties

    if (mailConfig.ssl) {
      properties.put("mail.smtp.socketFactory.port", mailConfig.port.toString)
      properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
    } else {
      properties.put("mail.smtp.starttls.enable", mailConfig.starttls.toString)
    }
    properties.put("mail.smtp.auth", mailConfig.auth.toString)
    properties.put("mail.smtp.host", mailConfig.host)
    properties.put("mail.smtp.port", mailConfig.port.toString)

    properties
  }

  def send(message: MailMessage) = {
    val messageToSend = MailBox.mailMessageToMessage(session,message)
    session.getTransport.sendMessage(messageToSend,messageToSend.getAllRecipients)
  }


}