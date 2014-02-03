package com.busymachines.commons.domain

import java.util.Currency

object Euro {
  val euro = Currency.getInstance("EUR")

  def apply(amount: BigDecimal) =
    Money(euro, amount)

  def apply(amount: Double) =
    Money(euro, BigDecimal(amount))
}

case class Money(currency: Currency, amount : BigDecimal) {
  def this(currency: Currency, amount : Double) =
    this(currency, BigDecimal(amount))

  def +(that : Money) = { 
    require(this.currency == that.currency)
    copy(amount = this.amount + that.amount)
  }
  def abs = copy(amount = amount.abs)
}