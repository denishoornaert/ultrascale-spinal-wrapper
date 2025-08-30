package ultrascaleplus.signal.irq


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils._
import ultrascaleplus.scripts.TCLFactory


case class Trace(width: Int = 32) extends Bundle with PSPLInterface with TCL {

  assert(
    assertion = (Seq[Int](2, 4, 8, 16, 32) contains this.width),
    message   = f"Requested width (${this.width}) is not in the suported set (i.e., 2, 4, 8, 16, 32)!"
  )

  val ctl = Bool()
  val data = UInt(this.width bits)

  override def getTCL(): String = {
    val moduleName = Util.topmodule(this).getName()
    var tcl = ""
    tcl += TCLFactory.netConnection("processing_system_trace_clk_out", Seq("processing_system/trace_clk_out", "processing_system/pl_ps_trace_clk"))
    tcl += TCLFactory.interfaceConnection(f"processing_system_TRACE_0", Seq(f"${moduleName}/${this.getPartialName()}", f"processing_system/TRACE_0"))
    tcl += "\n"
    return tcl
  }
  
  override def setAttribute(): Unit = {
    if (this.isOutput) {
      this.addAttribute("X_INTERFACE_INFO", "MODE Master")
    }
    else {
      this.addAttribute("X_INTERFACE_INFO", "MODE Slave")
    }
    this.ctl.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:zynq_trace:1.0 ${this.getPartialName()} CTL")
    this.data.addAttribute("X_INTERFACE_INFO", f"xilinx.com:interface:zynq_trace:1.0 ${this.getPartialName()} DATA")
  }

}
