package kv260.interface.axi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.amba4.axi._

import ultrascaleplus.parameters._
import ultrascaleplus.interface.axi._


object LPD_HPM0 extends AbstractSecondaryAxi4() {

  override val aperture = AddressMap.LPD_HPM0

  override val config = Axi4Config(
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
    readIssuingCapability     =  8,
    writeIssuingCapability    =  8,
    combinedIssuingCapability = 16,
    readDataReorderingDepth   = -1
  )

  override val name = "lpd_hpm0"

}

object FPD_HPM0 extends AbstractSecondaryAxi4() {

  override val aperture = AddressMap.FPD_HPM0

  // Primary port from the pov of the PS but a secondary port from the pov of the PL
  override val config = Axi4Config(
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
    readIssuingCapability     =  8,
    writeIssuingCapability    =  8,
    combinedIssuingCapability = 16,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hpm0"

}

object FPD_HPM1 extends AbstractSecondaryAxi4() {

  override val aperture = AddressMap.FPD_HPM1

  // Primary port from the pov of the PS but a secondary port from the pov of the PL
  override val config = Axi4Config(
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
    readIssuingCapability     =  8,
    writeIssuingCapability    =  8,
    combinedIssuingCapability = 16,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hpm1"

}

object FPD_HP0 extends AbstractPrimaryAxi4() {

  // Secondary port from the pov of the PS but a primary port from the pov of the PL
  override val config = Axi4Config(
    addressWidth              =   40,
    dataWidth                 =  128,
    idWidth                   =    6,
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
    readIssuingCapability     = 16,
    writeIssuingCapability    = 16,
    combinedIssuingCapability = 32,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hp0"

}

object FPD_HP1 extends AbstractPrimaryAxi4() {

  // Secondary port from the pov of the PS but a primary port from the pov of the PL
  override val config = Axi4Config(
    addressWidth              =   40,
    dataWidth                 =  128,
    idWidth                   =    6,
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
    readIssuingCapability     = 16,
    writeIssuingCapability    = 16,
    combinedIssuingCapability = 32,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hp1"

}

object FPD_HP2 extends AbstractPrimaryAxi4() {

  // Secondary port from the pov of the PS but a primary port from the pov of the PL
  override val config = Axi4Config(
    addressWidth              =   40,
    dataWidth                 =  128,
    idWidth                   =    6,
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
    readIssuingCapability     = 16,
    writeIssuingCapability    = 16,
    combinedIssuingCapability = 32,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hp2"

}

object FPD_HP3 extends AbstractPrimaryAxi4() {

  // Secondary port from the pov of the PS but a primary port from the pov of the PL
  override val config = Axi4Config(
    addressWidth              =   40,
    dataWidth                 =  128,
    idWidth                   =    6,
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
    readIssuingCapability     = 16,
    writeIssuingCapability    = 16,
    combinedIssuingCapability = 32,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hp3"

}

object FPD_HPC0 extends AbstractPrimaryAxi4() {

  // Secondary port from the pov of the PS but a primary port from the pov of the PL
  override val config = Axi4Config(
    addressWidth              =   49,
    dataWidth                 =  128,
    idWidth                   =    6,
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
    readIssuingCapability     = 16,
    writeIssuingCapability    = 16,
    combinedIssuingCapability = 32,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hpc0"

}

object FPD_HPC1 extends AbstractPrimaryAxi4() {

  // Secondary port from the pov of the PS but a primary port from the pov of the PL
  override val config = Axi4Config(
    addressWidth              =   49,
    dataWidth                 =  128,
    idWidth                   =    6,
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
    readIssuingCapability     = 16,
    writeIssuingCapability    = 16,
    combinedIssuingCapability = 32,
    readDataReorderingDepth   = -1
  )

  override val name = "fpd_hpc1"

}
