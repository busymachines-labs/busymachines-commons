package com.busymachines.commons.test

import org.scalatest.FlatSpec
import com.busymachines.commons.mail.{MailBox, MailConfig}
import com.busymachines.commons.implicits._
/**
 * Created by paul on 2/4/14.
 */
class MailBoxTests extends FlatSpec {

  val mailBox = new MailBox(new MailConfig("test.busymachines.mail"))
  "MailBox" should "receive max 10 mails from a mailbox" in {
    val initialMessages = mailBox.getMessages(messageRange=(1,mailBox.getMessageCount().await)).await
    assert(initialMessages.size == 5)
  }


    it should "receive unseen messages from a mailbox" in {
      // Make top 10 messages UNSEEN
      val initialMessages = mailBox.getMessages(messageRange=(1,mailBox.getMessageCount().await)).await
      initialMessages.map(m=>println(s"message $m.subject has att ${m.attachments.size}"))
      assert(initialMessages.size == 5)
      mailBox.markInboxMessagesAsSeen(initialMessages,false).await

      // Get unseen messages & mark them as seen
      val messages = mailBox.getUnseenInboxMessages.await
      assert(messages.size == 5)
      mailBox.markInboxMessagesAsSeen(messages).await
      assert(mailBox.getUnseenInboxMessages.await.size == 0)
    }

  }
