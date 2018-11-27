package com.pako.store.search.impl

import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.slf4j.{Logger, LoggerFactory}

class ElasticLocalNode

object ElasticLocalNode {

  private final val log: Logger =
    LoggerFactory.getLogger(classOf[ElasticLocalNode])


  System.setProperty("es.set.netty.runtime.available.processors", "false")
  val node = LocalNode("tmp", "/tmp/elastic")
  val client = {
    val client = node.client(shutdownNodeOnClose = true)
    val res = client.execute {
      createIndex("products").mappings(
        mapping("product").fields(
          keywordField("id"),
          textField("name"),
          textField("desc"),
        )
      )
    }.await
    if( res.isError ) {
      log.error("Error create elastic search index",  res.error)
    }
    client
  }
}



