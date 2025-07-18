package ultrascaleplus.clock


import spinal.core._
import spinal.lib._


import ultrascaleplus.clock.pll.PllSource
import ultrascaleplus.utils.{TCL, PSPLInterface, Log, Util}
import ultrascaleplus.scripts.{TCLFactory}


object ClockMapped {

  def apply(source: PllSource, target: HertzNumber): ClockMapped = new ClockMapped(source, target)

}

/** Mappable clock (w.r.t. Xilinx param and TCL) 
 *
 *  @constructor Creates a mappable clock source.
 *  @parameter source PLL clock source.
 *  @parameter target Target frequency desired.
 */
class ClockMapped(val source: PllSource, val target: HertzNumber) extends Bundle with PSPLInterface with TCL {

  val clock = in(Bool())

  val frequency = source.round(target)
  Log.info(f"[${this.source.name}] ${this.target} requested but ${this.frequency} selected.")

  val domain = ClockDomain(
    clock     = this.clock,
    frequency = FixedFrequency(this.frequency), 
  )

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


object ClockResetMapped {

  def apply(source: PllSource, target: HertzNumber): ClockResetMapped = new ClockResetMapped(source, target)

}


/** Mappable clock with associated reset (w.r.t. Xilinx param and TCL) 
 *
 *  @constructor Creates a mappable clock source.
 *  @parameter source PLL clock source.
 *  @parameter target Target frequency desired.
 */
class ClockResetMapped(source: PllSource, target: HertzNumber) extends ClockMapped(source: PllSource, target: HertzNumber) with TCL {
  
  val reset = in(Bool())

  override val domain = ClockDomain(
    clock     = this.clock,
    reset     = this.reset,
    frequency = FixedFrequency(this.frequency), 
    config    = ClockDomainConfig(resetActiveLevel = HIGH)
  )

  override def getTCL(): String = {
    val topmodule = Util.topmodule(this)
    val index = this.getPartialName().takeRight(1)
    var tcl = ""
    tcl += super.getTCL()
    tcl +=f"set reset_system_${index} [ create_bd_cell -type ip -vlnv xilinx.com:ip:proc_sys_reset:5.0 reset_system_${index} ]\n"
    tcl += TCLFactory.netConnection(this.clock.getName(), Seq(f"reset_system_${index}/slowest_sync_clk"))
    tcl += TCLFactory.netConnection(f"periph_reset_${index}", Seq(f"reset_system_${index}/peripheral_reset", f"${topmodule}/${this.reset.getName()}"))
    tcl += TCLFactory.netConnection(this.reset.getName(), Seq(f"processing_system/pl_resetn${index}", f"reset_system_${index}/ext_reset_in"))
    tcl += "\n"
    return tcl
  }

}
