package com.pako.store.catalog.impl

import java.net.URI

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{ServiceCall, ServiceLocator}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.pako.store.catalog.api
import com.pako.store.catalog.api.{CatalogProduct, CatalogService, ProductEventChanged}
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class CatalogServiceImpl(
                          persistentEntityRegistry: PersistentEntityRegistry,
                          serviceLocator: ServiceLocator,
                          ws: WSClient
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

  init()

  case class LegacyProduct(_id: String, name: String, description: String, price: Double)
  object LegacyProduct {
    implicit val format = Json.reads[LegacyProduct]
  }

  def fetchAllProduct(uri: URI): Unit = {
    ws.url(uri.toString)
      .get()
      .map{ response =>
        val json = response.json.as[JsArray]
        response.json.validate[Seq[LegacyProduct]]
      }
      .map( lps =>
        lps.get.foreach{ lp =>
          val cp = CatalogProduct(lp._id, lp.name, lp.description, lp.price)
          storeProduct().invoke(cp)
        }
      )
  }

  def init(): Unit = {
    serviceLocator.locate( "LegacyProduct" )
      .filter(_.isDefined)
      .map(_.get)
      .onComplete{
        case Success(value) => fetchAllProduct(value)
        case err => println(s"Encontrado Error ${err}")
      }
  }
}
