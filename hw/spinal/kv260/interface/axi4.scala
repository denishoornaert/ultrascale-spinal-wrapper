package kv260.interface.axi

import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.amba4.axi._

import ultrascaleplus.parameters._
import ultrascaleplus.interface.axi._


object LPD {

  object HPM0 {

    val apertures = Seq(
      AddressMap.LPD_HPM0
    )

    val config = Axi4Config(
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
  }
}

object FPD {

  object HPM0 {

    val apertures = Seq(
      AddressMap.FPD_HPM0
    )
  
    // Primary port from the pov of the PS but a secondary port from the pov of the PL
    val config = Axi4Config(
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

  }

  object HPM1 {

    val apertures = Seq(
      AddressMap.FPD_HPM1
    )

    // Primary port from the pov of the PS but a secondary port from the pov of the PL
    val config = Axi4Config(
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

  }

  object HP0 {

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    val config = Axi4Config(
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

  }

  object HP1 {

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    val config = Axi4Config(
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

  }

  object HP2 {

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    val config = Axi4Config(
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

  }

  object HP3 {

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    val config = Axi4Config(
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

  }

  object HPC0 {

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    val config = Axi4Config(
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

  }

  object HPC1 {

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    val config = Axi4Config(
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

  }
}
