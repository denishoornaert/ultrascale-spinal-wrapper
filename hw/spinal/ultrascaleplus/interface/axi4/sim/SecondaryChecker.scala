package ultrascaleplus.interface.axi.sim


import scala.collection.mutable
import scala.collection.mutable.Map

import spinal.core._
import spinal.core.sim._
import spinal.sim._
import spinal.lib.bus.amba4.axi._


class Axi4CheckerSecondary(axi: Axi4, clockDomain: ClockDomain) {

  // Simulated memory
  val memory = SparseMemory()
  memory.loadDebugSequence(BigInt("0800000000", 16), 8*64, 1)

  // Self defined driver
  clockDomain.onRisingEdges({this.check()})

  // Define default values for signals
  // Read - Address phase
  axi.ar.ready #= false
  // Read - Data phase
  axi.r.valid  #= false
  axi.r.data   #= 0
  axi.r.id     #= 0
  axi.r.resp   #= 0
  axi.r.last   #= false
  // Write - Address phase
  axi.aw.ready #= false
  // Write - Data phase
  axi.w.ready  #= false
  // Write - response phase
  axi.b.valid  #= false
  axi.b.id     #= 0
  axi.b.resp   #= 0

  // Maintaining clock count
  var clock_count: BigInt = 0

  // AR
  val maxReads = 8
  var readCount = 0
  // R
  val RQueue = mutable.Queue[AxiJob]()
  var rBeatCount = 0
  // AW
  val maxWrites = 1
  var writeCount = 0
  // W
  val WQueue = new mutable.Queue[AxiJob]()
  var wBeatCount = 0
  // B
  val BQueue = new mutable.Queue[Int]()

  def updateAR(): Unit = {
    axi.ar.ready #= (if (axi.ar.valid.toBoolean & axi.ar.ready.toBoolean) false else (readCount < maxReads))
  }

  def updateR(): Unit = {
    if (RQueue.nonEmpty) {
      val context = RQueue.front
      axi.r.valid #= true
      axi.r.data  #= memory.readBigInt(context.alignedBurstAddress(rBeatCount, log2Up(axi.config.dataWidth/8)), axi.config.dataWidth/8)
      axi.r.id    #= context.id
      axi.r.resp  #= 1 // OKAY
      axi.r.last  #= (rBeatCount == context.len)
    }
    else {
      axi.r.valid #= false
      axi.r.data  #= 0
      axi.r.id    #= 0
      axi.r.resp  #= 0
      axi.r.last  #= false
    }
  }
  
  def updateAW(): Unit = {
    axi.aw.ready #= (if (axi.aw.valid.toBoolean & axi.aw.ready.toBoolean) false else (writeCount < maxWrites))
  }

  def updateW(): Unit = {
    axi.w.ready #= !(axi.w.valid.toBoolean & axi.w.ready.toBoolean & axi.w.last.toBoolean)
  }

  def updateB(): Unit = {
    axi.b.valid #= (if (axi.b.valid.toBoolean & axi.b.ready.toBoolean) false else BQueue.nonEmpty)
    axi.b.id    #= (if (BQueue.nonEmpty) BQueue.front else 0)
    axi.b.resp  #= 0 // OKAY // TODO: should depend on access done in onWHandshake()
  }

  def onARHandshake(): Unit = {
    readCount += 1
    RQueue += new AxiJob(axi.ar, axi.ar.payload.addr.toBigInt, axi.ar.payload.id.toInt, axi.ar.payload.len.toInt) // Ugly
  }

  def onRHandshake(): Unit = {
    if (axi.r.last.toBoolean) {
      RQueue.dequeue()
      rBeatCount = 0
      readCount -= 1
    }
    else {
      rBeatCount += 1
    }
  }
  
  def onAWHandshake(): Unit = {
    writeCount += 1
    WQueue += new AxiJob(axi.aw, axi.aw.payload.addr.toBigInt, axi.aw.payload.id.toInt, axi.aw.payload.len.toInt) // Ugly
  }

  def onWHandshake(): Unit = {
    val context = WQueue.front
    
    if (wBeatCount == context.len)
      assert(
        assertion = axi.w.last.toBoolean,
        message   = f"Expected axi.w.last to be assert at the ${wBeatCount}th beat"
      )

    // must be done before manipulating the wBeatcounter
    memory.writeArray(context.alignedBurstAddress(wBeatCount, log2Up(axi.config.dataWidth/8)), axi.w.payload.data.toBytes)
    
    if (axi.w.last.toBoolean) {
      assert(
        assertion = wBeatCount == context.len,
        message   = f"axi.w.last asserted before due: ${wBeatCount}/${context.len}"
      )
      wBeatCount = 0
      BQueue += context.id
      WQueue.dequeue()
    }
    else {
      wBeatCount += 1
    }
  }

  def onBHandshake(): Unit = {
    val context = BQueue.front
    //println("Sending ack (B)")
    BQueue.dequeue()
    writeCount -= 1
  }

  def check(): Unit = {
    // Maintain clock count
    clock_count += clock_count
    // Check channels
    if (axi.ar.valid.toBoolean & axi.ar.ready.toBoolean)
      onARHandshake()
    updateAR()
    if (RQueue.nonEmpty & axi.r.valid.toBoolean & axi.r.ready.toBoolean)
      onRHandshake()
    updateR()
    // Check order of updateX and onXHandshake
    updateAW()
    updateW()
    updateB()
    if (axi.aw.valid.toBoolean & axi.aw.ready.toBoolean)
      onAWHandshake()
    if (WQueue.nonEmpty & axi.w.valid.toBoolean & axi.w.ready.toBoolean)
      onWHandshake()
    if (BQueue.nonEmpty & axi.b.valid.toBoolean & axi.b.ready.toBoolean)
      onBHandshake()
  }

}
