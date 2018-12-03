package com.pako.store.customer.impl

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.pako.store.customer.api.{Customer, CustomerService}

import scala.concurrent.{ExecutionContext, Future}

class CustomerServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext)
  extends CustomerService {

  override def getCustomer(id: String): ServiceCall[NotUsed, Customer] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[CustomerEntity](id)
    ref.ask(GetCustomer(id)).map(p => p)
  }

  override def storeProduct(id: String): ServiceCall[Customer, NotUsed] = ServiceCall { product =>
    val ref = persistentEntityRegistry.refFor[CustomerEntity](product.id)
    ref.ask(AddCustomer(product))
    Future.successful(NotUsed)
  }

}
