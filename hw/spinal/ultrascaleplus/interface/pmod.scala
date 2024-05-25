package ultrascaleplus.interface.pmod

import spinal.core._
import spinal.lib._

import generic.interface.pmod._
import scripts._


abstract class AbstractPMOD() {

  val port: PMOD

  val names: Array[String]

  def addToConstraints(): Unit = {
    for (pin <- 0 until this.port.amount)
      Constraints.add("set_property PACKAGE_PIN "+names(pin)+" [get_ports "+this.port.getPartialName()+"["+pin+"]];")
  }

}
