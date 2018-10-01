enablePlugins(DockerPlugin)

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


dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jre-alpine")
    runRaw("apk update && apk upgrade")
    runRaw("apk add git bash curl python make g++")

    runRaw("apk add nodejs-current npm")
    runRaw("npm install -g shelljs")

    add(artifact, artifactTargetPath)

    env("RELEASE", "1")
    env("SLACK_CHANNEL_URL", sys.env("SLACK_CHANNEL_URL"))
    env("BUILD_ROOT", "/opt/builds")
    env("DIST_ROOT", "/mnt/dist.masterwallet.pro")

    volume("/mnt/dist.masterwallet.pro", "/mnt/dist.masterwallet.pro")
    volume("/opt/builds", "/opt/builds")

    expose(8029)
    entryPoint("java", "-Djava.net.preferIPv4Stack=true", "-jar", artifactTargetPath)
  }
}
