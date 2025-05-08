package ultrascaleplus


import sys.process._
import scala.util.matching.Regex
import upickle.default._


import ultrascaleplus.utils.Log


object Vivado {

  var version: String = null

  val IPVersionMap= read[Map[String, Map[String, String]]](os.read(os.pwd / "hw/ext/VivadoIPVersion.json"))

  def supportedVivadoVersions: Seq[String] = {
    return IPVersionMap.keys.toSeq
  }

  def getIPVersion(ip: String): String = {
    return IPVersionMap(this.version)(ip)
  }

  def detectVivadoVersion(): String = {
    var detectedVersion = ""
    // Execute bash command and get output in string
    try {
      val versionCommandOutput = Seq("vivado", "-version").!!
      // Extract version from pattern
      val versionPattern = "v([0-9]*.[0-9])".r
      versionPattern.findFirstMatchIn(versionCommandOutput) match {
        case Some(m) => detectedVersion = m.group(1)
        case None    => throw new Exception("")
      }
    }
    catch {
      case e: java.io.IOException => {
        Log.info("No Vivado install found... Make sure to source your install 'settings.sh'!")
      }
      case e: java.lang.RuntimeException => {
        Log.info("Calling 'vivado -version' failed... Make sure to source your install 'settings.sh'!")
      }
      case e: Exception => {
        Log.info("Vivado install found but no version could be inferred from command line!")
      }
      System.exit(-1);
    }
    return detectedVersion
  }
  
  def apply(targetVersion: String = "auto"): Unit = {
    this.version = this.detectVivadoVersion()
    // Report to user on specified version and detected one
    if (targetVersion == "auto") {
      Log.info(f"Vivado version ${this.version} detected and picked!")
    }
    else if (supportedVivadoVersions contains targetVersion) {
      if (targetVersion != this.version) {
        this.version = targetVersion
        Log.info(f"$Vivado version ${version} specified but version ${this.version} detected! (v${this.version} conserved for TCl script generation)")
      }
      else {
        Log.info(f"Vivado version ${version} specified and detected!")
      }
    }
    else {
      Log.info(f"Requested vivado version (v${targetVersion}) is not supported!")
      System.exit(-1)
    }
  }

}
