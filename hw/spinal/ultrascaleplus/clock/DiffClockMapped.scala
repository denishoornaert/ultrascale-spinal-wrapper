package ultrascaleplus.clock


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils.{TCL, XDC, PSPLInterface, Log, Util}
import ultrascaleplus.scripts.{TCLFactory}


abstract class DiffClockTemplate {

  val pin: String

}


object DiffClockMapped {

  def apply(frequency: HertzNumber, pin: String): DiffClockMapped = new DiffClockMapped(frequency, pin)
 
  def apply(frequency: HertzNumber, template: DiffClockTemplate) = new DiffClockMapped(frequency, template.pin)

}

/** Mappable differential clock (p & n)
 *  
 *  @constructor Creates a mappable differential clock.
 *  @parameter frequency Traget frequency for the clock.
 *  @parameter pin Physical board depend pin driving the clock.
 */
class DiffClockMapped(val frequency: HertzNumber, val pin: String) extends Bundle with TCL with XDC with PSPLInterface {

  val clock = new Bundle {
    val p = in(Bool())
    val n = in(Bool())
  }

  val domain = ClockDomain(
    clock     = this.clock.p,
    frequency = FixedFrequency(this.frequency), 
  )

  private val xilinxInterfaceName = "xilinx.com:interface:diff_clock_rtl:1.0"

  override def getTCL(): String = {
    var tcl = ""
    tcl += f"set ${this.clock.getName()}_external [ create_bd_intf_port -mode Slave -vlnv ${this.xilinxInterfaceName} ${this.clock.getName()}_external ]"
    tcl +=  "set_property -dict [ list \\\n"
    tcl += f"  CONFIG.FREQ_HZ {${this.frequency.toBigDecimal}} \\\n"
    tcl +=  "] $"+f"${this.clock.getName()}_external\n"
    tcl +=  "\n"
    tcl += TCLFactory.interfaceConnection(f"${this.clock.getName()}_intermediate", Seq(this.clock.getName(), f"${this.clock.getName()}_external"))
    tcl +=  "\n"
    return tcl
  }

  override def getXDC(): String = {
    var constraint = ""
    constraint += f"set_property PACKAGE_PIN ${this.pin} [get_ports ${this.clock.p.getName()}_external_clk_p]\n"
    constraint += f"create_clock -period ${this.frequency.toTime.toBigDecimal} -name ${this.clock.getName()}_external [get_ports ${this.clock.getName()}_external_clk_p]"
    constraint +=  "\n"
    return constraint
  }

  override def setAttribute(): Unit = {
    this.clock.p.addAttribute("X_INTERFACE_INFO", f"${this.xilinxInterfaceName} ${this.clock.n.getName()} CLK_P")
    this.clock.n.addAttribute("X_INTERFACE_INFO", f"${this.xilinxInterfaceName} ${this.clock.p.getName()} CLK_N")
  }

}
