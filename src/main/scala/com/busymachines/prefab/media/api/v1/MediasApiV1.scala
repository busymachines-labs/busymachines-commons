package com.busymachines.prefab.media.api.v1

import com.busymachines.commons.spray.CommonHttpService
import akka.actor.ActorRefFactory
import com.busymachines.prefab.authentication.logic.PrefabAuthenticator
import com.busymachines.prefab.media.db.MediaDao
import com.busymachines.prefab.media.domain.Media
import com.busymachines.commons.domain.MimeType
import com.busymachines.commons.domain.MimeTypes
import com.busymachines.prefab.media.api.v1.model.MediaApiV1JsonFormats
import com.busymachines.prefab.media.api.v1.model.MediaInput
import com.busymachines.commons.Implicits._
import com.busymachines.commons.domain.Id
import spray.http.MediaType
import com.busymachines.commons.EntityNotFoundException
import spray.http.HttpEntity
import com.busymachines.prefab.media.Implicits._
import com.busymachines.prefab.media.service.MimeTypeDetector
import com.busymachines.prefab.media.logic.DefaultMimeTypeDetector
import spray.httpx.SprayJsonSupport

class MediasApiV1[SecurityContext](mediaDao: MediaDao, mimeTypeDetector:MimeTypeDetector)(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService with MediaApiV1Directives with SprayJsonSupport {

  private def decodeWebBase64(src: String): Option[Array[Byte]] = {
    val base64 = "data:(.*);base64,(.*)".r
    src match {
      case base64(mimetype, data) => Some(data.decodeBase64)
      case _ =>
        None
    }
  }

  val route =
    path("medias") {
      post {
        entity(as[MediaInput]) { mediaInput =>
          complete {
            mediaDao.store(mimeTypeDetector.mimeTypeOf(Some(mediaInput.name.getOrElse(throw new Exception("The file must have a name."))),None).getOrElse(throw new Exception(s"Cannot detect the mime type of the file from it's filename.")), mediaInput.name, decodeWebBase64(mediaInput.data).getOrElse(throw new Exception("The file data could not be properly web base64 decoded."))) map (_.id.value)
          }
        }
      }
    } ~
    path("medias" / MatchId[Media]) { mediaId =>
        delete {
          complete {
            mediaDao.delete(mediaId) map (_ => "OK")
          }
        } ~
        get {
          //TODO add authentication
            parameters(
              'raw.as[Boolean]?) { raw =>
                mediaDao.retrieve(mediaId).await match {
                  case None => complete {
                    throw new EntityNotFoundException(mediaId.toString, "media")
                  }
                  case Some(media) => respondWithMediaType(MediaType.custom(media.mimeType.value)) {
                    complete {
                      raw match {
                        case Some(true) => HttpEntity(media.data)
                        case Some(false) => media
                        case None => media
                      }

                    }
                  }
                }
              }
          }
      }
}



        
