package com.busymachines.commons.logging.domain

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by alex on 22.09.2014.
 */
class IndexSelector(indexNamePrefix:String, indexNameDateFormat:String, private var date:DateTime) {
  def isUpdated: Boolean = date.dayOfYear.get!=DateTime.now.dayOfYear.get

  def getName={
    def print= s"$indexNamePrefix-${DateTimeFormat.forPattern(indexNameDateFormat).print(DateTime.now)}"
    if(isUpdated) date=DateTime.now
    print
  }
}
