package bus.amba4.ace

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

case class AceConfig(
    addressWidth : Int,
    dataWidth    : Int,
    axi          : Axi4Config,
    useProt      : Boolean = true
) {
}

case class Ace(config: AceConfig) extends Bundle with IMasterSlave {
  
  val ac  = master(Stream(AceAC(config)))
  val ar  =  slave(Stream(AceAR(config)))
  val cd  =  slave(Stream(AceCD(config)))

  val axi = slave(Axi4(config.axi))

  override def asMaster(): Unit = {
    master(ac, axi)
    slave(ar, cd)
  }

  def <<(that: Ace): Unit = that >> this

  def >>(that: Ace): Unit = {
    this.ac  drive that.ac
    that.ar  drive this.ar
    that.cd  drive this.cd
    this.axi drive that.axi
  }

  def <<(that: Axi4): Unit = that >> this

  def >>(that: Axi4): Unit = {
    this.ac.setBlocked()
    this.ar.setIdle()
    this.cd.setIdle()
    this.axi >> that.axi
  }
}
