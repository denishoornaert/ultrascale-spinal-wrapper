package ultrascaleplus


import sys.process._
import scala.util.matching.Regex
import upickle.default._


import ultrascaleplus.utils.Log
import scala.io.Source

/** Object storing and referencing all things Vivado.
 *  
 *  The object aims to provide (via an abstract interface) information relative 
 *  about the Vivado version desired to the remaining of the library. For 
 *  instance, this includes Vivado version, IP version for that Vivado, ...
 */
object Vivado {

  /** 
   *  Year of the vivado version employed. E.g., "2019" if version is "2019.2".
   *  **Do not modify by yourself!**
   */
  var year    : String = null

  /** 
   *  Revision of the Vivado version employed. E.g., "2" if version is "2019.2".
   *  **Do not modify by yourself!**
   */
  var revision: String = null

  /** 
   *  Nested mapping: Vivado version, Xilinx IP, IP version.
   */
  private val IPVersionMap: Map[String, Map[String, String]] = readIPVersionMap("/VivadoIPVersion.json")

  /** Method loading the Vivado-IP-Version mapping stored in an external JSON 
   *  file.
   *
   *  @param resourcePath Absolute path to JSON file storing the mapping.
   *  @return Nested [[Map]] equivalent containing in the JSON file.
   */
  private def readIPVersionMap(resourcePath: String): Map[String, Map[String, String]] = {
    val resource_url = getClass.getResource(resourcePath)
    val content = Source.fromURL(resource_url).mkString
    val map = read[Map[String, Map[String, String]]](content)
    return map
  }

  /**
   *  Method checks if a Vivado version has already been set.
   *  If no, installed version can automatically be picked up 
   *  (i.e., [[Config.vivado]] == "auto") or user-set version
   *  can be used (i.e., [[Config.vivado]] == XXXX.X).
   */
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

  /** Returns list of suported Vivado version by the library.
   *
   *  @return Returns supported Vivado versions as a sequence of strings.
   */
  private def supportedVivadoVersions: Seq[String] = {
    return IPVersionMap.keys.toSeq
  }

  /** Automatically detects the Vivado version istalled on the system (if any).
   *
   *  @return Returns the found version as a [[String]].
   */
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
  
  /** Returns Vivado version used for the project.
   *
   *  @return version [[String]] in the format "year.revision".
   */
  def version: String = {
    this.setVersionIfNotDefined()
    return f"${this.year}.${this.revision}"
  }

  /** Method returning the version of a given IP for the Vivado set.
   *
   *  @param ip IP's name for which the version is required.
   *  @return version Version of the IP requested.
   */
  def getIPVersion(ip: String): String = {
    this.setVersionIfNotDefined()
    return IPVersionMap(this.version)(ip)
  }

}
