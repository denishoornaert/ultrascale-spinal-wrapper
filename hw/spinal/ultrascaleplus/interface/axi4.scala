package ultrascaleplus.interface.axi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.amba4.axi._


object Axi4Mapped {

  def apply(config: Axi4Config, apertures: Seq[SizeMapping]): Axi4Mapped = {
    return new Axi4Mapped(config, apertures)
  }

}


class Axi4Mapped(config: Axi4Config, mappings: Seq[SizeMapping]) extends Axi4(config) {

  val apertures = mappings

  def generateFieldAttribute(interface: String, channel: String, field: String): String = {
    return "xilinx.com:interface:aximm:1.0 "+interface+" "+channel.toUpperCase()+field.toUpperCase()
  }
  
  def setInterfaceAttributes(): Unit = {
    for (channel <- Seq(this.ar, this.r, this.aw, this.w, this.b)) {
      channel.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.valid.getPartialName()))
      channel.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.ready.getPartialName()))
      channel.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), e._1))}
    }
  }
  /*
  override def setAsMaster(): Unit = {
    super.setAsMaster()
    this.addAttribute("X_INTERFACE_INFO", f"XIL_INTERFACENAME ${this.getName()}, PROTOCOL AXI4, MODE Master")
  }

  override def setAsSLave(): Unit = {
    super.setAsSlave()
    this.addAttribute("X_INTERFACE_INFO", f"XIL_INTERFACENAME ${this.getName()}, PROTOCOL AXI4, MODE Slave")
  }

  override def intoMaster(): this.type = {
    super.intoMaster()
    this.addAttribute("X_INTERFACE_INFO", f"XIL_INTERFACENAME ${this.getName()}, PROTOCOL AXI4, MODE Master")
    return this
  }

  override def intoSlave(): this.type = {
    super.intoSlave()
    this.addAttribute("X_INTERFACE_INFO", f"XIL_INTERFACENAME ${this.getName()}, PROTOCOL AXI4, MODE Slave")
    return this
  }
  */

  this.setInterfaceAttributes()

}
