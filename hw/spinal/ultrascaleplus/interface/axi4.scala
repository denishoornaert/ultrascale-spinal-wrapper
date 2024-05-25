package ultrascaleplus.interface.axi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.amba4.axi._


abstract class AbstractAxi4() {

  def generateFieldAttribute(interface: String, channel: String, field: String): String = {
    return "xilinx.com:interface:aximm:1.0 "+interface+" "+channel.toUpperCase()+field.toUpperCase()
  }
  
  def setInterfaceAttributes(port: Axi4): Unit = {
    port.addAttribute("X_INTERFACE_INFO", "XIL_INTERFACENAME "+port.getName()+", PROTOCOL AXI4, MODE "+direction)
    // AW
    port.aw.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.aw.getPartialName(), port.aw.valid.getPartialName()))
    port.aw.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.aw.getPartialName(), port.aw.ready.getPartialName()))
    port.aw.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.aw.getPartialName(), e._1))}
    // W
    port.w.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.w.getPartialName(), port.w.valid.getPartialName()))
    port.w.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.w.getPartialName(), port.w.ready.getPartialName()))
    port.w.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.w.getPartialName(), e._1))}
    // B
    port.b.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.b.getPartialName(), port.b.valid.getPartialName()))
    port.b.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.b.getPartialName(), port.b.ready.getPartialName()))
    port.b.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.b.getPartialName(), e._1))}
    // AR
    port.ar.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.ar.getPartialName(), port.ar.valid.getPartialName()))
    port.ar.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.ar.getPartialName(), port.ar.ready.getPartialName()))
    port.ar.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.ar.getPartialName(), e._1))}
    // R
    port.r.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.r.getPartialName(), port.r.valid.getPartialName()))
    port.r.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.r.getPartialName(), port.r.ready.getPartialName()))
    port.r.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(port.getName(), port.r.getPartialName(), e._1))}
  }

  val direction: String

  val port: Axi4

}


abstract class AbstractPrimaryAxi4 extends AbstractAxi4() {
  
  override val direction: String = "Master"

  val aperture: SizeMapping

}


abstract class AbstractSecondaryAxi4 extends AbstractAxi4() {

  override val direction: String = "Slave"

}
