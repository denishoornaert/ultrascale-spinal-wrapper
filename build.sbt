ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.14"
ThisBuild / organization := "de.tum"

val spinalVersion = "1.12.0"
val spinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion
val spinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion
val spinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)
// val upickle = "com.lihaoyi" %% "upickle" % "4.1.0"
// val oslib = "com.lihaoyi" %% "os-lib" % "0.11.3"

libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.3"

lazy val lib = (project in file("."))
  .settings(
    name := "ultrascale-spinal-wrapper",
    Compile / scalaSource := baseDirectory.value / "hw" / "spinal",
    Compile / resourceDirectory := baseDirectory.value / "hw" / "ext",
    libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin)
  )

fork := true
