package ultrascaleplus.ip


import spinal.core._
import spinal.lib._


import ultrascaleplus.utils._


/** Abstract class designed to create a "nice" interface between SpinalHDL
 *  and Verilog. Mostly use to hide hardcoded connections between the SpinalHDL
 *  structured/hierarchical IO  ([[Bundle]]) and the "unstructured" verilog IO.
 *
 */
abstract class XilinxIPBlackBox() extends Component with TCL {

  /** The verilog shell to pretend exists (i.e., black box). */
  val blackbox: BlackBox

}
