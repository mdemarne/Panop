import sbt._
import Process._
import Keys._

// Thanks to leon (LARA/EPFL) for the script generation!
object PanopBuild extends Build {
  private val scriptName = "panop"
  def scriptFile = file(".") / scriptName

  private val nl = System.getProperty("line.separator")
  def is64 = System.getProperty("sun.arch.data.model") == "64"
  def ldLibraryDir32 = file(".") / "lib-bin" / "32"
  def ldLibraryDir64 = file(".") / "lib-bin" / "64"

  val cleanTask = TaskKey[Unit]("clean", "Cleanup build and scripts.") <<= (streams, clean) map { (s, c) =>
    if (scriptFile.isFile) scriptFile.delete
  }

  val scriptTask = TaskKey[Unit]("script", "Generate the panop Bash script") <<= (streams, dependencyClasspath in Compile, classDirectory in Compile, resourceDirectory in Compile) map { (s, cps, out, res) =>
    try {
      scriptFile match {
        case f if f.exists =>
          s.log.info("Regenerating '"+f.getName+"' script ("+(if(is64) "64b" else "32b")+")...")
          f.delete
        case f => s.log.info("Generating '"+f.getName+"' script ("+(if(is64) "64b" else "32b")+")...")
      }

      val paths = (res.getAbsolutePath +: out.getAbsolutePath +: cps.map(_.data.absolutePath)).mkString(":")

      IO.write(scriptFile, s"""|#!/bin/bash --posix
                      |
                      |SCALACLASSPATH="$paths"
                      |
                      |java -Xmx2G -Xms512M -classpath $${SCALACLASSPATH} -Dscala.usejavacp=false scala.tools.nsc.MainGenericRunner -classpath $${SCALACLASSPATH} panop.ui.Main $$@ 2>&1 | tee -i last.log
                      |""".stripMargin)

      scriptFile.setExecutable(true)
    } catch {
      case e: Throwable =>
        s.log.error("There was an error while generating the script file: " + e.getLocalizedMessage)
    }
  }

  lazy val root = Project(
    id = "panop-core",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "panop-core",
      description := "Simple Tool For Parallel Online Search",
      organization := "name.demarne.m",
      version := "1.0-SNAPSHOT",
      scalaVersion := "2.11.6",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.3.12",
        "org.scalaj" %% "scalaj-http" % "1.1.5",
        "org.scalatest" %% "scalatest" % "2.2.4" % "test",
        "org.scala-lang" % "scala-compiler" % "2.11.6")
      ) ++ Seq(cleanTask, scriptTask))

  /*lazy val root = (project in file(".")).
    settings(
      name := "panop",
      description := "Simple Tool For Parallel Online Search",
      organization := "name.demarne.m",
      scalaVersion := "2.11.6",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.3.12",
        "org.scalaj" %% "scalaj-http" % "1.1.5",
        "org.scalatest" %% "scalatest" % "2.2.4" % "test",
        "org.scala-lang" % "scala-compiler" % "2.11.6"),
      scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked"))*/

}