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
import ultrascaleplus.bus.amba.axi4._
import ultrascaleplus.clock.{ClockResetMapped, PLClockingArea}
import ultrascaleplus.clock.pll._
import ultrascaleplus.scripts._
import ultrascaleplus.utils._


class UltraScalePlusConfig(
  val withPL_CLK0    : HertzNumber = 100 MHz,
  val withPL_CLK1    : HertzNumber =   0 MHz,
  val withPL_CLK2    : HertzNumber =   0 MHz,
  val withPL_CLK3    : HertzNumber =   0 MHz,
  val withLPD_HPM0   : Boolean     = false,
  val withLPD_HP0    : Boolean     = false,
  val withFPD_HPM0   : Boolean     = false,
  val withFPD_HPM1   : Boolean     = false,
  val withFPD_HP0    : Boolean     = false,
  val withFPD_HP1    : Boolean     = false,
  val withFPD_HP2    : Boolean     = false,
  val withFPD_HP3    : Boolean     = false,
  val withFPD_HPC0   : Boolean     = false,
  val withFPD_HPC1   : Boolean     = false,
  val withFPD_ACP    : Boolean     = false,
  val withFPD_ACE    : Boolean     = false,
  val withDBG_CTI0   : Boolean     = false,
  val withDBG_CTI1   : Boolean     = false,
  val withDBG_CTI2   : Boolean     = false,
  val withDBG_CTI3   : Boolean     = false,
  val withDBG_CTO0   : Boolean     = false,
  val withDBG_CTO1   : Boolean     = false,
  val withDBG_CTO2   : Boolean     = false,
  val withDBG_CTO3   : Boolean     = false,
  val withPL_PS_IRQ0 : Int         =     0,
  val withPL_PS_IRQ1 : Int         =     0,
  val withTRACE      : Boolean     = false
  ) {
}


class UltraScalePlusIO(config: UltraScalePlusConfig) extends Bundle {
  val pl = new Bundle {
    val clk0 = (config.withPL_CLK0 > (0 MHz)) generate (    in(ClockResetMapped(PLL.IO, config.withPL_CLK0)))
    val clk1 = (config.withPL_CLK1 > (0 MHz)) generate (    in(ClockResetMapped(PLL.IO, config.withPL_CLK1)))
    val clk2 = (config.withPL_CLK2 > (0 MHz)) generate (    in(ClockResetMapped(PLL.IO, config.withPL_CLK2)))
    val clk3 = (config.withPL_CLK3 > (0 MHz)) generate (    in(ClockResetMapped(PLL.IO, config.withPL_CLK3)))
  }
  val lpd = new Bundle {
    val hp0  = (config.withLPD_HP0          ) generate (master(Axi4Mapped(LPD.HP0 )))
    val hpm0 = (config.withLPD_HPM0         ) generate ( slave(Axi4Mapped(LPD.HPM0)))
  }
  val fpd = new Bundle {
    val hpm0 = (config.withFPD_HPM0         ) generate ( slave(Axi4Mapped(FPD.HPM0)))
    val hpm1 = (config.withFPD_HPM1         ) generate ( slave(Axi4Mapped(FPD.HPM1)))
    val hp0  = (config.withFPD_HP0          ) generate (master(Axi4Mapped(FPD.HP0 )))
    val hp1  = (config.withFPD_HP1          ) generate (master(Axi4Mapped(FPD.HP1 )))
    val hp2  = (config.withFPD_HP2          ) generate (master(Axi4Mapped(FPD.HP2 )))
    val hp3  = (config.withFPD_HP3          ) generate (master(Axi4Mapped(FPD.HP3 )))
    val hpc0 = (config.withFPD_HPC0         ) generate (master(Axi4Mapped(FPD.HPC0)))
    val hpc1 = (config.withFPD_HPC1         ) generate (master(Axi4Mapped(FPD.HPC1)))
    val acp  = (config.withFPD_ACP          ) generate (master(Axi4Mapped(FPD.ACP )))
//  val fpd_ace  = (withFPD_ACE    ) generate ( slave(Axi4(KriaPorts.FPD_ACE_Config )))
  }
  val dbg = new Bundle {
    val cti0 = (config.withDBG_CTI0         ) generate ( slave(CrossTrigger()))
    val cti1 = (config.withDBG_CTI1         ) generate ( slave(CrossTrigger()))
    val cti2 = (config.withDBG_CTI2         ) generate ( slave(CrossTrigger()))
    val cti3 = (config.withDBG_CTI3         ) generate ( slave(CrossTrigger()))
    val cto0 = (config.withDBG_CTO0         ) generate (master(CrossTrigger()))
    val cto1 = (config.withDBG_CTO1         ) generate (master(CrossTrigger()))
    val cto2 = (config.withDBG_CTO2         ) generate (master(CrossTrigger()))
    val cto3 = (config.withDBG_CTO3         ) generate (master(CrossTrigger()))
  }
  val irq = new Bundle {
    val toPS0 = (config.withPL_PS_IRQ0 > 0  ) generate (out(IRQ(config.withPL_PS_IRQ0)))
    val toPS1 = (config.withPL_PS_IRQ1 > 0  ) generate (out(IRQ(config.withPL_PS_IRQ1)))
  }
  val trace = (config.withTRACE             ) generate ( in(Trace(32)))
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
  
  
  override def getTCL(): String = {
    var tcl = ""
    tcl +=f"set processing_system [ create_bd_cell -type ip -vlnv xilinx.com:ip:zynq_ultra_ps_e:${Vivado.getIPVersion("zynq_ultra_ps_e")} processing_system ]\n"
    tcl += "set_property -dict [list \\\n"
    tcl += "  CONFIG.PSU_BANK_0_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_BANK_1_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_BANK_2_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_BANK_3_IO_STANDARD {LVCMOS18} \\\n"
    tcl += "  CONFIG.PSU_DDR_RAM_HIGHADDR {0xFFFFFFFF} \\\n"
    tcl += "  CONFIG.PSU_DDR_RAM_HIGHADDR_OFFSET {0x800000000} \\\n"
    tcl += "  CONFIG.PSU_DDR_RAM_LOWADDR_OFFSET {0x80000000} \\\n"
    tcl += "  CONFIG.PSU_MIO_0_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_0_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_10_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_10_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_11_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_11_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_12_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_12_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_12_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_13_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_13_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_13_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_14_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_14_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_14_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_15_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_15_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_15_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_16_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_16_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_16_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_17_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_17_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_17_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_18_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_18_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_18_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_19_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_19_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_19_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_1_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_1_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_20_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_20_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_20_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_21_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_21_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_21_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_INPUT_TYPE {cmos} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_22_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_23_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_23_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_23_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_24_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_24_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_25_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_25_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_27_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_27_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_27_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_28_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_29_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_29_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_29_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_2_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_2_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_30_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_32_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_32_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_32_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_33_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_33_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_33_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_34_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_34_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_34_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_35_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_35_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_36_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_36_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_36_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_37_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_38_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_38_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_38_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_39_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_39_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_39_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_3_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_3_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_40_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_40_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_40_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_41_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_41_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_41_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_42_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_42_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_42_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_43_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_43_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_43_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_44_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_44_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_44_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_45_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_46_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_46_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_46_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_47_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_47_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_47_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_48_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_48_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_48_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_49_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_49_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_49_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_4_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_4_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_50_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_50_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_50_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_INPUT_TYPE {cmos} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_51_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_54_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_54_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_56_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_56_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_57_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_57_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_58_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_58_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_59_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_59_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_5_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_5_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_60_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_60_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_61_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_61_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_62_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_62_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_63_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_63_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_64_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_64_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_65_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_65_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_66_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_66_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_67_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_67_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_68_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_68_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_69_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_69_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_6_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_6_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_76_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_76_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_77_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_77_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_7_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_7_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_7_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_8_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_8_POLARITY {Default} \\\n"
    tcl += "  CONFIG.PSU_MIO_8_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_9_DRIVE_STRENGTH {4} \\\n"
    tcl += "  CONFIG.PSU_MIO_9_SLEW {slow} \\\n"
    tcl += "  CONFIG.PSU_MIO_TREE_PERIPHERALS {Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#Quad SPI Flash#SPI 1#GPIO0 MIO#GPIO0 MIO#SPI 1#SPI 1#SPI 1#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0\\\n"
    tcl += "MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#GPIO0 MIO#I2C 1#I2C 1#PMU GPI 0#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#PMU GPI 5#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#PMU GPO 3#GPIO1\\\n"
    tcl += "MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO#GPIO1 MIO##########################} \\\n"
    tcl += "  CONFIG.PSU_MIO_TREE_SIGNALS {sclk_out#miso_mo1#mo2#mo3#mosi_mi0#n_ss_out#sclk_out#gpio0[7]#gpio0[8]#n_ss_out[0]#miso#mosi#gpio0[12]#gpio0[13]#gpio0[14]#gpio0[15]#gpio0[16]#gpio0[17]#gpio0[18]#gpio0[19]#gpio0[20]#gpio0[21]#gpio0[22]#gpio0[23]#scl_out#sda_out#gpi[0]#gpio1[27]#gpio1[28]#gpio1[29]#gpio1[30]#gpi[5]#gpio1[32]#gpio1[33]#gpio1[34]#gpo[3]#gpio1[36]#gpio1[37]#gpio1[38]#gpio1[39]#gpio1[40]#gpio1[41]#gpio1[42]#gpio1[43]#gpio1[44]#gpio1[45]#gpio1[46]#gpio1[47]#gpio1[48]#gpio1[49]#gpio1[50]#gpio1[51]##########################}\\\n"
    tcl += "\\\n"
    tcl += "  CONFIG.PSU__ACT_DDR_FREQ_MHZ {1066.656006} \\\n"
    tcl += "  CONFIG.PSU__AFI0_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__AFI1_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__CAN1__PERIPHERAL__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU_CTRL__ACT_FREQMHZ {1333.333008} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU_CTRL__FREQMHZ {1333.333} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU_CTRL__SRCSEL {APLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__ACPU__FRAC_ENABLED {1} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__APLL_CTRL__FRACFREQ {1333.333} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__APLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__APLL_FRAC_CFG__ENABLED {1} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_FPD_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_FPD_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_FPD_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TRACE_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TRACE_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TSTMP_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TSTMP_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DBG_TSTMP_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DDR_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DDR_CTRL__FREQMHZ {1200} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DDR_CTRL__SRCSEL {DPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPDMA_REF_CTRL__ACT_FREQMHZ {444.444336} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPDMA_REF_CTRL__FREQMHZ {600} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPDMA_REF_CTRL__SRCSEL {APLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__DPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GDMA_REF_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GDMA_REF_CTRL__FREQMHZ {600} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GDMA_REF_CTRL__SRCSEL {DPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GPU_REF_CTRL__ACT_FREQMHZ {499.994995} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GPU_REF_CTRL__FREQMHZ {600} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__GPU_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_LSBUS_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_LSBUS_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_LSBUS_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_MAIN_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_MAIN_CTRL__FREQMHZ {533.33} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__TOPSW_MAIN_CTRL__SRCSEL {DPLL} \\\n"
    tcl += "  CONFIG.PSU__CRF_APB__VPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__ADMA_REF_CTRL__ACT_FREQMHZ {499.994995} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__ADMA_REF_CTRL__FREQMHZ {500} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__ADMA_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__AMS_REF_CTRL__ACT_FREQMHZ {49.999500} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CAN1_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CPU_R5_CTRL__ACT_FREQMHZ {533.328003} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CPU_R5_CTRL__FREQMHZ {533.333} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__CPU_R5_CTRL__SRCSEL {RPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DBG_LPD_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DBG_LPD_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DBG_LPD_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__DLL_REF_CTRL__ACT_FREQMHZ {999.989990} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__I2C1_REF_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__I2C1_REF_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__I2C1_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOU_SWITCH_CTRL__ACT_FREQMHZ {249.997498} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOU_SWITCH_CTRL__FREQMHZ {250} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__IOU_SWITCH_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_LSBUS_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_LSBUS_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_LSBUS_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_SWITCH_CTRL__ACT_FREQMHZ {499.994995} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_SWITCH_CTRL__FREQMHZ {500} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__LPD_SWITCH_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PCAP_CTRL__ACT_FREQMHZ {199.998001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PCAP_CTRL__FREQMHZ {200} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__PCAP_CTRL__SRCSEL {IOPLL} \\\n"
    if (config.withPL_CLK0 > (0 MHz)) {
      tcl +=f"  CONFIG.PSU__CRL_APB__PL0_REF_CTRL__ACT_FREQMHZ {${this.io.pl.clk0.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL0_REF_CTRL__FREQMHZ {${this.io.pl.clk0.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL0_REF_CTRL__SRCSEL {${this.io.pl.clk0.source.name}} \\\n"
    }
    if (config.withPL_CLK1 > (0 MHz)) {
      tcl +=f"  CONFIG.PSU__CRL_APB__PL1_REF_CTRL__ACT_FREQMHZ {${this.io.pl.clk1.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL1_REF_CTRL__FREQMHZ {${this.io.pl.clk1.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL1_REF_CTRL__SRCSEL {${this.io.pl.clk1.source.name}} \\\n"
    }
    if (config.withPL_CLK2 > (0 MHz)) {
      tcl +=f"  CONFIG.PSU__CRL_APB__PL2_REF_CTRL__ACT_FREQMHZ {${this.io.pl.clk2.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL2_REF_CTRL__FREQMHZ {${this.io.pl.clk2.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL2_REF_CTRL__SRCSEL {${this.io.pl.clk2.source.name}} \\\n"
    }
    if (config.withPL_CLK3 > (0 MHz)) {
      tcl +=f"  CONFIG.PSU__CRL_APB__PL3_REF_CTRL__ACT_FREQMHZ {${this.io.pl.clk3.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL3_REF_CTRL__FREQMHZ {${this.io.pl.clk3.frequency.decompose._1}} \\\n"
      tcl +=f"  CONFIG.PSU__CRL_APB__PL3_REF_CTRL__SRCSEL {${this.io.pl.clk3.source.name}} \\\n"
    }
    tcl += "  CONFIG.PSU__CRL_APB__QSPI_REF_CTRL__ACT_FREQMHZ {124.998749} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__QSPI_REF_CTRL__FREQMHZ {125} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__QSPI_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__RPLL_CTRL__SRCSEL {PSS_REF_CLK} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__SPI1_REF_CTRL__ACT_FREQMHZ {199.998001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__SPI1_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__TIMESTAMP_REF_CTRL__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__TIMESTAMP_REF_CTRL__FREQMHZ {100} \\\n"
    tcl += "  CONFIG.PSU__CRL_APB__TIMESTAMP_REF_CTRL__SRCSEL {IOPLL} \\\n"
    tcl += "  CONFIG.PSU__CSUPMU__PERIPHERAL__VALID {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__BG_ADDR_COUNT {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__BRC_MAPPING {ROW_BANK_COL} \\\n"
    tcl += "  CONFIG.PSU__DDRC__BUS_WIDTH {64 Bit} \\\n"
    tcl += "  CONFIG.PSU__DDRC__CL {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__CLOCK_STOP_EN {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__COMPONENTS {Components} \\\n"
    tcl += "  CONFIG.PSU__DDRC__CWL {14} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_ADDR_MAPPING {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_CAL_MODE_ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_CRC_CONTROL {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_T_REF_MODE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DDR4_T_REF_RANGE {Normal (0-85)} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DEVICE_CAPACITY {8192 MBits} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DM_DBI {DM_NO_DBI} \\\n"
    tcl += "  CONFIG.PSU__DDRC__DRAM_WIDTH {16 Bits} \\\n"
    tcl += "  CONFIG.PSU__DDRC__ECC {Disabled} \\\n"
    tcl += "  CONFIG.PSU__DDRC__FGRM {1X} \\\n"
    tcl += "  CONFIG.PSU__DDRC__LP_ASR {manual normal} \\\n"
    tcl += "  CONFIG.PSU__DDRC__MEMORY_TYPE {DDR 4} \\\n"
    tcl += "  CONFIG.PSU__DDRC__PARITY_ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__PER_BANK_REFRESH {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__PHY_DBI_MODE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__RANK_ADDR_COUNT {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__ROW_ADDR_COUNT {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__SELF_REF_ABORT {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__SPEED_BIN {DDR4_2400R} \\\n"
    tcl += "  CONFIG.PSU__DDRC__STATIC_RD_MODE {0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__TRAIN_DATA_EYE {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__TRAIN_READ_GATE {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__TRAIN_WRITE_LEVEL {1} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_FAW {30.0} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RAS_MIN {33} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RC {47.06} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RCD {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__T_RP {16} \\\n"
    tcl += "  CONFIG.PSU__DDRC__VREF {1} \\\n"
    tcl += "  CONFIG.PSU__DDR_HIGH_ADDRESS_GUI_ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__DDR__INTERFACE__FREQMHZ {600.000} \\\n"
    tcl += "  CONFIG.PSU__FPD_SLCR__WDT1__ACT_FREQMHZ {99.999001} \\\n"
    tcl +=f"  CONFIG.PSU__FPGA_PL0_ENABLE {${(config.withPL_CLK0 > (0 MHz)).toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FPGA_PL1_ENABLE {${(config.withPL_CLK1 > (0 MHz)).toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FPGA_PL2_ENABLE {${(config.withPL_CLK2 > (0 MHz)).toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FPGA_PL3_ENABLE {${(config.withPL_CLK3 > (0 MHz)).toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_IN_0 {${config.withDBG_CTI0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_IN_1 {${config.withDBG_CTI1.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_IN_2 {${config.withDBG_CTI2.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_IN_3 {${config.withDBG_CTI3.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_OUT_0 {${config.withDBG_CTO0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_OUT_1 {${config.withDBG_CTO1.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_OUT_2 {${config.withDBG_CTO2.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__FTM__CTI_OUT_3 {${config.withDBG_CTO3.toInt}} \\\n"
    tcl += "  CONFIG.PSU__GPIO0_MIO__IO {MIO 0 .. 25} \\\n"
    tcl += "  CONFIG.PSU__GPIO0_MIO__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__GPIO1_MIO__IO {MIO 26 .. 51} \\\n"
    tcl += "  CONFIG.PSU__GPIO1_MIO__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__I2C0__PERIPHERAL__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__I2C1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__I2C1__PERIPHERAL__IO {MIO 24 .. 25} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC0_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC1_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC2_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__IOU_TTC_APB_CLK__TTC3_SEL {APB} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC0__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC1__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC2__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__TTC3__ACT_FREQMHZ {100.000000} \\\n"
    tcl += "  CONFIG.PSU__IOU_SLCR__WDT0__ACT_FREQMHZ {99.999001} \\\n"
    tcl += "  CONFIG.PSU__LPD_SLCR__CSUPMU__ACT_FREQMHZ {100.000000} \\\n"
    if (this.config.withFPD_HPM0)
      tcl += "  CONFIG.PSU__MAXIGP0__DATA_WIDTH {128} \\\n"
    if (this.config.withFPD_HPM1)
      tcl += "  CONFIG.PSU__MAXIGP1__DATA_WIDTH {128} \\\n"
    if (this.config.withLPD_HPM0)
      tcl += "  CONFIG.PSU__MAXIGP2__DATA_WIDTH {128} \\\n"
    tcl += "  CONFIG.PSU__NUM_FABRIC_RESETS {4} \\\n"
    tcl += "  CONFIG.PSU__OVERRIDE__BASIC_CLOCK {0} \\\n"
    tcl += "  CONFIG.PSU__PL_CLK0_BUF {"+(if (config.withPL_CLK0 > (0 MHz)) "True" else "False")+"} \\\n"
    tcl += "  CONFIG.PSU__PL_CLK1_BUF {"+(if (config.withPL_CLK1 > (0 MHz)) "True" else "False")+"} \\\n"
    tcl += "  CONFIG.PSU__PL_CLK2_BUF {"+(if (config.withPL_CLK2 > (0 MHz)) "True" else "False")+"} \\\n"
    tcl += "  CONFIG.PSU__PL_CLK3_BUF {"+(if (config.withPL_CLK3 > (0 MHz)) "True" else "False")+"} \\\n"
    tcl += "  CONFIG.PSU__PMU_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__AIBACK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__EMIO_GPI__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__EMIO_GPO__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI0__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI0__IO {MIO 26} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI1__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI2__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI3__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI4__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI5__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPI5__IO {MIO 31} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO0__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO1__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO2__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO3__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO3__IO {MIO 35} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO3__POLARITY {low} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO4__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__GPO5__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PMU__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__PMU__PLERROR__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__PRESET_APPLIED {1} \\\n"
    tcl += "  CONFIG.PSU__PROTECTION__MASTERS {USB1:NonSecure;0|USB0:NonSecure;0|S_AXI_LPD:NA;0|S_AXI_HPC1_FPD:NA;1|S_AXI_HPC0_FPD:NA;1|S_AXI_HP3_FPD:NA;1|S_AXI_HP2_FPD:NA;1|S_AXI_HP1_FPD:NA;1|S_AXI_HP0_FPD:NA;1|S_AXI_ACP:NA;0|S_AXI_ACE:NA;0|SD1:NonSecure;0|SD0:NonSecure;0|SATA1:NonSecure;0|SATA0:NonSecure;0|RPU1:Secure;1|RPU0:Secure;1|QSPI:NonSecure;1|PMU:NA;1|PCIe:NonSecure;0|NAND:NonSecure;0|LDMA:NonSecure;1|GPU:NonSecure;1|GEM3:NonSecure;0|GEM2:NonSecure;0|GEM1:NonSecure;0|GEM0:NonSecure;0|FDMA:NonSecure;1|DP:NonSecure;0|DAP:NA;1|Coresight:NA;1|CSU:NA;1|APU:NA;1} \\\n"
    tcl += "  \\\n"
    tcl += "  CONFIG.PSU__PROTECTION__SLAVES {LPD;USB3_1_XHCI;FE300000;FE3FFFFF;0|LPD;USB3_1;FF9E0000;FF9EFFFF;0|LPD;USB3_0_XHCI;FE200000;FE2FFFFF;0|LPD;USB3_0;FF9D0000;FF9DFFFF;0|LPD;UART1;FF010000;FF01FFFF;0|LPD;UART0;FF000000;FF00FFFF;0|LPD;TTC3;FF140000;FF14FFFF;1|LPD;TTC2;FF130000;FF13FFFF;1|LPD;TTC1;FF120000;FF12FFFF;1|LPD;TTC0;FF110000;FF11FFFF;1|FPD;SWDT1;FD4D0000;FD4DFFFF;1|LPD;SWDT0;FF150000;FF15FFFF;1|LPD;SPI1;FF050000;FF05FFFF;1|LPD;SPI0;FF040000;FF04FFFF;0|FPD;SMMU_REG;FD5F0000;FD5FFFFF;1|FPD;SMMU;FD800000;FDFFFFFF;1|FPD;SIOU;FD3D0000;FD3DFFFF;1|FPD;SERDES;FD400000;FD47FFFF;1|LPD;SD1;FF170000;FF17FFFF;0|LPD;SD0;FF160000;FF16FFFF;0|FPD;SATA;FD0C0000;FD0CFFFF;0|LPD;RTC;FFA60000;FFA6FFFF;1|LPD;RSA_CORE;FFCE0000;FFCEFFFF;1|LPD;RPU;FF9A0000;FF9AFFFF;1|LPD;R5_TCM_RAM_GLOBAL;FFE00000;FFE3FFFF;1|LPD;R5_1_Instruction_Cache;FFEC0000;FFECFFFF;1|LPD;R5_1_Data_Cache;FFED0000;FFEDFFFF;1|LPD;R5_1_BTCM_GLOBAL;FFEB0000;FFEBFFFF;1|LPD;R5_1_ATCM_GLOBAL;FFE90000;FFE9FFFF;1|LPD;R5_0_Instruction_Cache;FFE40000;FFE4FFFF;1|LPD;R5_0_Data_Cache;FFE50000;FFE5FFFF;1|LPD;R5_0_BTCM_GLOBAL;FFE20000;FFE2FFFF;1|LPD;R5_0_ATCM_GLOBAL;FFE00000;FFE0FFFF;1|LPD;QSPI_Linear_Address;C0000000;DFFFFFFF;1|LPD;QSPI;FF0F0000;FF0FFFFF;1|LPD;PMU_RAM;FFDC0000;FFDDFFFF;1|LPD;PMU_GLOBAL;FFD80000;FFDBFFFF;1|FPD;PCIE_MAIN;FD0E0000;FD0EFFFF;0|FPD;PCIE_LOW;E0000000;EFFFFFFF;0|FPD;PCIE_HIGH2;8000000000;BFFFFFFFFF;0|FPD;PCIE_HIGH1;600000000;7FFFFFFFF;0|FPD;PCIE_DMA;FD0F0000;FD0FFFFF;0|FPD;PCIE_ATTRIB;FD480000;FD48FFFF;0|LPD;OCM_XMPU_CFG;FFA70000;FFA7FFFF;1|LPD;OCM_SLCR;FF960000;FF96FFFF;1|OCM;OCM;FFFC0000;FFFFFFFF;1|LPD;NAND;FF100000;FF10FFFF;0|LPD;MBISTJTAG;FFCF0000;FFCFFFFF;1|LPD;LPD_XPPU_SINK;FF9C0000;FF9CFFFF;1|LPD;LPD_XPPU;FF980000;FF98FFFF;1|LPD;LPD_SLCR_SECURE;FF4B0000;FF4DFFFF;1|LPD;LPD_SLCR;FF410000;FF4AFFFF;1|LPD;LPD_GPV;FE100000;FE1FFFFF;1|LPD;LPD_DMA_7;FFAF0000;FFAFFFFF;1|LPD;LPD_DMA_6;FFAE0000;FFAEFFFF;1|LPD;LPD_DMA_5;FFAD0000;FFADFFFF;1|LPD;LPD_DMA_4;FFAC0000;FFACFFFF;1|LPD;LPD_DMA_3;FFAB0000;FFABFFFF;1|LPD;LPD_DMA_2;FFAA0000;FFAAFFFF;1|LPD;LPD_DMA_1;FFA90000;FFA9FFFF;1|LPD;LPD_DMA_0;FFA80000;FFA8FFFF;1|LPD;IPI_CTRL;FF380000;FF3FFFFF;1|LPD;IOU_SLCR;FF180000;FF23FFFF;1|LPD;IOU_SECURE_SLCR;FF240000;FF24FFFF;1|LPD;IOU_SCNTRS;FF260000;FF26FFFF;1|LPD;IOU_SCNTR;FF250000;FF25FFFF;1|LPD;IOU_GPV;FE000000;FE0FFFFF;1|LPD;I2C1;FF030000;FF03FFFF;1|LPD;I2C0;FF020000;FF02FFFF;0|FPD;GPU;FD4B0000;FD4BFFFF;1|LPD;GPIO;FF0A0000;FF0AFFFF;1|LPD;GEM3;FF0E0000;FF0EFFFF;0|LPD;GEM2;FF0D0000;FF0DFFFF;0|LPD;GEM1;FF0C0000;FF0CFFFF;0|LPD;GEM0;FF0B0000;FF0BFFFF;0|FPD;FPD_XMPU_SINK;FD4F0000;FD4FFFFF;1|FPD;FPD_XMPU_CFG;FD5D0000;FD5DFFFF;1|FPD;FPD_SLCR_SECURE;FD690000;FD6CFFFF;1|FPD;FPD_SLCR;FD610000;FD68FFFF;1|FPD;FPD_DMA_CH7;FD570000;FD57FFFF;1|FPD;FPD_DMA_CH6;FD560000;FD56FFFF;1|FPD;FPD_DMA_CH5;FD550000;FD55FFFF;1|FPD;FPD_DMA_CH4;FD540000;FD54FFFF;1|FPD;FPD_DMA_CH3;FD530000;FD53FFFF;1|FPD;FPD_DMA_CH2;FD520000;FD52FFFF;1|FPD;FPD_DMA_CH1;FD510000;FD51FFFF;1|FPD;FPD_DMA_CH0;FD500000;FD50FFFF;1|LPD;EFUSE;FFCC0000;FFCCFFFF;1|FPD;Display \\\n"
    tcl += "  Port;FD4A0000;FD4AFFFF;0|FPD;DPDMA;FD4C0000;FD4CFFFF;0|FPD;DDR_XMPU5_CFG;FD050000;FD05FFFF;1|FPD;DDR_XMPU4_CFG;FD040000;FD04FFFF;1|FPD;DDR_XMPU3_CFG;FD030000;FD03FFFF;1|FPD;DDR_XMPU2_CFG;FD020000;FD02FFFF;1|FPD;DDR_XMPU1_CFG;FD010000;FD01FFFF;1|FPD;DDR_XMPU0_CFG;FD000000;FD00FFFF;1|FPD;DDR_QOS_CTRL;FD090000;FD09FFFF;1|FPD;DDR_PHY;FD080000;FD08FFFF;1|DDR;DDR_LOW;0;7FFFFFFF;1|DDR;DDR_HIGH;800000000;87FFFFFFF;1|FPD;DDDR_CTRL;FD070000;FD070FFF;1|LPD;Coresight;FE800000;FEFFFFFF;1|LPD;CSU_DMA;FFC80000;FFC9FFFF;1|LPD;CSU;FFCA0000;FFCAFFFF;1|LPD;CRL_APB;FF5E0000;FF85FFFF;1|FPD;CRF_APB;FD1A0000;FD2DFFFF;1|FPD;CCI_REG;FD5E0000;FD5EFFFF;1|LPD;CAN1;FF070000;FF07FFFF;0|LPD;CAN0;FF060000;FF06FFFF;0|FPD;APU;FD5C0000;FD5CFFFF;1|LPD;APM_INTC_IOU;FFA20000;FFA2FFFF;1|LPD;APM_FPD_LPD;FFA30000;FFA3FFFF;1|FPD;APM_5;FD490000;FD49FFFF;1|FPD;APM_0;FD0B0000;FD0BFFFF;1|LPD;APM2;FFA10000;FFA1FFFF;1|LPD;APM1;FFA00000;FFA0FFFF;1|LPD;AMS;FFA50000;FFA5FFFF;1|FPD;AFI_5;FD3B0000;FD3BFFFF;1|FPD;AFI_4;FD3A0000;FD3AFFFF;1|FPD;AFI_3;FD390000;FD39FFFF;1|FPD;AFI_2;FD380000;FD38FFFF;1|FPD;AFI_1;FD370000;FD37FFFF;1|FPD;AFI_0;FD360000;FD36FFFF;1|LPD;AFIFM6;FF9B0000;FF9BFFFF;1|FPD;ACPU_GIC;F9010000;F907FFFF;1} \\\n"
    tcl += "  \\\n"
    tcl += "  CONFIG.PSU__PSS_REF_CLK__FREQMHZ {33.333} \\\n"
    tcl += "  CONFIG.PSU__QSPI_COHERENCY {0} \\\n"
    tcl += "  CONFIG.PSU__QSPI_ROUTE_THROUGH_FPD {0} \\\n"
    tcl += "  CONFIG.PSU__QSPI__GRP_FBCLK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__DATA_MODE {x4} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__IO {MIO 0 .. 5} \\\n"
    tcl += "  CONFIG.PSU__QSPI__PERIPHERAL__MODE {Single} \\\n"
    if (this.config.withFPD_HPC0)
      tcl += "  CONFIG.PSU__SAXIGP0__DATA_WIDTH {128} \\\n"
    if (this.config.withFPD_HPC1)
      tcl += "  CONFIG.PSU__SAXIGP1__DATA_WIDTH {128} \\\n"
    if (this.config.withFPD_HP0)
      tcl += "  CONFIG.PSU__SAXIGP2__DATA_WIDTH {128} \\\n"
    if (this.config.withFPD_HP1)
      tcl += "  CONFIG.PSU__SAXIGP3__DATA_WIDTH {128} \\\n"
    if (this.config.withFPD_HP2)
      tcl += "  CONFIG.PSU__SAXIGP4__DATA_WIDTH {128} \\\n"
    if (this.config.withFPD_HP3)
      tcl += "  CONFIG.PSU__SAXIGP5__DATA_WIDTH {128} \\\n"
    if (this.config.withLPD_HP0)
      tcl += "  CONFIG.PSU__SAXIGP6__DATA_WIDTH {128} \\\n"
    tcl += "  CONFIG.PSU__SPI1__GRP_SS0__IO {MIO 9} \\\n"
    tcl += "  CONFIG.PSU__SPI1__GRP_SS1__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SPI1__GRP_SS2__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SPI1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__SPI1__PERIPHERAL__IO {MIO 6 .. 11} \\\n"
    tcl += "  CONFIG.PSU__SWDT0__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SWDT0__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__SWDT0__RESET__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SWDT1__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__SWDT1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__SWDT1__RESET__ENABLE {0} \\\n"
    if (this.config.withTRACE) {
      tcl += "  CONFIG.PSU__TRACE__INTERNAL_WIDTH {32} \\\n"
      tcl += "  CONFIG.PSU__TRACE__PERIPHERAL__ENABLE {1} \\\n"
      tcl += "  CONFIG.PSU__TRACE__PERIPHERAL__IO {EMIO} \\\n"
      tcl += "  CONFIG.PSU__TRACE__WIDTH {32Bit} \\\n"
    }
    tcl += "  CONFIG.PSU__TTC0__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC0__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC0__WAVEOUT__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC1__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC1__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC1__WAVEOUT__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC2__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC2__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC2__WAVEOUT__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC3__CLOCK__ENABLE {0} \\\n"
    tcl += "  CONFIG.PSU__TTC3__PERIPHERAL__ENABLE {1} \\\n"
    tcl += "  CONFIG.PSU__TTC3__WAVEOUT__ENABLE {0} \\\n"
    tcl +=f"  CONFIG.PSU__USE__IRQ0 {${this.config.withPL_PS_IRQ0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__IRQ1 {${this.config.withPL_PS_IRQ1.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__M_AXI_GP0 {${this.config.withFPD_HPM0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__M_AXI_GP1 {${this.config.withFPD_HPM1.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__M_AXI_GP2 {${this.config.withLPD_HPM0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_ACP {${this.config.withFPD_ACP.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP0 {${this.config.withFPD_HPC0.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP1 {${this.config.withFPD_HPC1.toInt}} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP2 {${this.config.withFPD_HP0.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP3 {${this.config.withFPD_HP1.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP4 {${this.config.withFPD_HP2.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP5 {${this.config.withFPD_HP3.toInt }} \\\n"
    tcl +=f"  CONFIG.PSU__USE__S_AXI_GP6 {${this.config.withLPD_HP0.toInt }} \\\n"
    tcl += "] $processing_system\n"
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
