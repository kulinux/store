package com.pako.store.customer.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}


object CustomerService  {
  val TOPIC_NAME = "customer"
}

/**
  * The store service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the CatalogService.
  */
trait CustomerService extends Service {


  def getCustomer(id: String): ServiceCall[NotUsed, Customer]

  def storeCustomer(id: String): ServiceCall[Customer, NotUsed]


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("customer")
      .withCalls(
        pathCall("/api/customer/customers/:id", getCustomer _),
      )
      .withTopics(
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

case class Customer( id: String, name: String, desc: String)

object Customer {
  implicit val format: Format[Customer] = Json.format[Customer]
}

