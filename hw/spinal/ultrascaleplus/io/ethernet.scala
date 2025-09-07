package ultrascaleplus.io.ethernet

import spinal.core._
import spinal.lib._

import ultrascaleplus.utils._

/** GT interface bundle/interface class.
 *
 *  NOTE: extending [[TCL]] and [[XDC]] is not needed as the component do.
 *
 *  @constructor Creates an SFP interface.
 *  @param txPins TX board pins' name for the platform.
 *  @param sfpPins SFP board pins' name for the platform.
 *  @param rxPins RX board pins' name for the platform.
 */
case class GT(txPins: Seq[String], sfpPins: Seq[String], rxPins: Seq[String]) extends Bundle with PSPLInterface {
  
  case class SFP(pins: Seq[String]) extends Bundle with TCL with XDC {
    
    assert(
      assertion = (pins.length == 1),
      message   = f"1 pins are expected but ${pins.length} are provided!"
    )

    val dis = Bool()

    override def getTCL(moduleName: String, clock: String): String = {
      var tcl = ""
      tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.getName()}]\n"
      tcl += f"set_property NAME ${this.getName()} [get_bd_ports /${this.getName()}]\n"
      tcl += "\n"
      return tcl
    }

    override def getXDC(): String = {
      var constraints = ""
      constraints += f"set_property PACKAGE_PIN ${pins(0)} [get_ports ${this.dis.getName()}]\n"
      constraints += f"set_property IOSTANDARD LVCMOS33 [get_ports ${this.dis.getName()}]\n"
      return constraints
    }
  }

  case class DifferentialPaire(pins: Seq[String]) extends Bundle with TCL with XDC {

    assert(
      assertion = (pins.length == 2),
      message   = f"2 pins are expected but ${pins.length} are provided!"
    )
    
    val p = Bool()
    val n = Bool()

    override def getTCL(moduleName: String, clock: String): String = {
      var tcl = ""
      tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/${this.getName()}]\n"
      tcl += f"set_property NAME ${this.getName()} [get_bd_ports /${this.getName()}]\n"
      tcl += "\n"
      return tcl
    }

    override def getXDC(): String = {
      var constraints = ""
      constraints += f"set_property PACKAGE_PIN ${pins(0)} [get_ports ${this.p.getName()}]\n"
      constraints += f"set_property PACKAGE_PIN ${pins(1)} [get_ports ${this.n.getName()}]\n"
      return constraints
    }
  }

  override def setAttribute(): Unit = {
    this.tx.p.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:sgmii:1.0 ${this.getPartialName()} TXP")
    this.tx.n.addAttribute("X_INTERFACE_INFO", f"xilinx.com;interface:sgmii:1.0 ${this.getPartialName()} TXN")
    this.rx.p.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:sgmii:1.0 ${this.getPartialName()} RXP")
    this.rx.n.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:sgmii:1.0 ${this.getPartialName()} RXN")
  }

  val tx  = out(DifferentialPaire(txPins))
  val sfp = out(SFP(sfpPins))
  val rx  =  in(DifferentialPaire(rxPins))

}
