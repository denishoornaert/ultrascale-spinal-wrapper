package ultrascaleplus.types


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils.{TCL,XDC,PSPLInterface,Util}


object DiffBool {
  
  def apply(): DiffBool = new DiffBool()

}


class DiffBool() extends Bundle with IMasterSlave {

  val p = Bool()
  val n = Bool()

  override def asMaster(): Unit = {
    out(p, n)
  }

}


object DiffBoolMapped {
  
  def apply(pins: Seq[String]): DiffBoolMapped = new DiffBoolMapped(pins)

}


class DiffBoolMapped(pins: Seq[String]) extends DiffBool() with TCL with XDC with PSPLInterface {
  
  assert(
    assertion = (pins.length == 2),
    message   = f"2 pins are expected for TX but ${pins.length} are provided!"
  )
    
  override def getXDC(): String = {
    var constraints = ""
    constraints += f"set_property PACKAGE_PIN ${pins(0)} [get_ports ${this.p.getName()}]\n"
    constraints += f"set_property PACKAGE_PIN ${pins(1)} [get_ports ${this.n.getName()}]\n"
    return constraints
  }

  override def getTCL(): String = {
    val moduleName = Util.topmodule(this).getName()
    var tcl = ""
    tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.p.getName()}]\n"
    tcl += f"set_property NAME ${this.p.getName()} [get_bd_ports /${this.p.getName()}]\n"
    tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.n.getName()}]\n"
    tcl += f"set_property NAME ${this.n.getName()} [get_bd_ports /${this.n.getName()}]\n"
    return tcl
  }

  override def setAttribute(): Unit = {
    this.p.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:sgmii:1.0 ${this.getPartialName()} TXP")
    this.n.addAttribute("X_INTERFACE_INFO", f"xilinx.com;interface:sgmii:1.0 ${this.getPartialName()} TXN")
  }

}
