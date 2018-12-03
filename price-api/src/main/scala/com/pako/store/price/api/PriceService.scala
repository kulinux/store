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

  def naivePrice(product: CatalogProduct): ServiceCall[NotUsed, Price]

  def price(product: CatalogProduct): ServiceCall[NotUsed, Price]

  def price(customer: Customer, product: CatalogProduct): ServiceCall[NotUsed, Price]

  def price(customer: Customer, product: Seq[CatalogProduct]): ServiceCall[NotUsed, Price]


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("catalog")
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
  implicit val formatSeq: Format[Seq[CatalogProduct]] = Json.format[Seq[CatalogProduct]]
  implicit val format: Format[Price] = Json.format[Price]
}

