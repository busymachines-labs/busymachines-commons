package com.busymachines.commons.spray

import java.io.File
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.language.postfixOps
import org.parboiled.common.FileUtils
import com.busymachines.commons.Implicits._
import akka.actor.ActorRefFactory
import spray.http.CacheDirectives.`max-age`
import spray.http.CacheDirectives.`public`
import spray.http.CacheDirectives.`no-cache`
import spray.http.ContentType
import spray.http.HttpCharsets
import spray.http.HttpHeaders.`Cache-Control`
import spray.http.MediaType
import spray.http.MediaTypes
import spray.httpx.marshalling.BasicMarshallers
import spray.routing.Directive.pimpApply
import spray.routing.Route
import spray.routing.directives.ContentTypeResolver
import spray.util.actorSystem
import com.busymachines.commons.CommonConfig
import com.busymachines.commons.ProfilingUtils.time
import scala.collection.concurrent.TrieMap

/**
 * Class that serves ui content. For url's with an extension, the corresponding resource is read from the
 * resourceRoot, 'public' by default, and returned. All other urls return the root document, index.html' by default.
 *
 * Resources are cached, both on the server and on the client by means of the Cache-Control header.
 * @param resourceRoot
 * @param rootDocument
 * @param actorRefFactory
 */
class UiService(resourceRoot: String = "public", rootDocument: String = "index.html")(implicit actorRefFactory: ActorRefFactory) extends CommonHttpService {

  private val root = resourceRoot.split("\\.").filter(_.nonEmpty).mkString("/")
  private val pattern = """(['\"])([/a-zA-Z_0-9 \-\.]*)(\?.*crc=)\?(.*)(['\"])""".r
  private val cacheTime: Duration = 7.days
  private val cacheTimeSecs = cacheTime.toSeconds
  private val cache = TrieMap[(String, Option[String]), Route]()
  
  if (CommonConfig.devmode)
    info(s"Resources are read from source folders in $resourceRoot (devmode)")

  def route =
    get {
      pathEnd {
        processPath("")
      } ~
      path(Rest) { path =>
        processPath(path)
      }
    }
  
  private def processPath(path: String) = 
    parameters('crc ?) { crc =>
      val (doc, ext, isRoot) = extension(path) match {
        case "" => (rootDocument, extension(rootDocument), true)
        case ext => (path, ext, false)
      }
      val mediaType = MediaTypes.forExtension(ext).getOrElse(MediaTypes.`application/octet-stream`)
      val shouldProcess = (isRoot || crc.isDefined) && !mediaType.binary
      if (!isRoot) {
        cache.getOrElseUpdate((path, crc), {
          debug(s"Caching resource : $doc")
          respondWithHeader(`Cache-Control`(`public`, `max-age`(cacheTimeSecs))) {
            getFromResource(doc, ext, mediaType, crc, shouldProcess, isRoot)
          }
        })
      } else {
        debug(s"Getting non-cachable resource : $doc")
        respondWithHeader(`Cache-Control`(`no-cache`)) {
          getFromResource(doc, ext, mediaType, crc, shouldProcess, isRoot)
        }
      }
    }

  def getFromResource(path: String, ext: String, mediaType: MediaType, crc: Option[String], shouldProcess: Boolean, isRoot: Boolean)(implicit refFactory: ActorRefFactory, resolver: ContentTypeResolver): Route = {
    
    time("Fetching resource " + path) {
    val contentType = if (mediaType.binary) ContentType(mediaType) else ContentType(mediaType, HttpCharsets.`UTF-8`)
    val classLoader = actorSystem(refFactory).dynamicAccess.classLoader
    implicit val bufferMarshaller = BasicMarshallers.byteArrayMarshaller(contentType)

    // Load resource
    loadResource("", path, classLoader) match {
      case Some(bytes) =>
        // Check crc
        crc.map(crc => if (crc == bytes.crc32.toString) Some(bytes) else None).getOrElse(Some(bytes)) match {
          case Some(bytes) =>
            complete {
              // Should process?
              if (shouldProcess) process(path, bytes, contentType, classLoader, isRoot)
              else bytes
            }
          case None =>
            reject
        }
      case None => 
        reject
    }
    }
  }

  lazy val resourceSourceRoots: List[File] = {
    val dirs = new File(".") :: new File(".").listFiles().filter(_.isDirectory).toList
    dirs.map(new File(_, "src/main/resources/" + root)).filter(_.exists)
  }

  def loadResource(basePath: String, relativePath: String, classLoader: ClassLoader): Option[Array[Byte]] = {
    val path = resolve(basePath, relativePath)
    def readFromClassPath = Option(classLoader.getResource(root + "/" + path)).map(resource => FileUtils.readAllBytes(resource.openStream))
    def readFromMetaInf = Option(classLoader.getResource("META-INF/resources/" + path)).map(resource => FileUtils.readAllBytes(resource.openStream))
    def readFromSourceRoots = resourceSourceRoots.collectFirst((f: File) => Option(FileUtils.readAllBytes(new File(f, path))))
    if (CommonConfig.devmode)
      readFromSourceRoots orElse readFromClassPath orElse readFromMetaInf
    else
      readFromClassPath orElse readFromMetaInf
  }

  def resolve(base: String, path: String) = {
    if (path.startsWith("/")) {
      path
    } else {
      var index = base.lastIndexOf('/')
      while (path.startsWith("../"))
        index = base.lastIndexOf('/', index)
      if (index == -1) path
      else base.substring(0, index) + "/" + path
    }
  }

  def stripMin(path: String): String = {
    val index = path.indexOf(".min?.")
    if (index >= 0)
      if (CommonConfig.devmode)
        path.substring(0, index) + path.substring(index + 5)
    else
        path.substring(0, index + 4) + path.substring(index + 5)
    else path
  }

  def process(basePath: String, bytes: Array[Byte], contentType: ContentType, classLoader: ClassLoader, isRoot: Boolean): Array[Byte] = {
    val out = new StringBuilder
    val shouldAddDebugHtml = CommonConfig.devmode && isRoot
    for (l <- Source.fromBytes(bytes, contentType.charset.toString).getLines) {
      val line = if (shouldAddDebugHtml) {
        val i0 = l.indexOf("</body>")
        val i = if (i0 >= 0) i0 else l.indexOf("</html>")
        if (i >= 0) {
          l.substring(0, i) + "<div style='position: absolute; right: 10px; bottom: 10px; border: 1px solid red; color: red; padding: 4px'>DEVMODE</div>" + l.substring(i)
        } else l
      } else l
      pattern.findFirstMatchIn(line) match {
        case Some(m) =>
          val path = stripMin(m.group(2))
          loadResource(basePath, path, classLoader) match {
            case None =>
              debug("Couldn't load resource " + basePath + "/" + path)
              out.append(m.before).append(m.group(1)).append(m.group(2)).append(m.group(3)).append("=!").append(m.group(4)).append(m.group(5)).append(m.after).append("\n")
            case Some(bytes) =>
              val crc = bytes.crc32
              out.append(m.before).append(m.group(1)).append(path).append(m.group(3)).append(crc).append(m.group(4)).append(m.group(5)).append(m.after).append("\n")
          }
        case None =>
          out.append(stripMin(line)).append('\n')

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