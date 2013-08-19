package com.busymachines.commons.elasticsearch

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress

object ESClient {
  implicit def toEsClient(client : ESClient) = new org.scalastuff.esclient.ESClient(client)
}

class ESClient(val config : ESConfig) extends TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", config.clusterName)) {
  addTransportAddresses(config.hostNames.map(new InetSocketTransportAddress(_, config.port)): _*)
} 
