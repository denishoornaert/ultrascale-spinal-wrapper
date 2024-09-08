package ultrascaleplus.interface.axi.sim


import spinal.core._
import spinal.lib.bus.amba4.axi._


class Axi4Resp(_id: Int) {

  // Timestamps
  val start: Int         = 0
  val stop : Int         = 0

  val id   : Int         = _id
  val data : Seq[BigInt] = null
  val resp : Int         = 0

  def isOKAY(): Boolean = {
    resp == 0
  }

}


class AxiJob(ax: Axi4Ax, _addr: BigInt, _id: Int, _len: Int) {

  // Keep track of the job age
  var age: Int = 0

  // Typical AXI info
  val addr   = _addr
  val id     = _id
  val len    = _len
  val size   = log2Up(16) // TODO: make modular from axi.config.dataWidth/8
  val burst  = 1 // INCR
  val cache  = 0
  val lock   = 0
  val prot   = 0
  val qos    = 0
  val region = 0

  val dataTransactionSize : Int  = (1 + len) << size
  val lowerWrapBoundary   : BigInt = (addr / dataTransactionSize) * dataTransactionSize 
  val upperWrapBoundary   : BigInt = lowerWrapBoundary + dataTransactionSize
 
  def older(): Unit = {
    age += 1
  }

  def incrAddress(i : Int): BigInt = {
    return ((addr >> size) + i) << size
  }
  
  def wrapAddress(i : Int): BigInt = {
    var ret = incrAddress(i)
    if (ret >= upperWrapBoundary) {
      ret = ret-dataTransactionSize
    }
    return ret
  }

  // check for read/write over 4k boundary
  if (burst == 1) {
    assert(
      assertion = (((len+1) << size) + (addr%4096)) < 4096,
      message   = s"Read/write request crossing 4KB boundary (addr=${addr.toString(16)}, len=${len}, size=${size})"
    )
  }

  def burstAddress(i : Int): BigInt = {
    val ret = burst match {
      case 0 => addr           // FIXED
      case 1 => incrAddress(i) // INCR
      case 2 => wrapAddress(i) // WRAP
    }
    return ret
  }

  def alignedBurstAddress(i : Int, maxBurstSize : Int): BigInt = {
    return (burstAddress(i) >> maxBurstSize) << maxBurstSize
  }

}


class Axi4WJob(ax: Axi4Ax, _addr: BigInt, _id: Int, _len: Int, _data: Seq[BigInt]) extends AxiJob(ax, _addr, _id, _len) {
  val data = _data
}

class Axi4RJob(ax: Axi4Ax, addr: BigInt, id: Int, len: Int) extends AxiJob(ax, addr, id, len) {
}
