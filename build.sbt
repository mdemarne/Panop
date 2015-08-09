lazy val root = (project in file(".")).
  settings(
    name := "panop",
    scalaVersion := "2.11.7",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12"
  )
