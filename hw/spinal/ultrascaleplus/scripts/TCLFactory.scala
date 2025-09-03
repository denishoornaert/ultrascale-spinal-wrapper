package ultrascaleplus.scripts


import java.io._


import spinal.core._
import spinal.lib.bus.misc.SizeMapping


import ultrascaleplus._
import ultrascaleplus.parameters.AddressMap
import ultrascaleplus.utils.{TCL}


object TCLFactory {

  var target    : String = "vivado/untitled.tcl"
  var platform  : Option[UltraScalePlus] = None

  def apply(platform: UltraScalePlus): Unit = {
    this.platform = Some(platform)
    this.platform.get.setName(this.platform.get.getClass.getSimpleName)
    this.target = f"vivado/${this.platform.get.getName()}.tcl"
  }

  // Call from inside

  def setProperty(name: String, value: String, obj: String): String = {
    return "set_property -name \""+name+"\" -value \""+value+"\" -object "+obj+"\n"
  }
  
  def ifObjectExists(codeBlock: String): String = {
    val tabversion = codeBlock.linesIterator.map("\t" + _).mkString("\n")
    return if (codeBlock.trim.nonEmpty) "if { $obj != \"\" } {\n"+tabversion+"\n}\n" else ""

  }

  def checkAndCreateFileset(fileset: String, filesetType: String): String = {
    val empty = "\"\""
    var tcl = ""
    tcl += f"if {[string equal [get_filesets -quiet ${fileset}] ${empty}]} {\n"
    tcl += f"  create_fileset -${filesetType} ${fileset}\n"
    tcl += f"}\n"
    tcl += f"\n"
    return tcl
  }

  def setObject(source: String): String = {
    return f"set obj [${source}]\n"
  }

  def importConstraints(fileset: String): String = {
    var tcl = ""
    tcl +=  "add_files -fileset constrs_1 -norecurse ./hw/gen/"+this.platform.get.getName()+".xdc\n"
    tcl +=  "import_files -fileset constrs_1 ./hw/gen/"+this.platform.get.getName()+".xdc\n"
    tcl +=  "\n"
    return tcl
  }

  def addFileset(fileset: String): String = {
    var tcl = ""
    tcl += f"set obj [get_filesets ${fileset}]\n"
    tcl +=  "set files [list [file normalize \"./hw/gen/"+this.platform.get.getName()+".v\"]]\n"
    tcl +=  "add_files -norecurse -fileset $obj $files\n"
    tcl +=  "\n"
    return tcl
  }

  def addSources(fileset: String): String = {
    val empty = "\"\"" 
    var tcl = ""
    tcl += f"if { [get_files ${this.platform.get.getName()}.v] == ${empty} } {\n"
    tcl += f"  import_files -quiet -fileset ${fileset} ./hw/gen/${this.platform.get.getName()}.v\n"
    tcl +=  "}\n"
    return tcl
  }

  def print(level: String, id: Int, message: String): String = {
    return Vivado.version match {
      case "2019.2" => f"common::send_msg_id \"BD_TCL-${id}\" \"${level}\" \"${message}\""
      case _        => f"common::send_gid_msg -ssname BD::TCL -id ${id} -severity \"${level}\" \"${message}\""
    }
  }

  def createDesign(name: String): String = {
    return f"create_bd_design ${name}\n"
  }

  def checkIPs(): String = {
    var tcl = ""
    tcl += this.print("INFO", 2011, "Checking if the selected IPs exist in the project's IP catalog.")+"\n"
    tcl +=f"set list_check_ips \"xilinx.com:ip:proc_sys_reset:5.0 xilinx.com:ip:zynq_ultra_ps_e:${Vivado.getIPVersion("zynq_ultra_ps_e")}\"\n"
    tcl += "set list_ips_missing \"\"\n"
    tcl += "foreach ip_vlnv $list_check_ips {\n"
    tcl += "  set ip_obj [get_ipdefs -all $ip_vlnv]\n"
    tcl += "  if { $ip_obj eq \"\" } {\n"
    tcl += "    lappend list_ips_missing $ip_vlnv\n"
    tcl += "  }\n"
    tcl += "}\n"
    tcl += "\n"
    tcl += "if { $list_ips_missing ne \"\" } {\n"
    tcl += "  catch {"+this.print("ERROR", 2012, "The following IPs are not found in the IP Catalog:\\n  $list_ips_missing\\n\\nResolution: Please add the repository containing the IP(s) to the project.")+" }\n"
    tcl += "  "+this.print("WARNING", 2023, "Will not continue with creation of design due to the error(s) above.")+"\n"
    tcl += "  return 3\n"
    tcl += "}\n"
    return tcl
  }

  def checkModules(): String = {
    var tcl = ""
    tcl += this.print("INFO", 2020, f"Checking if the following modules exist in the project's sources: ${this.platform.get.getName()}.")+"\n"
    tcl +=  "set list_mods_missing \"\"\n"
    tcl += f"foreach mod_vlnv ${this.platform.get.getName()} {\n"
    tcl +=  "  if { [can_resolve_reference $mod_vlnv] == 0 } {\n"
    tcl +=  "    lappend list_mods_missing $mod_vlnv\n"
    tcl +=  "  }\n"
    tcl +=  "}\n"
    tcl +=  "\n"
    tcl +=  "if { $list_mods_missing ne \"\" } {\n"
    tcl +=  "  catch {"+this.print("ERROR", 2021, "The following module(s) are not found in the project: $list_mods_missing")+"}\n"
    tcl +=  "  "+this.print("INFO", 2022, "Please add source files for the missing module(s) above.")+"\n"
    tcl +=  "  "+this.print("WARNING", 2023, "Will not continue with creation of design due to the error(s) above.")+"\n"
    tcl +=  "  return 3\n"
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def createHierarchy(): String = {
    var tcl = ""
    tcl += "set parentObj [get_bd_cells [get_bd_cells /]]\n"
    tcl += "if { $parentObj == \"\" } {\n"
    tcl += "  catch {"+this.print("ERROR", 2090, "Unable to find parent cell!")+"}\n"
    tcl += "  return\n"
    tcl += "}\n"
    tcl += "\n"
    tcl += "set parentType [get_property TYPE $parentObj]\n"
    tcl += "if { $parentType ne \"hier\" } {\n"
    tcl += "  catch {"+this.print("ERROR", 2091, "Parent <$parentObj> has TYPE = <$parentType>. Expected to be <hier>.")+"}\n"
    tcl += "  return\n"
    tcl += "}\n"
    tcl += "\n"
    tcl += "set oldCurInst [current_bd_instance .]\n"
    tcl += "\n"
    tcl += "current_bd_instance $parentObj\n"
    tcl += "\n"
    return tcl
  }

  def createBlock(): String = {
    val empty = "\"\"" 
    var tcl = ""
    tcl += f"set block_name ${this.platform.get.getName()}\n"
    tcl += f"if { [catch {set ${this.platform.get.getName()} [create_bd_cell -type module -reference ${this.platform.get.getName()} ${this.platform.get.getName()}] } errmsg] } {\n"
    tcl +=  "  catch {"+this.print("ERROR", 2095, f"Unable to add referenced block <${this.platform.get.getName()}>. Please add the files for ${this.platform.get.getName()}'s definition into the project.")+"}\n"
    tcl +=  "  return 1\n"
    tcl +=  "} elseif { $"+this.platform.get.getName()+f" eq ${empty} } {\n"
    tcl +=  "  catch {"+this.print("ERROR", 2096, f"Unable to referenced block <${this.platform.get.getName()}>. Please add the files for ${this.platform.get.getName()}'s definition into the project.")+"}\n"
    tcl +=  "  return 1\n"
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def instantiateResetSystem(): String = {
    return "set reset_system [ create_bd_cell -type ip -vlnv xilinx.com:ip:proc_sys_reset:5.0 reset_system ]\n\n"
  }

  def anyConnection(netType: String, source: String, targets: Seq[String]): String = {
    var tcl = f"connect_bd_${netType}net -${netType}net ${source}"
    for (target <- targets)
      tcl += f" [get_bd_${netType}pins ${target}]"
    return tcl+"\n"
  }

  def netConnection(source: String, targets: Seq[String]): String = {
    return this.anyConnection("", source, targets)
  }

  def interfaceConnection(source: String, targets: Seq[String]): String = {
    return this.anyConnection("intf_", source, targets)
  }

  def addressMap(addressBase: BigInt, rangeSize: BigInt, port: String, target: String): String = {
    return f"assign_bd_address -offset 0x${addressBase.toString(16)} -range 0x${rangeSize.toString(16)} -target_address_space [get_bd_addr_spaces ${port}] [get_bd_addr_segs ${target}] -force\n"
  }
  
  def addXilinxIPs(component: Component): String = {
    var tcl = ""
    for (element <- component.children) {
      // Bundle MUST stay at the last place!
      element match {
        case _:TCL       => tcl += element.asInstanceOf[TCL].getTCL()
        case _:Component => tcl += this.addXilinxIPs(element.asInstanceOf[Component])
        case _           => tcl += ""
      }
    }
    return tcl
  }

  def addInterfaces(bundle: Bundle): String = {
    var tcl = ""
    for ((name, element) <- bundle.elements) {
      // Bundle MUST stay at the last place!
      element match {
        case _:TCL      => tcl += element.asInstanceOf[TCL].getTCL()
        case _:Bundle   => tcl += this.addInterfaces(element.asInstanceOf[Bundle])
        case _          => tcl += ""
      }
    }
    return tcl
  }

  def saveAndValidate(): String = {
    var tcl = ""
    tcl += "current_bd_instance $oldCurInst\n"
    tcl += "\n"
    tcl += "validate_bd_design\n"
    tcl += "save_bd_design\n"
    tcl += "\n"
    tcl += "set_property REGISTERED_WITH_MANAGER \"1\" [get_files design_1.bd]\n" 
    tcl += "set_property SYNTH_CHECKPOINT_MODE \"Hierarchical\" [get_files design_1.bd]\n"
    tcl += "\n"
    return tcl
  }

  def wrapDesign(fileset: String): String = {
    val path = f"./vivado/${this.platform.get.getName()}/${this.platform.get.getName()}.gen/${fileset}/bd/design_1/hdl/design_1_wrapper.v"
    var tcl = "" 
    tcl +=  "if { [get_property IS_LOCKED [ get_files -norecurse design_1.bd]] == 1 } {\n"
    tcl +=  "  import_files -fileset sources_1 [file normalize \""+path+"\"]\n"
    tcl +=  "} else {\n"
    tcl += f"  set wrapper_path [make_wrapper -fileset ${fileset} -files [ get_files -norecurse design_1.bd] -top]\n"
    tcl += f"  add_files -norecurse -fileset ${fileset} "+"$wrapper_path\n"
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def disableIDRFlow(): String = {
    val empty = "\"\""
    var tcl = ""
    tcl += f"set idrFlowPropertiesConstraints ${empty}\n"
    tcl +=  "catch {\n"
    tcl +=  "  set idrFlowPropertiesConstraints [get_param runs.disableIDRFlowPropertyConstraints]\n"
    tcl +=  "  set_param runs.disableIDRFlowPropertyConstraints 1\n"
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def setImplmentationStrategy(fileset: String): String = {
    var tcl = ""
    tcl += f"set obj [get_runs ${fileset}]\n"
    tcl +=  this.setProperty("strategy", "Vivado Implementation Defaults", "$obj")
    tcl +=  this.setProperty("steps.write_bitstream.args.readback_file", "0", "$obj")
    tcl +=  this.setProperty("steps.write_bitstream.args.verbose", "0", "$obj")
    tcl += f"current_run -implementation [get_runs ${fileset}]\n"
    tcl +=  "catch {\n"
    tcl +=  "  if { $idrFlowPropertiesConstraints != {} } {\n"
    tcl +=  "    set_param runs.disableIDRFlowPropertyConstraints $idrFlowPropertiesConstraints\n"
    tcl +=  "  }\n"
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def createGadgets(synthFileset:String, implFileset: String): String = {
    var tcl = ""
    tcl +=  "set obj [get_dashboard_gadgets [list \"drc_1\"]]\n"
    tcl +=  this.setProperty("reports", f"${implFileset}#${implFileset}_route_report_drc_0", "$obj")
    tcl +=  "\n"
    tcl +=  "set obj [get_dashboard_gadgets [list \"methodology_1\"]]\n"
    tcl +=  this.setProperty("reports", f"${implFileset}#${implFileset}_route_report_methodology_0", "$obj")
    tcl +=  "\n"
    tcl +=  "set obj [get_dashboard_gadgets [list \"power_1\"]]\n"
    tcl +=  this.setProperty("reports", f"${implFileset}#${implFileset}_route_report_power_0", "$obj")
    tcl +=  "\n"
    tcl +=  "set obj [get_dashboard_gadgets [list \"timing_1\"]]\n"
    tcl +=  this.setProperty("reports", f"${implFileset}#${implFileset}_route_report_timing_summary_0", "$obj")
    tcl +=  "\n"
    tcl +=  "set obj [get_dashboard_gadgets [list \"utilization_1\"]]\n"
    tcl +=  this.setProperty("reports", f"${synthFileset}#${synthFileset}_synth_report_utilization_0", "$obj")
    tcl +=  this.setProperty("run.step", "synth_design", "$obj")
    tcl +=  this.setProperty("run.type", "synthesis", "$obj")
    tcl +=  "\n"
    tcl +=  "set obj [get_dashboard_gadgets [list \"utilization_2\"]]\n"
    tcl +=  this.setProperty("reports", f"${implFileset}#${implFileset}_place_report_utilization_0", "$obj")
    tcl +=  "\n"
    tcl +=  "move_dashboard_gadget -name {utilization_1} -row 0 -col 0\n"
    tcl +=  "move_dashboard_gadget -name {power_1} -row 1 -col 0\n"
    tcl +=  "move_dashboard_gadget -name {drc_1} -row 2 -col 0\n"
    tcl +=  "move_dashboard_gadget -name {timing_1} -row 0 -col 1\n"
    tcl +=  "move_dashboard_gadget -name {utilization_2} -row 1 -col 1\n"
    tcl +=  "move_dashboard_gadget -name {methodology_1} -row 2 -col 1\n"
    tcl +=  "\n"
    return tcl
  }

  def setTopModule(fileset: String): String = {
    var tcl = ""
    tcl +=  "set_property top design_1_wrapper [current_fileset]\n"
    tcl += f"update_compile_order -fileset ${fileset}\n"
    tcl +=  "\n"
    return tcl
  }

  def script(): String = {
    var tcl = ""

    // Create project
    Vivado.Project.fill("default")
    Vivado.Project.add("board_part_repo_paths", f"[file normalize \"~/.Xilinx/Vivado/${Vivado.version}/xhub/board_store/xilinx_board_store\"]")
    Vivado.Project.add("board_part"           , f"xilinx.com:${this.platform.get.board}:part0:${Vivado.getBoardVersion(this.platform.get.board)}")
    Vivado.Project.add("ip_output_repo"       , "$proj_dir/"+f"${this.platform.get.getName()}.cache/ip")
    Vivado.Project.add("platform.board_id"    , this.platform.get.board)
    Vivado.Project.add("sim.central_dir"      , "$proj_dir/"+f"${this.platform.get.getName()}.ip_user_files")
    tcl += Vivado.Project.getTCL()

    // Setup project
    //// Sources
    tcl += this.checkAndCreateFileset("sources_1", "srcset")
    tcl += this.addFileset("sources_1")
    tcl += this.setProperty("top", "design_1_wrapper", "$obj")
    //// Constraint
    tcl += this.checkAndCreateFileset("constrs_1", "constrset")
    tcl += this.setObject("get_filesets constrs_1")
    if (Constraints.mustImportConstraints())
      tcl += this.importConstraints("constrs_1")
    tcl += this.setObject("get_filesets constrs_1")
    //// Sim
    tcl += this.checkAndCreateFileset("sim_1", "simset")
    tcl += this.setObject("get_filesets sim_1")
    tcl += this.setProperty("top", "design_1_wrapper", "$obj")
    tcl += this.setProperty("top_lib", "xil_defaultlib", "$obj")
    //// Uilization
    tcl += this.setObject("get_filesets utils_1")
    tcl += this.setObject("get_filesets utils_1")
    tcl += this.addSources("sources_1")
    
    // Block design
    tcl += this.createDesign("design_1")
    tcl += this.checkIPs()
    tcl += this.print("INFO", 2020, f"Checking if the following modules exist in the project's sources: ${this.platform.get.getName()}.")+"\n"
    tcl += this.checkModules()
    tcl += this.createHierarchy()
    tcl += this.createBlock()

    // Instantiate IPs and interconnect them
    tcl += this.platform.get.getTCL()
    tcl += this.addXilinxIPs(this.platform.get)
    tcl += this.addInterfaces(this.platform.get.io)

    //// Save, validate, and wrap
    tcl += this.saveAndValidate()
    tcl += this.wrapDesign("sources_1")
    tcl += this.disableIDRFlow()

    // Setup synthesis
    Vivado.Synthesis.fill("default")
    tcl += Vivado.Synthesis.getTCL()

    // Setup implementation
    Vivado.Implementation.fill("default")
    tcl += Vivado.Implementation.getTCL()

    // vivado setup
    tcl += this.setImplmentationStrategy("impl_1")
    tcl += this.createGadgets("synth_1", "impl_1")

    // Go to bitstream
    tcl += this.setTopModule("sources_1")
    tcl += Vivado.Synthesis.perform()
    tcl += Vivado.Implementation.perform()
    tcl += Vivado.Implementation.bitstream()
    tcl += Vivado.Implementation.xsa()

    return tcl
  }

  def generate(): Unit = {
    // Generate tcl script as long string
    val tcl = this.script()
    // Check if destination exists; if not create.
    if (!new File("vivado").exists()) {
        if (!new File("vivado").mkdir()) {
          throw new RuntimeException("Can't create vivado directory")
        }
    }
    // Write script in destination
    val bw = new BufferedWriter(new FileWriter(new File(this.target), false))
    bw.write(tcl)
    bw.close
  }

}
