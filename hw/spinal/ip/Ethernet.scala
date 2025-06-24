package ultrascaleplus.ip


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


import ultrascaleplus.io.gt._


object Ethernet {

  case class xxv_ethernet() extends BlackBox() {
  
    val io = new Bundle {
  //    val ctl_tx_send_idle_0           =  in(Bool())
  //    val ctl_tx_send_lfi_0            =  in(Bool())
  //    val ctl_tx_send_rfi_0            =  in(Bool())
  //    val dclk                         =  in(Bool())
  //    val gt_refclk_n                  =  in(Bool())
  //    val gt_refclk_p                  =  in(Bool())
      val gt_rxn_in                    =  in(Bool())
      val gt_rxp_in                    =  in(Bool())
      val gt_txn_out                   = out(Bool())
      val gt_txp_out                   = out(Bool())
  //    val gtwiz_reset_rx_datapath_0    =  in(Bool())
  //    val gtwiz_reset_tx_datapath_0    = out(Bool())
  //    val pm_tick_0                    =  in(Bool())
  //    val qpllreset_in_0               =  in(Bool())
  //    val rx_clk_out_0                 = out(Bool())
  //    val rx_core_clk_0                =  in(Bool())
  //    val rx_reset_0                   =  in(Bool())
  //    val rxoutclksel_in_0             = out(Bool())
  //    val s_axi_aclk_0                 =  in(Bool())
  //    val s_axi_aresetn_0              =  in(Bool())
  //    val s_axi_arvalid_0              =  in(Bool())
  //    val s_axi_arready_0              = out(Bool())
  //    val s_axi_araddr_0               =  in(UInt(32 bits))
  //    val s_axi_rvalid_0               = out(Bool())
  //    val s_axi_rready_0               =  in(Bool())
  //    val s_axi_rdata_0                = out(UInt(32 bits))
  //    val s_axi_rresp_0                = out(UInt( 3 bits))
  //    val s_axi_awvalid_0              =  in(Bool())
  //    val s_axi_awready_0              = out(Bool())
  //    val s_axi_awaddr_0               =  in(UInt(32 bits))
  //    val s_axi_wvalid_0               =  in(Bool())
  //    val s_axi_wready_0               = out(Bool())
  //    val s_axi_wdata_0                =  in(UInt(32 bits))
  //    val s_axi_wstrb_0                =  in(UInt( 4 bits))
  //    val s_axi_bvalid_0               = out(Bool())
  //    val s_axi_bready_0               =  in(Bool())
  //    val s_axi_bresp_0                = out(UInt( 3 bits))
  //    val stat_tx_bad_fcs_0            = out(Bool())
  //    val stat_tx_frame_error_0        = out(Bool())
  //    val stat_tx_local_fault_0        = out(Bool())
  //    val stat_tx_packet_large_0       = out(Bool())
  //    val stat_tx_packet_small_0       = out(Bool())
  //    val stat_tx_total_good_packets_0 = out(UInt(32 bits))
  //    val sys_reset                    =  in(Bool())
  //    val tx_axis_tvalid_0             =  in(Bool())
  //    val tx_axis_tready_0             = out(Bool())
  //    val tx_axis_tdata_0              =  in(UInt(32 bits))
  //    val tx_axis_tkeep_0              =  in(Bool())
  //    val tx_axis_tlast_0              =  in(Bool())
  //    val tx_axis_tuser_0              =  in(UInt( 1 bits))
  //    val tx_clk_out_0                 = out(Bool())
  //    val tx_preamblein_0              =  in(Bool())
  //    val tx_reset_0                   =  in(Bool())
  //    val tx_unfout_0                  = out(Bool())
  //    val txoutclksel_in_0             =  in(Bool())
  //    val user_tx_reset_0              =  in(Bool())
    }
  
  }


  /** Class structuring/defining the required values for configuring a 
   *  Xilinx ethernet IP.
   *
   *  @constructor Builds a Xilinx Ethernet IP configuration.
   *  @param group The GT group selection.
   *  @param lane The selected lane.
   */
  case class Config(group: String, lane: String) {}

}


/** Class encapsulating the Xilinx Ethernet IP and generating the glueing
 *  logic and TCL.
 *
 *  @constructor config Xilinx ethernet IP configuration [[Ethernet.Config]].
 */
case class Ethernet(config: Ethernet.Config) extends XilinxIPBlackBox() {

  override def getTCL(moduleName: String, clock: String): String = {
    var tcl = ""
    tcl += "set_property -dict [list\n"
    tcl += "  CONFIG.BASE_R_KR {BASE-R}\n"
    tcl += "  CONFIG.GT_GROUP_SELECT {"+this.config.group+"}\n"
    tcl += "  CONFIG.INCLUDE_AXI4_INTERFACE {1}\n"
    tcl += "  CONFIG.INCLUDE_STATISTICS_COUNTERS {1}\n"
    tcl += "  CONFIG.LANE1_GT_LOC {"+this.config.lane+"}\n"
    tcl += "] ${"+this.blackbox.getName()+"}\n"
    tcl += "\n"
    return tcl
  }

  override val blackbox = Ethernet.xxv_ethernet()

  val io = new Bundle{
    val gt = master(GT())
  } 

  // Hardcoded io connections
  io.gt.tx.n <> blackbox.io.gt_txn_out
  io.gt.tx.p <> blackbox.io.gt_txp_out
  io.gt.rx.n <> blackbox.io.gt_rxn_in
  io.gt.rx.p <> blackbox.io.gt_rxp_in
  io.gt.sfp.dis := False

}
