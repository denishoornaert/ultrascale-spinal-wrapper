package ultrascaleplus.io.pmod


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils._


object PMOD {
  
  def apply(pins: Seq[String]): PMOD = {
    return new PMOD(pins)
  }

}


class PMOD(pins: Seq[String]) extends Vec[Bool](Bool, Vector.fill(pins.length)(Bool())) with TCL with XDC {

  assert(
    assertion = (pins.length <= 8),
    message   = f"Length given (${pins.length}) is larger than the allowed length of 8!"
  )

  val pinNames = pins

  override def getTCL(moduleName: String, clock: String): String = {
    var tcl = ""
    for (pin <- 0 until pinNames.length) {
      tcl += f"make_bd_pins_external  [get_bd_pins ${moduleName}/io_${this.getPartialName()}_${pin}]\n"
      tcl += f"set_property NAME io_${this.getPartialName()}_${pin} [get_bd_ports /io_${this.getPartialName()}_${pin}_0]\n"
    }
    return tcl
  }

  override def getXDC(): String = {
    var constraints = ""
    for ((name, pin) <- pinNames.zipWithIndex) {
      constraints += f"set_property PACKAGE_PIN ${name} [get_ports {io_${this.getPartialName()}_${pin}}]\n"
      constraints += f"set_property IOSTANDARD LVCMOS33 [get_ports {io_${this.getPartialName()}_${pin}}]\n"
    }
    return constraints
  }

}
