import Dependencies._

lazy val root =
  (project in file("."))
    .enablePlugins(BuildInfoPlugin, JavaAppPackaging)
    .settings(
      name := "encrypted-data-project",
      organization := "com.ruchij",
      scalaVersion := Dependencies.ScalaVersion,
      version := "0.0.1",
      maintainer := "me@ruchij.com",
      libraryDependencies ++= rootDependencies ++ rootTestDependencies.map(_ % Test),
      buildInfoKeys := Seq[BuildInfoKey](name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "com.eed3si9n.ruchij",
      topLevelDirectory := None,
      scalacOptions ++= Seq("-Xlint", "-feature", "-Wconf:cat=lint-byname-implicit:s"),
      addCompilerPlugin(kindProjector),
      addCompilerPlugin(scalaTypedHoles)
    )

lazy val rootDependencies =
  Seq(catsEffect, jodaTime, doobieCore, faker, h2, postgresql)

lazy val rootTestDependencies =
  Seq(scalaTest, pegdown)

addCommandAlias("testWithCoverage", "; coverage; test; coverageReport")
