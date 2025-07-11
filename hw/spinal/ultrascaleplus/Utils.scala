package ultrascaleplus.utils


import Console.{RESET, YELLOW}


import spinal.lib.bus.misc.SizeMapping


/**
 * Trait for all elments interfacing the PS and PL sides.
 */
trait PSPLInterface {
  
  /**
   * Genrate and attach attribute to the interface
   */
  def setAttribute(): Unit = {}

}


/**
 * Trait for all elments that can generate TCL constructs
 */
trait TCL {

  /**
   * Generates the TCL script specific to the element.
   */
  def getTCL(moduleName: String, clock: String): String = {
    return ""
  }

}


/**
 * Trait for all elments that can generate XDC constructs
 */
trait XDC {

  /**
   * Generates the XDC script specific to the element.
   */
  def getXDC(): String = {
    return ""
  }

}


class Aperture(val name: String, base: BigInt, size: BigInt) extends SizeMapping(base, size) {}


object Log {

  def info(message: String): Unit = {
    println(f"${RESET}${YELLOW}[UltraScale+] ${message}${RESET}")
  }

}
