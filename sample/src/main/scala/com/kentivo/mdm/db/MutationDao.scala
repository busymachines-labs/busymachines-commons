package com.kentivo.mdm.db

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import com.kentivo.mdm.commons.ESSourceProvider
import com.kentivo.mdm.domain.DomainJsonFormats
import com.kentivo.mdm.domain.Mutation

class MutationDao(provider: ESSourceProvider)(implicit ec: ExecutionContext) extends AbstractDao[Mutation](provider.MUTATION) with DomainJsonFormats {

  def createMutation(mutation: Mutation) : Future[Unit] =  
    super.create(mutation)

  def findMutations(): Future[List[Mutation]] = 
    super.find()

}