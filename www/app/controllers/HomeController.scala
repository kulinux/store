package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.pako.store.catalog.api.{CatalogProduct, CatalogService, SearchService}
import javax.inject._
import play.api.http.HttpEntity
import play.api.libs.json.{Format, Json}
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()
  (
    cc: ControllerComponents,
    catalogService: CatalogService,
    searchService: SearchService,
  ) extends AbstractController(cc) {


  implicit lazy val executionContext = defaultExecutionContext


  val dummyProduct = Seq(
    CatalogProductJson("1", "name1", "desc1", 200.2, Seq()),
    CatalogProductJson("2", "name2", "desc2", 100.0, Seq())
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def image(image: String) = Action.async {
      catalogService.image.invoke(image)
      .map(stream =>
        HttpEntity.Streamed(
          stream,
          None,
          Some("image/jpeg")
        )
      )
      .map( body =>
        Result (
          ResponseHeader(200, Map.empty),
          body
        )
      )
  }

  def products() = Action.async{
    val sr = searchService.searchByTag("HOME").invoke()
    //val sr = searchService.searchProduct("Jamon").invoke()

    val contentProduct : Future[Seq[CatalogProductJson]] =
      sr.map( x => {
        val invokes: Seq[Future[CatalogProduct]] = x.ids.map(catalogService.getProduct(_).invoke())
        val all : Future[Seq[CatalogProduct]] = Future.sequence(invokes)
        all
        }
      ).map( Await.result(_, 1 second )) //Avoid this!!!
      .map( x => x.map( cp => {
        CatalogProductJson(cp.id, cp.name, cp.desc, cp.basePrice, cp.img)
      }) )

    contentProduct.map( jsons => Ok(Json.toJson(jsons)))
  }
}

case class CatalogProductJson(id: String, name: String, description: String, price: Double, images: Seq[String])

object CatalogProductJson {
  implicit val format: Format[CatalogProductJson] = Json.format[CatalogProductJson]
}
