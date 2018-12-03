package com.pako.store.price.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.spi.persistence.{InMemoryOffsetStore, OffsetStore}
import com.pako.store.price.api.PriceService
import com.softwaremill.macwire._

class PriceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new PriceApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new PriceApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[PriceService])
}

abstract class PriceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[PriceService](wire[PriceServiceImpl])

  override def offsetStore: OffsetStore = new InMemoryOffsetStore()


}
