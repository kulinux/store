package com.pako.store.catalog.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.spi.persistence.{InMemoryOffsetStore, OffsetStore}
import com.pako.store.catalog.api.{CatalogService, SearchService}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.ElasticDsl._

import org.slf4j.{ Logger, LoggerFactory }

class SearchLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SearchApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SearchApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SearchService])
}

abstract class SearchApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[SearchService](wire[SearchServiceImpl])

  lazy val catalogService = serviceClient.implement[CatalogService]

  lazy val elastic = ElasticLocalNode.client

  override def offsetStore: OffsetStore = new InMemoryOffsetStore()
}

object ElasticLocalNode {

  private final val log: Logger =
    LoggerFactory.getLogger(classOf[SearchApplication])


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



