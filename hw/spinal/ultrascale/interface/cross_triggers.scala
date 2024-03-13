package interface.debug

import spinal.core._
import spinal.lib._


case class CrossTrigger() extends Bundle with IMasterSlave {
  
  val pl_ps_trigger =  in(Bool())
  val ps_pl_trigger = out(Bool())

  override def asMaster(): Unit = {
    out(pl_ps_trigger)
    in(ps_pl_trigger)
  }

  def <<(that: CrossTrigger): Unit = that >> this

  def >>(that: CrossTrigger): Unit = {
    that.pl_ps_trigger := this.pl_ps_trigger
    this.ps_pl_trigger := that.ps_pl_trigger
  }

}
