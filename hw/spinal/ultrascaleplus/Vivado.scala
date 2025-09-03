package ultrascaleplus


import sys.process._
import scala.util.matching.Regex
import upickle.default._

import scala.collection.mutable
import scala.collection.immutable 

import ultrascaleplus.scripts.TCLFactory
import ultrascaleplus.utils.{TCL,Log}
import scala.io.Source
import scala.tools.nsc.doc.html.HtmlTags.P

class VivadoCatalogItem(val vendor: String, val name: String, val version: String, val itemType: String) {}

object VivadoCatalogItem {

  def apply(line: String): VivadoCatalogItem = apply(line, null)

  def apply(line: String, itemType: String): VivadoCatalogItem = {
    val fields = line.split(":")
    fields.size match {
      case 3 => new VivadoCatalogItem(
        vendor = fields(0),
        name = fields(1),
        version = fields(2),
        itemType = itemType
      )
      case 4 => new VivadoCatalogItem(
        vendor = fields(0),
        itemType = if (itemType == null) fields(1) else itemType,
        name = fields(2),
        version = fields(3)
      )
      case _: Int => throw new RuntimeException(s"Invalid item line ${line}")
    }
  }
}

  /**
    * Collect the vendor and version of available IPs and Boards, and list of the available parts for the currently sourced Vivado version.
    *
    * @param ips dictionary with name index of ips found in the sourced Vivado version
    * @param boards dictionary with name index of boards found in the sourced Vivado version
    * @param parts list of available parts in the sourced Vivado version
    */
  private class VivadoCatalog (
    private val ips: Map[String, VivadoCatalogItem],
    private val boards: Map[String, VivadoCatalogItem],
    private val parts: List[String]
  ) {
    
    /**
      * Return the version of the passed IP, if present in the catalog
      *
      * @param ipName The name of the IP of interest
      * @return Option object containing the version if IP found
      */
    def getIpVersion(ipName: String) : Option[String] = return ips.get(ipName).map(i => i.version)

    /**
      * Return the version of the passed Board, if present in the catalog
      *
      * @param boardName The name of the Board of interest
      * @return Option object containing the version if Board found
      */
    def getBoardVersion(boardName: String) : Option[String] = return boards.get(boardName).map(i => i.version)

    /**
      * Check if the passed part is present in the catalog
      *
      * @param partName Name of the part of interest
      * @return [[True]] if present, false otherwise
      */
    def isPartPresent(partName: String) : Boolean = parts.contains(partName)
  }


/** Object storing and referencing all things Vivado.
 *  
 *  The object aims to provide (via an abstract interface) information relative 
 *  about the Vivado version desired to the remaining of the library. For 
 *  instance, this includes Vivado version, IP version for that Vivado, ...
 */
object Vivado {


  /**
   * Populate a singleton object [[VivadoCatalog]]
   * The data is collected via TCL scripts, could be necessary to update to python shell in future versions of Vivado.
   **/
  private var catalog: VivadoCatalog = {

    setVersionIfNotDefined()

    val resource_url = getClass.getResource("/vivado_catalog_scan.tcl")
    val content = Source.fromURL(resource_url).mkString

    var path: os.Path = os.temp.dir() / "vivado_catalog_scan.tcl"

    os.write(path, content)

    val res = os.proc("vivado", "-nolog", "-nojournal", "-notrace", "-mode", "batch", "-source", path).call()

    if (res.exitCode != 0)
      throw new RuntimeException(s"Failed to execture probing tcl:\n${res.err.toString()}")

    os.remove(path)

    def readLines(filename: String) = {
      val path = os.root / "tmp" / filename
      os.read
        .lines
        .stream(path)
    }

    val boards = readLines("vivado_boards.txt")
        .map(l => VivadoCatalogItem(l, "board"))
        .map(i => (i.name, i))
        .toList
        .toMap

    val ips = readLines("vivado_ips.txt")
        .map(l => VivadoCatalogItem(l, "ip"))
        .map(i => (i.name, i))
        .toList
        .toMap

    val parts = readLines("vivado_parts.txt")
        .toList

    new VivadoCatalog(
      ips = ips,
      boards = boards,
      parts = parts
    )
  }

  private var versionFound: Array[java.lang.String] = null

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
    if (this.versionFound == null) {
      // Report to user on specified version and detected one
      if (Config.vivado == "auto") { 
        versionFound = this.detectVivadoVersion().split('.')
        Log.info(f"Vivado version ${this.year}.${this.revision} detected and picked!")
      }
      else if (supportedVivadoVersions contains Config.vivado) {
        versionFound = this.detectVivadoVersion().split('.')
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
        System.exit(-1)
      }
      case e: java.lang.RuntimeException => {
        Log.info("Calling 'vivado -version' failed... Make sure to source your install 'settings.sh'!")
        System.exit(-1)
      }
      case e: Exception => {
        Log.info("Vivado install found but no version could be inferred from command line!")
        System.exit(-1)
      }
      System.exit(-1);
    }
    return detectedVersion
  }
  
  /** Year of the vivado version employed. E.g., "2019" if version is "2019.2". */
  def year: String = {
    this.setVersionIfNotDefined()
    return this.versionFound(0)
  }

  /** Revision of the Vivado version employed. E.g., "2" if version is "2019.2". */
  def revision: String = {
    this.setVersionIfNotDefined()
    return this.versionFound(1)
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
  def getIPVersion(ip: String): String = catalog.getIpVersion(ip).getOrElse(throw new RuntimeException(s"IP ${ip} does not exist in the catalog"))

  /** Method returning the version of a given board for the Vivado set.
   *
   *  @param board Boards's name for which the version is required.
   *  @return version Version of the board requested.
   */
  def getBoardVersion(board: String): String = catalog.getBoardVersion(board).getOrElse(throw new RuntimeException(s"Board ${board} does not exist in the catalog"))

  class Properties(val target: String, mode: String = "default") extends TCL {

    private var properties = Map[String, String]()

    this.fill(mode)

    override def getTCL(): String = {
      var tcl = ""
      for (property <- this.properties) {
        tcl += TCLFactory.setProperty(property._1, property._2, "$obj")
      }
      return tcl
    }

    private def fill(filepath: os.ReadablePath): Unit = {
      this.properties = read[Map[String, String]](os.read(filepath))
    }
    
    def fill(mode: String): Unit = {
      println(f"${this.target}/${mode}.json")
      this.fill(os.pwd / "hw" / "ext" / "Vivado" / Vivado.year / f"${this.target}" / f"${mode}.json")
    }

    /**
     * Note that if a property entry already exists, it will be 
     * update/overwritten.
     */
    def add(name: String, value: String): Unit = {
      this.properties += (name -> value)
    }

    /**
     * Note that if a property entry already exists, it will be 
     * update/overwritten.
     */
    def add(another: Properties): Unit = {
      this.properties ++= another.properties
    }
  }

  class Report(runName: String, detailedReportName: String) extends TCL {

    private val properties = new Properties("Report", detailedReportName)

    override def getTCL(): String = {
      var tcl = ""
      tcl += TCLFactory.setObject(f"get_report_configs -of_objects [get_runs ${this.runName}] ${this.runName}_${this.detailedReportName}")
      tcl += TCLFactory.ifObjectExists(this.properties.getTCL())
      tcl += "\n"
      return tcl
    }

  } 

  object Project extends TCL {

    private val properties = new Properties("Project")

    override def getTCL(): String = {
      var tcl = ""
      tcl += f"create_project ${TCLFactory.platform.get.getName()} ./vivado/${TCLFactory.platform.get.getName()} -part ${TCLFactory.platform.get.boardPart}\n"
      tcl +=  "set proj_dir [get_property directory [current_project]]\n"
      tcl +=  "\n"
      tcl +=  "set obj [current_project]\n"
      tcl +=  this.properties.getTCL()
      tcl +=  "\n"
      return tcl
    }

    /**
     * Note that if a property entry already exists, it will be 
     * update/overwritten.
     */
    def add(name: String, value: String): Unit = {
      this.properties.add(name, value)
    }

    def fill(mode: String): Unit = {
      this.properties.fill(mode)
    }

  }

  object Synthesis extends TCL {

    private val properties = new Properties("Synthesis")

    private val reports = Seq[Report](
      new Report("synth_1", "synth_report_utilization_0")
    )

    def perform(): String = {
      var tcl = ""
      tcl += f"launch_runs synth_1 -jobs 4\n"
      tcl += f"wait_on_run synth_1\n"
      tcl +=  "\n"
      return tcl
    }
    
    override def getTCL(): String = {
      var tcl = f"set obj [get_runs synth_1]\n"
      tcl += TCLFactory.setProperty("flow", f"Vivado Synthesis ${Vivado.year}", "$obj")
      tcl += this.properties.getTCL()
      tcl += "\n"
      for (report <- this.reports)
        tcl += report.getTCL()
      tcl += "\n"
      tcl += "current_run -synthesis [get_runs synth_1]\n\n"
      return tcl
    }

    def fill(mode: String): Unit = {
      this.properties.fill(mode)
    }

  }
  
  object Implementation extends TCL {

    private val properties = new Properties("Implementation")
    
    private val reports = Seq[Report](
      new Report("impl_1", "init_report_timing_summary_0"),
      new Report("impl_1", "opt_report_drc_0"),
      new Report("impl_1", "opt_report_timing_summary_0"),
      new Report("impl_1", "power_opt_report_timing_summary_0"),
      new Report("impl_1", "place_report_io_0"),
      new Report("impl_1", "place_report_utilization_0"),
      new Report("impl_1", "place_report_control_sets_0"),
      new Report("impl_1", "place_report_incremental_reuse_0"),
      new Report("impl_1", "place_report_incremental_reuse_1"),
      new Report("impl_1", "place_report_timing_summary_0"),
      new Report("impl_1", "post_place_power_opt_report_timing_summary_0"),
      new Report("impl_1", "phys_opt_report_timing_summary_0"),
      new Report("impl_1", "route_report_drc_0"),
      new Report("impl_1", "route_report_methodology_0"),
      new Report("impl_1", "route_report_power_0"),
      new Report("impl_1", "route_report_status_0"),
      new Report("impl_1", "route_report_timing_summary_0"),
      new Report("impl_1", "route_report_incremental_reuse_0"),
      new Report("impl_1", "route_report_clock_utilization_0"),
      new Report("impl_1", "route_report_bus_skew_0"),
      new Report("impl_1", "post_route_phys_opt_report_timing_summary_0"),
      new Report("impl_1", "post_route_phys_opt_report_bus_skew_0")
    )
    
    def perform(): String = {
      var tcl = ""
      tcl += f"launch_runs impl_1 -to_step write_bitstream -jobs 4\n"
      tcl += f"wait_on_run impl_1\n"
      tcl +=  "\n"
      return tcl
    }

    def bitstream(): String = {
      return f"file copy -force ./vivado/${TCLFactory.platform.get.getName()}/${TCLFactory.platform.get.getName()}.runs/impl_1/design_1_wrapper.bit ./${TCLFactory.platform.get.getName()}.bit\n"
    }

    def xsa(): String = {
      return f"write_hw_platform -fixed -include_bit -force -file ./${TCLFactory.platform.get.getName()}.xsa\n"
    }

    override def getTCL(): String = {
      var tcl = f"set obj [get_runs impl_1]\n"
      tcl += TCLFactory.setProperty("flow", f"Vivado Implementation ${Vivado.year}", "$obj")
      tcl += this.properties.getTCL()
      tcl += "\n"
      for (report <- this.reports)
        tcl += report.getTCL()
      return tcl
    }

    def fill(mode: String): Unit = {
      this.properties.fill(mode)
    }

  }

}
