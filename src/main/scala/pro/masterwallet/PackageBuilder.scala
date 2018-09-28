package pro.masterwallet

import java.io.File
import scala.sys.process._
import java.util.concurrent._;

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
    // todo: copy distribution to CDN
    // todo: send slack message
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
    val buildDir = new File(buildRoot + "/" + version)
    if (!buildDir.exists) {
      println("Folder created " + buildDir.toString)
      buildDir.mkdirs 
    }

    println("cloning 3 projects... ")    
    gitClone("https://github.com/masterwallet/identity-server-js")
    gitClone("https://github.com/masterwallet/masterwallet-desktop")
    gitClone("https://github.com/masterwallet/identity-webclient")    
    
    val latch = new CountDownLatch(2);
    new IdentityClientThread(latch, buildDir).start
    new IdentityServerThread(latch, buildDir).start
    latch.await
    println("Ready with webclient and server. Copied to desktop");

    val stepDesktopBuild = new CountDownLatch(1);
    new DesktopDistributionThread(latch, buildDir, version).start
    stepDesktopBuild.await
  }
}
