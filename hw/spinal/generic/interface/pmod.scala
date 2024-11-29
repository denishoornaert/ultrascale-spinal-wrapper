package generic.interface.pmod

import spinal.core._
import spinal.lib._


case class PMOD() extends Bundle with IMasterSlave {

  val amount: Int = 8

  val pins: Vec[Bool] = Vec(out(Bool()), this.amount)

  override def asMaster(): Unit = {
    out(pins)
  }

  def setAllOutputs(): Unit = {
    for (p <- 0 until pins.size)
      if (pins(p).isOutput)
        pins(p).set()
  }

  def clearAllOutputs(): Unit = {
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
