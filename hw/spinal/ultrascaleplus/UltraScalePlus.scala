package ultrascaleplus

/** Package for all thing UltrScalePlus.
  * It provides all common nad abstract constructs to build and define
  * detailed UltraScale+ implemntations.
  *
  * class implemented in this package are:
  *  - [[ultrascaleplus.UltraScalePlusConfig]]
  *  - [[ultrascaleplus.UltraScalePlusIO]]
  *  - [[ultrascaleplus.UltraScalePlus]]
  * 
  */
 package object scaladoc {}


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


import ultrascaleplus.signal.crosstrigger._
import ultrascaleplus.signal.irq._
import ultrascaleplus.signal.trace._
import ultrascaleplus.bus.amba.axi4._
import ultrascaleplus.clock.{ClockResetMapped, PLClockingArea, TraceClockMapped}
import ultrascaleplus.clock.pll._
import ultrascaleplus.scripts._
import ultrascaleplus.scripts.TCLFactory.Properties
import ultrascaleplus.utils._


class UltraScalePlusConfig(
  val withPL_CLK0    : PLL     = PLL.IO(100 MHz),
  val withPL_CLK1    : PLL     = PLL.IO(  0 MHz),
  val withPL_CLK2    : PLL     = PLL.IO(  0 MHz),
  val withPL_CLK3    : PLL     = PLL.IO(  0 MHz),
  val withLPD_HPM0   : Boolean = false,
  val withLPD_HP0    : Boolean = false,
  val withFPD_HPM0   : Boolean = false,
  val withFPD_HPM1   : Boolean = false,
  val withFPD_HP0    : Boolean = false,
  val withFPD_HP1    : Boolean = false,
  val withFPD_HP2    : Boolean = false,
  val withFPD_HP3    : Boolean = false,
  val withFPD_HPC0   : Boolean = false,
  val withFPD_HPC1   : Boolean = false,
  val withFPD_ACP    : Boolean = false,
  val withFPD_ACE    : Boolean = false,
  val withDBG_CTI0   : Boolean = false,
  val withDBG_CTI1   : Boolean = false,
  val withDBG_CTI2   : Boolean = false,
  val withDBG_CTI3   : Boolean = false,
  val withDBG_CTO0   : Boolean = false,
  val withDBG_CTO1   : Boolean = false,
  val withDBG_CTO2   : Boolean = false,
  val withDBG_CTO3   : Boolean = false,
  val withPL_PS_IRQ0 : Int     =     0,
  val withPL_PS_IRQ1 : Int     =     0,
  val withTRACE      : Boolean = false
  ) {
}


class UltraScalePlusIO(config: UltraScalePlusConfig) extends Bundle {
  val pl = new Bundle {
    val clk0 = (config.withPL_CLK0.enabled) generate (    in(ClockResetMapped(config.withPL_CLK0)))
    val clk1 = (config.withPL_CLK1.enabled) generate (    in(ClockResetMapped(config.withPL_CLK1)))
    val clk2 = (config.withPL_CLK2.enabled) generate (    in(ClockResetMapped(config.withPL_CLK2)))
    val clk3 = (config.withPL_CLK3.enabled) generate (    in(ClockResetMapped(config.withPL_CLK3)))
  }
  val lpd = new Bundle {
    val hp0  = (config.withLPD_HP0        ) generate (master(Axi4Mapped(LPD.HP0 )))
    val hpm0 = (config.withLPD_HPM0       ) generate ( slave(Axi4Mapped(LPD.HPM0)))
  }
  val fpd = new Bundle {
    val hpm0 = (config.withFPD_HPM0       ) generate ( slave(Axi4Mapped(FPD.HPM0)))
    val hpm1 = (config.withFPD_HPM1       ) generate ( slave(Axi4Mapped(FPD.HPM1)))
    val hp0  = (config.withFPD_HP0        ) generate (master(Axi4Mapped(FPD.HP0 )))
    val hp1  = (config.withFPD_HP1        ) generate (master(Axi4Mapped(FPD.HP1 )))
    val hp2  = (config.withFPD_HP2        ) generate (master(Axi4Mapped(FPD.HP2 )))
    val hp3  = (config.withFPD_HP3        ) generate (master(Axi4Mapped(FPD.HP3 )))
    val hpc0 = (config.withFPD_HPC0       ) generate (master(Axi4Mapped(FPD.HPC0)))
    val hpc1 = (config.withFPD_HPC1       ) generate (master(Axi4Mapped(FPD.HPC1)))
    val acp  = (config.withFPD_ACP        ) generate (master(Axi4Mapped(FPD.ACP )))
//  val fpd_ace  = (withFPD_ACE    ) generate ( slave(Axi4(KriaPorts.FPD_ACE_Config )))
  }
  val dbg = new Bundle {
    val cti0 = (config.withDBG_CTI0       ) generate ( slave(CrossTrigger()))
    val cti1 = (config.withDBG_CTI1       ) generate ( slave(CrossTrigger()))
    val cti2 = (config.withDBG_CTI2       ) generate ( slave(CrossTrigger()))
    val cti3 = (config.withDBG_CTI3       ) generate ( slave(CrossTrigger()))
    val cto0 = (config.withDBG_CTO0       ) generate (master(CrossTrigger()))
    val cto1 = (config.withDBG_CTO1       ) generate (master(CrossTrigger()))
    val cto2 = (config.withDBG_CTO2       ) generate (master(CrossTrigger()))
    val cto3 = (config.withDBG_CTO3       ) generate (master(CrossTrigger()))
  }
  val irq = new Bundle {
    val toPS0 = (config.withPL_PS_IRQ0 > 0) generate (out(IRQ(config.withPL_PS_IRQ0)))
    val toPS1 = (config.withPL_PS_IRQ1 > 0) generate (out(IRQ(config.withPL_PS_IRQ1)))
  }
  val trace = new Bundle {
    val clk = (config.withTRACE             ) generate ( in(TraceClockMapped()))
    val bus = (config.withTRACE             ) generate ( in(Trace(32)))
  }
}


abstract class UltraScalePlus (
  val config       : UltraScalePlusConfig = new UltraScalePlusConfig()
) extends Component with TCL {

  // Components name for TCL
  val board: String
  val version: String
  val boardPart: String

  // Abstractt IO
  val io: UltraScalePlusIO

  private val properties = new Properties("/Vivado/IP/zynq_ultra_ps_e.json", "$processing_system")
 

  // Double check clocks' PLL's settings/configurations.
  this.afterElaboration({
    val confs = Seq(config.withPL_CLK0, config.withPL_CLK1, config.withPL_CLK2, config.withPL_CLK3)
    for (source <- Seq("IOPLL", "RPLL")) {
    val validPlls = confs.filter(c => (c.asInstanceOf[PLL].name == source) && c.asInstanceOf[PLL].enabled).map(_.asInstanceOf[PLL].multiplier)
      if (validPlls.nonEmpty) {
        assert(
          assertion = validPlls.forall(_ == validPlls.head),
          message   = f"[${source}] Clock configurations requested cannot be met: different multiplier must be used."
        )
      }
    }
  })
  
  override def getTCL(): String = {
    var tcl = ""
    tcl +=f"set processing_system [ create_bd_cell -type ip -vlnv xilinx.com:ip:zynq_ultra_ps_e:${Vivado.getIPVersion("zynq_ultra_ps_e")} processing_system ]\n"
    val confs = Seq(config.withPL_CLK0, config.withPL_CLK1, config.withPL_CLK2, config.withPL_CLK3)
    val clocks = Seq(this.io.pl.clk0, this.io.pl.clk1, this.io.pl.clk2, this.io.pl.clk3)
    for (((conf, clock), i) <- confs.lazyZip(clocks).zipWithIndex) {
      if (conf.enabled) {
        this.properties.add(f"CONFIG.PSU__CRL_APB__PL${i}_REF_CTRL__ACT_FREQMHZ", f"${clock.frequency.decompose._1}")
        this.properties.add(f"CONFIG.PSU__CRL_APB__PL${i}_REF_CTRL__FREQMHZ"    , f"${clock.frequency.decompose._1}")
        this.properties.add(f"CONFIG.PSU__CRL_APB__PL${i}_REF_CTRL__SRCSEL"     , f"${clock.source.name}"           )
        this.properties.add(f"CONFIG.PSU__FPGA_PL${i}_ENABLE"                   ,  "1"                              )
        this.properties.add(f"CONFIG.PSU__PL_CLK${i}_BUF"                       ,  "True"                           )
      }
    }
    if (this.config.withFPD_HPM0) {
      this.properties.add("CONFIG.PSU__MAXIGP0__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__M_AXI_GP0"     , "1"  )
    }
    if (this.config.withFPD_HPM1) {
      this.properties.add("CONFIG.PSU__MAXIGP1__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__M_AXI_GP1"     , "1"  )
    }
    if (this.config.withLPD_HPM0) {
      this.properties.add("CONFIG.PSU__MAXIGP2__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__M_AXI_GP2"     , "1"  )
    }
    if (this.config.withFPD_HPC0) {
      this.properties.add("CONFIG.PSU__SAXIGP0__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__S_AXI_GP0"     , "1"  ) 
    }
    if (this.config.withFPD_HPC1) {
      this.properties.add("CONFIG.PSU__SAXIGP1__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__S_AXI_GP1"     , "1"  ) 
    }
    if (this.config.withFPD_HP0) {
      this.properties.add("CONFIG.PSU__SAXIGP2__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__S_AXI_GP2"     , "1"  ) 
    }
    if (this.config.withFPD_HP1) {
      this.properties.add("CONFIG.PSU__SAXIGP3__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__S_AXI_GP3"     , "1"  ) 
    }
    if (this.config.withFPD_HP2) {
      this.properties.add("CONFIG.PSU__SAXIGP4__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__S_AXI_GP4"     , "1"  ) 
    }
    if (this.config.withFPD_HP3) {
      this.properties.add("CONFIG.PSU__SAXIGP5__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__S_AXI_GP5"     , "1"  ) 
    }
    if (this.config.withLPD_HP0) {
      this.properties.add("CONFIG.PSU__SAXIGP6__DATA_WIDTH", "128")
      this.properties.add("CONFIG.PSU__USE__S_AXI_GP6"     , "1"  ) 
    }
    if (this.config.withFPD_ACP) {
      this.properties.add("CONFIG.PSU__USE__S_AXI_ACP"     , "1"  ) 
    }
    if (this.config.withTRACE) {
      this.properties.add("CONFIG.PSU__TRACE__INTERNAL_WIDTH"    , "32"   )
      this.properties.add("CONFIG.PSU__TRACE__PERIPHERAL__ENABLE", "1"    )
      this.properties.add("CONFIG.PSU__TRACE__PERIPHERAL__IO"    , "EMIO" )
      this.properties.add("CONFIG.PSU__TRACE__WIDTH"             , "32Bit")
    }
    if (config.withDBG_CTI0)
      this.properties.add("CONFIG.PSU__FTM__CTI_IN_0", "1")
    if (config.withDBG_CTI1)
      this.properties.add("CONFIG.PSU__FTM__CTI_IN_1", "1")
    if (config.withDBG_CTI2)
      this.properties.add("CONFIG.PSU__FTM__CTI_IN_2", "1")
    if (config.withDBG_CTI3)
      this.properties.add("CONFIG.PSU__FTM__CTI_IN_3", "1")
    if (config.withDBG_CTO0)
      this.properties.add("CONFIG.PSU__FTM__CTI_OUT_0", "1")
    if (config.withDBG_CTO1)
      this.properties.add("CONFIG.PSU__FTM__CTI_OUT_1", "1")
    if (config.withDBG_CTO2)
      this.properties.add("CONFIG.PSU__FTM__CTI_OUT_2", "1")
    if (config.withDBG_CTO3)
      this.properties.add("CONFIG.PSU__FTM__CTI_OUT_3", "1")
    if (config.withPL_PS_IRQ0 > 0)
      this.properties.add("CONFIG.PSU__USE__IRQ0", f"${this.config.withPL_PS_IRQ0}")
    if (config.withPL_PS_IRQ1 > 0)
      this.properties.add("CONFIG.PSU__USE__IRQ1", f"${this.config.withPL_PS_IRQ1}")
    tcl += this.properties.getTCL() 
    tcl += "\n"
    return tcl
  }

  def setAttribute(bundle: Bundle): Unit = {
    for ((name, element) <- bundle.elements) {
      // Bundle MUST stay at the last place!
      element match {
        case _:PSPLInterface => element.asInstanceOf[PSPLInterface].setAttribute()
        case _:Bundle        => this.setAttribute(element.asInstanceOf[Bundle])
        case _               => {}
      }
    }
  }

  def addConstraints(bundle: Bundle): Unit = {
    for ((name, element) <- bundle.elements) {
      // Bundle MUST stay at the last place!
      element match {
        case _:XDC    => Constraints.add(element.asInstanceOf[XDC].getXDC())
        case _:Bundle => this.addConstraints(element.asInstanceOf[Bundle])
        case _        => {}
      }
    }
  }

  def generate(): Unit = {
    this.setAttribute(this.io)
    this.addConstraints(this.io)
    TCLFactory.generate()
  }

  // Get name of the class (should be the off spring).
  TCLFactory(this)
  Constraints(this.getClass.getSimpleName)
 
}
