import Dependencies._

inThisBuild {
  Seq(
    organization := "com.ruchij",
    scalaVersion := Dependencies.ScalaVersion,
    version := "0.0.1",
    maintainer := "me@ruchij.com",
    scalacOptions ++= Seq("-Xlint", "-feature", "-Wconf:cat=lint-byname-implicit:s"),
    addCompilerPlugin(kindProjector),
    addCompilerPlugin(scalaTypedHoles)
  )
}

lazy val root =
  (project in file("."))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "encrypted-data-project",
      libraryDependencies ++= rootDependencies ++ rootTestDependencies.map(_ % Test),
      buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "com.eed3si9n.ruchij",
      topLevelDirectory := None
    )
    .dependsOn(migration)

lazy val migration =
  (project in file("./migration"))
    .settings(
      name := "migration-application",
      libraryDependencies ++= Seq(catsEffect, flyway, h2, postgresql, pureconfig, logback)
    )

lazy val rootDependencies =
  Seq(catsEffect, jodaTime, doobieCore, doobieHikari, faker, h2, postgresql, pureconfig, logback, log4cats)

lazy val rootTestDependencies =
  Seq(scalaTest, pegdown)

addCommandAlias("testWithCoverage", "; coverage; test; coverageReport")
