package ultrascaleplus.signal.crosstrigger


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils._
import ultrascaleplus.scripts.TCLFactory


case class CrossTrigger() extends Bundle with IMasterSlave with PSPLInterface with TCL {

  val pl_ps_trigger =  in(Bool())
  val ps_pl_trigack = out(Bool())

  override def asMaster(): Unit = {
    out(pl_ps_trigger)
    in(ps_pl_trigack)
  }

  override def getTCL(moduleName: String): String = {
    val index = this.getPartialName().takeRight(1)
    var tcl = ""
    if (this.isMasterInterface)
      tcl += TCLFactory.interfaceConnection(this.getPartialName(), Seq(f"${moduleName}/${this.getPartialName()}", f"processing_system/PL_PS_TRIGGER_${index}"))
    else
      tcl += TCLFactory.interfaceConnection(f"processing_system_PS_PL_TRIGGER_${index}", Seq(f"${moduleName}/${this.getPartialName()}", f"processing_system/PS_PL_TRIGGER_${index}"))
    tcl += "\n"
    return tcl
  }

  override def setAttribute(): Unit = {
    if (this.isMasterInterface) {
      this.addAttribute("X_INTERFACE_INFO", "MODE Master")
    }
    else {
      this.addAttribute("X_INTERFACE_INFO", "MODE Slave")
    }
    this.pl_ps_trigger.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:trigger:1.0 ${this.getPartialName()} TRIG")
    this.ps_pl_trigack.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:trigger:1.0 ${this.getPartialName()} ACK")
  }
}
