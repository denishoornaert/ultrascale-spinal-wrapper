package ultrascaleplus.bus.amba.ace


import spinal.core._
import spinal.lib._


import ultrascaleplus.scripts.{TCLFactory}
import ultrascaleplus.utils.{PSPLInterface, TCL, Util, hasClockReset}
import ultrascaleplus.clock.ClockResetMapped


abstract class AceMappedInstanceTemplate() {

  val name  : String
  val domain: String
  val id    : String
  val config: AceConfig

}


object AceMapped {

  def apply(config: AceConfig, name: String, id: String, domain: String): AceMapped = {
    return new AceMapped(config, name, id, domain)
  }

  def apply(metadata: AceMappedInstanceTemplate): AceMapped = {
    return new AceMapped(metadata.config, metadata.name, metadata.id, metadata.domain)
  }

}


class AceMapped(config: AceConfig, name: String, id: String, domain: String) extends Ace(config) with hasClockReset with PSPLInterface with TCL {
  
  this.setName(f"${domain}${if(name != "") f"_${name}" else ""}")

  override def getTCL(): String = {
    val moduleName = Util.topmodule(this).getName()
    // String preprocessin
    val portUpper = name.toUpperCase()
    val portLower = name.toLowerCase()
    val domainUpper = domain.toUpperCase()
    val domainLower = domain.toLowerCase()
    // String building
    var tcl = ""
    if (!this.clockAssociated) {
      throw new Exception(f"No clock associated with ${this.getName()}");
    }
    else {
      tcl += TCLFactory.netConnection(this.clock.get.clock.getName(), Seq(f"processing_system/s${portLower}${domainLower}_aclk"))
      tcl += TCLFactory.interfaceConnection(f"${moduleName}_${domainLower}${if(name != "") f"_${portLower}" else ""}", Seq(f"${moduleName}/${domainLower}${if (portLower != "") f"_${portLower}" else ""}", f"processing_system/S_AXI${if(portUpper != "") f"_${portUpper}" else ""}_${domainUpper}"))
      tcl += "\n"
    }
    return tcl
  }

  def generateFieldAttribute(interface: String, channel: String, field: String): String = {
    return "xilinx.com:interface:acemm:1.0 "+interface+" "+channel.toUpperCase()+field.toUpperCase()
  }
  
  override def setAttribute(): Unit = {
    if (!clock.isDefined) {
      throw new Exception(f"No clock associated with ${this.getName()}");
    }
    else {
      this.clock.get.addAttribute("X_INTERFACE_PARAMETER", f"ASSOCIATED_BUSIF = ${this.getName()}, ASSOCIATED_RESET = ${this.clock.get.reset.getName()}")
      val direction = if (this.isSlaveInterface) "Master" else "Slave"
      this.addAttribute("X_INTERFACE_PARAMETER", f"XIL_INTERFACENAME = ${this.getName()}, PROTOCOL = ACE, MODE = ${direction}, ASSOCIATED_CLOCK = ${this.clock.get.clock.getName()}, FREQ_HZ = ${this.clock.get.frequency.toLong}, FREQ_TOLERANCE_HZ = 0")
      for (channel <- Seq(this.ac, this.cr, this.cd, this.ar, this.r, this.aw, this.w, this.b)) {
        channel.valid.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.valid.getPartialName()))
        channel.ready.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), channel.ready.getPartialName()))
        channel.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateFieldAttribute(this.getName(), channel.getPartialName(), e._1))}
      }
    }
  }

}
