package ultrascaleplus.scripts


import java.io._

import spinal.core._
import spinal.lib.bus.misc.SizeMapping

import kv260._
import ultrascaleplus.parameters.AddressMap


object TCLFactory {

  var target    : String = "vivado/untitled.tcl"
  var moduleName: String = "untitled"
  var platform  : Option[KV260] = None

  def apply(platform: KV260): Unit = {
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
    tcl += f"create_project ${this.moduleName} ./vivado/${this.moduleName} -part xck26-sfvc784-2LV-c\n"
    tcl +=  "set proj_dir [get_property directory [current_project]]\n"
    tcl +=  "\n"
    tcl +=  "set obj [current_project]\n"
    tcl +=  setProperty("board_part_repo_paths", "[file normalize \"~/.Xilinx/Vivado/2022.2/xhub/board_store/xilinx_board_store\"]" , "$obj")
    tcl +=  setProperty("board_part", "xilinx.com:kv260_som:part0:1.4", "$obj")
    tcl +=  setProperty("default_lib", "xil_defaultlib", "$obj")
    tcl +=  setProperty("enable_resource_estimation", "0", "$obj")
    tcl +=  setProperty("enable_vhdl_2008", "1", "$obj")
    tcl +=  setProperty("ip_cache_permissions", "read write", "$obj")
    tcl +=  setProperty("ip_output_repo", "$proj_dir/"+f"${this.moduleName}.cache/ip", "$obj")
    tcl +=  setProperty("mem.enable_memory_map_generation", "1", "$obj")
    tcl +=  setProperty("platform.board_id", "kv260_som", "$obj")
    tcl +=  setProperty("revised_directory_structure", "1", "$obj")
    tcl +=  setProperty("sim.central_dir", "$proj_dir/"+f"${this.moduleName}.ip_user_files", "$obj")
    tcl +=  setProperty("sim.ip.auto_export_scripts", "1", "$obj")
    tcl +=  setProperty("sim_compile_state", "1", "$obj")
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
    return f"common::send_gid_msg -ssname BD::TCL -id ${id}"+" -severity \""+level+"\" \""+message+"\""
  }

  def createDesign(name: String): String = {
    return f"create_bd_design ${name}\n"
  }

  def checkIPs(): String = {
    var tcl = ""
    tcl += "set list_check_ips \"xilinx.com:ip:proc_sys_reset:5.0 xilinx.com:ip:zynq_ultra_ps_e:3.4\"\n"
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

  def instantiateProcessingSystem(): String = {
    var tcl = ""
    tcl += "set processing_system [ create_bd_cell -type ip -vlnv xilinx.com:ip:zynq_ultra_ps_e:3.4 processing_system ]\n"
    tcl += "set_property -dict [list \\\n"
    tcl += "  CONFIG.PSU_BANK_0_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_BANK_1_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_BANK_2_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_BANK_3_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_DDR_RAM_HIGHADDR {0xFFFFFFFF} \\\n"
    tcl += "  CONFIG.PSU_DDR_RAM_HIGHADDR_OFFSET {0x800000000} \\\n"
    tcl += "  CONFIG.PSU_DDR_RAM_LOWADDR_OFFSET {0x80000000} \\\n"
    tcl += "  CONFIG.PSU_MIO_0_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_0_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_10_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_10_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_11_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_11_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_12_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_12_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_12_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_13_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_13_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_13_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_14_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_14_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_14_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_15_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_15_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_15_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_16_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_16_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_16_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_17_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_17_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_17_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_18_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_18_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_18_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_19_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_19_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_19_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_1_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_1_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_20_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_20_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_20_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_21_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_21_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_21_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_INPUT_TYPE {cmos} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_23_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_23_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_23_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_24_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_24_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_25_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_25_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_27_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_27_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_27_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_28_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_29_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_29_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_29_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_2_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_2_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_30_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_32_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_32_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_32_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_33_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_33_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_33_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_34_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_34_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_34_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_35_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_35_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_36_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_36_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_36_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_37_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_38_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_38_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_38_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_39_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_39_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_39_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_3_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_3_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_40_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_40_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_40_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_41_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_41_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_41_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_42_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_42_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_42_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_43_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_43_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_43_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_44_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_44_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_44_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_45_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_46_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_46_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_46_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_47_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_47_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_47_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_48_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_48_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_48_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_49_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_49_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_49_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_4_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_4_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_50_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_50_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_50_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_INPUT_TYPE {cmos} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_54_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_54_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_56_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_56_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_57_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_57_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_58_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_58_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_59_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_59_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_5_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_5_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_60_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_60_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_61_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_61_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_62_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_62_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_63_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_63_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_64_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_64_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_65_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_65_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_66_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_66_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_67_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_67_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_68_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_68_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_69_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_69_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_6_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_6_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_76_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_76_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_77_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_77_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_7_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_7_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_7_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_8_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_8_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_8_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_9_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_9_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_TREE_PERIPHERALS {Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#SPI 1#GPIO0 MIO#GPIO0 MIO#SPI 1#SPI 1#SPI 1#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0\\\n"
    tcl += "MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#I2C 1#I2C 1#PMU GPI 0#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#PMU GPI 5#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#PMU GPO 3#GPIO1\\\n"
    tcl += "MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO##########################} \\\n"
    tcl += "  CONFIG.PSU_MIO_TREE_SIGNALS {sclk_out#miso_mo1#mo2#mo3#mosi_mi0#n_ss_out#sclk_out#gpio0[7]#gpio0[8]#n_ss_out[0]#miso#mosi#gpio0[12]#gpio0[13]#gpio0[14]#gpio0[15]#gpio0[16]#gpio0[17]#gpio0[18]#gpio0[19]#gpio0[20]#gpio0[21]#gpio0[22]#gpio0[23]#scl_out#sda_out#gpi[0]#gpio1[27]#gpio1[28]#gpio1[29]#gpio1[30]#gpi[5]#gpio1[32]#gpio1[33]#gpio1[34]#gpo[3]#gpio1[36]#gpio1[37]#gpio1[38]#gpio1[39]#gpio1[40]#gpio1[41]#gpio1[42]#gpio1[43]#gpio1[44]#gpio1[45]#gpio1[46]#gpio1[47]#gpio1[48]#gpio1[49]#gpio1[50]#gpio1[51]##########################}\\\n"
    tcl += "\\\n"
    tcl += "  CONFIG.PSU__ACT_DDR_FREQ_MHZ {1066.656006} \\\n"
    tcl += "  CONFIG.PSU__AFI0_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__AFI1_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__CAN1__PERIPHERAL__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU_CTRL__ACT_FREQMHZ {1333.333008} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU_CTRL__FREQMHZ {1333.333} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU_CTRL__SRCSEL {APLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU__FRAC_ENABLED {1} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__APLL_CTRL__FRACFREQ {1333.333} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__APLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__APLL_FRAC_CFG__ENABLED {1} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_FPD_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_FPD_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_FPD_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TRACE_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TRACE_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TSTMP_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TSTMP_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TSTMP_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DDR_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DDR_CTRL__FREQMHZ {1200} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DDR_CTRL__SRCSEL {DPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPDMA_REF_CTRL__ACT_FREQMHZ {444.444336} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPDMA_REF_CTRL__FREQMHZ {600} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPDMA_REF_CTRL__SRCSEL {APLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GDMA_REF_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GDMA_REF_CTRL__FREQMHZ {600} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GDMA_REF_CTRL__SRCSEL {DPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GPU_REF_CTRL__ACT_FREQMHZ {499.994995} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GPU_REF_CTRL__FREQMHZ {600} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GPU_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_LSBUS_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_LSBUS_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_LSBUS_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_MAIN_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_MAIN_CTRL__FREQMHZ {533.33} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_MAIN_CTRL__SRCSEL {DPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__VPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__ADMA_REF_CTRL__ACT_FREQMHZ {499.994995} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__ADMA_REF_CTRL__FREQMHZ {500} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__ADMA_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__AMS_REF_CTRL__ACT_FREQMHZ {49.999500} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CAN1_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CPU_R5_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CPU_R5_CTRL__FREQMHZ {533.333} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CPU_R5_CTRL__SRCSEL {RPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DBG_LPD_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DBG_LPD_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DBG_LPD_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DLL_REF_CTRL__ACT_FREQMHZ {999.989990} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__I2C1_REF_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__I2C1_REF_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__I2C1_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOU_SWITCH_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOU_SWITCH_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOU_SWITCH_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_LSBUS_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_LSBUS_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_LSBUS_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_SWITCH_CTRL__ACT_FREQMHZ {499.994995} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_SWITCH_CTRL__FREQMHZ {500} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_SWITCH_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PCAP_CTRL__ACT_FREQMHZ {199.998001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PCAP_CTRL__FREQMHZ {200} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PCAP_CTRL__SRCSEL {IOPLL} \\\n"
    tcl +=f"  CONFIG.PSU__CRL_APB__PL0_REF_CTRL__ACT_FREQMHZ {${this.platform.get.actualFrequency.decompose._1}} \\\n"
    tcl +=f"  CONFIG.PSU__CRL_APB__PL0_REF_CTRL__FREQMHZ {${this.platform.get.actualFrequency.decompose._1}} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PL0_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PL1_REF_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__QSPI_REF_CTRL__ACT_FREQMHZ {124.998749} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__QSPI_REF_CTRL__FREQMHZ {125} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__QSPI_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__RPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__SPI1_REF_CTRL__ACT_FREQMHZ {199.998001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__SPI1_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__TIMESTAMP_REF_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__TIMESTAMP_REF_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__TIMESTAMP_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CSUPMU__PERIPHERAL__VALID {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__BG_ADDR_COUNT {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__BRC_MAPPING {ROW_BANK_COL} \\\n"
    tcl += "  CONFIG.PSU__DDRC__BUS_WIDTH {64 Bit} \\\n"
    tcl += "  CONFIG.PSU__DDRC__CL {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__CLOCK_STOP_EN {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__COMPONENTS {Components} \\\n"
    tcl += "  CONFIG.PSU__DDRC__CWL {14} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_ADDR_MAPPING {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_CAL_MODE_ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_CRC_CONTROL {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_T_REF_MODE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_T_REF_RANGE {Normal (0-85)} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DEVICE_CAPACITY {8192 MBits} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DM_DBI {DM_NO_DBI} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DRAM_WIDTH {16 Bits} \\\n"
    tcl += "  CONFIG.PSU__DDRC__ECC {Disabled} \\\n"
    tcl += "  CONFIG.PSU__DDRC__FGRM {1X} \\\n"
    tcl += "  CONFIG.PSU__DDRC__LP_ASR {manual normal} \\\n"
    tcl += "  CONFIG.PSU__DDRC__MEMORY_TYPE {DDR 4} \\\n"
    tcl += "  CONFIG.PSU__DDRC__PARITY_ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__PER_BANK_REFRESH {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__PHY_DBI_MODE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__RANK_ADDR_COUNT {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__ROW_ADDR_COUNT {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__SELF_REF_ABORT {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__SPEED_BIN {DDR4_2400R} \\\n"
    tcl += "  CONFIG.PSU__DDRC__STATIC_RD_MODE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__TRAIN_DATA_EYE {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__TRAIN_READ_GATE {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__TRAIN_WRITE_LEVEL {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_FAW {30.0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RAS_MIN {33} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RC {47.06} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RCD {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RP {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__VREF {1} \\\n"
    tcl += "  CONFIG.PSU__DDR_HIGH_ADDRESS_GUI_ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__DDR__INTERFACE__FREQMHZ {600.000} \\\n"
    tcl += "  CONFIG.PSU__FPD_SLCR__WDT1__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__FPGA_PL0_ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__FPGA_PL1_ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__GPIO0_MIO__IO {MIO 0 .. 25} \\\n"
    tcl += "  CONFIG.PSU__GPIO0_MIO__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__GPIO1_MIO__IO {MIO 26 .. 51} \\\n"
    tcl += "  CONFIG.PSU__GPIO1_MIO__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__I2C0__PERIPHERAL__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__I2C1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__I2C1__PERIPHERAL__IO {MIO 24 .. 25} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC0_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC1_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC2_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC3_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC0__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC1__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC2__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC3__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__WDT0__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__LPD_SLCR__CSUPMU__ACT_FREQMHZ {100.000000} \\\n"
    if (this.platform.get.io.withFPD_HPM0)
      tcl += "  CONFIG.PSU__MAXIGP0__DATA_WIDTH {128} \\\n"
    if (this.platform.get.io.withFPD_HPM1)
      tcl += "  CONFIG.PSU__MAXIGP1__DATA_WIDTH {128} \\\n"
    if (this.platform.get.io.withLPD_HPM0)
      tcl += "  CONFIG.PSU__MAXIGP2__DATA_WIDTH {128} \\\n"
    tcl += "  CONFIG.PSU__OVERRIDE__BASIC_CLOCK {0} \\\n"
    tcl += "  CONFIG.PSU__PL_CLK0_BUF {TRUE} \\\n"
    tcl += "  CONFIG.PSU__PMU_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__AIBACK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__EMIO_GPI__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__EMIO_GPO__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI0__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI0__IO {MIO 26} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI1__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI2__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI3__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI4__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI5__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI5__IO {MIO 31} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO0__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO1__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO2__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO3__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO3__IO {MIO 35} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO3__POLARITY {low} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO4__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO5__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__PLERROR__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PRESET_APPLIED {1} \\\n"
    tcl += "  CONFIG.PSU__PROTECTION__MASTERS {USB1:NonSecure;0|USB0:NonSecure;0|S_AXI_LPD:NA;0|S_AXI_HPC1_FPD:NA;1|S_AXI_HPC0_FPD:NA;1|S_AXI_HP3_FPD:NA;1|S_AXI_HP2_FPD:NA;1|S_AXI_HP1_FPD:NA;1|S_AXI_HP0_FPD:NA;1|S_AXI_ACP:NA;0|S_AXI_ACE:NA;0|SD1:NonSecure;0|SD0:NonSecure;0|SATA1:NonSecure;0|SATA0:NonSecure;0|RPU1:Secure;1|RPU0:Secure;1|QSPI:NonSecure;1|PMU:NA;1|PCIe:NonSecure;0|NAND:NonSecure;0|LDMA:NonSecure;1|GPU:NonSecure;1|GEM3:NonSecure;0|GEM2:NonSecure;0|GEM1:NonSecure;0|GEM0:NonSecure;0|FDMA:NonSecure;1|DP:NonSecure;0|DAP:NA;1|Coresight:NA;1|CSU:NA;1|APU:NA;1} \\\n"
    tcl += "  \\\n"
    tcl += "  CONFIG.PSU__PROTECTION__SLAVES {LPD;USB3_1_XHCI;FE300000;FE3FFFFF;0|LPD;USB3_1;FF9E0000;FF9EFFFF;0|LPD;USB3_0_XHCI;FE200000;FE2FFFFF;0|LPD;USB3_0;FF9D0000;FF9DFFFF;0|LPD;UART1;FF010000;FF01FFFF;0|LPD;UART0;FF000000;FF00FFFF;0|LPD;TTC3;FF140000;FF14FFFF;1|LPD;TTC2;FF130000;FF13FFFF;1|LPD;TTC1;FF120000;FF12FFFF;1|LPD;TTC0;FF110000;FF11FFFF;1|FPD;SWDT1;FD4D0000;FD4DFFFF;1|LPD;SWDT0;FF150000;FF15FFFF;1|LPD;SPI1;FF050000;FF05FFFF;1|LPD;SPI0;FF040000;FF04FFFF;0|FPD;SMMU_REG;FD5F0000;FD5FFFFF;1|FPD;SMMU;FD800000;FDFFFFFF;1|FPD;SIOU;FD3D0000;FD3DFFFF;1|FPD;SERDES;FD400000;FD47FFFF;1|LPD;SD1;FF170000;FF17FFFF;0|LPD;SD0;FF160000;FF16FFFF;0|FPD;SATA;FD0C0000;FD0CFFFF;0|LPD;RTC;FFA60000;FFA6FFFF;1|LPD;RSA_CORE;FFCE0000;FFCEFFFF;1|LPD;RPU;FF9A0000;FF9AFFFF;1|LPD;R5_TCM_RAM_GLOBAL;FFE00000;FFE3FFFF;1|LPD;R5_1_Instruction_Cache;FFEC0000;FFECFFFF;1|LPD;R5_1_Data_Cache;FFED0000;FFEDFFFF;1|LPD;R5_1_BTCM_GLOBAL;FFEB0000;FFEBFFFF;1|LPD;R5_1_ATCM_GLOBAL;FFE90000;FFE9FFFF;1|LPD;R5_0_Instruction_Cache;FFE40000;FFE4FFFF;1|LPD;R5_0_Data_Cache;FFE50000;FFE5FFFF;1|LPD;R5_0_BTCM_GLOBAL;FFE20000;FFE2FFFF;1|LPD;R5_0_ATCM_GLOBAL;FFE00000;FFE0FFFF;1|LPD;QSPI_Linear_Address;C0000000;DFFFFFFF;1|LPD;QSPI;FF0F0000;FF0FFFFF;1|LPD;PMU_RAM;FFDC0000;FFDDFFFF;1|LPD;PMU_GLOBAL;FFD80000;FFDBFFFF;1|FPD;PCIE_MAIN;FD0E0000;FD0EFFFF;0|FPD;PCIE_LOW;E0000000;EFFFFFFF;0|FPD;PCIE_HIGH2;8000000000;BFFFFFFFFF;0|FPD;PCIE_HIGH1;600000000;7FFFFFFFF;0|FPD;PCIE_DMA;FD0F0000;FD0FFFFF;0|FPD;PCIE_ATTRIB;FD480000;FD48FFFF;0|LPD;OCM_XMPU_CFG;FFA70000;FFA7FFFF;1|LPD;OCM_SLCR;FF960000;FF96FFFF;1|OCM;OCM;FFFC0000;FFFFFFFF;1|LPD;NAND;FF100000;FF10FFFF;0|LPD;MBISTJTAG;FFCF0000;FFCFFFFF;1|LPD;LPD_XPPU_SINK;FF9C0000;FF9CFFFF;1|LPD;LPD_XPPU;FF980000;FF98FFFF;1|LPD;LPD_SLCR_SECURE;FF4B0000;FF4DFFFF;1|LPD;LPD_SLCR;FF410000;FF4AFFFF;1|LPD;LPD_GPV;FE100000;FE1FFFFF;1|LPD;LPD_DMA_7;FFAF0000;FFAFFFFF;1|LPD;LPD_DMA_6;FFAE0000;FFAEFFFF;1|LPD;LPD_DMA_5;FFAD0000;FFADFFFF;1|LPD;LPD_DMA_4;FFAC0000;FFACFFFF;1|LPD;LPD_DMA_3;FFAB0000;FFABFFFF;1|LPD;LPD_DMA_2;FFAA0000;FFAAFFFF;1|LPD;LPD_DMA_1;FFA90000;FFA9FFFF;1|LPD;LPD_DMA_0;FFA80000;FFA8FFFF;1|LPD;IPI_CTRL;FF380000;FF3FFFFF;1|LPD;IOU_SLCR;FF180000;FF23FFFF;1|LPD;IOU_SECURE_SLCR;FF240000;FF24FFFF;1|LPD;IOU_SCNTRS;FF260000;FF26FFFF;1|LPD;IOU_SCNTR;FF250000;FF25FFFF;1|LPD;IOU_GPV;FE000000;FE0FFFFF;1|LPD;I2C1;FF030000;FF03FFFF;1|LPD;I2C0;FF020000;FF02FFFF;0|FPD;GPU;FD4B0000;FD4BFFFF;1|LPD;GPIO;FF0A0000;FF0AFFFF;1|LPD;GEM3;FF0E0000;FF0EFFFF;0|LPD;GEM2;FF0D0000;FF0DFFFF;0|LPD;GEM1;FF0C0000;FF0CFFFF;0|LPD;GEM0;FF0B0000;FF0BFFFF;0|FPD;FPD_XMPU_SINK;FD4F0000;FD4FFFFF;1|FPD;FPD_XMPU_CFG;FD5D0000;FD5DFFFF;1|FPD;FPD_SLCR_SECURE;FD690000;FD6CFFFF;1|FPD;FPD_SLCR;FD610000;FD68FFFF;1|FPD;FPD_DMA_CH7;FD570000;FD57FFFF;1|FPD;FPD_DMA_CH6;FD560000;FD56FFFF;1|FPD;FPD_DMA_CH5;FD550000;FD55FFFF;1|FPD;FPD_DMA_CH4;FD540000;FD54FFFF;1|FPD;FPD_DMA_CH3;FD530000;FD53FFFF;1|FPD;FPD_DMA_CH2;FD520000;FD52FFFF;1|FPD;FPD_DMA_CH1;FD510000;FD51FFFF;1|FPD;FPD_DMA_CH0;FD500000;FD50FFFF;1|LPD;EFUSE;FFCC0000;FFCCFFFF;1|FPD;Display \\\n"
    tcl += "  Port;FD4A0000;FD4AFFFF;0|FPD;DPDMA;FD4C0000;FD4CFFFF;0|FPD;DDR_XMPU5_CFG;FD050000;FD05FFFF;1|FPD;DDR_XMPU4_CFG;FD040000;FD04FFFF;1|FPD;DDR_XMPU3_CFG;FD030000;FD03FFFF;1|FPD;DDR_XMPU2_CFG;FD020000;FD02FFFF;1|FPD;DDR_XMPU1_CFG;FD010000;FD01FFFF;1|FPD;DDR_XMPU0_CFG;FD000000;FD00FFFF;1|FPD;DDR_QOS_CTRL;FD090000;FD09FFFF;1|FPD;DDR_PHY;FD080000;FD08FFFF;1|DDR;DDR_LOW;0;7FFFFFFF;1|DDR;DDR_HIGH;800000000;87FFFFFFF;1|FPD;DDDR_CTRL;FD070000;FD070FFF;1|LPD;Coresight;FE800000;FEFFFFFF;1|LPD;CSU_DMA;FFC80000;FFC9FFFF;1|LPD;CSU;FFCA0000;FFCAFFFF;1|LPD;CRL_APB;FF5E0000;FF85FFFF;1|FPD;CRF_APB;FD1A0000;FD2DFFFF;1|FPD;CCI_REG;FD5E0000;FD5EFFFF;1|LPD;CAN1;FF070000;FF07FFFF;0|LPD;CAN0;FF060000;FF06FFFF;0|FPD;APU;FD5C0000;FD5CFFFF;1|LPD;APM_INTC_IOU;FFA20000;FFA2FFFF;1|LPD;APM_FPD_LPD;FFA30000;FFA3FFFF;1|FPD;APM_5;FD490000;FD49FFFF;1|FPD;APM_0;FD0B0000;FD0BFFFF;1|LPD;APM2;FFA10000;FFA1FFFF;1|LPD;APM1;FFA00000;FFA0FFFF;1|LPD;AMS;FFA50000;FFA5FFFF;1|FPD;AFI_5;FD3B0000;FD3BFFFF;1|FPD;AFI_4;FD3A0000;FD3AFFFF;1|FPD;AFI_3;FD390000;FD39FFFF;1|FPD;AFI_2;FD380000;FD38FFFF;1|FPD;AFI_1;FD370000;FD37FFFF;1|FPD;AFI_0;FD360000;FD36FFFF;1|LPD;AFIFM6;FF9B0000;FF9BFFFF;1|FPD;ACPU_GIC;F9010000;F907FFFF;1} \\\n"
    tcl += "  \\\n"
    tcl += "  CONFIG.PSU__PSS_REF_CLK__FREQMHZ {33.333} \\\n"
    tcl += "  CONFIG.PSU__QSPI_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__QSPI_ROUTE_THROUGH_FPD {0} \\\n"
    tcl += "  CONFIG.PSU__QSPI__GRP_FBCLK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__DATA_MODE {x4} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__IO {MIO 0 .. 5} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__MODE {Single} \\\n"
    if (this.platform.get.io.withFPD_HPC0)
      tcl += "  CONFIG.PSU__SAXIGP0__DATA_WIDTH {128} \\\n"
    if (this.platform.get.io.withFPD_HPC1)
      tcl += "  CONFIG.PSU__SAXIGP1__DATA_WIDTH {128} \\\n"
    if (this.platform.get.io.withFPD_HP0)
      tcl += "  CONFIG.PSU__SAXIGP2__DATA_WIDTH {128} \\\n"
    if (this.platform.get.io.withFPD_HP1)
      tcl += "  CONFIG.PSU__SAXIGP3__DATA_WIDTH {128} \\\n"
    if (this.platform.get.io.withFPD_HP2)
      tcl += "  CONFIG.PSU__SAXIGP4__DATA_WIDTH {128} \\\n"
    if (this.platform.get.io.withFPD_HP3)
      tcl += "  CONFIG.PSU__SAXIGP5__DATA_WIDTH {128} \\\n"
    tcl += "  CONFIG.PSU__SPI1__GRP_SS0__IO {MIO 9} \\\n"
    tcl += "  CONFIG.PSU__SPI1__GRP_SS1__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SPI1__GRP_SS2__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SPI1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__SPI1__PERIPHERAL__IO {MIO 6 .. 11} \\\n"
    tcl += "  CONFIG.PSU__SWDT0__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SWDT0__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__SWDT0__RESET__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SWDT1__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SWDT1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__SWDT1__RESET__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC0__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC0__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC0__WAVEOUT__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC1__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC1__WAVEOUT__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC2__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC2__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC2__WAVEOUT__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC3__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC3__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC3__WAVEOUT__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__USE__IRQ0 {0} \\\n"
    tcl +=f"  CONFIG.PSU__USE__M_AXI_GP0 {${this.platform.get.io.withFPD_HPM0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__M_AXI_GP1 {${this.platform.get.io.withFPD_HPM1.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__M_AXI_GP2 {${this.platform.get.io.withLPD_HPM0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP0 {${this.platform.get.io.withFPD_HPC0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP1 {${this.platform.get.io.withFPD_HPC1.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP2 {${this.platform.get.io.withFPD_HP0.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP3 {${this.platform.get.io.withFPD_HP1.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP4 {${this.platform.get.io.withFPD_HP2.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP5 {${this.platform.get.io.withFPD_HP3.toInt }} \\\n"
    tcl += "] $processing_system\n"
    tcl += "\n"
    return tcl
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
  
  val portIndex = Map.apply("HPC0" -> 0, "HPC1" -> 1, "HP0" -> 2, "HP1" -> 3, "HP2" -> 4, "HP3" -> 5)

  def addSecondaryPort(port: String, clock: String): String = {
    val upper = port.toUpperCase()
    val lower = port.toLowerCase()
    val index = this.portIndex(upper)
    var tcl = ""
    tcl += netConnection(clock, Seq(f"processing_system/saxi${lower}_fpd_aclk"))
    tcl += interfaceConnection(f"${this.moduleName}_io_fpd_${lower}", Seq(f"${this.moduleName}/io_fpd_${lower}", f"processing_system/S_AXI_${upper}_FPD"))
    tcl += addressMap(AddressMap.HIGH_DDR.base, AddressMap.HIGH_DDR.size, f"${this.moduleName}/io_fpd_${lower}", f"processing_system/SAXIGP${index}/${upper}_DDR_HIGH")
    tcl += addressMap(AddressMap.LOW_DDR.base , AddressMap.LOW_DDR.size , f"${this.moduleName}/io_fpd_${lower}", f"processing_system/SAXIGP${index}/${upper}_DDR_LOW")
    tcl += addressMap(AddressMap.OCM.base     , AddressMap.OCM.size     , f"${this.moduleName}/io_fpd_${lower}", f"processing_system/SAXIGP${index}/${upper}_LPS_OCM")
    tcl += addressMap(AddressMap.QSPI.base    , AddressMap.QSPI.size    , f"${this.moduleName}/io_fpd_${lower}", f"processing_system/SAXIGP${index}/${upper}_QSPI")
    tcl += "\n"
    return tcl
  }

  def addPrimaryPort(port: String, domain: String, clock: String, range: SizeMapping): String = {
    val portUpper = port.toUpperCase()
    val portLower = port.toLowerCase()
    val domainUpper = domain.toUpperCase()
    val domainLower = domain.toLowerCase()
    var tcl = ""
    tcl += netConnection(clock, Seq(f"processing_system/maxi${portLower}_${domainLower}_aclk"))
    tcl += interfaceConnection(f"processing_system_M_AXI_${portUpper}_${domainUpper}", Seq(f"${this.moduleName}/io_${domainLower}_${portLower}", f"processing_system/M_AXI_${portUpper}_${domainUpper}"))
    tcl += addressMap(range.base, range.size, "processing_system/Data", f"${this.moduleName}/io_${domainLower}_${portLower}/reg0")
    tcl += "\n"
    return tcl
  }

  def addPMOD(port: String, pins: Int): String = {
    var tcl = ""
    for (pin <- 0 until pins) {
      tcl += f"make_bd_pins_external  [get_bd_pins ${this.moduleName}/io_${port}_pins_${pin}]\n"
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

  def createSynthesis(fileset: String, constraintFileset: String): String = {
    val strategy = "\"Vivado Synthesis Defaults\""
    val flow     = "\"Vivado Synthesis 2022\""
    var tcl = ""
    tcl += f"set_property strategy ${strategy} [get_runs ${fileset}]\n"
    tcl += f"set_property flow ${flow} [get_runs ${fileset}]\n"
    tcl += f"set obj [get_runs ${fileset}]\n"
    tcl +=  "set_property set_report_strategy_name 1 $obj\n"
    tcl +=  "set_property report_strategy {Vivado Synthesis Default Reports} $obj\n"
    tcl +=  "set_property set_report_strategy_name 0 $obj\n"
    tcl +=  "\n"
    tcl += f"set obj [get_report_configs -of_objects [get_runs ${fileset}] ${fileset}_synth_report_utilization_0]\n"
    tcl += f"set obj [get_runs ${fileset}]\n"
    tcl +=  "set_property -name \"auto_incremental_checkpoint\" -value \"1\" -objects $obj\n"
    tcl +=  "set_property -name \"strategy\" -value \"Vivado Synthesis Defaults\" -objects $obj\n"
    tcl +=  "\n"
    tcl += f"current_run -synthesis [get_runs ${fileset}]\n"
    tcl +=  "\n"
    return tcl
  }

  def createImplementation(fileset: String): String = {
    val strategy = "\"Vivado Implementation Defaults\""
    val flow     = "\"Vivado Implementation 2022\""
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
    tcl += this.instantiateProcessingSystem()
    //// Connections
    ////// Clock and reset
    tcl += this.netConnection("pl_clk0", Seq(f"${this.moduleName}/clk", "reset_system/slowest_sync_clk", "processing_system/pl_clk0"))
    tcl += this.netConnection("periph_reset", Seq("reset_system/peripheral_reset", f"${this.moduleName}/reset"))
    tcl += this.netConnection("pl_resetn", Seq("processing_system/pl_resetn0", "reset_system/ext_reset_in"))
    tcl += "\n"
    ////// Buses
    if (this.platform.get.io.withFPD_HP0)
      tcl += this.addSecondaryPort("HP0", "pl_clk0")
    if (this.platform.get.io.withFPD_HP1)
      tcl += this.addSecondaryPort("HP1", "pl_clk0")
    if (this.platform.get.io.withFPD_HP2)
      tcl += this.addSecondaryPort("HP2", "pl_clk0")
    if (this.platform.get.io.withFPD_HP3)
      tcl += this.addSecondaryPort("HP3", "pl_clk0")
    if (this.platform.get.io.withFPD_HPC0)
      tcl += this.addSecondaryPort("HPC0", "pl_clk0")
    if (this.platform.get.io.withFPD_HPC1)
      tcl += this.addSecondaryPort("HPC1", "pl_clk0")
    if (this.platform.get.io.withFPD_HPM0)
      tcl += this.addPrimaryPort("HPM0", "FPD", "pl_clk0", AddressMap.FPD_HPM0)
    if (this.platform.get.io.withFPD_HPM1)
      tcl += this.addPrimaryPort("HPM1", "FPD", "pl_clk0", AddressMap.FPD_HPM1)
    if (this.platform.get.io.withLPD_HPM0)
      tcl += this.addPrimaryPort("HPM0", "LPD", "pl_clk0", AddressMap.LPD_HPM0)
    //// I/O PMOD
    if (this.platform.get.io.withIO_PMOD0)
      tcl += this.addPMOD("pmod0", this.platform.get.io.pmod0.amount)
    //// Save, validate, and wrap
    tcl += this.saveAndValidate()
    tcl += this.wrapDesign("sources_1")
    tcl += this.disableIDRFlow()

    // Setup synthesis
    tcl += this.createSynthesis("synth_1", "constrs_1")

    // Setup implementation
    tcl += this.createImplementation("impl_1")

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
    val tcl = this.script()
    val bw = new BufferedWriter(new FileWriter(new File(this.target), false))
    bw.write(tcl)
    bw.close
  }

}
