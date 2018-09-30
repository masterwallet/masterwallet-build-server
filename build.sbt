lazy val akkaHttpVersion = "10.1.4"
lazy val akkaVersion    = "2.5.16"

mainClass in (Compile, run) := Some("pro.masterwallet.BuilderServer")
mainClass in (Compile, packageBin) := Some("pro.masterwallet.BuilderServer")


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
      "com.lihaoyi" %% "ujson"          % "0.6.6",
      "com.lihaoyi" %% "ammonite-ops"   % "1.1.0"
//       "com.lihaoyi" %% "ammonite-shell" % "1.1.0-34-b991be4"
    )
  )
