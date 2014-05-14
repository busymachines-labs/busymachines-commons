package com.busymachines.commons

import java.util.Locale

/**
 * Created by ruud on 04/01/14.
 */
class RichLocale(val locale: Locale) extends AnyVal {

  def isRoot =
    locale == Locale.ROOT

  def isParentOf(other: Locale) =
    locale.getLanguage.isEmpty || (locale.getLanguage == other.getLanguage &&
      (locale.getCountry.isEmpty || locale.getCountry == other.getCountry &&
        (locale.getVariant.isEmpty || locale.getVariant == other.getVariant)))

}
