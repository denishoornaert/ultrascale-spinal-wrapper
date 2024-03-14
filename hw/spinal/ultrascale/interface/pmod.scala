package interface.io

import spinal.core._
import spinal.lib._

import kria._
import scripts._


case class PMOD(amount: Int = 8, interface_name: String = "undefined") extends Bundle {
 
  assert(this.amount <= 8)

  for (pin <- 0 until this.amount)
    Constraints.add("set_property PACKAGE_PIN "+KriaPorts.pmod_pins(pin)+" [get_ports "+this.interface_name+"["+pin+"]];")

  val pins = Vec(out(Bool()), this.amount)

  def setAllInputs(): Unit = {
    for (p <- 0 until pins.size)
      if (pins(p).isOutput)
        pins(p).set()
  }

  def clearAllInputs(): Unit = {
    for (p <- 0 until pins.size)
      if (pins(p).isOutput)
        pins(p).clear()
  }
  
  def makeAllInput(): Unit = {
    in(pins)
  }

  def makeAllOutput(): Unit = {
    out(pins)
  }

  def makeInput(index: Int): Unit = {
    in(pins(index))
  }

  def makeOutput(index: Int): Unit = {
    out(pins(index))
  }
}
