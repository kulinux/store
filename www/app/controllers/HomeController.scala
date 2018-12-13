package controllers

import com.pako.store.catalog.api.{CatalogService, SearchService}
import javax.inject._
import play.api.libs.json.{Format, Json}
import play.api.mvc._

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


  val dummyProduct = Seq(
    CatalogProduct("1", "name1", "desc1"),
    CatalogProduct("2", "name2", "desc2")
  )

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def products() = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(dummyProduct))
  }
}

case class CatalogProduct(id: String, name: String, desc: String)

object CatalogProduct {
  implicit val format: Format[CatalogProduct] = Json.format[CatalogProduct]
}
