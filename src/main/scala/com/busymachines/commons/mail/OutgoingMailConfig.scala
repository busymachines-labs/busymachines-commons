package com.busymachines.commons.mail

import com.busymachines.commons.CommonConfig

/**
 * Created by paul on 2/6/14.
 */
case class OutgoingMailConfig(baseName: String) extends CommonConfig(baseName) {
  def auth = boolean("auth")
  def starttls = boolean("starttls")
  def ssl = boolean("ssl")
  def host = string("host")
  def port = int("port")
  def userName = string("userName")
  def password = string("password")
}