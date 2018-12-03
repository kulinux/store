package com.pako.store.price.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.pako.store.customer.api.Customer
import com.pako.store.price.api.{Price, PriceService}
import com.pako.store.catalog.api.CatalogProduct

import scala.concurrent.ExecutionContext

class PriceServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext)
  extends PriceService {

  override def naivePrice(product: CatalogProduct): ServiceCall[NotUsed, Price] = ???

  override def price(product: CatalogProduct): ServiceCall[NotUsed, Price] = ???

  override def price(customer: Customer, product: CatalogProduct): ServiceCall[NotUsed, Price] = ???

  override def price(customer: Customer, product: Seq[CatalogProduct]): ServiceCall[NotUsed, Price] = ???
}
