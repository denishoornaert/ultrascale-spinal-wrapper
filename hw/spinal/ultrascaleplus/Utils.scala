package ultrascaleplus.utils


import Console.{RESET, YELLOW}


import spinal.core._
import spinal.lib._
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
  def getTCL(): String = {
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

object Util {

  /** Looks for the topmodule of a given module.
   *  
   *  @parameter component The child component we want to look-up the root.
   *  @return topmodule The parent/topmodule/root component of the component given in parameter.
   */
  def topmodule(component: Component): Component = {
    var module = component
    while (module.parent != null) {
      module = module.parent
    }
    return module
  }

  /** Looks for the topmodule of a given module.
   *  
   *  @parameter interface The interface of a child component we want to look-up the root.
   *  @return topmodule The parent/topmodule/root component of the component given in parameter.
   */
  def topmodule(interface: Data): Component = {
    return topmodule(interface.component)
  }

}
