package pro.masterwallet

import java.io.File
import scala.sys.process._
import scala.util.Properties.envOrElse
import ammonite.ops._
import ujson._
import java.util.concurrent._
import java.time.temporal.ChronoUnit
import java.time.LocalDateTime

abstract class BuilderThread(
    latch: Option[CountDownLatch] = None, 
    currentDir: Option[File] = None
) extends Thread {
  def cmd(cmdLine: String) = Process(cmdLine, cwd = currentDir).!
  def npm(command: String) = cmd("/usr/bin/node /usr/bin/npm " + command + "")
  def done = { if (latch.isDefined) latch.get.countDown  }
} 

class IdentityClientThread(latch: CountDownLatch, buildDir: File) 
	extends BuilderThread(Some(latch), Some(new File(buildDir.toString + "/identity-webclient"))) {
  override def run = {
    println("Starting Client Thread")
    npm("install")
    npm("run electron")
    println("Finished Client Thread")
    done
  }
}

class IdentityServerThread(latch: CountDownLatch, buildDir: File) 
	extends BuilderThread(Some(latch), Some(new File(buildDir.toString + "/identity-server-js"))) {
  override def run = {
    println("Starting Server Thread")
    npm("install")
    npm("run electron:hard")
    println("Finished Server Thread")
    done
  }
}

class DesktopDistributionThread(latch: CountDownLatch, buildDir: File, version: String) 
        extends BuilderThread(Some(latch), Some(new File(buildDir.toString + "/masterwallet-desktop"))) {
  override def run = {
    println("Starting Desktop Thread")
    npm("install")
    npm("run dist")
    println("Finished Desktop Thread")
    done
  }
}



class PackageBuilder(val version: String) extends Thread {
  val buildRoot = "/opt/builds"

  def isPresent: Boolean = {
    // flag whether we should not launch the build
    // may be we should check CDN folder and not release in 
    val fn = buildRoot + "/" + version + "/masterwallet-desktop/release"
//    val fn2 = buildRoot + "/" + version +  "/identity-server-js/package.json"
    new File(fn).exists
  }

  def cmd(cmdLine: String) = {
    Process(cmdLine, cwd = Some(new File( buildRoot + "/" + version )) ).!
  }

  def gitClone(url: String) = {
    val parts: Array[String] = url.split('/')
    val pkg: String = parts(parts.size - 1)

    println("Cloning package " + pkg)
    cmd("rm -rf " + pkg) // clean previous version, if any
    cmd("git clone --depth=1 " + url + "")
  }

  override def run = {
    implicit val wd = pwd
    val mwwd = wd
    val startTime = LocalDateTime.now

    val defaultDistRoot = root / 'mnt / "dist.masterwallet.pro"
    val DIST_ROOT = if (!envOrElse("DIST_ROOT", "").isEmpty) Path(sys.env("DIST_ROOT")) else defaultDistRoot
    if (!exists(DIST_ROOT)) {
      throw new Exception("Missing distribution folder " + DIST_ROOT.toString)
    }
    val CHANNEL_URL:String = sys.env("SLACK_CHANNEL_URL")
    if (CHANNEL_URL.isEmpty) {
      throw new Exception("Missing SLACK_CHANNEL_URL in variables")
    }
    val DIST_HTTP_ROOT: String = envOrElse("DIST_HTTP_ROOT", "http://dist.masterwallet.pro")

    val buildDir = new File(buildRoot + "/" + version)
    if (!buildDir.exists) {
      println("Folder created " + buildDir.toString)
      buildDir.mkdirs 
    }

    println("Cloning 3 projects... RELEASE=" + envOrElse("RELEASE", ""))    
    gitClone("https://github.com/masterwallet/identity-server-js")
    gitClone("https://github.com/masterwallet/masterwallet-desktop")
    gitClone("https://github.com/masterwallet/identity-webclient")    
    
    val latch = new CountDownLatch(2);
    new IdentityClientThread(latch, buildDir).start
    new IdentityServerThread(latch, buildDir).start
    latch.await
    println("Ready with webclient and server. Copied to desktop");

    val stepDesktopBuild = new CountDownLatch(1);
    new DesktopDistributionThread(stepDesktopBuild, buildDir, version).start
    stepDesktopBuild.await

    println("Copying files to distribution")
    %mv(mwwd / "masterwallet-desktop" / 'release, DIST_ROOT / version)
    %rm("-rf", DIST_ROOT / version / "linux-unpacked" )
    %rm("-rf", DIST_ROOT / version / "mac" )
    %rm("-rf", DIST_ROOT / version / "win-unpacked" )
    %rm("-rf", DIST_ROOT / version / "builder-effective-config.yaml" )
    %rm("-rf", DIST_ROOT / version / "latest-mac.yml" )

    // Send slack notification
    val minutes = ChronoUnit.MINUTES.between(startTime, LocalDateTime.now)
    val text = (ls! DIST_ROOT / version)
      .filter(f => (f.toString.endsWith(".zip") || f.toString.endsWith(".exe")))
      .map(f => (DIST_HTTP_ROOT + "/" + version + "/" + f.name))
      .mkString("\n") + s"\nSpent: ${minutes} min"

    println(SlackMessage(Js.Arr(Js.Obj("title" -> s"New Distribution ${version}", "text" -> text )))
      .send)
  }
}
