package com.pako.store.search.impl

import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.slf4j.{Logger, LoggerFactory}

class ElasticLocalNode

object ElasticLocalNode {

  private final val log: Logger =
    LoggerFactory.getLogger(classOf[ElasticLocalNode])


  System.setProperty("es.set.netty.runtime.available.processors", "false")
  val node = LocalNode("Store", "/tmp/elastic")
  val client = {
    println("Client Created")
    val client = node.client(shutdownNodeOnClose = true)
    val res = client.execute {
      createIndex("products").mappings(
        mapping("product").fields(
          keywordField("id"),
          textField("name"),
          textField("desc"),
        )
      )
      .shards(1)
      .replicas(1)
    }.await
    if( res.isError ) {
      log.error("Error create elastic search index",  res.error)
    }
    client
  }
}



