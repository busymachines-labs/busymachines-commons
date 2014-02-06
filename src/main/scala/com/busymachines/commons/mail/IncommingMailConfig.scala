package com.busymachines.commons.mail

import com.busymachines.commons.implicits._
import com.busymachines.commons.CommonConfig

class IncommingMailConfig(baseName: String) extends CommonConfig(baseName) {
  def protocol = string("protocol") 
  def host = string("host") 
  def port = int("port") 
  def userName = string("userName") 
  def password = string("password") 
}