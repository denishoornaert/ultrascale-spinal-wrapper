ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.16"
ThisBuild / organization := "de.tum"

val spinalVersion = "1.12.0"
val spinalCore = "com.github.spinalhdl" %% "spinalhdl-core" % spinalVersion
val spinalLib = "com.github.spinalhdl" %% "spinalhdl-lib" % spinalVersion
val spinalIdslPlugin = compilerPlugin("com.github.spinalhdl" %% "spinalhdl-idsl-plugin" % spinalVersion)

libraryDependencies += "org.scala-lang" %% "toolkit" % "0.7.0"
// libraryDependencies += "com.lihaoyi" %% "upickle" % "4.1.0"

lazy val lib = (project in file("."))
  .settings(
    name := "ultrascale-spinal-wrapper",
    Compile / scalaSource := baseDirectory.value / "hw" / "spinal",
    Compile / resourceDirectory := baseDirectory.value / "hw" / "ext",
    libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin)
  )

fork := true
