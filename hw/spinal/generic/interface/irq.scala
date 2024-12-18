package generic.interface.irq

import spinal.core._
import spinal.lib._


case class IRQ(amount: Int = 4) extends Bundle with IMasterSlave {

  val irq: Vec[Bool] = Vec(in(Bool()), this.amount)

  override def asMaster(): Unit = {
    out(irq)
  }

  def setAllOutputs(): Unit = {
    for (line <- 0 until this.amount)
      if (irq(line).isOutput)
        irq(line).set()
  }

  def clearAllOutputs(): Unit = {
    for (line <- 0 until this.amount)
      if (irq(line).isOutput)
        irq(line).clear()
  }

}
