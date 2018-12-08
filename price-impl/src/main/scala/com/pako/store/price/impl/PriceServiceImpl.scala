package com.pako.store.price.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.pako.store.catalog.api.CatalogProduct
import com.pako.store.customer.api.CustomerService
import com.pako.store.price.api.{Price, PriceService}

import scala.concurrent.{ExecutionContext, Future}

class PriceServiceImpl(customerService: CustomerService)(implicit ec: ExecutionContext)
  extends PriceService {

  override def naivePrice: ServiceCall[CatalogProduct, Price] =
    ServiceCall( product =>
      Future.successful(Price(Seq(product), None, product.basePrice))
    )

  override def price: ServiceCall[CatalogProduct, Price] = naivePrice

  override def priceCustomer(customer: String): ServiceCall[CatalogProduct, Price] =
    ServiceCall( product =>
      customerService.getCustomer(customer)
          .invoke()
          .map( cust => Price(Seq(product), Some(cust), product.basePrice))
    )


  override def priceCart(customer: String): ServiceCall[Seq[CatalogProduct], Price] =
    ServiceCall( products =>
      customerService.getCustomer(customer)
        .invoke()
        .map( cust => {
            val price = products.foldLeft(0.0)( (total, nextP) => total + nextP.basePrice )
            Price(products, Some(cust), price)
          }
        )
    )
}
