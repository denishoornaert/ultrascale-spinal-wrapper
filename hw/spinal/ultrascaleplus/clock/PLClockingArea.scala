package ultrascaleplus.clock


import spinal.core._
import spinal.lib._


/** Alias version of ClockingArea. The sole purpose is to offer another way to 
 *  call the rework method via a nicer name.
 */
class PLClockingArea(clockdomain: ClockDomain) extends ClockingArea(clockdomain) {

  def this(clockdomain: ClockMapped) = {
    this(clockdomain.domain)
  }

  def this(clockdomain: ClockResetMapped) = {
    this(clockdomain.domain)
  }

  def this(clockdomain: TraceClockMapped) = {
    this(clockdomain.domain.get)
  }

}
