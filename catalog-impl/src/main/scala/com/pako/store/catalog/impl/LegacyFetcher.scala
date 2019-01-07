package com.pako.store.catalog.impl

import java.net.URI

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.pako.store.catalog.api.CatalogProduct
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._


case class ImgLegacyProduct(id: String, hash: String, ext: String)
case class LegacyProduct(_id: String, name: String, description: String, price: Double, tag: String, image: Seq[ImgLegacyProduct] )

object ImgLegacyProduct {
  implicit val format = Json.reads[ImgLegacyProduct]
}
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


  def image(img: String): Source[ByteString, NotUsed] = {
    val resFut = ws.url(s"/upload/$img")
      .withMethod("GET")
      .stream()


    val stream = Await.result(resFut, 1 second )

    val res : Source[ByteString, NotUsed]
      = stream.bodyAsSource
        .asInstanceOf[Source[ByteString, NotUsed]]

    res
  }



  def fetchAllProduct(): Unit = {
    ws.url(uri.toString)
      .get()
      .map{ response =>
        response.json.validate[Seq[LegacyProduct]]
      }
      .map( lps =>
        lps.get.foreach{ lp =>
          val imgs = for{
            im <- lp.image
          } yield im.hash + "." + im.ext

          val cp = CatalogProduct(lp._id, lp.name, lp.description, imgs, lp.price, Seq(lp.tag))
          storeProduct(cp)
        }
      )
  }



}
