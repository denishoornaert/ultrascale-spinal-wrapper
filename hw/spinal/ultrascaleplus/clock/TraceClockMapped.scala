package ultrascaleplus.clock


import spinal.core._
import spinal.lib._


import ultrascaleplus.clock.pll.PllSource
import ultrascaleplus.scripts.TCLFactory
import ultrascaleplus.utils.{TCL, PSPLInterface, Util, Log}


object TraceClockMapped {

  def apply(): TraceClockMapped = new TraceClockMapped()

}


class TraceClockMapped() extends Bundle with PSPLInterface with TCL {
  
  private val clock = in(Bool())
  private var feedbackClock: Option[ClockMapped] = None

  var domain: Option[ClockDomain] = None

  def associate(clock: ClockMapped): Unit = {
    this.feedbackClock = Some(clock)
    this.domain = Some(this.feedbackClock.get.domain)
    Log.info(f"[Trace clock] Trace clock operates at ${this.feedbackClock.get.frequency}.")
  }

  override def getTCL(): String = {
    // Check that a feedback clock is associated
    assert(
      assertion = this.feedbackClock.isDefined,
      message   = "No feedback clock associated with the TRACE port clock!"
    )
    // Proceed with generation
    val topmodule = Util.topmodule(this)
    var tcl = ""
    // Loopback/feedback clock wiring
    tcl += TCLFactory.netConnection(this.clock.getName(), Seq(f"processing_system/${this.feedbackClock.get.getName()}", f"processing_system/pl_ps_trace_clk"))
    tcl += TCLFactory.netConnection(this.clock.getName(), Seq(f"${topmodule}/${this.clock.getName()}", f"processing_system/trace_clk_out"))
    tcl += "\n"
    return tcl
  }

  override def setAttribute(): Unit = {
    this.clock.addAttribute("X_INTERFACE_INFO", f"xilinx.com:signal:clock:1.0 ${this.clock.getName()} CLK")
  }

}
