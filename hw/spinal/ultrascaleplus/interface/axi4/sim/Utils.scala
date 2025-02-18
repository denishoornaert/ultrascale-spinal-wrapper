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


class AxiJob(ax: Axi4Ax, _addr: BigInt, _id: Int, _len: Int, _size: Int) {

  // Keep track of the job age
  var age: Int = 0

  // Typical AXI info
  val actual_addr = _addr
  val addr   = (_addr >> log2Up(ax.config.bytePerWord)) << log2Up(ax.config.bytePerWord)
  val id     = _id
  val len    = _len
  val size   = _size
  val burst  = 1 // INCR
  val cache  = 8
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
      assertion = (((len+1) << size) + (addr%4096)) <= 4096,
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

// size is expressed in bytes and assumed to be a power of two
class Axi4WJob(ax: Axi4Ax, _addr: BigInt, _id: Int, _len: Int, _data: Seq[BigInt], _size: Int = 4) extends AxiJob(ax, _addr, _id, _len, _size) {
  val data = _data
}

class Axi4RJob(ax: Axi4Ax, addr: BigInt, id: Int, len: Int, size: Int = 4) extends AxiJob(ax, addr, id, len, size) {
}
