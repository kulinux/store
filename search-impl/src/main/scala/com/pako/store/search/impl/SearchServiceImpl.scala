package com.pako.store.catalog.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import com.pako.store.catalog.api
import com.pako.store.catalog.api.{CatalogService, ProductEventChanged, SearchService}

import scala.concurrent.{ExecutionContext, Future}

class SearchServiceImpl()(implicit ec: ExecutionContext)
  extends SearchService {

}
