package com.busymachines.prefab.media.elasticsearch

import com.busymachines.commons.elasticsearch.ESMapping
import com.busymachines.prefab.media.domain.HashedMedia

object MediaMapping extends ESMapping[HashedMedia] {
  val id = "id" -> "_id" as String & NotAnalyzed
  val mimeType = "mimeType" as String & Analyzed
  val name = "name" as String & Analyzed
  val hash = "hash" as String & NotAnalyzed
  val data = "data" as String & NotIndexed
}
