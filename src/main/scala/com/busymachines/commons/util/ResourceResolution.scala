package com.busymachines.commons.util

import com.busymachines.commons.CommonException

/**
 * Created by Lorand Szakacs, lorand.szakacs@busymachines.com, on 11/14/14.
 *
 */
trait ResourceResolution {
  protected def resourceURL(resourceName: String): java.net.URL = {
    val URL: java.net.URL = getClass.getResource(resourceName)
    if (URL == null) {
      throw new CommonException(s"Failed to locate resource: '${resourceName }' from package: '${getClass.getPackage.getName }'",
        Some("FailedToLocateResource"),
        parameters = Map(
          "package" -> s"${getClass.getPackage.getName }",
          "class" -> s"${getClass.getCanonicalName }"
        ),
        cause = None
      )
    } else {
      URL
    }
  }
}
