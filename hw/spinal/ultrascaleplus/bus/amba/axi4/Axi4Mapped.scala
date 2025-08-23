package ultrascaleplus.bus.amba.axi4


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


import ultrascaleplus.scripts.{TCLFactory}
import ultrascaleplus.utils.{Aperture, PSPLInterface, TCL, Util, hasClockReset}
import ultrascaleplus.clock.ClockResetMapped


abstract class Axi4MappedInstanceTemplate() {

  val name     : String
  val domain   : String
  val apertures: Seq[Aperture]
  val id       : String        = ""
  val config   : Axi4Config

} 


object Axi4Mapped {

  def apply(config: Axi4Config, name: String, apertures: Seq[Aperture], id: String, domain: String): Axi4Mapped = {
    return new Axi4Mapped(config, name, apertures, id, domain)
  }

  def apply(metadata: Axi4MappedInstanceTemplate): Axi4Mapped = {
    return new Axi4Mapped(metadata.config, metadata.name, metadata.apertures, metadata.id, metadata.domain)
  }

}


class Axi4Mapped(override val config: Axi4Config, name: String, val apertures: Seq[Aperture], id: String, domain: String) extends Axi4(config) with hasClockReset with PSPLInterface with TCL {

  this.setName(f"${domain}${if(name != "") f"_${name}" else ""}")

  override def getTCL(): String = {
    val moduleName = Util.topmodule(this).getName()
    // String preprocessin
    val portUpper = name.toUpperCase()
    val portLower = name.toLowerCase()
    val domainUpper = domain.toUpperCase()
    val domainLower = domain.toLowerCase()
    // String building
    // TODO: code could be optimized by checking whether the port is in primary/subordinate mode.
    // TODO: quite mesy at this point... Should be refactored
    var tcl = ""
    if (!this.clockAssociated) {
      throw new Exception(f"No clock associated with ${this.getName()}");
    }
    else {
      if (this.isSlaveInterface) { // If port is a secondary/subordinate (i.e., slave) FROM THE POV of this module!
        tcl += TCLFactory.netConnection(this.clock.get.clock.getName(), Seq(f"processing_system/maxi${portLower}_${domainLower}_aclk"))
        tcl += TCLFactory.interfaceConnection(f"processing_system_M_AXI_${portUpper}_${domainUpper}", Seq(f"${moduleName}/${domainLower}_${portLower}", f"processing_system/M_AXI_${portUpper}_${domainUpper}"))
        for (aperture <- apertures) {
          tcl += TCLFactory.addressMap(aperture.base, aperture.size, "processing_system/Data", f"${moduleName}/${domainLower}_${portLower}/reg0")
        }
      }
      else {
        tcl += TCLFactory.netConnection(this.clock.get.clock.getName(), Seq(f"processing_system/saxi${portLower}_${domainLower}_aclk"))
        tcl += TCLFactory.interfaceConnection(f"${moduleName}_${domainLower}${if(name != "") f"_${portLower}" else ""}", Seq(f"${moduleName}/${domainLower}${if (portLower != "") f"_${portLower}" else ""}", f"processing_system/S_AXI${if(portUpper != "") f"_${portUpper}" else ""}_${domainUpper}"))
        for (aperture <- apertures) {
          tcl += TCLFactory.addressMap(aperture.base, aperture.size, f"${moduleName}/${domainLower}${if(portLower != "") f"_${portLower}" else ""}", f"processing_system/SAXI${id}/${if(portUpper != "") f"${portUpper}" else f"${domainUpper}"}_${aperture.name}")
        }
      }
      tcl += "\n"
    }
    return tcl
  }

  def generateFieldAttribute(interface: String, channel: String, field: String): String = {
    return "xilinx.com:interface:aximm:1.0 "+interface+" "+channel.toUpperCase()+field.toUpperCase()
  }
  
  override def setAttribute(): Unit = {
    if (!clock.isDefined) {
      throw new Exception(f"No clock associated with ${this.getName()}");
    }
    else {
      this.clock.get.addAttribute("X_INTERFACE_PARAMETER", f"ASSOCIATED_BUSIF = ${this.getName()}, ASSOCIATED_RESET = ${this.clock.get.reset.getName()}")
      val direction = if (this.isSlaveInterface) "Master" else "Slave"
      this.addAttribute("X_INTERFACE_PARAMETER", f"XIL_INTERFACENAME = ${this.getName()}, PROTOCOL = AXI4, MODE = ${direction}, ASSOCIATED_CLOCK = ${this.clock.get.clock.getName()}, FREQ_HZ = ${this.clock.get.frequency.toLong}, FREQ_TOLERANCE_HZ = 0")
      for (channel <- Seq(this.ar, this.r, this.aw, this.w, this.b)) {
        channel.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.valid.getPartialName()))
        channel.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.ready.getPartialName()))
        channel.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), e._1))}
      }
    }
  }

  /** Required for connection with Axi4 buses.
   *
   *  @return A 1:1 copy of the [[Axi4Mapped]].
   */
  override def clone(): Axi4Mapped = {
    return new Axi4Mapped(
      config    = this.config,
      name      = this.name,
      apertures = this.apertures,
      id        = this.id,
      domain    = this.domain
    )
  }

}
