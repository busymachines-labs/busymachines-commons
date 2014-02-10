package com.busymachines.commons.mail

import com.busymachines.commons.mail.model.MailMessage
import javax.mail.Message.RecipientType
import org.joda.time.DateTime
import javax.mail.{Session, Message, Multipart}
import javax.mail.internet.{MimeMultipart, MimeMessage, MimeBodyPart}
import com.busymachines.commons.domain.{MimeType, MimeTypes, Media}
import javax.activation.{DataHandler, DataSource}
import javax.mail.util.ByteArrayDataSource
import scala.collection.JavaConversions._

/**
 * Created by paul on 2/6/14.
 */
object MailBox {

  private def mediaToAttachment(media:Media):MimeBodyPart = {
    val messageFilePart = new MimeBodyPart()
    val dataSource: DataSource = new ByteArrayDataSource(media.data, media.mimeType.value);
    messageFilePart.setDataHandler(new DataHandler(dataSource))
    messageFilePart.setFileName(media.name.getOrElse("Unknown file name"));
    messageFilePart
  }

  private def attachmentToMedia(attachment: MimeBodyPart): Media = {
    val is = attachment.getInputStream
    val fileName:Option[String] = attachment.getFileName match {
      case null =>
        attachment.getHeader("Content-Disposition") match {
          case header if header != null =>
            header.headOption match {
              case None => None
              case Some(value) => Some(value.replaceAllLiterally("attachment; filename=",""))
            }
          case null => None
        }
      case name:String => Some(name)
    }
    Media(
      mimeType = fileName.map(MimeTypes.fromResourceName(_)).getOrElse(MimeTypes.text),
      name = fileName,
      data = Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray)
  }

  def mailMessageToMessage(session:Session,message:MailMessage):Message = {
    val messageToSend = new MimeMessage(session)
    if (message.from.nonEmpty) messageToSend.addFrom(message.from.toArray)
    if (message.to.nonEmpty) messageToSend.addRecipients(RecipientType.TO, message.to.toArray)
    if (message.cc.nonEmpty) messageToSend.addRecipients(RecipientType.CC, message.cc.toArray)
    messageToSend.setSentDate(message.sendDate.getOrElse(DateTime.now).toDate)
    if (message.subject != None) messageToSend.setSubject(message.subject.get)
    // Content is set in a different way depending whether it has attachments or not
    if (message.attachments.nonEmpty) {
      val multipart = new MimeMultipart

      // Set the mail body
      val messageBodyPart = new MimeBodyPart
      messageBodyPart.setText(new String(message.content.get), null, "html")
      messageBodyPart.setHeader("Content-Type", "text/html; charset=ISO-8859-1")
      multipart.addBodyPart(messageBodyPart)

      // Add each attachment
      for (attachment <- message.attachments) {
        val messageFilePart = new MimeBodyPart()
        val dataSource: DataSource = new ByteArrayDataSource(attachment.data, attachment.mimeType.value);
        messageFilePart.setDataHandler(new DataHandler(dataSource))
        messageFilePart.setFileName(attachment.name.getOrElse("Unknown file name"));
        multipart.addBodyPart(messageFilePart)
      }

      messageToSend.setContent(multipart)

    } else {
      messageToSend.setHeader("Content-Type", "text/html; charset=ISO-8859-1")
      messageToSend.setText(new String(message.content.getOrElse("".getBytes)), null, "html")
    }

    messageToSend
  }

  /**
   * Converts a Java Mail message to a domain MailMessage
   * @param m the JavaMail message
   * @return the domain MailMessage
   */
  def messageToMailMessage(m:Message):MailMessage =
    MailMessage(
      messageNumber = m.getMessageNumber,
      from = m.getFrom match {case null => Nil case list => list.toList},
      to = m.getRecipients(RecipientType.TO) match {case null => Nil case list => list.toList},
      cc = m.getRecipients(RecipientType.CC) match {case null => Nil case list => list.toList},
      sendDate = Some(new DateTime(m.getSentDate)),
      subject = Some(m.getSubject),
      contentType = None,
      content =
        if ((m.getContentType.contains("text/plain") || m.getContentType.contains("text/html")) &&
          (m.getContent != null))
          Some(m.getContent.toString.getBytes)
        else None,
      attachments = // Read all attachments as Media objects
        m.getContentType.contains("multipart") match {
          case false => Nil
          case true =>
            val multiPart = m.getContent.asInstanceOf[Multipart]
            (0).until(multiPart.getCount) flatMap (partIndex =>
              multiPart.getBodyPart(partIndex).asInstanceOf[MimeBodyPart].getSize match {
                case -1 => None
                case _ =>
                  Some(attachmentToMedia(multiPart.getBodyPart(partIndex).asInstanceOf[MimeBodyPart]))
              }) toList
        })

}
