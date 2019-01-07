package com.pako.store.catalog.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.deser.{MessageSerializer, StrictMessageSerializer}
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer._
import com.lightbend.lagom.scaladsl.api.transport.MessageProtocol
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}
import play.mvc.Http.Response

import scala.collection.immutable


object CatalogService  {
  val TOPIC_NAME = "catalog"
}

/**
  * The store service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the CatalogService.
  */
trait CatalogService extends Service {

  def storeProduct(): ServiceCall[CatalogProduct, NotUsed]

  def getProduct(id: String): ServiceCall[NotUsed, CatalogProduct]

  def removeProduct(id: String): ServiceCall[NotUsed, NotUsed]

  def productTopic: Topic[ProductEventChanged]

  def image: ServiceCall[String, Source[ByteString, NotUsed]]



  override final def descriptor = {
    import Service._



    // @formatter:off
    named("catalog")
      .withCalls(
        pathCall("/api/catalog/products/:id", getProduct _),
        pathCall("/api/catalog/products/", storeProduct )
        /*
        ,pathCall("/api/catalog/image/:image", image _)
        ( StringMessageSerializer,
          MessageSerializer.sourceMessageSerializer(StringMessageSerializer)
        )
        */
      )
      .withTopics(
        topic(CatalogService.TOPIC_NAME, productTopic)
          .addProperty(
          KafkaProperties.partitionKeyStrategy,
          PartitionKeyStrategy[ProductEventChanged](x => x.product.name)
        )
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

case class ProductEventChanged(product: CatalogProduct, removed: Boolean)

object ProductEventChanged {
  implicit val format : Format[ProductEventChanged] = Json.format[ProductEventChanged]
}

case class CatalogProduct(id: String,
                          name: String,
                          desc: String,
                          img: Seq[String],
                          basePrice: Double,
                          tags: Seq[String] = Seq()
                         )

object CatalogProduct {
  implicit val format: Format[CatalogProduct] = Json.format[CatalogProduct]
}




