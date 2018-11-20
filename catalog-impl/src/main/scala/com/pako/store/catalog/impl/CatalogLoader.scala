package com.pako.store.catalog.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.pako.store.catalog.api.{CatalogService}
import com.softwaremill.macwire._

class CatalogLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CatalogApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CatalogApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[CatalogService])
}

abstract class CatalogApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[CatalogService](wire[CatalogServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = StoreSerializerRegistry

  // Register the store persistent entity
  persistentEntityRegistry.register(wire[CatalogEntity])
}
