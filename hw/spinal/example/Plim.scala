package example.plim

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


trait Plim {

  def connectARValid(primary: Bool, secondary: Bool): Unit = {
    secondary := primary
  }

  def connectARReady(primary: Bool, secondary: Bool): Unit = {
    primary := secondary
  }

  def connectARAddr(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectARId(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectARLen(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectARSize(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectARBurst(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectARLock(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectARCache(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectARProt(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectARQos(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectARRegion(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAR(primary: Axi4Ar, secondary: Axi4Ar): Unit = {
    connectARAddr(primary.addr, secondary.addr)
    connectARId(primary.id, secondary.id)
    connectARLen(primary.len, secondary.len)
    connectARSize(primary.size, secondary.size)
    connectARBurst(primary.burst, secondary.burst)
    connectARLock(primary.lock, secondary.lock)
    connectARCache(primary.cache, secondary.cache)
    connectARProt(primary.prot, secondary.prot)
    connectARQos(primary.qos, secondary.qos)
    connectARRegion(primary.region, secondary.region)
  }

  def connectRValid(primary: Bool, secondary: Bool): Unit = {
    primary := secondary
  }

  def connectRReady(primary: Bool, secondary: Bool): Unit = {
    secondary := primary
  }
  
  def connectRData(primary: Bits, secondary: Bits): Unit = {
    primary := secondary
  }

  def connectRId(primary: UInt, secondary: UInt): Unit = {
    primary := secondary
  }

  def connectRResp(primary: Bits, secondary: Bits): Unit = {
    primary := secondary
  }

  def connectRLast(primary: Bool, secondary: Bool): Unit = {
    primary := secondary
  }

  def connectRUser(primary: Bits, secondary: Bits): Unit = {
    primary := secondary
  }

  def connectR(primary: Axi4R, secondary: Axi4R): Unit = {
    connectRData(primary.data, secondary.data)
    connectRId(primary.id, secondary.id)
    connectRResp(primary.resp, secondary.resp)
    connectRLast(primary.last, secondary.last)
//    connectRUser(primary.user, secondary.user)
  }

  def connectAWValid(primary: Bool, secondary: Bool): Unit = {
    secondary := primary
  }

  def connectAWReady(primary: Bool, secondary: Bool): Unit = {
    primary := secondary
  }

  def connectAWAddr(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectAWId(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectAWLen(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectAWSize(primary: UInt, secondary: UInt): Unit = {
    secondary := primary
  }

  def connectAWBurst(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAWLock(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAWCache(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAWProt(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAWQos(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAWRegion(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAW(primary: Axi4Aw, secondary: Axi4Aw): Unit = {
    connectAWAddr(primary.addr, secondary.addr)
    connectAWId(primary.id, secondary.id)
    connectAWLen(primary.len, secondary.len)
    connectAWSize(primary.size, secondary.size)
    connectAWBurst(primary.burst, secondary.burst)
    connectAWLock(primary.lock, secondary.lock)
    connectAWCache(primary.cache, secondary.cache)
    connectAWProt(primary.prot, secondary.prot)
    connectAWQos(primary.qos, secondary.qos)
    connectAWRegion(primary.region, secondary.region)
  }
 
  def connectWValid(primary: Bool, secondary: Bool): Unit = {
    secondary := primary
  }

  def connectWReady(primary: Bool, secondary: Bool): Unit = {
    primary := secondary
  }

  def connectWData(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectWStrb(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectWUser(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectWLast(primary: Bool, secondary: Bool): Unit = {
    secondary := primary
  }

  def connectW(primary: Axi4W, secondary: Axi4W): Unit = {
    connectWData(primary.data, secondary.data)
    connectWStrb(primary.strb, secondary.strb)
//    connectWUser(primary.user, secondary.user)
    connectWLast(primary.last, secondary.last)
  }

  def connectBValid(primary: Bool, secondary: Bool): Unit = {
    primary := secondary
  }

  def connectBReady(primary: Bool, secondary: Bool): Unit = {
    secondary := primary
  }

  def connectBId(primary: UInt, secondary: UInt): Unit = {
    primary := secondary
  }

  def connectBResp(primary: Bits, secondary: Bits): Unit = {
    primary := secondary
  }

  def connectBUser(primary: Bits, secondary: Bits): Unit = {
    primary := secondary
  }

  def connectB(primary: Axi4B, secondary: Axi4B): Unit = {
    connectBId(primary.id, secondary.id)
    connectBResp(primary.resp, secondary.resp)
  }

  def connect(primary: Axi4, secondary: Axi4): Unit = {
    connectARValid(primary.ar.valid, secondary.ar.valid)
    connectARReady(primary.ar.ready, secondary.ar.ready)
    connectAR(primary.ar.payload, secondary.ar.payload)
    
    connectRValid(primary.r.valid, secondary.r.valid)
    connectRReady(primary.r.ready, secondary.r.ready)
    connectR(primary.r.payload, secondary.r.payload)
    
    connectAWValid(primary.aw.valid, secondary.aw.valid)
    connectAWReady(primary.aw.ready, secondary.aw.ready)
    connectAW(primary.aw.payload, secondary.aw.payload)
    
    connectWValid(primary.w.valid, secondary.w.valid)
    connectWReady(primary.w.ready, secondary.w.ready)
    connectW(primary.w.payload, secondary.w.payload)
    
    connectBValid(primary.b.valid, secondary.b.valid)
    connectBReady(primary.b.ready, secondary.b.ready)
    connectB(primary.b.payload, secondary.b.payload)
  }

}
