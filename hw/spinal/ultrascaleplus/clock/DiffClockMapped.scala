package ultrascaleplus.clock


import spinal.core._
import spinal.lib._


import ultrascaleplus.types._
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
class DiffClockMapped(val frequency: HertzNumber, val pin: String) extends DiffBool with TCL with XDC with PSPLInterface {

  val domain = ClockDomain(
    clock     = this.p,
    frequency = FixedFrequency(this.frequency), 
  )

  private val xilinxInterfaceName = "xilinx.com:interface:diff_clock_rtl:1.0"

  // Assumes that the input Bigdecimal describes seconds
  private def formatTimeTo(time: BigDecimal, target: String): BigDecimal = {
    return target match {
      case "hr"  => time / 3600.0
      case "min" => time / 60.0
      case "sec" => time / 1.0
      case "ms"  => time / 1.0e-3
      case "us"  => time / 1.0e-6
      case "ns"  => time / 1.0e-9
      case "ps"  => time / 1.0e-12
      case "fs"  => time / 1.0e-15
      case _     => 0
    }
  }

  override def getTCL(): String = {
    val moduleName = Util.topmodule(this).getName()
    var tcl = ""
    tcl += f"set ${this.getName()}_external [ create_bd_intf_port -mode Slave -vlnv ${this.xilinxInterfaceName} ${this.getName()}_external ]\n"
    tcl +=  "set_property -dict [ list \\\n"
    tcl += f"  CONFIG.FREQ_HZ {${this.frequency.toBigDecimal}} \\\n"
    tcl +=  "] $"+f"${this.getName()}_external\n"
    tcl +=  "\n"
    tcl += TCLFactory.interfaceConnection(f"${this.getName()}_intermediate", Seq(this.getName(), f"${this.getName()}_external"))
    tcl +=  "\n"
    return tcl
  }

  override def getXDC(): String = {
    val period = this.formatTimeTo(this.frequency.toTime.toBigDecimal, "ns")
    var constraint = ""
    constraint += f"set_property PACKAGE_PIN ${this.pin} [get_ports ${this.p.getName()}_external_clk_p]\n"
    constraint += f"create_clock -period ${period} -name ${this.getName()}_external [get_ports ${this.getName()}_external_clk_p]"
    constraint +=  "\n"
    return constraint
  }

  override def setAttribute(): Unit = {
    this.p.addAttribute("X_INTERFACE_INFO", f"${this.xilinxInterfaceName} ${this.n.getName()} CLK_P")
    this.n.addAttribute("X_INTERFACE_INFO", f"${this.xilinxInterfaceName} ${this.p.getName()} CLK_N")
  }

}
