package com.busymachines.commons.domain

import java.util.Currency

case class Money(currency: Currency, amount : BigDecimal) {
  def +(that : Money) = { 
    require(this.currency == that.currency)
    copy(amount = this.amount + that.amount)
  }
  def abs = copy(amount = amount.abs)
}