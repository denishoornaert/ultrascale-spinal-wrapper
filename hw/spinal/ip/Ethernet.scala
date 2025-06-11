package ultrascaleplus

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import ultrascaleplus.utils._

//extend the library to extend a predifined set
//How to import external clk 

case class Ethernet() extends BlackBox with TCL with XDC{
    
  override def getTCL(moduleName: String, clock: String): String = {
    var tcl = ""
    tcl += "set xlconstant_6 [ create_bd_cell -type ip -vlnv xilinx.com:ip:xlconstant:1.1 xlconstant_6 ]\n"
    tcl += "set_property -dict [list \\\n"
    tcl += "CONFIG.BASE_R_KR {BASE-R} \\\n"
    tcl += "CONFIG.GT_GROUP_SELECT {Quad_X1Y3} \\\n"
    tcl += "CONFIG.INCLUDE_AXI4_INTERFACE {1} \\\n"
    tcl += "CONFIG.INCLUDE_STATISTICS_COUNTERS {1} \\\n"
    tcl += "CONFIG.LANE1_GT_LOC {X1Y14} \\\n"
    tcl += "] $xxv_ethernet_0\n"
    tcl += "\n"
    return tcl//not finished becuase xdc references pins that are not in the tcl
  }
    
  override def getXDC(): String = {
    var constraints = ""
    constraints += "set_property PACKAGE_PIN C8 [get_ports gt_ref_clk_clk_p]\\\n"
    constraints += "create_clock -period 6.400 -name gt_ref_clk [get_ports gt_ref_clk_clk_p]\\\n"
    return constraints
  }
}
