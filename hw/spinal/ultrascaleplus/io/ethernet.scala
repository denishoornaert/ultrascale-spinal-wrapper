package ultrascaleplus.io.gt


import spinal.core._
import spinal.lib._


import ultrascaleplus.types._
import ultrascaleplus.utils._


abstract class GTMappedTemplate() {
  val TX : Seq[String]
  val SFP: Seq[String]
  val RX : Seq[String]
} 


object GT {

  def apply() = new GT()

}

/** GT interface bundle/interface class.
 *
 *  @constructor Creates a GT interface.
 */
class GT() extends Bundle with IMasterSlave {
  
  override def asMaster(): Unit = {
    out(this.tx, this.sfp)
    in(this.rx)
  }

  val tx = DiffBool()
  val rx = DiffBool()
  val sfp = new Bundle {
    val dis = Bool()
  }

}


case class GTMapped(config: GTMappedTemplate) extends GT() with PSPLInterface with TCL with XDC {
 
  assert(
    assertion = (this.config.SFP.length == 1),
    message   = f"1 pins are expected for SFP but ${this.config.SFP.length} are provided!"
  )
  
  assert(
    assertion = (this.config.TX.length == 2),
    message   = f"2 pins are expected for TX but ${this.config.TX.length} are provided!"
  )
  
  assert(
    assertion = (this.config.RX.length == 2),
    message   = f"2 pins are expected for TX but ${this.config.RX.length} are provided!"
  )
    
  // TODO: could be changed for xilinx interface
  override def getTCL(): String = {
    val moduleName = Util.topmodule(this).getName()
    var tcl = ""
    // TX
    tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.tx.p.getName()}]\n"
    tcl += f"set_property NAME ${this.tx.p.getName()} [get_bd_ports /${this.tx.p.getName()}]\n"
    tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.tx.n.getName()}]\n"
    tcl += f"set_property NAME ${this.tx.n.getName()} [get_bd_ports /${this.tx.n.getName()}]\n"
    // RX
    tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.rx.p.getName()}]\n"
    tcl += f"set_property NAME ${this.rx.p.getName()} [get_bd_ports /${this.rx.p.getName()}]\n"
    tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.rx.n.getName()}]\n"
    tcl += f"set_property NAME ${this.rx.n.getName()} [get_bd_ports /${this.rx.n.getName()}]\n"
    // SFP DIS
    tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.sfp.dis.getName()}]\n"
    tcl += f"set_property NAME ${this.sfp.dis.getName()} [get_bd_ports /${this.sfp.dis.getName()}]\n"
    tcl += "\n"
    return tcl
  }
    
  override def getXDC(): String = {
    var constraints = ""
    // TX
    constraints += f"set_property PACKAGE_PIN ${this.config.TX(0)} [get_ports ${this.tx.p.getName()}]\n"
    constraints += f"set_property PACKAGE_PIN ${this.config.TX(1)} [get_ports ${this.tx.n.getName()}]\n"
    // RX
    constraints += f"set_property PACKAGE_PIN ${this.config.RX(0)} [get_ports ${this.rx.p.getName()}]\n"
    constraints += f"set_property PACKAGE_PIN ${this.config.RX(1)} [get_ports ${this.rx.n.getName()}]\n"
    // SFP DIS
    constraints += f"set_property PACKAGE_PIN ${this.config.SFP(0)} [get_ports ${this.sfp.dis.getName()}]\n"
    constraints += f"set_property IOSTANDARD LVCMOS33 [get_ports ${this.sfp.dis.getName()}]\n"
    return constraints
  }

  override def setAttribute(): Unit = {
    // TX
    this.tx.p.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:sgmii:1.0 ${this.tx.getPartialName()} TXP")
    this.tx.n.addAttribute("X_INTERFACE_INFO", f"xilinx.com;interface:sgmii:1.0 ${this.tx.getPartialName()} TXN")
    // RX
    this.rx.p.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:sgmii:1.0 ${this.rx.getPartialName()} RXP")
    this.rx.n.addAttribute("X_INTERFACE_INFO", f"xilinx.com;interface:sgmii:1.0 ${this.rx.getPartialName()} RXN")
  }

}
