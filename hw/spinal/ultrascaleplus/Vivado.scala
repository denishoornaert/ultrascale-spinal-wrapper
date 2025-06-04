package ultrascaleplus


import sys.process._
import scala.util.matching.Regex
import upickle.default._


import ultrascaleplus.utils.Log


object Vivado {

  private var targetVersion: String = null
  
  private val IPVersionMap = read[Map[String, Map[String, String]]](os.read(os.pwd / "hw/ext/VivadoIPVersion.json"))

  private def setVersionIfNotDefined(): Unit = {
    if (targetVersion == null) {
      // Report to user on specified version and detected one
      if (Config.vivado == "auto") {
        targetVersion = this.detectVivadoVersion()
        Log.info(f"Vivado version ${this.version} detected and picked!")
      }
      else if (supportedVivadoVersions contains Config.vivado) {
        targetVersion = Config.vivado
        Log.info(f"$Vivado version ${targetVersion} will be used as specified!")
      }
      else {
        Log.info(f"Requested vivado version (v${targetVersion}) is not supported!")
        System.exit(-1)
      }
    }
  } 

  private def supportedVivadoVersions: Seq[String] = {
    return IPVersionMap.keys.toSeq
  }

  private def detectVivadoVersion(): String = {
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
  
  def version: String = {
    this.setVersionIfNotDefined()
    return this.targetVersion
  }
  
  def getIPVersion(ip: String): String = {
    this.setVersionIfNotDefined()
    return IPVersionMap(this.targetVersion)(ip)
  }

}
