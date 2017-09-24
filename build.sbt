import sbtcrossproject.{crossProject, CrossType}

enablePlugins(GitVersioning)

val scala210 = "2.10.6"
val scala211 = "2.11.11"
val scala212 = "2.12.3"

val baseSettings = Seq(
  organization := "com.github.wookietreiber",
  name := "scala-cli-tools",
  git.baseVersion := "0.0.1",

  scalaVersion := scala211,
  crossScalaVersions := Seq(scala210, scala211, scala212),

  description := "Scala Command-Line Interface Tools",
  homepage := Some(url("https://github.com/wookietreiber/scala-cli-tools")),
  startYear := Some(2017),
  scmInfo := Some(ScmInfo(
    url("https://github.com/wookietreiber/scala-cli-tools"),
    "scm:git:git://github.com/wookietreiber/scala-cli-tools.git",
    Some("scm:git:https://github.com/wookietreiber/scala-cli-tools.git")
  )),
  // TODO apiURL
  licenses := Seq(
    "APL2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")
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

baseSettings
noPublish

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .settings(
    baseSettings,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.5.3" % "test"
    ),
    scalacOptions ++= Seq(scalaBinaryVersion.value match {
      case v if v.startsWith("2.12") => "-target:jvm-1.8"
      case _                         => "-target:jvm-1.7"
    }),
    testFrameworks := Seq(new TestFramework("utest.runner.Framework"))
  )
  .jsSettings(
  )
  .nativeSettings(
    crossScalaVersions := Seq(scala211),
    nativeLinkStubs := true
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native
