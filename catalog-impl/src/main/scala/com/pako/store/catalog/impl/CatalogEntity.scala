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
        ctx.thenPersist(ProductEvent(product, false)) {
          evt => ctx.reply(Done)
        }
      }
      case (RemoveProduct(id), ctx, state) if(state.isDefined) => {
        ctx.thenPersist(ProductEvent(state.get.product, true)) {
          evt => ctx.reply(Done)
        }
      }
    }
    .onReadOnlyCommand[GetProduct, api.CatalogProduct] {
      case (GetProduct(id), ctx, state) => {
        ctx.reply(state.get.product)
      }
    }
    .onEvent {
      case (ProductEvent(product, removed), state) if(removed == false) => {
        Some(ProductState(product))
      }
    }
}

sealed trait ProductCommand[R] extends ReplyType[R]

case class AddProduct(p: api.CatalogProduct) extends ProductCommand[Done]
case class GetProduct(id: String) extends ProductCommand[api.CatalogProduct]
case class RemoveProduct(id: String) extends ProductCommand[api.CatalogProduct]

case class ProductEvent(p: api.CatalogProduct, removed: Boolean) extends AggregateEvent[ProductEvent] {
  override def aggregateTag = ProductEvent.Tag
}

object ProductEvent {
  implicit val format = Json.format[ProductEvent]
  val Tag = AggregateEventTag[ProductEvent]
}

case class ProductState(product: api.CatalogProduct) {
}

object StoreSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[ProductEvent]
  )
}
