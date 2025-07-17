package ultrascaleplus.ip


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.bus.amba4.axilite._
import spinal.lib.bus.amba4.axis._


import ultrascaleplus.io.gt._


object Ethernet {

  case class xxv_ethernet() extends BlackBox() {
  
    val io = new Bundle {
      val ctl_tx_send_idle_0           =  in(Bool())
      val ctl_tx_send_lfi_0            =  in(Bool())
      val ctl_tx_send_rfi_0            =  in(Bool())
  //    val dclk                         =  in(Bool()) // clock 75 MHz
  //    val gt_refclk_n                  =  in(Bool()) // Comes from the constraint. SI570User (dtb). Specified in constraints
  //    val gt_refclk_p                  =  in(Bool()) // Comes from the constraint. SI570User (dtb). Specified in constraints
      val gt_rxn_in                    =  in(Bool())
      val gt_rxp_in                    =  in(Bool())
      val gt_txn_out                   = out(Bool())
      val gt_txp_out                   = out(Bool())
  //    val gtwiz_reset_rx_datapath_0    =  in(Bool()) // same reset as everyone
  //    val gtwiz_reset_tx_datapath_0    =  in(Bool()) // same reset as everyone
      val pm_tick_0                    =  in(Bool())
      val qpllreset_in_0               =  in(Bool())
      val rx_reset_0                   =  in(Bool())
      val tx_reset_0                   =  in(Bool())
      val rx_clk_out_0                 = out(Bool())
      val rx_core_clk_0                =  in(Bool())
  //    val tx_clk_out_0                 = out(Bool()) // Operating clock of the IP; must be fedback to data source
  //    val user_tx_reset_0              = out(Bool()) // reset associated with tx_clk_out_0
  //    val sys_reset                    =  in(Bool()) // overall reset of the IP (TODO: to double)
      val tx_preamblein_0              =  in(UInt(56 bits))
  //    val tx_unfout_0                  = out(Bool()) // Not connected (maybe an error signal)
      val txoutclksel_in_0             =  in(UInt(3 bits))
      val rxoutclksel_in_0             =  in(UInt(3 bits))
  //    val s_axi_aclk_0                 =  in(Bool()) // in patrick's design same as 75 MHz (TODO: check if must be the same or different)
  //    val s_axi_aresetn_0              =  in(Bool())
      val s_axi_arvalid_0              =  in(Bool())
      val s_axi_arready_0              = out(Bool())
      val s_axi_araddr_0               =  in(UInt(32 bits))
      val s_axi_rvalid_0               = out(Bool())
      val s_axi_rready_0               =  in(Bool())
      val s_axi_rdata_0                = out(Bits(32 bits))
      val s_axi_rresp_0                = out(Bits( 2 bits))
      val s_axi_awvalid_0              =  in(Bool())
      val s_axi_awready_0              = out(Bool())
      val s_axi_awaddr_0               =  in(UInt(32 bits))
      val s_axi_wvalid_0               =  in(Bool())
      val s_axi_wready_0               = out(Bool())
      val s_axi_wdata_0                =  in(Bits(32 bits))
      val s_axi_wstrb_0                =  in(Bits( 4 bits))
      val s_axi_bvalid_0               = out(Bool())
      val s_axi_bready_0               =  in(Bool())
      val s_axi_bresp_0                = out(Bits( 2 bits))
      val stat_tx_bad_fcs_0            = out(Bool())
      val stat_tx_frame_error_0        = out(Bool())
      val stat_tx_local_fault_0        = out(Bool())
      val stat_tx_packet_large_0       = out(Bool())
      val stat_tx_packet_small_0       = out(Bool())
      val stat_tx_total_good_packets_0 = out(UInt(32 bits))
      val tx_axis_tvalid_0             =  in(Bool())
      val tx_axis_tready_0             = out(Bool())
      val tx_axis_tdata_0              =  in(Bits(32 bits))
      val tx_axis_tkeep_0              =  in(Bits( 4 bits))
      val tx_axis_tlast_0              =  in(Bool())
      val tx_axis_tuser_0              =  in(Bits( 4 bits))
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

  /** Configuration of the Slave/Secondary AxiLite configuration port of the 
   *  Xilinx Ethenet IP. Mirrors the blackboxed IP; do not modify.
   */
  val AxiPortConfig = AxiLite4Config(32, 32)

  /** configuration of the transmission (TX) Axi4Stream port of the Xilinx
   *  Ethernet IP. Mirrors the blackboxed IP; do not modify.
   */
  val AxiTXConfig = Axi4StreamConfig(
    dataWidth =    4,
    userWidth =    1,
    useKeep   = true,
    useLast   = true,
    useUser   = true
  )

  case class StatInterface() extends Bundle with IMasterSlave {
    // TODO: could be defined as an interface! (xilinx.com:display_xxv_ethernet:statics_port:2.0)
 
    val bad_fcs_0            = out(Bool())
    val frame_error_0        = out(Bool())
    val local_fault_0        = out(Bool())
    val packet_large_0       = out(Bool())
    val packet_small_0       = out(Bool())
    val total_good_packets_0 = out(UInt(32 bits))

    override def asMaster(): Unit = {
      in(bad_fcs_0, frame_error_0, local_fault_0, packet_large_0, packet_small_0, total_good_packets_0)
    }

  }

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

  /*
  this.afterElaboration({
    assert(
      assertion = (this.clockDomain.get.frequency.getValue == (74.99252 MHz)),
      message   = f"Xilinx ethernet IP (${this.getName()}) requires a frequency of 75 MHz (i.e., 74.99252 MHz) but ${this.clockDomain.get.frequency.getValue} is provided."
    )
  })
  */

  val io = new Bundle{
    val gt  = master(GT())
    val axi = slave(AxiLite4(Ethernet.AxiPortConfig))
    val tx  = new Bundle {
      val axis = slave(Axi4Stream(Ethernet.AxiTXConfig))
    }
    val stat = new Bundle {
      val tx = out(Ethernet.StatInterface())
    }
  }

  // Hardcoded io connections
  blackbox.io.ctl_tx_send_idle_0  <> False
  blackbox.io.ctl_tx_send_lfi_0   <> False
  blackbox.io.ctl_tx_send_rfi_0   <> False
  blackbox.io.pm_tick_0           <> False
  blackbox.io.qpllreset_in_0      <> False
  blackbox.io.rx_reset_0          <> False
  blackbox.io.tx_reset_0          <> False
  blackbox.io.tx_preamblein_0     <> 0
  blackbox.io.txoutclksel_in_0    <> 0
  blackbox.io.rxoutclksel_in_0    <> 0
  blackbox.io.rx_clk_out_0        <> blackbox.io.rx_core_clk_0
  //// GT
  io.gt.tx.n                      <> blackbox.io.gt_txn_out
  io.gt.tx.p                      <> blackbox.io.gt_txp_out
  io.gt.rx.n                      <> blackbox.io.gt_rxn_in
  io.gt.rx.p                      <> blackbox.io.gt_rxp_in
  io.gt.sfp.dis                   <> False
  //// AXI 
  blackbox.io.s_axi_arvalid_0     <> io.axi.ar.valid
  io.axi.ar.ready                 <> blackbox.io.s_axi_arready_0
  blackbox.io.s_axi_araddr_0      <> io.axi.ar.addr
  io.axi.r.valid                  <> blackbox.io.s_axi_rvalid_0
  blackbox.io.s_axi_rready_0      <> io.axi.r.ready
  io.axi.r.data                   <> blackbox.io.s_axi_rdata_0
  io.axi.r.resp                   <> blackbox.io.s_axi_rresp_0
  blackbox.io.s_axi_awvalid_0     <> io.axi.aw.valid
  io.axi.aw.ready                 <> blackbox.io.s_axi_awready_0
  blackbox.io.s_axi_awaddr_0      <> io.axi.aw.addr
  blackbox.io.s_axi_wvalid_0      <> io.axi.w.valid
  io.axi.w.ready                  <> blackbox.io.s_axi_wready_0
  blackbox.io.s_axi_wdata_0       <> io.axi.w.data
  blackbox.io.s_axi_wstrb_0       <> io.axi.w.strb
  io.axi.b.valid                  <> blackbox.io.s_axi_bvalid_0
  blackbox.io.s_axi_bready_0      <> io.axi.b.ready
  io.axi.b.resp                   <> 0
  //// TX
  blackbox.io.tx_axis_tvalid_0    <> io.tx.axis.valid
  io.tx.axis.ready                <> blackbox.io.tx_axis_tready_0
  blackbox.io.tx_axis_tdata_0     <> io.tx.axis.data
  blackbox.io.tx_axis_tkeep_0     <> io.tx.axis.keep
  blackbox.io.tx_axis_tlast_0     <> io.tx.axis.last
  blackbox.io.tx_axis_tuser_0     <> io.tx.axis.user
  //// TX STAT
  io.stat.tx.bad_fcs_0            <> blackbox.io.stat_tx_bad_fcs_0
  io.stat.tx.frame_error_0        <> blackbox.io.stat_tx_frame_error_0
  io.stat.tx.local_fault_0        <> blackbox.io.stat_tx_local_fault_0
  io.stat.tx.packet_large_0       <> blackbox.io.stat_tx_packet_large_0
  io.stat.tx.packet_small_0       <> blackbox.io.stat_tx_packet_small_0
  io.stat.tx.total_good_packets_0 <> blackbox.io.stat_tx_total_good_packets_0

}
