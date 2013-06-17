package com.kentivo.mdm.logic

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Props
import spray.can.client.HttpClient
import spray.client.HttpConduit
import spray.client.HttpConduit._
import spray.http.HttpMethods.GET
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.io.IOExtension
import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure
import scala.io.Codec
import spray.http.BasicHttpCredentials
import scala.xml.NodeSeq
import spray.httpx.unmarshalling.BasicUnmarshallers._
import spray.httpx.unmarshalling.Unmarshaller
import spray.httpx.unmarshalling.pimpHttpEntity
import spray.http.MediaTypes._
import spray.http.HttpBody
import scala.xml.XML
import spray.http.EmptyEntity
import java.io.InputStreamReader
import java.io.ByteArrayInputStream
import com.kentivo.mdm.db.ItemDao
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.MimeType
import com.kentivo.mdm.domain.PropertyType
import com.busymachines.commons.domain.Id
import com.kentivo.mdm.domain.Party
import scala.concurrent.Await
import java.util.Locale
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyValue
import com.kentivo.mdm.domain.PropertyScope
import com.busymachines.commons.domain.HasId
import com.kentivo.mdm.domain.Repository
import com.kentivo.mdm.domain.Mutation
import com.kentivo.mdm.logic.implicits._
import org.joda.time.DateTime
import com.kentivo.mdm.db.HasValueForPropertyDaoFilter
import com.kentivo.mdm.domain.Media
import com.kentivo.mdm.db.MediaDao
import com.kentivo.mdm.db.HasValueForProperty

class IcecatAdapter(system: ActorSystem, itemDao: ItemDao, mediaDao : MediaDao)(implicit ec: ExecutionContext) {

  val repository = Id[Repository]("icecat-repo")
  val icecatRootItemId = Id[Item]("icecat-root")
  val categoryIcecatIdProperty = Id[Property]("icecat-category-id")
  val categoryNameProperty = Id[Property]("icecat-category-name")
  val categoryDescriptionProperty = Id[Property]("icecat-category-description")
  val categoryKeywordsProperty = Id[Property]("icecat-category-keywords")
  val categoryImageProperty = Id[Property]("icecat-category-image")
  val categoryThumbnailProperty = Id[Property]("icecat-category-thumbnail")

  def toInt(s : String) : Option[Int] = 
    try {
      Some(Integer.parseInt(s.trim))
    } catch {
      case e: NumberFormatException => None
    }
    
  implicit private class RichXml(xml : NodeSeq) {
    def i18nValues(element : String) : Seq[(Locale, String)] =  
      (for {
        elt <- xml \ element
        value = (elt \ "@Value").toString if value != ""
        langid <- toInt((elt \ "@langid").toString)
        locale = langIdMap.get(langid)
      } yield (locale, value)).collect {
        case (Some(locale), value) => (locale, value)
      }
  }

  lazy val langIdMap: Map[Int, Locale] = {
    val text = io.Source.fromInputStream(getClass.getResourceAsStream("/icecat/LanguageList.xml"))(Codec.UTF8).mkString
    val xml = XML.loadString(text)
    val localesById = for (lang <- xml \ "Response" \ "LanguageList" \ "Language") yield {
      (lang \ "@ID").toString.toInt -> new Locale((lang \ "@ShortCode").toString)
    }
    localesById.toMap
  }

  def importCategories(view : Future[RepositoryView]) = {
    val ioBridge = IOExtension(system).ioBridge()
    val httpClient = system.actorOf(Props(new HttpClient(ioBridge)))
    val conduit = system.actorOf(
      props = Props(new HttpConduit(httpClient, "data.icecat.biz", 80)),
      name = "http-conduit")
    val pipeline: HttpRequest => Future[HttpResponse] = (
      addCredentials(BasicHttpCredentials("ruudditerwich", "tpacoh18")) ~> sendReceive(conduit))

    // fetch categories
    val response: Future[HttpResponse] = 
      pipeline(HttpRequest(method = GET, uri = "/export/freeurls/categories.xml"))
      
    // wait for view and response
    view.zip(response).onComplete {
      case Success((view, response)) => response.entity.as[NodeSeq] match {
        case Right(xml) => processResult(view, xml)
        case Left(_) =>
      }
      case Failure(error) => println("FAIL:" + error)
    }
  }

  def processResult(view : RepositoryView, xml: NodeSeq) = {

    var itemsToWrite = Map[Id[Item], Item]()
    
    val mutation = Mutation("Icecat Import", DateTime.now)
    
    val mutator = new Mutator(view, itemDao, mutation)

    //http://data.icecat.biz/export/freeurls/categories.xml
    // http://data.icecat.biz/export/freexml/1495.xml

    // Get or create root item.
    mutator.modifyItem(icecatRootItemId, _.copy(isCategory = true))
    mutator.modifyProperty(icecatRootItemId, categoryIcecatIdProperty, _.copy(name = Map(Locale.ROOT -> "IcecatId"), scope = PropertyScope.Category))
    mutator.modifyProperty(icecatRootItemId, categoryImageProperty, _.copy(name = Map(Locale.ROOT -> "Image"), `type` = PropertyType.Media, scope = PropertyScope.Category))
    mutator.modifyProperty(icecatRootItemId, categoryThumbnailProperty, _.copy(name = Map(Locale.ROOT -> "Thumbnail"), `type` = PropertyType.Media, scope = PropertyScope.Category))
    mutator.modifyProperty(icecatRootItemId, categoryNameProperty, _.copy(name = Map(Locale.ROOT -> "Name"), scope = PropertyScope.Category, multiLingual = true))
    mutator.modifyProperty(icecatRootItemId, categoryDescriptionProperty, _.copy(name = Map(Locale.ROOT -> "Description"), scope = PropertyScope.Category, multiLingual = true))
    mutator.modifyProperty(icecatRootItemId, categoryKeywordsProperty, _.copy(name = Map(Locale.ROOT -> "Keywords"), scope = PropertyScope.Category, multiLingual = true, multiValue = true))

    // Read all existing icecat categories
    val categories = mutator.searchItems(HasValueForProperty(categoryIcecatIdProperty))
    
    val categoriesById : Map[String, Item] = 
      categories.flatMap(c => c.propertyValue(categoryIcecatIdProperty).map(_._3.value -> c)).toMap
      
      println("Icecat ids:" + categories.map(_.id))
    
    // Proces categories
    (xml \ "Response" \ "CategoriesList" \ "Category").foreach {
      catNode =>
        val id = Id[Item]((catNode \ "@ID").toString)
        // TODO search by icecat id 
        mutator.setValue(id, categoryImageProperty, readAndStoreMedia(catNode \ "@LowPic").map(_.id.toString))
        mutator.setValue(id, categoryThumbnailProperty, readAndStoreMedia(catNode \ "@ThumbPic").map(_.id.toString))
        mutator.setValues(id, categoryNameProperty, catNode.i18nValues("Name"))
        mutator.setValues(id, categoryDescriptionProperty, catNode.i18nValues("Description"))
        mutator.setValues(id, categoryKeywordsProperty, catNode.i18nValues("Keywords"))
        println("Item: " + mutator.findItem(id))
        
        
//        val category = categoriesById.getOrElse(id, mutator.newItem)
//        println(names.mkString("\n"))
        // Find or create a category with this id
//        val category = propertyValue(categories, categoryIcecatIdProperty, PropertyScope.Category, _ => None) match {
//          case Some((item, property, value)) => item
//          case None => Item(repository, mutation.id)
//        }
//        // Set id property
//        
//        items.find(_.id.toString == id).getOrElse {
////          itemsToWrite :+= Item(id = Id(id), repository = Id(repository), isCategory = true)
//        }
    }
    
    mutator.flush

    println("Write:" + itemsToWrite.mkString("\n"))

  }
  
  def readAndStoreMedia(url : Any) : Option[Media] = 
    Await.result(mediaDao.importUrl(url.toString), 10 seconds)
}