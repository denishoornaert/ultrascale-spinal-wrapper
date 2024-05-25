package kv260.interface.pmod

import spinal.core._
import spinal.lib._

import generic.interface.pmod._
import ultrascaleplus.interface.pmod._


object IO_PMOD0 extends AbstractPMOD() {

  override val port = slave(PMOD())
 
  /*
   * Note: The position of the pin names in the list matters! The position is the number of the pin in the pmod interface.
   * H12 -> 0 upper part
   * E10 -> 2 upper part
   * D10 -> 4 upper part
   * C11 -> 6 upper part
   * B10 -> 1 lower part
   * E12 -> 3 lower part
   * D11 -> 5 lower part
   * B11 -> 7 lower part
   */
  override val names = Array("H12", "B10", "E10", "E12", "D10", "D11", "C11", "B11")

  this.port.setPartialName("pmod0")
  this.addToConstraints()

}
