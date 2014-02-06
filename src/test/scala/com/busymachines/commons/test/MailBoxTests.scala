package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.mail.{IncommingMailBox, IncommingMailConfig}
import com.busymachines.commons.implicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by paul on 2/4/14.
 */
class MailBoxTests extends FlatSpec {

  val mailBox = new IncommingMailBox(new IncommingMailConfig("test.busymachines.mail"))
  "MailBox" should "receive max 10 mails from a mailbox" in {
    val initialMessages = mailBox.getMessages(messageRange = (1, mailBox.getMessageCount().await)).await
    assert(initialMessages.size == 6)
  }

  it should "receive messages within a specific date range from a mailbox" in {
    val formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
    val dateTimeRange = (formatter.parseDateTime("04/02/2014 00:00:00"), formatter.parseDateTime("05/02/2014 00:00:00"))
    val initialMessages = mailBox.getMessages(dateRange = dateTimeRange).await
    assert(initialMessages.size == 5)
    mailBox.markInboxMessagesAsSeen(initialMessages, false).await
  }

  it should "receive unseen messages from a mailbox" in {
    // Make top 10 messages UNSEEN
    val initialMessages = mailBox.getMessages(messageRange = (1, mailBox.getMessageCount().await)).await
    assert(initialMessages.size == 6)
    mailBox.markInboxMessagesAsSeen(initialMessages, false).await

    // Get unseen messages & mark them as seen
    val messages = mailBox.getUnseenInboxMessages.await
    assert(messages.size == 6)
    mailBox.markInboxMessagesAsSeen(messages).await
    assert(mailBox.getUnseenInboxMessages.await.size == 0)
  }

}
