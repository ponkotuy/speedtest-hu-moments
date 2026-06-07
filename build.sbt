// SPDX-License-Identifier: Apache-2.0

import scala.util.Try

ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "local.speedtest"
ThisBuild / licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

def javaFeatureVersion: Int = {
  val version = sys.props.getOrElse("java.specification.version", "8")
  val feature = if (version.startsWith("1.")) version.drop(2) else version
  Try(feature.takeWhile(_.isDigit).toInt).getOrElse(8)
}

val warningSuppressingJavaOptions =
  if (javaFeatureVersion >= 24) {
    Seq(
      "--enable-native-access=ALL-UNNAMED",
      "--sun-misc-unsafe-memory-access=allow"
    )
  } else {
    Seq.empty
  }

lazy val root = (project in file("."))
  .enablePlugins(JmhPlugin)
  .settings(
    name := "speedtest-hu-moments",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked"
    ),
    libraryDependencies ++= Seq(
      "org.openpnp" % "opencv" % "4.9.0-0",
      "org.scalameta" %% "munit" % "1.1.1" % Test
    ),
    Compile / run / fork := true,
    Compile / run / javaOptions ++= warningSuppressingJavaOptions,
    Test / fork := true,
    Test / javaOptions ++= warningSuppressingJavaOptions,
    Jmh / javaOptions ++= warningSuppressingJavaOptions
  )
