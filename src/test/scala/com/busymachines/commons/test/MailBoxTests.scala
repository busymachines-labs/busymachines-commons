package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.mail.{OutgoingMailConfig, OutgoingMailBox, IncommingMailBox, IncommingMailConfig}
import com.busymachines.commons.implicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.busymachines.commons.mail.model.MailMessage
import javax.mail.Address
import javax.mail.internet.InternetAddress

/**
 * Created by paul on 2/4/14.
 */
class MailBoxTests extends FlatSpec {

  val incommingMailBox = new IncommingMailBox(new IncommingMailConfig("test.busymachines.mail.incomming"))
  val outgoingMailBox = new OutgoingMailBox(new OutgoingMailConfig("test.busymachines.mail.outgoing"))

  "MailBox" should "receive max 10 mails from a mailbox" in {
    val initialMessages = incommingMailBox.getMessages(messageRange = (1, incommingMailBox.getMessageCount().await)).await
    assert(initialMessages.size == 6)
  }

  it should "receive messages within a specific date range from a mailbox" in {
//    val formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
//    val dateTimeRange = (formatter.parseDateTime("04/02/2014 00:00:00"), formatter.parseDateTime("05/02/2014 00:00:00"))
//    val initialMessages = incommingMailBox.getMessages(dateRange = dateTimeRange).await
//    assert(initialMessages.size == 5)
//    incommingMailBox.markInboxMessagesAsSeen(initialMessages, false).await
  }

  it should "receive unseen messages from a mailbox" in {
//    val initialMessages = incommingMailBox.getMessages(messageRange = (1, incommingMailBox.getMessageCount().await)).await
//    assert(initialMessages.size == 6)
//    incommingMailBox.markInboxMessagesAsSeen(initialMessages, false).await
//
//    // Get unseen messages & mark them as seen
//    val messages = incommingMailBox.getUnseenInboxMessages.await
//    assert(messages.size == 6)
//    incommingMailBox.markInboxMessagesAsSeen(messages).await
//    assert(incommingMailBox.getUnseenInboxMessages.await.size == 0)
  }

//  it should "send messages using SMTP" in {
//    outgoingMailBox.send(MailMessage(
//      from=List(new InternetAddress("test@busymachines.com")),
//      to=List(new InternetAddress("paul.sabou@gmail.com")),
//      subject=Some("Test message 1"))
//    )
//  }
  
}
