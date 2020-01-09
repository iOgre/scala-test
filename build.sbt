import Dependencies._

ThisBuild / scalaVersion := "2.13.0"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "wire",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0",
    libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC17",
    libraryDependencies += "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10",

  )
