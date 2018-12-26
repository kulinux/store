package com.pako.store.catalog.impl

import java.net.URI

import com.pako.store.catalog.api.CatalogProduct
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext


case class LegacyProduct(_id: String, name: String, description: String, price: Double)
object LegacyProduct {
  implicit val format = Json.reads[LegacyProduct]
}


class LegacyFetcher( ws: WSClient,
                     uri: URI,
                     storeProduct: CatalogProduct => Unit
                   )
                   (implicit val executionContext: ExecutionContext) {



  private final val log: Logger =
    LoggerFactory.getLogger(classOf[LegacyFetcher])



  def fetchAllProduct(): Unit = {
    ws.url(uri.toString)
      .get()
      .map{ response =>
        response.json.validate[Seq[LegacyProduct]]
      }
      .map( lps =>
        lps.get.foreach{ lp =>
          log.info(s"One product from legacy to store $lp")
          val cp = CatalogProduct(lp._id, lp.name, lp.description, lp.price)
          storeProduct(cp)
        }
      )
  }



}
