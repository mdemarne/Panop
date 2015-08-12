lazy val root = (project in file(".")).
  settings(
    name := "panop",
    description := "Simple Tool For Parallel Online Search",
        organization := "name.demarne.m",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.3.12",
      "org.scalaj" %% "scalaj-http" % "1.1.5",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scala-lang" % "scala-compiler" % "2.11.6"
    ),
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")
  )
