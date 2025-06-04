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

    override val id = ""

    override val config = Axi4Config(
      addressWidth              =    40,
      dataWidth                 =   128,
      idWidth                   =    16,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =    16,
      awUserWidth               =    16,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =     8,
      writeIssuingCapability    =     8,
      combinedIssuingCapability =    16,
      readDataReorderingDepth   =    -1
    )
  
  }
  
  object HP0 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = ""

    override val domain = "lpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "GP6"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    49,
      dataWidth                 =   128,
      idWidth                   =     6,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     1,
      awUserWidth               =     1,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }

}

object FPD {

  object HPM0 extends Axi4MappedInstanceTemplate() {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hpm0"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.FPD_HPM0
    )
  
    override val id = ""

    // Primary port from the pov of the PS but a secondary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    40,
      dataWidth                 =   128,
      idWidth                   =    16,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =    16,
      awUserWidth               =    16,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =     8,
      writeIssuingCapability    =     8,
      combinedIssuingCapability =    16,
      readDataReorderingDepth   =    -1
    )

  }

  object HPM1 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hpm1"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.FPD_HPM1
    )

    override val id = ""

    // Primary port from the pov of the PS but a secondary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    40,
      dataWidth                 =   128,
      idWidth                   =    16,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =    16,
      awUserWidth               =    16,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =     8,
      writeIssuingCapability    =     8,
      combinedIssuingCapability =    16,
      readDataReorderingDepth   =    -1
    )

  }

  object ACP extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "acp"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "ACP"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    40,
      dataWidth                 =   128,
      idWidth                   =     5,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     2,
      awUserWidth               =     2,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }

  object HP0 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hp0"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "GP2"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    49,
      dataWidth                 =   128,
      idWidth                   =     6,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     1,
      awUserWidth               =     1,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }

  object HP1 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hp1"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "GP3"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    49,
      dataWidth                 =   128,
      idWidth                   =     6,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     1,
      awUserWidth               =     1,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }

  object HP2 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hp2"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "GP4"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    49,
      dataWidth                 =   128,
      idWidth                   =     6,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     1,
      awUserWidth               =     1,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }

  object HP3 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hp3"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "GP5"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    49,
      dataWidth                 =   128,
      idWidth                   =     6,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     1,
      awUserWidth               =     1,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }

  object HPC0 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hpc0"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "GP0"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    49,
      dataWidth                 =   128,
      idWidth                   =     6,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     1,
      awUserWidth               =     1,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }

  object HPC1 extends Axi4MappedInstanceTemplate {

    // Should ideally be the concatenation of the parent object and the current object in small letters
    override val name = "hpc1"

    override val domain = "fpd"

    override val apertures = Seq(
      AddressMap.HIGH_DDR,
      AddressMap.LOW_DDR,
      AddressMap.OCM,
      AddressMap.QSPI
    )

    override val id = "GP1"

    // Secondary port from the pov of the PS but a primary port from the pov of the PL
    override val config = Axi4Config(
      addressWidth              =    49,
      dataWidth                 =   128,
      idWidth                   =     6,
      useId                     =  true,
      useRegion                 = false,
      useBurst                  =  true,
      useLock                   =  true,
      useCache                  =  true,
      useSize                   =  true,
      useQos                    =  true,
      useLen                    =  true,
      useLast                   =  true,
      useResp                   =  true,
      useProt                   =  true,
      useStrb                   =  true,
      useAllStrb                = false,
      arUserWidth               =     1,
      awUserWidth               =     1,
      rUserWidth                =    -1,
      wUserWidth                =    -1,
      bUserWidth                =    -1,
      readIssuingCapability     =    16,
      writeIssuingCapability    =    16,
      combinedIssuingCapability =    32,
      readDataReorderingDepth   =    -1
    )

  }
}
