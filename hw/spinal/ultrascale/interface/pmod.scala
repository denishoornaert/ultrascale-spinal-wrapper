package interface.io

import spinal.core._
import spinal.lib._


case class PMOD(amount: Int = 8) extends Bundle {
 
  assert(this.amount <= 8)

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
