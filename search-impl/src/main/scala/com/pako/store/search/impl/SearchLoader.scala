package com.pako.store.catalog.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.spi.persistence.OffsetStore
import com.pako.store.catalog.api.SearchService
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

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

  lazy val catalogService = serviceClient.implement[SearchService]

  override def offsetStore: OffsetStore = ???
}



