package ultrascaleplus.interface.axi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


import ultrascaleplus.scripts.{TCLFactory}
import ultrascaleplus.utils.{Aperture, PSPLInterface}


abstract class Axi4MappedInstanceTemplate() {

  val name     : String
  val domain   : String
  val apertures: Seq[Aperture]
  val id       : Int
  val config   : Axi4Config

} 


object Axi4Mapped {

  def apply(config: Axi4Config, name: String, apertures: Seq[Aperture], id: Int, domain: String): Axi4Mapped = {
    return new Axi4Mapped(config, name, apertures, id, domain)
  }

  def apply(metadata: Axi4MappedInstanceTemplate): Axi4Mapped = {
    return new Axi4Mapped(metadata.config, metadata.name, metadata.apertures, metadata.id, metadata.domain)
  }

}


class Axi4Mapped(override val config: Axi4Config, name: String, val apertures: Seq[Aperture], id: Int, domain: String) extends Axi4(config) with PSPLInterface {

  this.setName(name)

  override def getTCL(moduleName: String, clock: String): String = {
    // String preprocessing
    val portUpper = this.getPartialName().toUpperCase()
    val portLower = this.getPartialName().toLowerCase()
    val domainUpper = domain.toUpperCase()
    val domainLower = domain.toLowerCase()
    // String building
    // TODO: code could be optimized by checking whether the port is in primary/subordinate mode.
    var tcl = ""
    if (this.isSlaveInterface) { // If port is a secondary/subordinate (i.e., slave) FROM THE POV of this module!
      tcl += TCLFactory.netConnection(clock, Seq(f"processing_system/maxi${portLower}_${domainLower}_aclk"))
      tcl += TCLFactory.interfaceConnection(f"processing_system_M_AXI_${portUpper}_${domainUpper}", Seq(f"${moduleName}/${domainLower}_${portLower}", f"processing_system/M_AXI_${portUpper}_${domainUpper}"))
      for (aperture <- apertures) {
        tcl += TCLFactory.addressMap(aperture.base, aperture.size, "processing_system/Data", f"${moduleName}/${domainLower}_${portLower}/reg0")
      }
    }
    else {
      tcl += TCLFactory.netConnection(clock, Seq(f"processing_system/saxi${portLower}_${domainLower}_aclk"))
      tcl += TCLFactory.interfaceConnection(f"${moduleName}_${domainLower}_${portLower}", Seq(f"${moduleName}/${domainLower}_${portLower}", f"processing_system/S_AXI_${portUpper}_${domainUpper}"))
      for (aperture <- apertures) {
        tcl += TCLFactory.addressMap(aperture.base, aperture.size, f"${moduleName}/${domainLower}_${portLower}", f"processing_system/SAXIGP${id}/${portUpper}_${aperture.name}")
      }
    }
    tcl += "\n"
    return tcl
  }

  def generateFieldAttribute(interface: String, channel: String, field: String): String = {
    return "xilinx.com:interface:aximm:1.0 "+interface+" "+channel.toUpperCase()+field.toUpperCase()
  }
  
  override def setAttribute(): Unit = {
    val direction = if (this.isSlaveInterface) "Master" else "Slave"
    this.addAttribute("X_INTERFACE_INFO", f"XIL_INTERFACENAME ${this.getName()}, PROTOCOL AXI4, MODE ${direction}")
    for (channel <- Seq(this.ar, this.r, this.aw, this.w, this.b)) {
      channel.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.valid.getPartialName()))
      channel.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.ready.getPartialName()))
      channel.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), e._1))}
    }
  }

}
