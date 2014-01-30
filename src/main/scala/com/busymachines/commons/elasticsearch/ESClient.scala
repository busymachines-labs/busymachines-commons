package com.busymachines.commons.elasticsearch

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import com.busymachines.commons.Logging
import org.elasticsearch.Version

object ESClient {
  implicit def toEsClient(client : ESClient) = new org.scalastuff.esclient.ESClient(client)
}

class ESClient(val config : ESConfig) extends TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", config.clusterName)) with Logging {
  info("Using ElasticSearch client " + Version.CURRENT)
  addTransportAddresses(config.hostNames.map(new InetSocketTransportAddress(_, config.port)): _*)
} 
