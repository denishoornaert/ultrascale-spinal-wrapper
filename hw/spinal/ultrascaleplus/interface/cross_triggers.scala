package ultrascaleplus.interface.crosstrigger

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
