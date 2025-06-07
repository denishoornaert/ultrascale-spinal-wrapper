package ultrascaleplus


import sys.process._
import scala.util.matching.Regex
import upickle.default._


import ultrascaleplus.utils.Log
import scala.io.Source


object Vivado {

  var year    : String = null
  var revision: String = null

  private val IPVersionMap = readIPVersionMap("/VivadoIPVersion.json")

  private def readIPVersionMap(resourcePath: String): Map[String,Map[String,String]] = {
    val resource_url = getClass.getResource("/VivadoIPVersion.json")
    val content = Source.fromURL(resource_url).mkString
    val map = read[Map[String, Map[String, String]]](content)
    return map
  }

  private def setVersionIfNotDefined(): Unit = {
    if ((this.year == null) || (this.revision == null)) {
      // Report to user on specified version and detected one
      if (Config.vivado == "auto") { 
        val versionFound: Array[java.lang.String] = this.detectVivadoVersion().split('.')
        this.year = versionFound(0)
        this.revision = versionFound(1)
        Log.info(f"Vivado version ${this.year}.${this.revision} detected and picked!")
      }
      else if (supportedVivadoVersions contains Config.vivado) {
        val versionFound: Array[java.lang.String] = this.detectVivadoVersion().split('.')
        this.year = versionFound(0)
        this.revision = versionFound(1)
        Log.info(f"$Vivado version ${this.year}.${this.revision} will be used as specified!")
      }
      else {
        Log.info(f"Requested vivado version (v${this.year}.${this.revision}) is not supported!")
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
    return f"${this.year}.${this.revision}"
  }

  def getIPVersion(ip: String): String = {
    this.setVersionIfNotDefined()
    return IPVersionMap(this.version)(ip)
  }

}
