package ultrascaleplus.interface.axi.sim


import scala.math.pow

import spinal.core._
import spinal.core.sim._
import spinal.sim._
import spinal.lib.bus.amba4.axi._
import scala.collection.mutable
import scala.collection.mutable.Map


class Axi4CheckerPrimary(axi: Axi4, clockDomain: ClockDomain) {
 
  axi.ar.valid  #= false
  axi.ar.addr   #= 0
  axi.ar.id     #= 0
  axi.ar.burst  #= 0
  axi.ar.len    #= 0
  axi.ar.size   #= 0
  axi.ar.cache  #= 0
  axi.ar.lock   #= 0
  axi.ar.prot   #= 0
  axi.ar.qos    #= 0
  axi.ar.region #= 0
  axi.r.ready   #= false
  axi.aw.valid  #= false
  axi.aw.addr   #= 0
  axi.aw.id     #= 0
  axi.aw.burst  #= 0
  axi.aw.len    #= 0
  axi.aw.size   #= 0
  axi.aw.cache  #= 0
  axi.aw.lock   #= 0
  axi.aw.prot   #= 0
  axi.aw.qos    #= 0
  axi.aw.region #= 0
  axi.w.valid   #= false
  axi.w.data    #= 0
  axi.w.strb    #= 0
  axi.w.last    #= false

  // Maintaining clock count
  var clock_count: BigInt = 0

  // AR
  val ARQueue = mutable.Queue[Axi4RJob]()
  // R
  val RQueue = mutable.Queue[Int]()
  val RMonitor: Map[Int, Int] = Map()
  // AW
  val AWQueue = new mutable.Queue[Axi4WJob]()
  // W
  val WQueue = new mutable.Queue[Axi4WJob]()
  // B
  val BMonitor: Map[Int, Int] = Map()

  var wBeatCount = 0
  
  def genReadCmd(addr: BigInt, id: Int, len: Int): Unit = {
    ARQueue += new Axi4RJob(axi.ar, addr, id, len-1)
  }
  
  def genWriteCmd(addr: BigInt, id: Int, len: Int, data: Seq[BigInt]): Unit = {
    AWQueue += new Axi4WJob(axi.aw, addr, id, data.length-1, data)
  }

  def updateAR(): Unit = {
    if (ARQueue.nonEmpty) {
      val context = ARQueue.front
      axi.ar.valid  #= true
      axi.ar.addr   #= context.addr
      axi.ar.id     #= context.id
      axi.ar.burst  #= context.burst
      axi.ar.len    #= context.len
      axi.ar.size   #= context.size
      axi.ar.cache  #= context.cache
      axi.ar.lock   #= context.lock
      axi.ar.prot   #= context.prot
      axi.ar.qos    #= context.qos
      axi.ar.region #= context.region
    }
    else {
      axi.ar.valid  #= false
      axi.ar.addr   #= 0
      axi.ar.id     #= 0
      axi.ar.burst  #= 0
      axi.ar.len    #= 0
      axi.ar.size   #= 0
      axi.ar.cache  #= 0
      axi.ar.lock   #= 0
      axi.ar.prot   #= 0
      axi.ar.qos    #= 0
      axi.ar.region #= 0
    }
  }

  def updateR(): Unit = {
    axi.r.ready #= (if (axi.r.valid.toBoolean & axi.r.ready.toBoolean & axi.r.last.toBoolean) false else true)
  }
  
  def updateAW(): Unit = {
    if (AWQueue.nonEmpty) {
      val context = AWQueue.front
      axi.aw.valid  #= true
      axi.aw.addr   #= context.addr
      axi.aw.id     #= context.id
      axi.aw.burst  #= context.burst
      axi.aw.len    #= context.len
      axi.aw.size   #= context.size
      axi.aw.cache  #= context.cache
      axi.aw.lock   #= context.lock
      axi.aw.prot   #= context.prot
      axi.aw.qos    #= context.qos
      axi.aw.region #= context.region
    }
    else {
      axi.aw.valid  #= false
      axi.aw.addr   #= 0
      axi.aw.id     #= 0
      axi.aw.burst  #= 0
      axi.aw.len    #= 0
      axi.aw.size   #= 0
      axi.aw.cache  #= 0
      axi.aw.lock   #= 0
      axi.aw.prot   #= 0
      axi.aw.qos    #= 0
      axi.aw.region #= 0
    }
  }

  def updateW(): Unit = {
    if (WQueue.nonEmpty) {
      val context = WQueue.front
      axi.w.valid #= true
      axi.w.data  #= context.data(wBeatCount)
      axi.w.strb  #= pow(2, (axi.config.dataWidth/8)).toInt-1
      if (wBeatCount == context.len) {
        axi.w.last #= true
        wBeatCount = 0
      }
      else {
        axi.w.last #= false
        wBeatCount += 1
      }
    }
    else {
      axi.w.valid #= false
      axi.w.data  #= 0
      axi.w.strb  #= 0
      axi.w.last  #= false
    }
  }

  def updateB(): Unit = {
    axi.b.ready #= true
  }
  
  def onARHandshake(): Unit = {
    ARQueue.dequeue
    RQueue += axi.ar.len.toInt
    if (!(RMonitor contains axi.ar.id.toInt)) {
      RMonitor += (axi.ar.id.toInt -> 0)
    }
    RMonitor(axi.ar.id.toInt) += 1
  }

  def onRHandshake(): Unit = {
    //println(f"Value read: ${axi.r.data.toBigInt}")
    if (axi.r.last.toBoolean) {
      assert(
        assertion = RMonitor(axi.r.id.toInt) > 0,
        message   = f"No read response expcted for ${axi.r.id.toInt}"
      )
      RMonitor(axi.r.id.toInt) -= 1
    }
  }
  
  def onAWHandshake(): Unit = {
    WQueue += AWQueue.dequeue
    //WQueue += axi.aw.len.toInt
    if (!(BMonitor contains axi.aw.id.toInt)) {
      BMonitor += (axi.aw.id.toInt -> 0)
    }
    BMonitor(axi.aw.id.toInt) += 1
  }

  def onWHandshake(): Unit = {
    if (axi.w.last.toBoolean)
      WQueue.dequeue()
  }

  def onBHandshake(): Unit = {
    //println(BMonitor, axi.b.id.toInt)
    assert(
      assertion = BMonitor(axi.b.id.toInt) > 0,
      message   = f"No write response expected for ${axi.b.id.toInt}"
    )
    BMonitor(axi.b.id.toInt) -= 1
  }

  def check(): Unit = {
    // Maintain 
    clock_count += clock_count
    // Check channels
    if (ARQueue.nonEmpty & axi.ar.valid.toBoolean & axi.ar.ready.toBoolean)
      onARHandshake()
    updateAR()
    if (axi.r.valid.toBoolean & axi.r.ready.toBoolean)
      onRHandshake()
    updateR()
    if (AWQueue.nonEmpty & axi.aw.valid.toBoolean & axi.aw.ready.toBoolean)
      onAWHandshake()
    updateAW()
    if (WQueue.nonEmpty & axi.w.valid.toBoolean & axi.w.ready.toBoolean)
      onWHandshake()
    updateW()
    if (axi.b.valid.toBoolean & axi.b.ready.toBoolean)
      onBHandshake()
    updateB()
  }

}
