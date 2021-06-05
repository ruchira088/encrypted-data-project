import sbt._

object Dependencies
{
  val ScalaVersion = "2.13.6"

  lazy val kindProjector = "org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full

  lazy val scalaTypedHoles = "com.github.cb372" % "scala-typed-holes" % "0.1.9" cross CrossVersion.full

  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "2.5.1"

  lazy val doobieCore = "org.tpolecat" %% "doobie-core" % "0.13.4"

  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % "0.13.4"

  lazy val jodaTime = "joda-time" % "joda-time" % "2.10.10"

  lazy val faker = "com.github.javafaker" % "javafaker" % "1.0.2"

  lazy val h2 = "com.h2database" % "h2" % "1.4.200"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.2.20"

  lazy val pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.15.0"

  lazy val flyway = "org.flywaydb" % "flyway-core" % "7.9.2"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9"

  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
}