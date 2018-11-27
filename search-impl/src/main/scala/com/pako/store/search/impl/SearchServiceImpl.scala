package com.pako.store.catalog.impl

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.pako.store.catalog.api.{CatalogService, ProductEventChanged, SearchResult, SearchService}
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


  override def search(q: String) : ServiceCall[NotUsed, SearchResult] = {

    val sr: Future[SearchResult] = elastic.execute(
      searchWithType("products" / "product").query(q)
    ).filter(_.isSuccess)
    .map(_.result)
    .map(sr => sr.hits.hits.map(_.fields("id").toString))
    .map(res => SearchResult(res.toSeq))

    ServiceCall { q => sr }
  }

  private val productEvent = catalogService.productTopic

  productEvent.subscribe.withGroupId("search-service")
      .atLeastOnce(Flow[ProductEventChanged].map(toDocument).collect{ case Some(x) => x }.mapAsync(1)(store))

  def toDocument(event: ProductEventChanged): Option[ProductEventChanged] = {
    Some(event)
  }

  def store(event: ProductEventChanged) : Future[Done]  = {
    println(s"Store product $event.product")
    elastic.execute{
      indexInto("products" / "product").fields(
        "id" -> event.product.id,
        "name" -> event.product.name,
        "desc" -> event.product.desc
      ).refresh(RefreshPolicy.Immediate)
    }
    .filter(_.isSuccess)
    .flatMap(_ => Future.successful(Done))
  }

}
