package com.pako.store.price.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.pako.store.catalog.api.CatalogProduct
import com.pako.store.customer.api.Customer
import play.api.libs.json.{Format, Json}


/**
  * The store service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the CatalogService.
  */
trait PriceService extends Service {

  def naivePrice: ServiceCall[CatalogProduct, Price]

  def price: ServiceCall[CatalogProduct, Price]

  def priceCustomer(customer: String): ServiceCall[CatalogProduct, Price]

  def priceCart(customer: String): ServiceCall[Seq[CatalogProduct], Price]


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("price")
      .withCalls(
      )
      .withTopics(
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

case class Price(product: Seq[CatalogProduct], customer: Option[Customer], qty: Double)


object Price {
  implicit val formatSeq: Format[CatalogProduct] = Json.format[CatalogProduct]
  implicit val format: Format[Price] = Json.format[Price]
}

