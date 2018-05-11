import sbtcrossproject.CrossPlugin.autoImport.crossProject

enablePlugins(GitVersioning)

val scala210 = "2.10.7"
val scala211 = "2.11.12"
val scala212 = "2.12.6"

lazy val baseSettings = Seq(
  organization := "com.github.wookietreiber",
  git.baseVersion := "0.1.0",
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala210, scala211, scala212),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked"
  ),
  scalacOptions ++= Seq(scalaBinaryVersion.value match {
    case v if v.startsWith("2.12") => "-target:jvm-1.8"
    case _                         => "-target:jvm-1.7"
  }),
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

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
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
  .jsSettings()
  .nativeSettings(
    scalaVersion := scala211,
    crossScalaVersions := Seq(scala211),
    nativeLinkStubs := true
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val root = (project in file("."))
  .aggregate(
    coreJVM,
    coreJS,
    coreNative
  )
  .settings(
    baseSettings,
    noPublish
  )
