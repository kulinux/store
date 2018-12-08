package com.pako.store.customer.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.pako.store.customer.api.Customer
import play.api.libs.json.Json

import scala.collection.immutable.Seq

class CustomerEntity extends PersistentEntity {
  override type Command = CustomerCommand[_]
  override type Event = CustomerEvent
  override type State = Option[CustomerState]

  override def initialState = Option.empty

  override def behavior: Behavior = Actions()
    .onCommand[AddCustomer, Done] {
      case (AddCustomer(product), ctx, state) => {
        ctx.thenPersist(CustomerEvent(product)) {
          evt => ctx.reply(Done)
        }
      }
    }
    .onReadOnlyCommand[GetCustomer, Customer] {
      case (GetCustomer(id), ctx, state) => {
        ctx.reply(state.get.product)
      }
    }
    .onEvent {
      case (CustomerEvent(product), state) => {
        Some(CustomerState(product))
      }
    }
}

sealed trait CustomerCommand[R] extends ReplyType[R]

case class AddCustomer(p: Customer) extends CustomerCommand[Done]
case class GetCustomer(id: String) extends CustomerCommand[Customer]
case class CustomerEvent(p: Customer) extends AggregateEvent[CustomerEvent] {
  override def aggregateTag = CustomerEvent.Tag
}

object CustomerEvent {
  implicit val format = Json.format[CustomerEvent]
  val Tag = AggregateEventTag[CustomerEvent]
}

case class CustomerState(product: Customer) {
}

object CustomerSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[CustomerEvent]
  )
}
