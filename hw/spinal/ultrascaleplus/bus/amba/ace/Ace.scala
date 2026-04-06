package ultrascaleplus.bus.amba.ace

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


class AceConfig(
  val addressWidth : Int,
  val dataWidth    : Int,
  val idWidth      : Int = -1,
  val useId        : Boolean = true,
  val useRegion    : Boolean = true,
  val useBurst     : Boolean = true,
  val useLock      : Boolean = true,
  val useCache     : Boolean = true,
  val useSize      : Boolean = true,
  val useQos       : Boolean = true,
  val useLen       : Boolean = true,
  val useLast      : Boolean = true,
  val useResp      : Boolean = true,
  val useProt      : Boolean = true,
  val useStrb      : Boolean = true,
  val useAllStrb   : Boolean = false,
  val forceAxi4Len : Boolean = false,
  val useDomain    : Boolean = true,
  val useSnoop     : Boolean = true,
  val useUnique    : Boolean = false,
  val useBar       : Boolean = true,
  val useAck       : Boolean = true,
  val arUserWidth  : Int = -1,
  val awUserWidth  : Int = -1,
  val rUserWidth   : Int = -1,
  val wUserWidth   : Int = -1,
  val bUserWidth   : Int = -1,
  val readIssuingCapability     : Int = -1,
  val writeIssuingCapability    : Int = -1,
  val combinedIssuingCapability : Int = -1,
  val readDataReorderingDepth   : Int = -1
) {
  
  def useArUser = arUserWidth >= 0
  def useAwUser = awUserWidth >= 0
  def useRUser = rUserWidth >= 0
  def useWUser = wUserWidth >= 0
  def useBUser = bUserWidth >= 0
  def useArwUser = arwUserWidth >= 0 //Shared AR/AW channel
  def arwUserWidth = Math.max(arUserWidth, awUserWidth)
  def sizeWidth = 3
  def lenWidth = 8
  def lockWidth = 1

  if(useId) {
    require(idWidth >= 0,"You need to set idWidth")
  }

  require(combinedIssuingCapability >= scala.math.max(readIssuingCapability, writeIssuingCapability), "Inconsistent combined issuing capability")
  require(readDataReorderingDepth <= readIssuingCapability, "Inconsistent read data reordering depth")

  require(List(8, 16, 32, 64, 128, 256, 512, 1024) contains dataWidth, "Valid data width: 8, 16, 32, 64, 128, 256, 512 or 1024 bit")

  def addressType = UInt(addressWidth bits)
  def dataType    = Bits(dataWidth bits)
  def idType      = UInt(idWidth bits)
  def lenType     = UInt(8 bits)
  def bytePerWord = dataWidth/8
  def symbolRange = log2Up(bytePerWord)-1 downto 0
  def wordRange   = addressWidth-1 downto log2Up(bytePerWord)

}

object AceConfig {

  def apply(
    addressWidth : Int,
    dataWidth    : Int,
    idWidth      : Int = -1,
    useId        : Boolean = true,
    useRegion    : Boolean = true,
    useBurst     : Boolean = true,
    useLock      : Boolean = true,
    useCache     : Boolean = true,
    useSize      : Boolean = true,
    useQos       : Boolean = true,
    useLen       : Boolean = true,
    useLast      : Boolean = true,
    useResp      : Boolean = true,
    useProt      : Boolean = true,
    useStrb      : Boolean = true,
    useAllStrb   : Boolean = false,
    forceAxi4Len : Boolean = false,
    useDomain    : Boolean = true,
    useSnoop     : Boolean = true,
    useUnique    : Boolean = false,
    useBar       : Boolean = true,
    useAck       : Boolean = true,
    arUserWidth  : Int = -1,
    awUserWidth  : Int = -1,
    rUserWidth   : Int = -1,
    wUserWidth   : Int = -1,
    bUserWidth   : Int = -1,
    readIssuingCapability     : Int = -1,
    writeIssuingCapability    : Int = -1,
    combinedIssuingCapability : Int = -1,
    readDataReorderingDepth   : Int = -1
  ) = new AceConfig(
    addressWidth =  addressWidth,
    dataWidth    = dataWidth,
    idWidth      = idWidth,
    useId        = useId,
    useRegion    = useRegion,
    useBurst     = useBurst,
    useLock      = useLock,
    useCache     = useCache,
    useSize      = useSize,
    useQos       = useQos,
    useLen       = useLen,
    useLast      = useLast,
    useResp      = useResp,
    useProt      = useProt,
    useStrb      = useStrb,
    useAllStrb   = useAllStrb,
    forceAxi4Len = forceAxi4Len,
    useDomain    = useDomain,
    useSnoop     = useSnoop,
    useUnique    = useUnique,
    useBar       = useBar,
    useAck       = useAck,
    arUserWidth  = arUserWidth,
    awUserWidth  = awUserWidth,
    rUserWidth   = rUserWidth,
    wUserWidth   = wUserWidth,
    bUserWidth   = bUserWidth,
    readIssuingCapability     = readIssuingCapability,
    writeIssuingCapability    = writeIssuingCapability,
    combinedIssuingCapability = combinedIssuingCapability,
    readDataReorderingDepth   = readDataReorderingDepth
  )
}


case class Ace(config: AceConfig) extends Bundle with IMasterSlave {

  val ac = master(Stream(AceAc(config)))
  val cr =  slave(Stream(AceCr(config)))
  val cd =  slave(Stream(AceCd(config)))
  val ar =  slave(Stream(AceAr(config)))
  val r  = master(Stream(AceR(config)))
  val aw =  slave(Stream(AceAw(config)))
  val w  =  slave(Stream(AceW(config)))
  val b  = master(Stream(AceB(config)))

  override def asMaster(): Unit = {
    master(cr, cd, ar, aw, w)
    slave(ac, r, b)
  }

  def <<(that: Ace): Unit = that >> this

  def >>(that: Ace): Unit = {
    this.ac >> that.ac
    that.ar >> this.ar
    that.cd >> this.cd
    that.ar >> this.ar
    this.r  >> that.r
    that.aw >> this.aw
    that.w  >> this.w
    this.b  >> that.b
  }

  def setIdle(): Unit = {
    if (ac.isMasterInterface)
      ac.setIdle()
    if (cr.isMasterInterface)
      cr.setIdle()
    if (cd.isMasterInterface)
      cd.setIdle()
    if (ar.isMasterInterface)
      ar.setIdle()
    if (r.isMasterInterface)
      r.setIdle()
    if (aw.isMasterInterface)
      aw.setIdle()
    if (w.isMasterInterface)
      w.setIdle()
    if (b.isMasterInterface)
      b.setIdle()
  }

  def setBlocked(): Unit = {
    if (ac.isSlaveInterface)
      ac.setBlocked()
    if (cr.isSlaveInterface)
      cr.setBlocked()
    if (cd.isSlaveInterface)
      cd.setBlocked()
    if (ar.isSlaveInterface)
      ar.setBlocked()
    if (r.isSlaveInterface)
      r.setBlocked()
    if (aw.isSlaveInterface)
      aw.setBlocked()
    if (w.isSlaveInterface)
      w.setBlocked()
    if (b.isSlaveInterface)
      b.setBlocked()
  }

}
