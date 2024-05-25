package kv260.interface.axi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.amba4.axi._

import ultrascaleplus.parameters._
import ultrascaleplus.interface.axi._


object LPD_HPM0 extends AbstractPrimaryAxi4() {

  override val aperture = AddressMap.LPD_HPM0

  override val port = master(
    Axi4(
      Axi4Config(
        addressWidth              =   40,
        dataWidth                 =  128,
        idWidth                   =   16,
        useId                     = true,
        useRegion                 = true,
        useBurst                  = true,
        useLock                   = true,
        useCache                  = true,
        useSize                   = true,
        useQos                    = true,
        useLen                    = true,
        useLast                   = true,
        useResp                   = true,
        useProt                   = true,
        useStrb                   = true,
        useAllStrb                = false,
        arUserWidth               = -1,
        awUserWidth               = -1,
        rUserWidth                = -1,
        wUserWidth                = -1,
        bUserWidth                = -1,
        readIssuingCapability     = -1,
        writeIssuingCapability    = -1,
        combinedIssuingCapability = -1,
        readDataReorderingDepth   = -1
      )
    )
  )

  this.port.setPartialName("lpd_hpm0")
  this.setInterfaceAttributes(this.port)
}

object FPD_HPM0 extends AbstractPrimaryAxi4() {

  override val aperture = AddressMap.FPD_HPM0

  override val port = master(
    Axi4(
      Axi4Config(
        addressWidth              =   40,
        dataWidth                 =  128,
        idWidth                   =   16,
        useId                     = true,
        useRegion                 = true,
        useBurst                  = true,
        useLock                   = true,
        useCache                  = true,
        useSize                   = true,
        useQos                    = true,
        useLen                    = true,
        useLast                   = true,
        useResp                   = true,
        useProt                   = true,
        useStrb                   = true,
        useAllStrb                = false,
        arUserWidth               = -1,
        awUserWidth               = -1,
        rUserWidth                = -1,
        wUserWidth                = -1,
        bUserWidth                = -1,
        readIssuingCapability     = -1,
        writeIssuingCapability    = -1,
        combinedIssuingCapability = -1,
        readDataReorderingDepth   = -1
      )
    )
  )

  this.port.setPartialName("fpd_hpm0")
  this.setInterfaceAttributes(this.port)
}

object FPD_HPM1 extends AbstractPrimaryAxi4() {

  override val aperture = AddressMap.FPD_HPM1

  override val port = master(
    Axi4(
      Axi4Config(
        addressWidth              =   40,
        dataWidth                 =  128,
        idWidth                   =   16,
        useId                     = true,
        useRegion                 = true,
        useBurst                  = true,
        useLock                   = true,
        useCache                  = true,
        useSize                   = true,
        useQos                    = true,
        useLen                    = true,
        useLast                   = true,
        useResp                   = true,
        useProt                   = true,
        useStrb                   = true,
        useAllStrb                = false,
        arUserWidth               = -1,
        awUserWidth               = -1,
        rUserWidth                = -1,
        wUserWidth                = -1,
        bUserWidth                = -1,
        readIssuingCapability     = -1,
        writeIssuingCapability    = -1,
        combinedIssuingCapability = -1,
        readDataReorderingDepth   = -1
      )
    )
  )

  this.port.setPartialName("fpd_hpm1")
  this.setInterfaceAttributes(this.port)
}

object FPD_HP0 extends AbstractSecondaryAxi4() {

  override val port = slave(
    Axi4(
      Axi4Config(
        addressWidth              =   40,
        dataWidth                 =  128,
        idWidth                   =   16,
        useId                     = true,
        useRegion                 = true,
        useBurst                  = true,
        useLock                   = true,
        useCache                  = true,
        useSize                   = true,
        useQos                    = true,
        useLen                    = true,
        useLast                   = true,
        useResp                   = true,
        useProt                   = true,
        useStrb                   = true,
        useAllStrb                = false,
        arUserWidth               = -1,
        awUserWidth               = -1,
        rUserWidth                = -1,
        wUserWidth                = -1,
        bUserWidth                = -1,
        readIssuingCapability     = -1,
        writeIssuingCapability    = -1,
        combinedIssuingCapability = -1,
        readDataReorderingDepth   = -1
      )
    )
  )

  this.port.setPartialName("fpd_hp0")
  this.setInterfaceAttributes(this.port)
}

object FPD_HP1 extends AbstractSecondaryAxi4() {

  override val port = slave(
    Axi4(
      Axi4Config(
        addressWidth              =   40,
        dataWidth                 =  128,
        idWidth                   =   16,
        useId                     = true,
        useRegion                 = true,
        useBurst                  = true,
        useLock                   = true,
        useCache                  = true,
        useSize                   = true,
        useQos                    = true,
        useLen                    = true,
        useLast                   = true,
        useResp                   = true,
        useProt                   = true,
        useStrb                   = true,
        useAllStrb                = false,
        arUserWidth               = -1,
        awUserWidth               = -1,
        rUserWidth                = -1,
        wUserWidth                = -1,
        bUserWidth                = -1,
        readIssuingCapability     = -1,
        writeIssuingCapability    = -1,
        combinedIssuingCapability = -1,
        readDataReorderingDepth   = -1
      )
    )
  )

  this.port.setPartialName("fpd_hp1")
  this.setInterfaceAttributes(this.port)
}

object FPD_HP2 extends AbstractSecondaryAxi4() {

  override val port = slave(
    Axi4(
      Axi4Config(
        addressWidth              =   40,
        dataWidth                 =  128,
        idWidth                   =   16,
        useId                     = true,
        useRegion                 = true,
        useBurst                  = true,
        useLock                   = true,
        useCache                  = true,
        useSize                   = true,
        useQos                    = true,
        useLen                    = true,
        useLast                   = true,
        useResp                   = true,
        useProt                   = true,
        useStrb                   = true,
        useAllStrb                = false,
        arUserWidth               = -1,
        awUserWidth               = -1,
        rUserWidth                = -1,
        wUserWidth                = -1,
        bUserWidth                = -1,
        readIssuingCapability     = -1,
        writeIssuingCapability    = -1,
        combinedIssuingCapability = -1,
        readDataReorderingDepth   = -1
      )
    )
  )

  this.port.setPartialName("fpd_hp2")
  this.setInterfaceAttributes(this.port)
}

object FPD_HP3 extends AbstractSecondaryAxi4() {

  override val port = slave(
    Axi4(
      Axi4Config(
        addressWidth              =   40,
        dataWidth                 =  128,
        idWidth                   =   16,
        useId                     = true,
        useRegion                 = true,
        useBurst                  = true,
        useLock                   = true,
        useCache                  = true,
        useSize                   = true,
        useQos                    = true,
        useLen                    = true,
        useLast                   = true,
        useResp                   = true,
        useProt                   = true,
        useStrb                   = true,
        useAllStrb                = false,
        arUserWidth               = -1,
        awUserWidth               = -1,
        rUserWidth                = -1,
        wUserWidth                = -1,
        bUserWidth                = -1,
        readIssuingCapability     = -1,
        writeIssuingCapability    = -1,
        combinedIssuingCapability = -1,
        readDataReorderingDepth   = -1
      )
    )
  )

  this.port.setPartialName("fpd_hp3")
  this.setInterfaceAttributes(this.port)
}
