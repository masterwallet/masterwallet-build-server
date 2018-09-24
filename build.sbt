lazy val akkaHttpVersion = "10.1.4"
lazy val akkaVersion    = "2.5.16"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "pro.masterwallet",
      scalaVersion    := "2.12.6"
    )),
    name := "masterwallet-build-server",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test,

      "org.scalaj"  %% "scalaj-http"    % "2.4.1",
      "com.lihaoyi" %% "ujson"          % "0.6.6"
    )
  )
