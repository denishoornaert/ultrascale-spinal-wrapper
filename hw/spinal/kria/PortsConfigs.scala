package kria

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._


object KriaPorts {

  def generateFieldAttribute(encoding: String, interface: String, channel: String, field: String): String = {
    return encoding+" "+interface+" "+channel.toUpperCase()+field.toUpperCase()
  }

  def generateAceFieldAttribute(interface: String, channel: String, field: String): String = {
    return this.generateFieldAttribute("xilinx.com:interface:acemm:1.0", interface, channel, field)
  }

/*  val FPD_ACE_Config = AceConfig(
    addressWidth =   44,
    dataWidth    =  128,
    useProt      = true,
    axi          = Axi4Config(
      addressWidth              =   44,
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
      readIssuingCapability     = -1,
      writeIssuingCapability    = -1,
      combinedIssuingCapability = -1,
      readDataReorderingDepth   = -1
    )
  )*/

}
