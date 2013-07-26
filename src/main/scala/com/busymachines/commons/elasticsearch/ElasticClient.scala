package com.busymachines.commons.elasticsearch

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress

class ElasticClient(configuration: EsConfiguration) extends TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", configuration.clusterName)) {
  addTransportAddresses((for (esHostName <- configuration.esHostNames) yield new InetSocketTransportAddress(esHostName, configuration.esPort)): _*)
} 
