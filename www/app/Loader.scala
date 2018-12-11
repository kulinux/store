package loader

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent, ServiceAcl, ServiceInfo, ServiceLocator}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.pako.store.catalog.api.{CatalogService, SearchService}
import com.pako.store.customer.api.CustomerService
import com.pako.store.price.api.PriceService
import controllers.{AssetsComponents, HomeController}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
import play.filters.HttpFiltersComponents
import com.softwaremill.macwire._
import router.Routes

import scala.collection.immutable
import scala.concurrent.ExecutionContext


class WwwLoader extends ApplicationLoader {


  override def load(context: Context) = context.environment.mode match {
    case Mode.Dev =>
      (new WwwApplication(context) with LagomDevModeComponents).application
    case _ =>
      new WwwApplication(context) with LagomServiceClientComponents {
        override def serviceLocator: ServiceLocator = NoServiceLocator
      }.application
  }
}

abstract class WwwApplication(context: Context) extends BuiltInComponentsFromContext(context)
  with AssetsComponents
  with HttpFiltersComponents
  with AhcWSComponents
  with LagomConfigComponent
  with LagomServiceClientComponents {

  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
    "www",
    Map(
      "www" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
    )
  )
  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher



  lazy val router = {
    val prefix: String = "/"
    wire[Routes]
  }


  lazy val home = wire[HomeController]


  lazy val customerService = serviceClient.implement[CustomerService]
  lazy val catalogService = serviceClient.implement[CatalogService]
  lazy val priceService = serviceClient.implement[PriceService]
  lazy val searchService = serviceClient.implement[SearchService]

}


