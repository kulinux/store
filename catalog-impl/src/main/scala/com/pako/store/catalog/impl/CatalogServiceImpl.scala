package com.pako.store.catalog.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.pako.store.catalog.api
import com.pako.store.catalog.api.CatalogService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Implementation of the StoreService.
  */
class CatalogServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) (implicit ec: ExecutionContext)
extends CatalogService {
  override def storeProduct(): ServiceCall[api.Product, NotUsed] = ServiceCall { product =>
    val ref = persistentEntityRegistry.refFor[CatalogEntity](product.id)
    ref.ask(AddProduct(product))
    Future.successful(NotUsed)
  }

  override def getProduct(id: String): ServiceCall[NotUsed, api.Product] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[CatalogEntity](id)
    ref.ask(GetProduct(id)).map(p => p)
  }

}
