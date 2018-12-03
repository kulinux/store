package com.pako.store.catalog.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}



trait SearchService extends Service {

  def searchProduct(q: String) : ServiceCall[NotUsed, SearchResult]


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("search")
      .withCalls(
        pathCall("/api/search/products/:q", searchProduct _),
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}


case class SearchResult(ids: Seq[String])


object SearchResult {
  implicit val format: Format[SearchResult] = Json.format[SearchResult]
}

