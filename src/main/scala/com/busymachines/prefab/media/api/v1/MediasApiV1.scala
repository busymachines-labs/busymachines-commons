package com.busymachines.prefab.media.api.v1

import com.busymachines.commons.spray.CommonHttpService
import akka.actor.ActorRefFactory
import com.busymachines.prefab.party.logic.UserAuthenticator
import com.busymachines.prefab.media.db.MediaDao
import com.busymachines.prefab.party.api.v1.PartyApiV1Directives
import com.busymachines.prefab.media.api.v1.model.MediaApiV1JsonFormats
import com.busymachines.prefab.media.api.v1.model.MediaInput
import com.busymachines.commons.implicits._

class MediasApiV1(mediaDao: MediaDao, authenticator: UserAuthenticator)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with MediaApiV1Directives {
  val route =
    path("medias") {
      authenticate(authenticator) { implicit securityContext =>
        get {
          complete {
            "OK"
          }
        } ~
          post {
            entity(as[MediaInput]) { mediaInput =>
              complete {
                "OK"
                //decodeWebBase64(entity.data).map(fdcManager.importPharmaCon(entity.name.getOrElse("The imported file must have a name."), _)).getOrElse(throw new Exception(s"The uploaded data could not be converted from base64"))
              }
            }
          }
      }
    }
}



        
