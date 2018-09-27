package pro.masterwallet

import java.io.File
import ammonite.ops._
// import ammonite.shell._

class PackageBuilder(val version: String) extends Thread {
  val buildRoot = "/opt/builds"

  def isPresent: Boolean = {
    exists(Path(buildRoot) / version / "identity-server-js" / "package.json")
  }
  override def run = {

    // if (!exists(Path(buildRoot) / version)) {
    //  %mkdir(Path(buildRoot) / version)
    // }

    implicit val wd = Path(buildRoot) / version

    println(%% git ("clone", "--depth=1", "https://github.com/masterwallet/identity-server-js"));
    println(%% git ("clone", "--depth=1", "https://github.com/masterwallet/identity-webclient"));
    println(%% git ("clone", "--depth=1", "https://github.com/masterwallet/masterwallet-desktop"));
  }
}
