package com.busymachines.commons.mail

import java.io.ByteArrayOutputStream
import java.util.Properties

import scala.Some

import com.busymachines.commons.domain.{Media, MimeType}
import com.busymachines.commons.mail.model.MailMessage
import javax.mail._
import javax.mail.Message.RecipientType
import javax.mail.internet.MimeBodyPart
import javax.mail.search._
import org.joda.time.DateTime
import scala.concurrent.Future

/**
 * Facilitates mail related services.
 * @param mailConfig the mail configuration
 */
class MailBox(mailConfig: MailConfig) {

  val inboxFolder = "INBOX"

  private val session = Session.getDefaultInstance(serverProperties)
  private val store = session.getStore(mailConfig.protocol)

  private def serverProperties: Properties = {
    val properties = new Properties()

    // server setting
    properties.put(String.format("mail.%s.host", mailConfig.protocol), mailConfig.host);
    properties.put(String.format("mail.%s.port", mailConfig.protocol), String.valueOf(mailConfig.port))

    // SSL setting
    properties.setProperty(
      String.format("mail.%s.socketFactory.class", mailConfig.protocol),
      "javax.net.ssl.SSLSocketFactory")
    properties.setProperty(
      String.format("mail.%s.socketFactory.fallback", mailConfig.protocol),
      "false")
    properties.setProperty(
      String.format("mail.%s.socketFactory.port", mailConfig.protocol),
      String.valueOf(mailConfig.port))

    properties
  }

  private def attachmentToMedia(attachment: MimeBodyPart): Media = {

    // Read the content
    val buffer = new Array[Byte](4096)
    val output = new ByteArrayOutputStream
    var byteRead = 0

    while ((byteRead = attachment.getInputStream.read(buffer)) != -1) {
      output.write(buffer, 0, byteRead)
    }

    // Build the media
    Media(
      mimeType = MimeType("text/plain"),
      name = Some(attachment.getFileName),
      data = output.toByteArray)

  }

  private def connectOrReconnect = synchronized {
    if (!store.isConnected) store.connect(mailConfig.userName, mailConfig.password)
  }

  /**
   * Marks specific inbox messages as SEEN.
   * @param messages the messages to be marked as seen
   * @return
   */
  def markInboxMessagesAsSeen(messages:List[MailMessage]):Future[Unit] =
    markWithFlag(inboxFolder,messages,new Flags(Flags.Flag.SEEN),true)

  /**
   * Marks specific folder messages with flag.
   * @param folderName the folder name
   * @param messages the messages to be marked
   * @param flags the flags to set
   * @param flagSet whether to set or unset the flag
   * @return
   */
  def markWithFlag(folderName: String = inboxFolder,messages:List[MailMessage],flags:Flags,flagSet:Boolean=true):Future[Unit] =
    Future.successful({
      connectOrReconnect

      val folder = store.getFolder(folderName)
      folder.open(Folder.READ_WRITE)

      folder.setFlags(messages.map(_.messageNumber).toArray,flags,flagSet)
    })


  /**
   * Get all INBOX UNSEEN messages
   * @return
   */
  def getUnseenInboxMessages:Future[List[MailMessage]] =
    getMessages(inboxFolder,Some(new FlagTerm(new Flags(Flags.Flag.SEEN),false)))

  /**
   * Get all the messages from a specific folder that match a specific search criteria.
   * @param folderName the folder where to search
   * @param flagTerm the flag based search criteria
   * @return
   */
  def getMessages(folderName: String = inboxFolder,flagTerm:Option[FlagTerm]=None): Future[List[MailMessage]] =
  Future.successful(
  {
    connectOrReconnect

    // Retrieve messages from the mail folder
    val folder = store.getFolder(folderName)
    folder.open(Folder.READ_ONLY)
    val messages =
      flagTerm match {
        case Some(flagTermValue) => folder.search(flagTermValue)
        case None => folder.getMessages(0,10)
      }

    messages.map(m =>
      MailMessage(
        messageNumber = m.getMessageNumber,
        from = m.getFrom.toList,
        to = m.getRecipients(RecipientType.TO).toList,
        cc = m.getRecipients(RecipientType.CC).toList,
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
                Part.ATTACHMENT.equalsIgnoreCase(multiPart.getBodyPart(partIndex).asInstanceOf[MimeBodyPart].getDisposition()) match {
                  case true => Some(attachmentToMedia(multiPart.getBodyPart(partIndex).asInstanceOf[MimeBodyPart]))
                  case false => None
                }) toList
          })) toList    
  })
}