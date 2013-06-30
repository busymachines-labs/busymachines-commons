package com.kentivo.mdm.logic

import java.util.Locale

import scala.Option.option2Iterable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.io.Codec
import scala.xml.NodeSeq
import scala.xml.XML

import org.joda.time.DateTime

import com.busymachines.commons.dao.elasticsearch.MediaDao
import com.busymachines.commons.domain.Id
import com.busymachines.commons.domain.Media
import com.busymachines.commons.implicits.RichString
import com.kentivo.mdm.db.HasValueForProperty
import com.kentivo.mdm.db.ItemDao
import com.kentivo.mdm.domain.Item
import com.kentivo.mdm.domain.Mutation
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.PropertyScope
import com.kentivo.mdm.domain.PropertyType
import com.kentivo.mdm.domain.Repository
import com.kentivo.mdm.logic.implicits.RichItem

import akka.actor.ActorSystem
import spray.client.pipelining.Post
import spray.client.pipelining.addCredentials
import spray.client.pipelining.pimpWithResponseTransformation
import spray.client.pipelining.sendReceive
import spray.client.pipelining.sendReceive$default$3
import spray.client.pipelining.unmarshal
import spray.http.BasicHttpCredentials
import spray.http.HttpRequest

class IcecatAdapter(itemDao: ItemDao, mediaDao : MediaDao)(implicit ec: ExecutionContext, system: ActorSystem) {

  val repository = Id.static[Repository]("icecat-repo")
  val icecatRootItemId = Id.static[Item]("icecat-root")
  val categoryIcecatIdProperty = Id.static[Property]("icecat-category-id")
  val categoryNameProperty = Id.static[Property]("icecat-category-name")
  val categoryDescriptionProperty = Id.static[Property]("icecat-category-description")
  val categoryKeywordsProperty = Id.static[Property]("icecat-category-keywords")
  val categoryImageProperty = Id.static[Property]("icecat-category-image")
  val categoryThumbnailProperty = Id.static[Property]("icecat-category-thumbnail")

  lazy val langIdMap: Map[Int, Locale] = {
    val text = io.Source.fromInputStream(getClass.getResourceAsStream("/icecat/LanguageList.xml"))(Codec.UTF8).mkString
    val xml = XML.loadString(text)
    println("Loaded language list:" + xml)
    val localesById = for (lang <- xml \ "Response" \ "LanguageList" \ "Language") yield {
      (lang \ "@ID").toString.toInt -> new Locale((lang \ "@ShortCode").toString)
    }
    localesById.toMap
  }
  
  implicit class RichXml(xml : NodeSeq) {
    def i18nValues(element : String) : Seq[(Locale, String)] =  
      (for {
        elt <- xml \ element
        value = (elt \ "@Value").toString if value != ""
        langid <- (elt \ "@langid").toString.toOptionInt
        locale = langIdMap.get(langid)
      } yield (locale, value)).collect {
        case (Some(locale), value) => (locale, value)
      }
  }
  
  def importAll = {
    val repository = Repository()
    Await.result(readCategoriesXml.map(categoriesXml => processCategoriesXml(repository, categoriesXml)), 20 minutes)
  }
  
  def readCategoriesXml: Future[NodeSeq] = {
    val pipeline: HttpRequest => Future[NodeSeq] = (
      addCredentials(BasicHttpCredentials("ruudditerwich", "tpacoh18")) ~> 
      sendReceive ~> unmarshal[NodeSeq])
      pipeline(Post("http://data.icecat.biz/export/freeurls/categories.xml"))
  }

  def processCategoriesXml(repository : Repository, xml: NodeSeq) = {

    println("Processing categories")
    
    var itemsToWrite = Map[Id[Item], Item]()
    
    val mutation = Mutation("Icecat Import", DateTime.now)
    
    val mutator = new Mutator(itemDao, repository, mutation)

    //http://data.icecat.biz/export/freeurls/categories.xml
    // http://data.icecat.biz/export/freexml/1495.xml


    // Get or create root item.
    mutator.modifyItem(icecatRootItemId)(_.copy(isCategory = true))
    mutator.modifyProperty(icecatRootItemId, categoryIcecatIdProperty)(_.copy(name = Map(Locale.ROOT -> "IcecatId"), scope = PropertyScope.Category))
    mutator.modifyProperty(icecatRootItemId, categoryImageProperty)(_.copy(name = Map(Locale.ROOT -> "Image"), `type` = PropertyType.Media, scope = PropertyScope.Category))
    mutator.modifyProperty(icecatRootItemId, categoryThumbnailProperty)(_.copy(name = Map(Locale.ROOT -> "Thumbnail"), `type` = PropertyType.Media, scope = PropertyScope.Category))
    mutator.modifyProperty(icecatRootItemId, categoryNameProperty)(_.copy(name = Map(Locale.ROOT -> "Name"), scope = PropertyScope.Category, multiLingual = true))
    mutator.modifyProperty(icecatRootItemId, categoryDescriptionProperty)(_.copy(name = Map(Locale.ROOT -> "Description"), scope = PropertyScope.Category, multiLingual = true))
    mutator.modifyProperty(icecatRootItemId, categoryKeywordsProperty)(_.copy(name = Map(Locale.ROOT -> "Keywords"), scope = PropertyScope.Category, multiLingual = true, multiValue = true))

        println("Start searching")

    // Read all existing icecat categories
    val categories = mutator.searchItems(HasValueForProperty(categoryIcecatIdProperty), 1 minute)
    println("Got existing categories: " + categories)
    
    val categoriesById : Map[String, Item] = 
      categories.flatMap(c => c.propertyValue(categoryIcecatIdProperty).map(_._3.value -> c)).toMap
      
      println("Icecat ids:" + categories.map(_.id))
    
    // Proces categories
    (xml \ "Response" \ "CategoriesList" \ "Category").foreach {
      catNode =>
        println("Node:" + catNode)
        val id = (catNode \ "@ID").toString
        val item = categoriesById.getOrElse(id, mutator.createItem)
        // TODO search by icecat id 
//        mutator.setValue(item.id, categoryImageProperty, readAndStoreMedia(catNode \ "@LowPic").map(_.id.toString))
//        mutator.setValue(item.id, categoryThumbnailProperty, readAndStoreMedia(catNode \ "@ThumbPic").map(_.id.toString))
        mutator.setValues(item.id, categoryNameProperty, catNode.i18nValues("Name"))
        mutator.setValues(item.id, categoryDescriptionProperty, catNode.i18nValues("Description"))
        mutator.setValues(item.id, categoryKeywordsProperty, catNode.i18nValues("Keywords"))
        println("Item: " + mutator.retrieve(item.id))
        
        
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
    
    mutator.write(1 minute)

    println("Write:" + itemsToWrite.mkString("\n"))

  }
  
  def readAndStoreMedia(url : Any) : Option[Media] = 
    Await.result(mediaDao.importUrl(url.toString), 100 seconds)
}