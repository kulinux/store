package com.pako.store.catalog.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.pako.store.catalog.api
import play.api.libs.json.Json

import scala.collection.immutable.Seq

class CatalogEntity extends PersistentEntity {
  override type Command = ProductCommand[_]
  override type Event = ProductEvent
  override type State = Option[ProductState]

  override def initialState = Option.empty

  override def behavior: Behavior = Actions()
    .onCommand[AddProduct, Done] {
      case (AddProduct(product), ctx, state) => {
        ctx.thenPersist(ProductEvent(product)) {
          evt => ctx.reply(Done)
        }
      }
    }
    .onReadOnlyCommand[GetProduct, api.Product] {
      case (GetProduct(id), ctx, state) => {
        ctx.reply(state.get.product)
      }
    }
    .onEvent {
      case (ProductEvent(product), state) => {
        Some(ProductState(product))
      }
    }
}

sealed trait ProductCommand[R] extends ReplyType[R]

case class AddProduct(p: api.Product) extends ProductCommand[Done]
case class GetProduct(id: String) extends ProductCommand[api.Product]
case class ProductEvent(p: api.Product) extends AggregateEvent[ProductEvent] {
  override def aggregateTag = ProductEvent.Tag
}

object ProductEvent {
  implicit val format = Json.format[ProductEvent]
  val Tag = AggregateEventTag[ProductEvent]
}

case class ProductState(product: api.Product) {
}

object StoreSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[ProductEvent]
  )
}
