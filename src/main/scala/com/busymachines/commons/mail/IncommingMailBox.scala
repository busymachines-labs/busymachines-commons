package com.busymachines.commons.mail

import java.util.Properties

import scala.Some
import scala.concurrent.Future

import com.busymachines.commons.Logging
import com.busymachines.commons.domain.{Media, MimeType, MimeTypes}
import com.busymachines.commons.mail.model.MailMessage
import javax.mail._
import javax.mail.Message.RecipientType
import javax.mail.internet.MimeBodyPart
import javax.mail.search._
import org.joda.time.DateTime

/**
 * Facilitates mail related services.
 * @param mailConfig the mail configuration
 */
class IncommingMailBox(mailConfig: IncommingMailConfig) extends Logging {

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

  private def connectOrReconnect = synchronized {
    if (!store.isConnected) store.connect(mailConfig.userName, mailConfig.password)
  }

  /**
   * Marks specific inbox messages as SEEN.
   * @param messages the messages to be marked as seen
   * @return
   */
  def markInboxMessagesAsSeen(messages: List[MailMessage], seenPolarity: Boolean = true):Future[Unit] = {
    mailConfig.protocol.equalsIgnoreCase("imap") match {
      case true => markWithFlag(inboxFolder,messages,new Flags(Flags.Flag.SEEN),seenPolarity)
      case false => throw new Exception(s"Only IMAP protocol supports setting the messages as SEEN")
    }

  }

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
  def getMessages(folderName: String = inboxFolder,flagTerm:Option[FlagTerm]=None,dateRange:Option[(Option[DateTime],Option[DateTime])] = None,messageRange:Option[(Int,Int)]=None): Future[List[MailMessage]] =
  Future.successful(
  {
    connectOrReconnect

    // Retrieve messages from the mail folder
    val folder = store.getFolder(folderName)
    folder.open(Folder.READ_ONLY)
    val messages =
      (flagTerm,dateRange,messageRange) match {
        case (Some(flagTermValue),None,None) => folder.search(flagTermValue)
        case (None,None,Some(range)) => folder.getMessages(range._1,range._2)
        case (None,Some(dateRangeValue),None) =>
          dateRangeValue match {
            case (None,None) => throw new Exception(s"Cannot search mail for date range and not specify at least one end of the range")
            case (Some(dateRangeStartValue),None) => folder.search(new ReceivedDateTerm(ComparisonTerm.GT, dateRangeStartValue.toDate))
            case (None,Some(dateRangeEndValue)) => folder.search(new ReceivedDateTerm(ComparisonTerm.LT, dateRangeEndValue.toDate))
            case (Some(dateRangeStartValue),Some(dateRangeEndValue)) => folder.search(new AndTerm(new ReceivedDateTerm(ComparisonTerm.LT, dateRangeEndValue.toDate), new ReceivedDateTerm(ComparisonTerm.GT, dateRangeStartValue.toDate)))
          }
        case _ => throw new Exception(s"Unknown mail query")
      }

    messages.map(MailBox.messageToMailMessage(_)) toList
  })

  def getMessageCount(folderName: String = inboxFolder):Future[Int] = Future.successful({
    connectOrReconnect

    // Retrieve messages from the mail folder
    val folder = store.getFolder(folderName)
    folder.open(Folder.READ_ONLY)
    folder.getMessageCount

  })
}