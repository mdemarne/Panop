lazy val root = (project in file(".")).
  settings(
    name := "panop",
    scalaVersion := "2.11.7",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12",
    libraryDependencies += "org.scalaj" %% "scalaj-http" % "1.1.5",
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
