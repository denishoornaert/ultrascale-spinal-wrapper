package ultrascaleplus

import Console.{RESET, YELLOW}
import sys.process._
import scala.util.matching.Regex


object Vivado {

  var version: String = null

  def IPVersionMap: Map[String, Map[String, String]] = Map(
    "2024.1" -> Map(
      "zynq_ultra_ps_e" -> "3.5",
      "proc_sys_reset"  -> "5.0"
    ),
    "2022.2" -> Map(
      "zynq_ultra_ps_e" -> "3.4",
      "proc_sys_reset"  -> "5.0"
    )
  )

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
      case e: java.io.IOException        => { println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] No Vivado install found... Make sure to source your install 'settings.sh'!${RESET}") }
      case e: java.lang.RuntimeException => { println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] Calling 'vivado -version' failed... Make sure to source your install 'settings.sh'!${RESET}") }
      case e: Exception                  => { println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] Vivado install found but no version could be inferred from command line!${RESET}") }
      System.exit(-1);
    }
    return detectedVersion
  }
  
  def apply(targetVersion: String = "auto"): Unit = {
    this.version = this.detectVivadoVersion()
    // Report to user on specified version and detected one
    if (targetVersion == "auto") {
      println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] Vivado version ${this.version} detected and picked!${RESET}")
    }
    else if (supportedVivadoVersions contains targetVersion) {
      if (targetVersion != this.version) {
        this.version = targetVersion
        println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] Vivado version ${version} specified but version ${this.version} detected! (v${this.version} conserved for TCl script generation)${RESET}")
      }
      else {
        println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] Vivado version ${version} specified and detected!${RESET}")
      }
    }
    else {
        println(f"${RESET}${YELLOW}[UltraScale+ Wrapper] Requested vivado version (v${targetVersion}) is not supported!${RESET}")
        System.exit(-1)
    }
  }

}
