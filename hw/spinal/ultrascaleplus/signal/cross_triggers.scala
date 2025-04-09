package ultrascaleplus.signal.crosstrigger

import spinal.core._
import spinal.lib._

import generic.interface.crosstrigger._


abstract class AbstractCrossTrigger() {

  val port: CrossTrigger

  def generateFieldAttribute(interface: String, channel: String): String = {
    return "xilinx.com:interface:trigger:1.0 "+interface+" "+channel.toUpperCase()
  }

  def setInterfaceAttributes(): Unit = {
    if (port.isMasterInterface) {
      port.pl_ps_trigger.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.port.getPartialName(), "ACK"))
      port.ps_pl_trigger.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.port.getPartialName(), "TRIG"))
    }
    else {
      port.pl_ps_trigger.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.port.getPartialName(), "TRIG"))
      port.ps_pl_trigger.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.port.getPartialName(), "ACK"))
    }
  }
}


object DBG_CTI0 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti0")
  this.setInterfaceAttributes()

}

object DBG_CTI1 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti1")
  this.setInterfaceAttributes()

}

object DBG_CTI2 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti2")
  this.setInterfaceAttributes()

}

object DBG_CTI3 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti3")
  this.setInterfaceAttributes()

}

object DBG_CTO0 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto0")
  this.setInterfaceAttributes()

}

object DBG_CTO1 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto1")
  this.setInterfaceAttributes()

}

object DBG_CTO2 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto2")
  this.setInterfaceAttributes()

}

object DBG_CTO3 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto3")
  this.setInterfaceAttributes()

}
