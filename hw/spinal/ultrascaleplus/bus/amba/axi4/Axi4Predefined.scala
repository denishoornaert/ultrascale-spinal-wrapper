package ultrascaleplus.bus.amba.axi4


import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


import ultrascaleplus.utils.Aperture
import ultrascaleplus.parameters._


object LPD {

  object HPM0 extends Axi4MappedInstanceTemplate() {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hpm0"

    override val domain = "lpd"

    override val apertures = Seq(
      AddressMap.LPD_HPM0
    )

    override val id = -1

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
  }
}

object FPD {

  object HPM0 extends Axi4MappedInstanceTemplate() {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hpm0"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.FPD_HPM0
    )
  
    val id = -1

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

  object HPM1 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    val name = "hpm1"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.FPD_HPM1
    )

    val id = -1

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

  object HP0 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    val name = "hp0"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    val id = 2

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

  object HP1 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    val name = "hp1"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    val id = 3

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

  object HP2 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    val name = "hp2"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    val id = 4

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

  object HP3 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    val name = "hp3"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    val id = 5

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

  object HPC0 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    val name = "hpc0"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    val id = 0

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

  object HPC1 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    val name = "hpc1"

    val domain = "fpd"

    val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    val id = 1

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
