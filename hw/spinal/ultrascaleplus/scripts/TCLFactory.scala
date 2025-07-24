package ultrascaleplus.scripts


import java.io._


import spinal.core._
import spinal.lib.bus.misc.SizeMapping


import ultrascaleplus._
import ultrascaleplus.parameters.AddressMap
import ultrascaleplus.utils.{TCL}


object TCLFactory {

  var target    : String = "vivado/untitled.tcl"
  var moduleName: String = "untitled"
  var platform  : Option[UltraScalePlus] = None

  def apply(platform: UltraScalePlus): Unit = {
    this.platform = Some(platform)
    this.moduleName = platform.getClass.getSimpleName
    this.target = f"vivado/${this.moduleName}.tcl"
  }

  // Call from inside

  def setProperty(name: String, value: String, obj: String): String = {
    return "set_property -name \""+name+"\" -value \""+value+"\" -object "+obj+"\n"
  }
  
  def createProject(): String = {
    var tcl = ""
    tcl +=  "\n"
    tcl += f"create_project ${this.moduleName} ./vivado/${this.moduleName} -part ${this.platform.get.boardPart}\n"
    tcl +=  "set proj_dir [get_property directory [current_project]]\n"
    tcl +=  "\n"
    tcl +=  "set obj [current_project]\n"
    tcl +=  setProperty("board_part_repo_paths", f"[file normalize \"~/.Xilinx/Vivado/${Vivado.version}/xhub/board_store/xilinx_board_store\"]" , "$obj")
    tcl +=  setProperty("board_part", f"xilinx.com:${this.platform.get.board}:part0:${Vivado.getBoardVersion(this.platform.get.board)}", "$obj")
    tcl +=  setProperty("default_lib", "xil_defaultlib", "$obj")
    // TODO implement version/properties map
    if (Vivado.version != "2019.2")
      tcl +=  setProperty("enable_resource_estimation", "0", "$obj")
    tcl +=  setProperty("enable_vhdl_2008", "1", "$obj")
    tcl +=  setProperty("ip_cache_permissions", "read write", "$obj")
    tcl +=  setProperty("ip_output_repo", "$proj_dir/"+f"${this.moduleName}.cache/ip", "$obj")
    tcl +=  setProperty("mem.enable_memory_map_generation", "1", "$obj")
    tcl +=  setProperty("platform.board_id", this.platform.get.board, "$obj")
    // TODO implement version/properties map
    if (Vivado.version != "2019.2")
      tcl +=  setProperty("revised_directory_structure", "1", "$obj")
    tcl +=  setProperty("sim.central_dir", "$proj_dir/"+f"${this.moduleName}.ip_user_files", "$obj")
    tcl +=  setProperty("sim.ip.auto_export_scripts", "1", "$obj")
    // TODO implement version/properties map
    if (Vivado.version != "2019.2")
      tcl +=  setProperty("sim_compile_state", "1", "$obj")
    tcl +=  setProperty("sim_compile_state"               , "1"                                                                                            , "$obj")
    tcl +=  setProperty("webtalk.activehdl_export_sim"    , "1"                                                                                            , "$obj")
    tcl +=  setProperty("webtalk.modelsim_export_sim"     , "1"                                                                                            , "$obj")
    tcl +=  setProperty("webtalk.questa_export_sim"       , "1"                                                                                            , "$obj")
    tcl +=  setProperty("webtalk.riviera_export_sim"      , "1"                                                                                            , "$obj")
    tcl +=  setProperty("webtalk.vcs_export_sim"          , "1"                                                                                            , "$obj")
    tcl +=  setProperty("webtalk.xsim_export_sim"         , "1"                                                                                            , "$obj")
    tcl += "\n"
    return tcl
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

  def setObject(fileset: String): String = {
    return f"set obj [get_filesets ${fileset}]\n\n"
  }

  def importConstraints(fileset: String): String = {
    var tcl = ""
    tcl +=  "add_files -fileset constrs_1 -norecurse ./hw/gen/"+this.moduleName+".xdc\n"
    tcl +=  "import_files -fileset constrs_1 ./hw/gen/"+this.moduleName+".xdc\n"
    tcl +=  "\n"
    return tcl
  }

  def addFileset(fileset: String): String = {
    var tcl = ""
    tcl += f"set obj [get_filesets ${fileset}]\n"
    tcl +=  "set files [list [file normalize \"./hw/gen/"+this.moduleName+".v\"]]\n"
    tcl +=  "add_files -norecurse -fileset $obj $files\n"
    tcl +=  "\n"
    return tcl
  }

  def addSources(fileset: String): String = {
    val empty = "\"\"" 
    var tcl = ""
    tcl += f"if { [get_files ${this.moduleName}.v] == ${empty} } {\n"
    tcl += f"  import_files -quiet -fileset ${fileset} ./hw/gen/${this.moduleName}.v\n"
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
    tcl +=  "set list_mods_missing \"\"\n"
    tcl += f"foreach mod_vlnv ${this.moduleName} {\n"
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
    tcl += f"set block_name ${this.moduleName}\n"
    tcl += f"if { [catch {set ${this.moduleName} [create_bd_cell -type module -reference ${this.moduleName} ${this.moduleName}] } errmsg] } {\n"
    tcl +=  "  catch {"+this.print("ERROR", 2095, f"Unable to add referenced block <${this.moduleName}>. Please add the files for ${this.moduleName}'s definition into the project.")+"}\n"
    tcl +=  "  return 1\n"
    tcl +=  "} elseif { $"+this.moduleName+f" eq ${empty} } {\n"
    tcl +=  "  catch {"+this.print("ERROR", 2096, f"Unable to referenced block <${this.moduleName}>. Please add the files for ${this.moduleName}'s definition into the project.")+"}\n"
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
  
  def addInterfaces(bundle: Bundle): String = {
    var tcl = ""
    for ((name, element) <- bundle.elements) {
      // Bundle MUST stay at the last place!
      element match {
        case _:TCL      => tcl += element.asInstanceOf[TCL].getTCL(this.moduleName, "pl_clk0") // TODO: must be replace with variable
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
    val path = f"./vivado/${this.moduleName}/${this.moduleName}.gen/${fileset}/bd/design_1/hdl/design_1_wrapper.v"
    var tcl = "" 
    tcl +=  "if { [get_property IS_LOCKED [ get_files -norecurse design_1.bd]] == 1 } {\n"
    tcl +=  "  import_files -fileset sources_1 [file normalize \""+path+"\"]\n"
    tcl +=  "} else {\n"
    tcl += f"set wrapper_path [make_wrapper -fileset ${fileset} -files [ get_files -norecurse design_1.bd] -top]\n"
    tcl += f"add_files -norecurse -fileset ${fileset} "+"$wrapper_path\n"
    tcl += "}\n"
    tcl += "\n"
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

  def reportSynthesisUtilization(fileset: String): String = {
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_synth_report_utilization_0]\n"
    tcl +=  "\n"
    return tcl
  }

  def setSynthesisAsCurrentStep(fileset: String): String = {
    var tcl = ""
    tcl += f"current_run -synthesis [get_runs ${fileset}]\n"
    tcl +=  "\n"
    return tcl
  }

  def createImplementation(fileset: String): String = {
    val strategy = "\"Vivado Implementation Defaults\""
    val flow = f"\"Vivado Implementation ${Vivado.year}\""
    var tcl = ""
    tcl += f"set_property strategy ${strategy} [get_runs ${fileset}]\n"
    tcl += f"set_property flow ${flow} [get_runs ${fileset}]\n"
    tcl += f"set obj [get_runs ${fileset}]\n"
    tcl +=  "set_property set_report_strategy_name 1 $obj\n"
    tcl +=  "set_property report_strategy {Vivado Implementation Default Reports} $obj\n"
    tcl +=  "set_property set_report_strategy_name 0 $obj\n"
    tcl +=  "\n"
    return tcl
  }

  def reportTiming(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_init_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  set_property -name \"is_enabled\" -value \"0\" -objects $obj\n"
    tcl +=  "  set_property -name \"options.max_paths\" -value \"10\" -objects $obj\n"
    tcl +=  "  set_property -name \"options.report_unconstrained\" -value \"1\" -objects $obj\n"
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def reportDRC(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_opt_report_drc_0]\n\n"
  }

  def reportOptTiming(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_opt_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("is_enabled", "0", "$obj")
    tcl +=  "  "+this.setProperty("options.max_paths", "10", "$obj")
    tcl +=  "  "+this.setProperty("options.report_unconstrained", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def reportOptPower(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_power_opt_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("is_enabled", "0", "$obj")
    tcl +=  "  "+this.setProperty("options.max_paths", "10", "$obj")
    tcl +=  "  "+this.setProperty("options.report_unconstrained", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }
  
  def reportPlaceIO(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_place_report_io_0]\n\n"
  }

  def reportPlaceUtilization(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_place_report_utilization_0]\n\n"
  }

  def reportPlaceControlSets(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_place_report_control_sets_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("options.verbose", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def reportPlaceIncremental(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_place_report_incremental_reuse_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("is_enabled", "0", "$obj")
    tcl +=  "}\n"
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_place_report_incremental_reuse_1]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("is_enabled", "0", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def reportPlaceTiming(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_place_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("is_enabled", "0", "$obj")
    tcl +=  "  "+this.setProperty("options.max_paths", "10", "$obj")
    tcl +=  "  "+this.setProperty("options.report_unconstrained", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }
  
  def reportPostPlacePower(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_post_place_power_opt_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("is_enabled", "0", "$obj")
    tcl +=  "  "+this.setProperty("options.max_paths", "10", "$obj")
    tcl +=  "  "+this.setProperty("options.report_unconstrained", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }
  
  def reportPhysTiming(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_phys_opt_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("is_enabled", "0", "$obj")
    tcl +=  "  "+this.setProperty("options.max_paths", "10", "$obj")
    tcl +=  "  "+this.setProperty("options.report_unconstrained", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }
  
  def reportRouteDRC(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_drc_0]\n\n"
  }

  def reportRouteMethodology(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_methodology_0]\n\n"
  }

  def reportRoutePower(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_power_0]\n\n"
  }

  def reportRouteStatus(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_status_0]\n\n"
  }

  def reportRouteTiming(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("options.max_paths", "10", "$obj")
    tcl +=  "  "+this.setProperty("options.report_unconstrained", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def reportRouteIncremental(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_incremental_reuse_0]\n"
  }

  def reportRouteClockUtilization(fileset: String): String = {
    return f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_clock_utilization_0]\n"
  }

  def reportRouteBusSkew(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_route_report_bus_skew_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("options.warn_on_violation", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }
  
  def reportPostRoutePhysTiming(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_post_route_phys_opt_report_timing_summary_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("options.max_paths", "10", "$obj")
    tcl +=  "  "+this.setProperty("options.report_unconstrained", "1", "$obj")
    tcl +=  "  "+this.setProperty("options.warn_on_violation", "1", "$obj")
    tcl +=  "}\n"
    tcl +=  "\n"
    return tcl
  }

  def reportPostRoutePhysBusSkew(fileset: String): String = {
    val empty = "\"\""
    val obj = "$obj"
    var tcl = ""
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_post_route_phys_opt_report_bus_skew_0]\n"
    tcl += f"if { ${obj} != ${empty} } {\n"
    tcl +=  "  "+this.setProperty("options.warn_on_violation", "1", "$obj")
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

  def synthesize(fileset: String): String = {
    var tcl = ""
    tcl += f"launch_runs ${fileset} -jobs 4\n"
    tcl += f"wait_on_run ${fileset}\n"
    tcl +=  "\n"
    return tcl
  }

  def implement(fileset: String): String = {
    var tcl = ""
    tcl += f"launch_runs ${fileset} -to_step write_bitstream -jobs 4\n"
    tcl += f"wait_on_run ${fileset}\n"
    tcl +=  "\n"
    return tcl
  }

  def getBitstream(fileset: String): String = {
    return f"file copy -force ./vivado/${this.moduleName}/${this.moduleName}.runs/${fileset}/design_1_wrapper.bit ./${this.moduleName}.bit\n"
  }

  def script(): String = {
    var tcl = ""

    // Create project
    tcl += this.createProject()

    // Setup project
    //// Sources
    tcl += this.checkAndCreateFileset("sources_1", "srcset")
    tcl += this.addFileset("sources_1")
    tcl += this.setProperty("top", "design_1_wrapper", "$obj")
    //// Constraint
    tcl += this.checkAndCreateFileset("constrs_1", "constrset")
    tcl += this.setObject("constrs_1")
    if (Constraints.mustImportConstraints())
      tcl += this.importConstraints("constrs_1")
    tcl += this.setObject("constrs_1")
    //// Sim
    tcl += this.checkAndCreateFileset("sim_1", "simset")
    tcl += this.setObject("sim_1")
    tcl += this.setProperty("top", "design_1_wrapper", "$obj")
    tcl += this.setProperty("top_lib", "xil_defaultlib", "$obj")
    //// Uilization
    tcl += this.setObject("utils_1")
    tcl += this.setObject("utils_1")
    tcl += this.addSources("sources_1")
    
    // Block design
    tcl += this.print("INFO", 2010, "Currently there is no design <design_1> in project, so creating one...")+"\n"
    tcl += this.createDesign("design_1")
    tcl += this.print("INFO", 2011, "Checking if the selected IPs exist in the project's IP catalog.")+"\n"
    tcl += this.checkIPs()
    tcl += this.print("INFO", 2020, f"Checking if the following modules exist in the project's sources: ${this.moduleName}.")+"\n"
    tcl += this.checkModules()
    tcl += this.createHierarchy()
    tcl += this.createBlock()
    tcl += this.instantiateResetSystem()
    tcl += this.platform.get.getTCL()
    //// Connections
    ////// Clock and reset
    tcl += this.netConnection("pl_clk0", Seq(f"${this.moduleName}/clk", "reset_system/slowest_sync_clk", "processing_system/pl_clk0"))
    tcl += this.netConnection("periph_reset", Seq("reset_system/peripheral_reset", f"${this.moduleName}/reset"))
    tcl += this.netConnection("pl_resetn", Seq("processing_system/pl_resetn0", "reset_system/ext_reset_in"))
    tcl += "\n"
    ////// Buses
    tcl += this.addInterfaces(this.platform.get.io)
    //// Save, validate, and wrap
    tcl += this.saveAndValidate()
    tcl += this.wrapDesign("sources_1")
    tcl += this.disableIDRFlow()

    // Setup synthesis
    Vivado.Synthesis.setMode("default")
    tcl += Vivado.Synthesis.getTCL()
    tcl += this.reportSynthesisUtilization("synth_1")
    tcl += this.setSynthesisAsCurrentStep("synth_1")

    // Setup implementation
    Vivado.Implementation.setMode("default")
    tcl += Vivado.Implementation.getTCL()

    // Setup reports
    tcl += this.reportTiming("impl_1")
    tcl += this.reportDRC("impl_1")
    tcl += this.reportOptTiming("impl_1")
    tcl += this.reportOptPower("impl_1")
    tcl += this.reportPlaceIO("impl_1")
    tcl += this.reportPlaceUtilization("impl_1")
    tcl += this.reportPlaceControlSets("impl_1")
    tcl += this.reportPlaceIncremental("impl_1")
    tcl += this.reportPlaceTiming("impl_1")
    tcl += this.reportPostPlacePower("impl_1")
    tcl += this.reportPhysTiming("impl_1")
    tcl += this.reportRouteDRC("impl_1")
    tcl += this.reportRouteMethodology("impl_1")
    tcl += this.reportRoutePower("impl_1")
    tcl += this.reportRouteStatus("impl_1")
    tcl += this.reportRouteTiming("impl_1")
    tcl += this.reportRouteIncremental("impl_1")
    tcl += this.reportRouteClockUtilization("impl_1")
    tcl += this.reportRouteBusSkew("impl_1")
    tcl += this.reportPostRoutePhysTiming("impl_1")
    tcl += this.reportPostRoutePhysBusSkew("impl_1")

    // vivado setup
    tcl += this.setImplmentationStrategy("impl_1")
    tcl += this.createGadgets("synth_1", "impl_1")

    // Go to bitstream
    tcl += this.setTopModule("sources_1")
    tcl += this.synthesize("synth_1")
    tcl += this.implement("impl_1")
    tcl += this.getBitstream("impl_1")

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
