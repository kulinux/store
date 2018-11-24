package com.pako.store.catalog.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.pako.store.catalog.api.{CatalogService, ProductEventChanged, SearchService}
import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class SearchServiceImpl(catalogService: CatalogService,
                        elastic: ElasticClient
                       )(implicit ec: ExecutionContext)
  extends SearchService {

  private final val log: Logger =
    LoggerFactory.getLogger(classOf[SearchApplication])

  private val productEvent = catalogService.productTopic

  productEvent.subscribe.withGroupId("search-service")
      .atLeastOnce(Flow[ProductEventChanged].map(toDocument).collect{ case Some(x) => x }.mapAsync(1)(store))

  def toDocument(event: ProductEventChanged): Option[ProductEventChanged] = {
    Some(event)
  }

  def store(event: ProductEventChanged) : Future[Done]  = {
    println(s"Store product $event.product")
    val res = elastic.execute{
      indexInto("product" / "product").fields(
        "id" -> event.product.id,
        "name" -> event.product.name,
        "desc" -> event.product.desc
      ).refresh(RefreshPolicy.Immediate)
    }.await

    if(res.isError) {
      log.error("Resultado Indexacion", res.error)
    }

    Future.successful(Done)
  }



}
