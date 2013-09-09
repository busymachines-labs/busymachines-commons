package com.kentivo.mdm.logic

import com.kentivo.mdm.domain.User
import com.kentivo.mdm.domain.Source
import com.busymachines.commons
import com.kentivo.mdm.db.SourceDao
import com.kentivo.mdm.domain.Repository
import scala.concurrent.Future
import com.kentivo.mdm.domain.Schedule
import org.joda.time.DateTime
import com.kentivo.mdm.domain.ScheduleRepeat
import com.kentivo.mdm.domain.Item
import java.util.Locale
import com.kentivo.mdm.domain.Property
import com.kentivo.mdm.domain.Mutation
import com.busymachines.commons.domain.Id

class SourceManager(sourceDao : SourceDao) {

  def findSources1(repository : Option[Id[Repository]])(implicit auth: SecurityContext): Future[Source] = null
  def findSources(repository : Option[Id[Repository]])(implicit auth: SecurityContext): Future[List[Source]] = {
    val repository = Id.generate[Repository]
    val mutation = Id.generate[Mutation]
    Future.successful(List(Source(name="Sample Source", repository = Id.generate, importSchedule=List(
        Schedule(DateTime.now, Some(ScheduleRepeat.Daily)),
        Schedule(DateTime.now, Some(ScheduleRepeat.Weekly)),
        Schedule(DateTime.now, Some(ScheduleRepeat.Monthly)),
        Schedule(DateTime.now, Some(ScheduleRepeat.Yearly)),
        Schedule(DateTime.now, None)
        ),
        model=List(
            Item(repository = Id.generate, mutation = Id.generate, name=Map(new Locale("nl") -> "Klant", Locale.forLanguageTag("") -> "CUSTOMER", Locale.CANADA_FRENCH -> "Customer"),
                properties=List(
                    Property(mutation = mutation, name=Map(new Locale("nl") -> "Prijs", Locale.forLanguageTag("") -> "Price", Locale.CANADA_FRENCH -> "Prix")),
                    Property(mutation = mutation, multiLingual = true, name=Map(Locale.forLanguageTag("") -> "Description")),
                    Property(mutation = mutation, multiValue = true, name=Map(Locale.forLanguageTag("") -> "Related Products"))))))))
//    sourceDao.findSources(repository)
  }

  /**
   * Create a source based on specific fields received.
   */
  def create(source: Source)(implicit auth: SecurityContext): Int = {
    0
  }
  /**
   * Create a source based on specific fields received.
   */
  def update(id : Id[Source], source: Source)(implicit auth: SecurityContext): Int = {
    0
  }

  /**
   * Find a specific source by id.
   */
  def find(id: Id[Source]): Source = {
   null
  }

  /**
   * Delete a specific source based on its id.
   */
  def delete(id: Id[Source]): String = {
    ""
  }

  /**
   * To check if user has enough rights to use a specific source id for specific operations (eg. to create a location for this sourceId) we have to
   * check if that source is the source of current user OR if it's a child source.
   */
  def userHasEnoughRights(sourceId: Int, user: User) = {
    false
  }
}