lazy val root = (project in file(".")).
  settings(
    name := "panop",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.3.12",
      "org.scalaj" %% "scalaj-http" % "1.1.5",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  )
