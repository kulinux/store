package com.pako.store.catalog.api

import com.lightbend.lagom.scaladsl.api.Service


trait SearchService extends Service {


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("search")
      .withAutoAcl(true)
    // @formatter:on
  }
}


