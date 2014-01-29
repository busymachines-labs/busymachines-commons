package com.busymachines.commons.spray

import java.io.File
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.language.postfixOps
import org.parboiled.common.FileUtils
import com.busymachines.commons.implicits._
import akka.actor.ActorRefFactory
import spray.http.CacheDirectives.`max-age`
import spray.http.CacheDirectives.`no-cache`
import spray.http.ContentType
import spray.http.HttpCharsets
import spray.http.HttpHeaders.`Cache-Control`
import spray.http.MediaType
import spray.http.MediaTypes
import spray.httpx.marshalling.BasicMarshallers
import spray.routing.Directive.pimpApply
import spray.routing.Route
import spray.routing.directives.CacheSpecMagnet.apply
import spray.routing.directives.CachingDirectives.cache
import spray.routing.directives.CachingDirectives.routeCache
import spray.routing.directives.ContentTypeResolver
import spray.util.actorSystem
import com.busymachines.commons.CommonConfig
import com.busymachines.commons.ProfilingUtils.time
import com.busymachines.commons.cache.Cache
import scala.concurrent.Future

class UiService(resourceRoot: String = "public", rootDocument: String = "index.html")(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService {

  private val root = resourceRoot.split("\\.").filter(_.nonEmpty).mkString("/")
  private val pattern = """(['\"])([/a-zA-Z_0-9 \.]*)-\?\?\?.([a-zA-Z_0-9]*)(['\"])""".r
  private val cacheTime: Duration = 7 days
  private val cacheTimeSecs = cacheTime.toSeconds
  private val theCache = Cache.expiringLru[String,Option[Array[Byte]]](Int.MaxValue)
  
  if (CommonConfig.devmode)
    info("Resources are read from source folders (devmode)")

  def route =
    get {
      path(Rest) {
        path =>
          val (doc, ext, shouldCache, shouldProcess) = extension(path) match {
            case "" => (rootDocument, extension(rootDocument), false, true)
            case ext => (path, ext, true, false)
          }
          val mediaType = MediaTypes.forExtension(ext).getOrElse(MediaTypes.`application/octet-stream`)
          if (mediaType.binary) {
            respondWithHeader(`Cache-Control`(`max-age`(cacheTimeSecs))) {
              getFromResource(doc, ext, mediaType, shouldProcess,shouldCache)
            }
          } else {
            if (shouldCache) {
                respondWithHeader(`Cache-Control`(`max-age`(cacheTimeSecs))) {
                  getFromResource(doc, ext, mediaType, shouldProcess,shouldCache)
                }
            } else {
              respondWithHeader(`Cache-Control`(`no-cache`)) {
                getFromResource(doc, ext, mediaType, shouldProcess,shouldCache)
              }
            }
          }
      }
    }

  def getFromResource(path: String, ext: String, mediaType: MediaType, shouldProcess: Boolean, shouldCache:Boolean)(implicit refFactory: ActorRefFactory, resolver: ContentTypeResolver): Route = {
    
    time("Fetching resource " + path + "." + ext) {
    val contentType = if (mediaType.binary) ContentType(mediaType) else ContentType(mediaType, HttpCharsets.`UTF-8`)
    val classLoader = actorSystem(refFactory).dynamicAccess.classLoader

    def content = loadResource("", path, classLoader,shouldCache).map {
      bytes =>
        if (mediaType.binary || !shouldProcess) bytes 
        else process(path, bytes, contentType, classLoader)
    }

    def contentBare =
      path.lastIndexOf('-') match {
        case -1 => None
        case i =>
          // extract crc
          path.substring(i + 1, path.length - ext.length - 1).toLongOption flatMap {
            crc =>
              val barePath = path.substring(0, i) + "." + ext
              loadResource("", barePath, classLoader,shouldCache).filter(_.crc32 == crc).map {
                bytes =>
                  if (mediaType.binary) bytes
                  else process(path, bytes, contentType, classLoader)
              }
          }
      }
    
    implicit val bufferMarshaller = BasicMarshallers.byteArrayMarshaller(contentType)
    content orElse contentBare match {
      case Some(bytes) => complete(bytes)
      case None => reject
    }
    }
  }

  lazy val resourceSourceRoots: List[File] = {
    val dirs = new File(".") :: new File(".").listFiles().filter(_.isDirectory()).toList
    dirs.map(new File(_, "src/main/resources/" + root)).filter(_.exists)
  }
  
    def loadDirectResource(basePath: String, relativePath: String, classLoader: ClassLoader): Option[Array[Byte]] = {
	    val path = resolve(basePath, relativePath)
		debug(s"Loading resource : ${path}")  
	    def readFromClassPath = Option(classLoader.getResource(root + "/" + path)).map(resource => FileUtils.readAllBytes(resource.openStream))
	    def readFromSourceRoots = resourceSourceRoots.collectFirst((f: File) => Option(FileUtils.readAllBytes(new File(f, path))))
	    if (CommonConfig.devmode)
	      readFromSourceRoots orElse readFromClassPath
	    else
	      readFromClassPath
  }


  def loadResource(basePath: String, relativePath: String, classLoader: ClassLoader, shouldCache:Boolean): Option[Array[Byte]] = {
    if (shouldCache) theCache.getOrElseUpdate(resolve(basePath, relativePath),Future.successful(loadDirectResource(basePath,relativePath,classLoader))).await(1 minute)
    else loadDirectResource(basePath,relativePath,classLoader)
  }
    
  def resolve(base: String, path: String) = {
    if (path.startsWith("/")) {
      path
    } else {
      var index = base.lastIndexOf('/');
      while (path.startsWith("../"))
        index = base.lastIndexOf('/', index)
      if (index == -1) path
      else base.substring(0, index) + "/" + path
    }
  }

  def process(basePath: String, bytes: Array[Byte], contentType: ContentType, classLoader: ClassLoader, shouldCache:Boolean = false): Array[Byte] = {
    val out = new StringBuilder
    for (line <- Source.fromBytes(bytes, contentType.charset.toString).getLines) {
      pattern.findFirstMatchIn(line) match {
        case Some(m) =>
          val ext = m.group(3)
          val path = m.group(2) + "." + ext
          loadResource(basePath, path, classLoader,shouldCache) match {
            case None =>
              out.append(m.before).append(m.group(1)).append(m.group(2)).append('-').append("!!!").append('.').append(m.group(3)).append(m.group(4)).append(m.after).append("\n")
            case Some(bytes) =>
              val crc = bytes.crc32
              out.append(m.before).append(m.group(1)).append(m.group(2)).append('-').append(crc).append('.').append(m.group(3)).append(m.group(4)).append(m.after).append("\n")
          }
        case None =>
          out.append(line).append('\n')

      }
    }
    out.toString.getBytes(contentType.charset.toString)
  }

  def extension(path: String): String =
    path.lastIndexOf('.') match {
      case -1 => ""
      case i => path.indexOf('/', i) match {
        case -1 => path.substring(i + 1)
        case i => ""
      }
    }

}