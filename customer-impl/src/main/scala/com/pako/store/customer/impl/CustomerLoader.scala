package com.pako.store.customer.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.pako.store.customer.api.CustomerService
import com.softwaremill.macwire._

class CustomerLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CustomerApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CustomerApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[CustomerService])
}

abstract class CustomerApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[CustomerService](wire[CustomerServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = CustomerSerializerRegistry

  // Register the store persistent entity
  persistentEntityRegistry.register(wire[CustomerEntity])
}
