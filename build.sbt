// ----------------------------------------------------------------------------
// sbt plugins
// ----------------------------------------------------------------------------

import sbtcrossproject.CrossPlugin.autoImport.crossProject

enablePlugins(GitVersioning)

// ----------------------------------------------------------------------------
// basic project settings
// ----------------------------------------------------------------------------

organization in ThisBuild := "com.github.wookietreiber"

git.baseVersion in ThisBuild := "0.3.1"

// ----------------------------------------------------------------------------
// scala compiler options
// ----------------------------------------------------------------------------

scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked"
)

scalacOptions in ThisBuild ++= Seq(scalaBinaryVersion.value match {
  case v if v.startsWith("2.12") => "-target:jvm-1.8"
  case _                         => "-target:jvm-1.7"
})

// ----------------------------------------------------------------------------
// base settings
// ----------------------------------------------------------------------------

val scala210 = "2.10.7"
val scala211 = "2.11.12"
val scala212 = "2.12.6"

lazy val baseSettings = Seq(
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala210, scala211, scala212),
  scalacOptions in (Compile, doc) ++= {
    val tree = if (version.value == git.baseVersion.value) {
      s"v${version.value}"
    } else {
      "master"
    }

    Seq(
      "-doc-title", "scala-cli-tools",
      "-doc-version", version.value,
      "-sourcepath", (baseDirectory in ThisBuild).value.toString,
      "-doc-source-url",
      s"https://github.com/wookietreiber/scala-cli-tools/tree/${tree}€{FILE_PATH}.scala",
      "-diagrams",
      "-groups",
      "-implicits"
    )
  },
  autoAPIMappings := true,
  apiURL := Some(url(
    "https://www.javadoc.io/doc/%s/%s_%s/%s".format(
      organization.value,
      name.value,
      scalaBinaryVersion,
      version.value
    )
  )),
  scalastyleConfig := file(".scalastyle-config.xml"),
  wartremoverErrors in (Compile, compile) ++= Seq(
    Wart.ArrayEquals,
    Wart.FinalCaseClass,
    Wart.OptionPartial,
    Wart.TraversableOps,
    Wart.TryPartial
  ),
  description := "Scala Command-Line Interface Tools",
  homepage := Some(url("https://github.com/wookietreiber/scala-cli-tools")),
  startYear := Some(2017),
  scmInfo := Some(ScmInfo(
    url("https://github.com/wookietreiber/scala-cli-tools"),
    "scm:git:git://github.com/wookietreiber/scala-cli-tools.git",
    Some("scm:git:https://github.com/wookietreiber/scala-cli-tools.git")
  )),
  licenses := Seq(
    "BSD 3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")
  ),
  developers += Developer(
    email = "christian.krause@mailbox.org",
    id = "wookietreiber",
    name = "Christian Krause",
    url = url("https://github.com/wookietreiber")
  ),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false
)

lazy val noPublish = Seq(
  publishArtifact := false,
  publish := {},
  publishLocal := {}
)

// ----------------------------------------------------------------------------
// projects
// ----------------------------------------------------------------------------

lazy val core = crossProject(JVMPlatform, NativePlatform)
  .settings(
    name := "scala-cli-tools",
    baseSettings,
    scalacOptions in (Compile, doc) ++= Seq(
      "-doc-root-content", "core/rootdoc.txt"
    ),
    libraryDependencies ++= Seq(
      "com.chuusai" %%% "shapeless" % "2.3.3",
      "com.lihaoyi" %%% "utest" % "0.6.4" % "test"
    ),
    testFrameworks := Seq(new TestFramework("utest.runner.Framework"))
  )
  .nativeSettings(
    scalaVersion := scala211,
    crossScalaVersions := Seq(scala211),
    nativeLinkStubs := true
  )

lazy val coreJVM = core.jvm
lazy val coreNative = core.native

lazy val appSettings = Seq(
  scalaVersion := scala211,
  crossScalaVersions := Seq(scala211),
  libraryDependencies += "com.github.scopt" %%% "scopt" % "3.7.0",
  buildInfoKeys := Seq[BuildInfoKey](name, version),
  buildInfoPackage := "scalax.cli",
  nativeMode := sys.env.getOrElse("NATIVE_MODE", "debug")
)

lazy val dehumanize = project
  .enablePlugins(BuildInfoPlugin, ScalaNativePlugin)
  .dependsOn(coreNative)
  .settings(
    baseSettings,
    name := "dehumanize",
    appSettings,
    noPublish
  )

lazy val humanize = project
  .enablePlugins(BuildInfoPlugin, ScalaNativePlugin)
  .dependsOn(coreNative)
  .settings(
    baseSettings,
    name := "humanize",
    appSettings,
    noPublish
  )

lazy val highlight = project
  .enablePlugins(BuildInfoPlugin, ScalaNativePlugin)
  .dependsOn(coreNative)
  .settings(
    baseSettings,
    name := "highlight",
    appSettings,
    noPublish
  )

lazy val meansd = project
  .enablePlugins(BuildInfoPlugin, ScalaNativePlugin)
  .dependsOn(coreNative)
  .settings(
    baseSettings,
    name := "meansd",
    appSettings,
    noPublish
  )

// ----------------------------------------------------------------------------
// install
// ----------------------------------------------------------------------------

val prefix = settingKey[String]("Installation prefix.")

val install = taskKey[Unit]("Install to prefix.")

lazy val root = (project in file("."))
  .aggregate(
    coreJVM,
    coreNative,
    dehumanize,
    humanize,
    highlight,
    meansd
  )
  .settings(
    baseSettings,
    noPublish
  )
  .settings(
    prefix := sys.env.getOrElse("PREFIX", "/usr/local"),
    install := {
      import java.nio.file.Files
      import java.nio.file.StandardCopyOption._

      val bindir = file(prefix.value) / "bin"
      if (!bindir.exists) bindir.mkdirs()

      val dehumanizeS = (dehumanize/nativeLink in Compile).value.toPath
      val highlightS = (highlight/nativeLink in Compile).value.toPath
      val humanizeS = (humanize/nativeLink in Compile).value.toPath
      val meansdS = (meansd/nativeLink in Compile).value.toPath

      val dehumanizeT = (bindir / "dehumanize").toPath
      val highlightT = (bindir / "highlight").toPath
      val humanizeT = (bindir / "humanize").toPath
      val meansdT = (bindir / "meansd").toPath

      Files.copy(dehumanizeS, dehumanizeT, COPY_ATTRIBUTES, REPLACE_EXISTING)
      Files.copy(highlightS,  highlightT,  COPY_ATTRIBUTES, REPLACE_EXISTING)
      Files.copy(humanizeS,   humanizeT,   COPY_ATTRIBUTES, REPLACE_EXISTING)
      Files.copy(meansdS,     meansdT,     COPY_ATTRIBUTES, REPLACE_EXISTING)
    }
  )
