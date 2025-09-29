package ultrascaleplus.clock


import spinal.core._
import spinal.lib._


import ultrascaleplus.clock.pll._
import ultrascaleplus.utils.{TCL, XDC, PSPLInterface, Log, Util}
import ultrascaleplus.scripts.{TCLFactory}


object ClockMapped {

  def apply(source: PLL): ClockMapped = new ClockMapped(source)

}

/** Mappable clock (w.r.t. Xilinx param and TCL) 
 *
 *  @constructor Creates a mappable clock source.
 *  @parameter source PLL clock source.
 */
class ClockMapped(val source: PLL) extends Bundle with PSPLInterface with TCL {

  val clock = in(Bool())

  val domain = ClockDomain(
    clock     = this.clock,
    frequency = FixedFrequency(source.frequency), 
    config    = ClockDomainConfig(
      clockEdge        = RISING
    )
  )

  /**
   * Abstracts access to the PLL source frequency.
   */
  def frequency: HertzNumber = source.frequency

  override def getTCL(): String = {
    val topmodule = Util.topmodule(this)
    var tcl = ""
    tcl += TCLFactory.netConnection(this.clock.getName(), Seq(f"${topmodule}/${this.clock.getName()}", f"processing_system/pl_${this.getPartialName()}"))
    tcl += "\n"
    return tcl
  }

  override def setAttribute(): Unit = {
    this.clock.addAttribute("X_INTERFACE_INFO", f"xilinx.com:signal:clock:1.0 ${this.clock.getName()} CLK")
  }

} 
