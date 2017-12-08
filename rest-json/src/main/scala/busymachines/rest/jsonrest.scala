package busymachines.rest

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 06 Sep 2017
  *
  */
object jsonrest {

  import de.heikoseeberger.akkahttpcirce

  type JsonSupport = akkahttpcirce.FailFastCirceSupport
  val JsonSupport: akkahttpcirce.FailFastCirceSupport.type = akkahttpcirce.FailFastCirceSupport

  type ErrorAccumulatingJsonSupport = akkahttpcirce.ErrorAccumulatingCirceSupport

  val ErrorAccumulatingJsonSupport: akkahttpcirce.ErrorAccumulatingCirceSupport.type =
    akkahttpcirce.ErrorAccumulatingCirceSupport
}
