package com.pako.store.search.impl

import akka.{Done, NotUsed}
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.pako.store.catalog.api.{CatalogService, ProductEventChanged, SearchResult, SearchService}
import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.searches.SearchRequest
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

class SearchServiceImpl(catalogService: CatalogService,
                        elastic: ElasticClient
                       )(implicit ec: ExecutionContext)
  extends SearchService {


  private final val log: Logger =
    LoggerFactory.getLogger(classOf[SearchApplication])

  override def searchProduct(q: String) : ServiceCall[NotUsed, SearchResult] = {
    ServiceCall { _ => elasticSearch(
      search("products").query(q)
    ) }
  }

  override def searchByTag(tag: String): ServiceCall[NotUsed, SearchResult] = {
    ServiceCall { _ =>
      elasticSearch(
        search("products" ).termQuery("tags", tag)
      ) }
  }


  private val productEvent = catalogService.productTopic

  productEvent.subscribe.withGroupId("search-service")
      .atLeastOnce(Flow[ProductEventChanged].map(toDocument).collect{ case Some(x) => x }.mapAsync(1)(store))



  private def elasticSearch( searchRequest: SearchRequest ): Future[SearchResult] = {
    elastic.execute(
      searchRequest
    )
      .filter(_.isSuccess)
      .map(_.result)
      .map(sr => sr.hits.hits.map(_.sourceAsMap("id").toString))
      .map(res => SearchResult(res.toSeq))
  }

  private def toDocument(event: ProductEventChanged): Option[ProductEventChanged] = {
    Some(event)
  }

  private def store(event: ProductEventChanged) : Future[Done]  = {
    println(s"Store product $event.product")
    val res = elastic.execute{
      indexInto("products" / "product").fields(
        "id" -> event.product.id,
        "name" -> event.product.name,
        "desc" -> event.product.desc,
        "tags" -> event.product.tags
      ).refresh(RefreshPolicy.Immediate)
    }
    .filter(_.isSuccess)
    .flatMap(_ => Future.successful(Done))

    res.onComplete(x => println(s"Tras search $x"))
    res
  }

}
