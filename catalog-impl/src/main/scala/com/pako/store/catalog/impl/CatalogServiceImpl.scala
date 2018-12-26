package com.pako.store.catalog.impl


import akka.NotUsed
import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.api.{ServiceCall, ServiceLocator}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.pako.store.catalog.api
import com.pako.store.catalog.api.{CatalogService, ProductEventChanged}
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class CatalogServiceImpl(
                          persistentEntityRegistry: PersistentEntityRegistry,
                          serviceLocator: ServiceLocator,
                          ws: WSClient,
                          system: ActorSystem
  )(implicit ec: ExecutionContext)
  extends CatalogService {

  override def storeProduct(): ServiceCall[api.CatalogProduct, NotUsed] = ServiceCall { product =>
    val ref = persistentEntityRegistry.refFor[CatalogEntity](product.id)
    ref.ask(AddProduct(product))
    Future.successful(NotUsed)
  }

  override def getProduct(id: String): ServiceCall[NotUsed, api.CatalogProduct] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[CatalogEntity](id)
    ref.ask(GetProduct(id)).map(p => p)
  }

  override def productTopic: Topic[ProductEventChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset => {
        println(s"Produce Topic from $fromOffset")
        persistentEntityRegistry.eventStream(ProductEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
      }
    }

  private def convertEvent(helloEvent: EventStreamElement[ProductEvent]): api.ProductEventChanged= {
    helloEvent.event match {
      case ProductEvent(msg) => {
        println(s"Encontre un evento!!! $msg")
        api.ProductEventChanged(msg)
      }
    }
  }

  val fetcher: LegacyFetcher = init()

  schedule()

  def init() = {
    val lFut = serviceLocator.locate( "LegacyProduct" )
      .filter(_.isDefined)
      .map(_.get)
      .map{ value =>
          new LegacyFetcher(ws, value, storeProduct().invoke(_))
      }

    Await.result(lFut, 1 second )

  }

  def schedule(): Unit = {
    system.scheduler.schedule(5 seconds, 5 seconds) {
      fetcher.fetchAllProduct()
    }
  }


}
