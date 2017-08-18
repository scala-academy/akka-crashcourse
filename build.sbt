lazy val akkaHttpVersion = "10.0.5"
lazy val akkaVersion    = "2.4.17"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "scala-academy",
      scalaVersion    := "2.11.8"
    )),
    name := "akka-crashcourse",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"       % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"         % "3.0.1"         % Test,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
    )
  )
