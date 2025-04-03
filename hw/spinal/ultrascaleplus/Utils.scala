package ultrascaleplus.utils


import spinal.lib.bus.misc.SizeMapping


/**
 * Trait for all elments interfacing the PS and PL sides.
 */
trait PSPLInterface {
  
  /**
   * Generates the TCL script specific to the element.
   */
  def getTCL(moduleName: String, clock: String): String = {
    return ""
  }

  /**
   * Genrate and attach attribute to the interface
   */
  def setAttribute(): Unit = {}

}


class Aperture(val name: String, base: BigInt, size: BigInt) extends SizeMapping(base, size) {}
