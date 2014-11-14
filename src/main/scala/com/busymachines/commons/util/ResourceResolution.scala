package com.busymachines.commons.util

/**
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 11/14/14.
 *
 */
trait ResourceResolution {
  protected def resourceURL(resourceName: String): java.net.URL = {
    val URL: java.net.URL = getClass.getResource(resourceName)
    URL
  }
}
