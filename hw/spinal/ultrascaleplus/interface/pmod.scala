package ultrascaleplus.interface.pmod

import spinal.core._
import spinal.lib._

import generic.interface.pmod._
import ultrascaleplus.scripts._


abstract class AbstractPMOD() {

  var port: Option[PMOD] = None

  val names: Array[String]

  def getConstraints(): String = {
    var constraints = ""
    for (pin <- 0 until this.port.get.amount) {
      constraints += f"set_property PACKAGE_PIN ${names(pin)} [get_ports {io_${this.port.get.getPartialName()}_pins_${pin}}]\n"
      constraints += f"set_property IOSTANDARD LVCMOS33 [get_ports {io_${this.port.get.getPartialName()}_pins_${pin}}]\n"
    }
    return constraints
  }

  def init(port: PMOD): Unit = {
    this.port = Some(port)
  }

}
