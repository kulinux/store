import org.apache.commons.io.FileUtils

organization in ThisBuild := "com.pako"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val cleanKafkaTemp = taskKey[Unit]("Delete Kafka Temporary")
cleanKafkaTemp := {
  val file = "target/lagom-dynamic-projects/"
  val toDelete = new java.io.File( file )
  val res = FileUtils.deleteDirectory(toDelete)
  println(s"Delete dir ${file} - ${res}!")
}


lagomUnmanagedServices in ThisBuild := Map("LegacyProduct" -> "http://localhost:1337/products")

lazy val `store` = (project in file("."))
  .aggregate(
    `catalog-api`,
    `catalog-impl`,
    `price-api`,
    `price-impl`,
    `customer-api`,
    `customer-impl`,
    `search-api`,
    `search-impl`,
    `www`
  ).settings(
    (ThisBuild / runAll) := ((ThisBuild / runAll) dependsOn cleanKafkaTemp).value
  )



lazy val `catalog-api` = (project in file("catalog-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `catalog-impl` = (project in file("catalog-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`catalog-api`)


lazy val `customer-api` = (project in file("customer-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `customer-impl` = (project in file("customer-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`customer-api`, `catalog-api`)


lazy val `price-api` = (project in file("price-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  ) .dependsOn(`customer-api`, `catalog-api`)

lazy val `price-impl` = (project in file("price-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`price-api`, `catalog-api`, `customer-api`)


lazy val elastic4sVersion = "6.4.0"
lazy val elasticDep = Seq(

  "org.apache.logging.log4j" % "log4j-core" % "2.9.1",
  "org.apache.logging.log4j" % "log4j-api" % "2.9.1",

  "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,

  // for the http client
  "com.sksamuel.elastic4s" %% "elastic4s-http" % elastic4sVersion,

  // if you want to use reactive streams
  "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % elastic4sVersion,

  // testing
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion,
  "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elastic4sVersion
)

cleanFiles += new java.io.File("/tmp/elastic" )

lazy val `search-api` = (project in file("search-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `search-impl` = (project in file("search-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    ) ++ elasticDep
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`search-api`, `catalog-api`)


lazy val `www` = (project in file("www"))
  .enablePlugins(PlayScala, LagomPlay)
  .dependsOn(`catalog-api`, `customer-api`, `price-api`, `search-api`)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      macwire,
      scalaTest,

      "org.ocpsoft.prettytime" % "prettytime" % "3.2.7.Final",
      "org.webjars" % "foundation" % "6.2.3",
      "org.webjars" % "foundation-icon-fonts" % "d596a3cfb3"
    )
  )
  .settings(lagomServicePort := 80)
  .enablePlugins(LagomScala)

