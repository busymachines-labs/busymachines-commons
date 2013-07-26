package com.busymachines.commons.elasticsearch

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.scalastuff.esclient.ESClient

object ElasticSearchClient {
  implicit def toEsClient(client : ElasticSearchClient) = new ESClient(client)
}

class ElasticSearchClient(val config : ElasticSearchConfiguration) extends TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", config.clusterName)) {
  addTransportAddresses(config.hostNames.map(new InetSocketTransportAddress(_, config.port)): _*)
} 
