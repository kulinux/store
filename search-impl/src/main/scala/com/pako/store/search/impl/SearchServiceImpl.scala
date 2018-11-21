package com.pako.store.catalog.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.pako.store.catalog.api.{CatalogService, ProductEventChanged, SearchService}

import scala.concurrent.{ExecutionContext, Future}

class SearchServiceImpl(catalogService: CatalogService)(implicit ec: ExecutionContext)
  extends SearchService {

  private val productEvent = catalogService.productTopic

  productEvent.subscribe.withGroupId("search-service")
      .atLeastOnce(Flow[ProductEventChanged].map(toDocument).collect{ case Some(x) => x }.mapAsync(1)(store))

  def toDocument(event: ProductEventChanged): Option[ProductEventChanged] = {
    Some(event)
  }

  def store(pe: ProductEventChanged) : Future[Done]  = {
    println(s"Store product $pe")
    Future.successful(Done)
  }



}
