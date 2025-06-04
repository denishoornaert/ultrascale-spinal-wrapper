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

  def connectARUser(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAR(primary: Axi4Ar, secondary: Axi4Ar): Unit = {
    connectARAddr(primary.addr, secondary.addr)
    if (primary.config.useId && secondary.config.useId)
      connectARId(primary.id, secondary.id)
    if (primary.config.useLen && secondary.config.useLen)
      connectARLen(primary.len, secondary.len)
    if (primary.config.useSize && secondary.config.useSize)
      connectARSize(primary.size, secondary.size)
    if (primary.config.useBurst && secondary.config.useBurst)
      connectARBurst(primary.burst, secondary.burst)
    if (primary.config.useLock && secondary.config.useLock)
      connectARLock(primary.lock, secondary.lock)
    if (primary.config.useCache && secondary.config.useCache)
      connectARCache(primary.cache, secondary.cache)
    if (primary.config.useProt && secondary.config.useProt)
      connectARProt(primary.prot, secondary.prot)
    if (primary.config.useQos && secondary.config.useQos)
      connectARQos(primary.qos, secondary.qos)
    if (primary.config.useRegion && secondary.config.useRegion)
      connectARRegion(primary.region, secondary.region)
    if (primary.config.arUserWidth > 0 && secondary.config.arUserWidth > 0)
      connectARUser(primary.user, secondary.user)
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
    if (primary.config.useId && secondary.config.useId)
      connectRId(primary.id, secondary.id)
    if (primary.config.useResp && secondary.config.useResp)
      connectRResp(primary.resp, secondary.resp)
    if (primary.config.useLast && secondary.config.useLast)
      connectRLast(primary.last, secondary.last)
    if (primary.config.rUserWidth > 0 && secondary.config.rUserWidth > 0)
      connectRUser(primary.user, secondary.user)
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

  def connectAWUser(primary: Bits, secondary: Bits): Unit = {
    secondary := primary
  }

  def connectAW(primary: Axi4Aw, secondary: Axi4Aw): Unit = {
    connectAWAddr(primary.addr, secondary.addr)
    if (primary.config.useId && secondary.config.useId)
      connectAWId(primary.id, secondary.id)
    if (primary.config.useLen && secondary.config.useLen)
      connectAWLen(primary.len, secondary.len)
    if (primary.config.useSize && secondary.config.useSize)
      connectAWSize(primary.size, secondary.size)
    if (primary.config.useBurst && secondary.config.useBurst)
      connectAWBurst(primary.burst, secondary.burst)
    if (primary.config.useLock && secondary.config.useLock)
      connectAWLock(primary.lock, secondary.lock)
    if (primary.config.useCache && secondary.config.useCache)
      connectAWCache(primary.cache, secondary.cache)
    if (primary.config.useProt && secondary.config.useProt)
      connectAWProt(primary.prot, secondary.prot)
    if (primary.config.useQos && secondary.config.useQos)
      connectAWQos(primary.qos, secondary.qos)
    if (primary.config.useRegion && secondary.config.useRegion)
      connectAWRegion(primary.region, secondary.region)
    if (primary.config.awUserWidth > 0 && secondary.config.awUserWidth > 0)
      connectAWUser(primary.user, secondary.user)
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
    if (primary.config.useStrb && secondary.config.useStrb)
      connectWStrb(primary.strb, secondary.strb)
    if (primary.config.useLast && secondary.config.useLast)
      connectWLast(primary.last, secondary.last)
    if (primary.config.wUserWidth > 0 && secondary.config.wUserWidth > 0)
      connectWUser(primary.user, secondary.user)
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
    if (primary.config.useId && secondary.config.useId)
      connectBId(primary.id, secondary.id)
    if (primary.config.useResp && secondary.config.useResp)
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
