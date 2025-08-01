package ultrascaleplus.clock


import spinal.core._
import spinal.lib._


object PLClockingArea {
  
  def apply(clockdomain: ClockResetMapped): PLClockingArea = new PLClockingArea(clockdomain)

}

/** Alias version of ClockingArea. The sole purpose is to offer another way to 
 *  call the rework method via a nicer name.
 */
class PLClockingArea(clockdomain: ClockResetMapped) extends ClockingArea(clockdomain.domain) {}
