package ultrascale.kria

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

  val FPD_ACE_Config = AceConfig(
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
  )

  def generateAxi4FieldAttribute(interface: String, channel: String, field: String): String = {
    return this.generateFieldAttribute("xilinx.com:interface:aximm:1.0", interface, channel, field)
  }

  def setAxi4InterfaceAttributes(port: Axi4): Unit = {
    // TODO: Is the port/interface tag needed?
    //val direction = if(port.isMasterInterface) "Master" else "Slave"
    //port.addAttribute("X_INTERFACE_INFO", "XIL_INTERFACENAME "+port.getName()+", PROTOCOL AXI4, MODE "+direction)
    // AW
    port.aw.valid.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.aw.getPartialName(), port.aw.valid.getPartialName()))
    port.aw.ready.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.aw.getPartialName(), port.aw.ready.getPartialName()))
    port.aw.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.aw.getPartialName(), e._1))}
    // W
    port.w.valid.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.w.getPartialName(), port.w.valid.getPartialName()))
    port.w.ready.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.w.getPartialName(), port.w.ready.getPartialName()))
    port.w.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.w.getPartialName(), e._1))}
    // B
    port.b.valid.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.b.getPartialName(), port.b.valid.getPartialName()))
    port.b.ready.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.b.getPartialName(), port.b.ready.getPartialName()))
    port.b.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.b.getPartialName(), e._1))}
    // AR
    port.ar.valid.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.ar.getPartialName(), port.ar.valid.getPartialName()))
    port.ar.ready.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.ar.getPartialName(), port.ar.ready.getPartialName()))
    port.ar.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.ar.getPartialName(), e._1))}
    // R
    port.r.valid.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.r.getPartialName(), port.r.valid.getPartialName()))
    port.r.ready.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.r.getPartialName(), port.r.ready.getPartialName()))
    port.r.payload.elements.map{e => e._2.addAttribute("X_INTERFACE_INFO", this.generateAxi4FieldAttribute(port.getName(), port.r.getPartialName(), e._1))}
  }

  val LPD_HPM0_Config = Axi4Config(
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

  val FPD_HPM0_Config = Axi4Config(
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

  val FPD_HPM1_Config = Axi4Config(
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

  val FPD_HP0_Config = Axi4Config(
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

  val FPD_HP1_Config = Axi4Config(
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

  val FPD_HP2_Config = Axi4Config(
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

  val FPD_HP3_Config = Axi4Config(
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

}
