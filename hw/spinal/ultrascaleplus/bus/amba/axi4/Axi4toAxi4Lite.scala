package ultrascaleplus.bus.amba.axi4

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi.{Axi4, Axi4Config, Axi4IdRemover, Axi4ReadOnly, Axi4Unburster, Axi4WriteOnly, Axi4WriteOnlyUpsizer, Axi4WriteOnlyDownsizer, Axi4ReadOnlyUpsizer, Axi4ReadOnlyDownsizer}
import spinal.lib.bus.amba4.axilite._


object Axi4toAxiLite4 {

  def apply(axi: Axi4, axiliteConfig: AxiLite4Config): AxiLite4 = {
    val bridge = Axi4toAxiLite4(axi.config, axiliteConfig)
    bridge.io.axi << axi
    return bridge.io.axilite
  }
  
  def toLiteConfig(config: Axi4Config) = {
    AxiLite4Config(
      addressWidth = config.addressWidth,
      dataWidth = config.dataWidth,
      readIssuingCapability = config.readIssuingCapability,
      writeIssuingCapability = config.writeIssuingCapability,
      combinedIssuingCapability = config.combinedIssuingCapability,
      readDataReorderingDepth = config.readDataReorderingDepth
    )
  }

  def toAxiConfig(config: AxiLite4Config) = {
    Axi4Config(
      addressWidth = config.addressWidth,
      dataWidth = config.dataWidth,
      useId = false,
      useRegion = false,
      useBurst = false,
      useLock = false,
      useCache = false,
      useSize = false,
      useQos = false,
      useLen = false,
      useResp = true,
      useProt = true,
      useStrb = true,
      readIssuingCapability = config.readIssuingCapability,
      writeIssuingCapability = config.writeIssuingCapability,
      combinedIssuingCapability = config.combinedIssuingCapability,
      readDataReorderingDepth = config.readDataReorderingDepth
    )
  }

}


case class Axi4toAxiLite4(axiConfig: Axi4Config, axiliteConfig: AxiLite4Config) extends Component {

  // Must be multiple of 32 bits or 64 bits (implied by AxiLite4Config requirement)
  require(axiConfig.dataWidth%axiliteConfig.dataWidth == 0)

  val io = new Bundle {
    val axi     =  slave(Axi4(axiConfig))
    val axilite = master(AxiLite4(axiliteConfig))
  }

  val readOnly = Axi4ReadOnlyToAxiLite4ReadOnly(axiConfig, axiliteConfig)
  readOnly.io.axi << io.axi.toReadOnly()
  io.axilite << readOnly.io.axilite

  val writeOnly = Axi4WriteOnlyToAxiLite4WriteOnly(axiConfig, axiliteConfig)
  writeOnly.io.axi << io.axi.toWriteOnly()
  io.axilite << writeOnly.io.axilite

}


case class Axi4ReadOnlyToAxiLite4ReadOnly(axiConfig: Axi4Config, axiliteConfig: AxiLite4Config) extends Component {

  val io = new Bundle {
    val axi     =  slave(Axi4ReadOnly(axiConfig))
    val axilite = master(AxiLite4ReadOnly(axiliteConfig))
  }

  val axiAddrResized = if (axiConfig.addressWidth != axiliteConfig.addressWidth) {
    Axi4ReadOnlyAddressResizer(io.axi, axiConfig.copy(addressWidth=axiliteConfig.addressWidth))
  } else {
    io.axi
  }

  val axiNoId = if (axiAddrResized.config.useId) {
    Axi4IdRemover(axiAddrResized)
  } else {
    axiAddrResized
  }

  val axiUnburst = if (axiNoId.config.useLen) {
    Axi4Unburster(axiNoId)
  } else {
    axiNoId
  }

  val axiNoSize = if (axiUnburst.config.useSize) {
    val axiNoSize = Axi4ReadOnly(axiUnburst.config.copy(useSize = false))
    axiNoSize.ar.arbitrationFrom(axiUnburst.ar)
    axiNoSize.ar.payload.assignSomeByName(axiUnburst.ar.payload)

    axiUnburst.r.arbitrationFrom(axiNoSize.r)
    axiUnburst.r.payload.assignSomeByName(axiNoSize.r.payload)
    axiNoSize
  } else {
    axiUnburst
  }

  val axiWidthAdapted = if (axiliteConfig.dataWidth != axiNoSize.config.dataWidth) {
    val intermediateConfig = axiNoSize.config.copy(useLen = true)
      val adapted = if (axiNoSize.config.dataWidth > axiliteConfig.dataWidth) {
      val adapted = Axi4ReadOnlyDownsizer(intermediateConfig, Axi4toAxiLite4.toAxiConfig(axiliteConfig))
      // Connect
      adapted.io.input.ar.arbitrationFrom(axiNoSize.ar)
      adapted.io.input.ar.payload.assignSomeByName(axiNoSize.ar.payload)
      adapted.io.input.ar.len := 0
      axiNoSize.r.arbitrationFrom(adapted.io.input.r)
      axiNoSize.r.payload.assignSomeByName(adapted.io.input.r.payload)
      // Return module
      adapted.io.output
    } else {
      val adapted = Axi4ReadOnlyUpsizer(intermediateConfig, Axi4toAxiLite4.toAxiConfig(axiliteConfig), 4)
      // Connect
      adapted.io.input.ar.arbitrationFrom(axiNoSize.ar)
      adapted.io.input.ar.payload.assignSomeByName(axiNoSize.ar.payload)
      adapted.io.input.ar.len := 0
      axiNoSize.r.arbitrationFrom(adapted.io.input.r)
      axiNoSize.r.payload.assignSomeByName(adapted.io.input.r.payload)
      // Return module
      adapted.io.output
    }
    adapted
  } else {
    axiNoSize
  }

  val axiMinimal = Axi4ReadOnly(Axi4toAxiLite4.toAxiConfig(axiliteConfig))
  axiWidthAdapted >> axiMinimal

  io.axilite.ar.arbitrationFrom(axiMinimal.ar)
  io.axilite.ar.payload.assignSomeByName(axiMinimal.ar.payload)

  axiMinimal.r.arbitrationFrom(io.axilite.r)
  axiMinimal.r.payload.assignSomeByName(io.axilite.r.payload)
  axiMinimal.r.last := True

}


case class Axi4WriteOnlyToAxiLite4WriteOnly(axiConfig: Axi4Config, axiliteConfig: AxiLite4Config) extends Component {
    
  val io = new Bundle {
    val axi     =  slave(Axi4WriteOnly(axiConfig))
    val axilite = master(AxiLite4WriteOnly(axiliteConfig))
  }

  val axiAddrResized = if (axiConfig.addressWidth != axiliteConfig.addressWidth) {
    Axi4WriteOnlyAddressResizer(io.axi, axiConfig.copy(addressWidth=axiliteConfig.addressWidth))
  } else {
    io.axi
  }

  val axiNoId = if (axiAddrResized.config.useId) {
    Axi4IdRemover(axiAddrResized)
  } else {
    axiAddrResized
  }

  val axiUnburst = if (axiNoId.config.useLen) {
    Axi4Unburster(axiNoId)
  } else {
    axiNoId
  }

  val axiNoSize = if (axiUnburst.config.useSize) {
    val axiNoSize = Axi4WriteOnly(axiUnburst.config.copy(useSize = false))
    axiNoSize.aw.arbitrationFrom(axiUnburst.aw)
    axiNoSize.aw.payload.assignSomeByName(axiUnburst.aw.payload)

    axiNoSize.w.arbitrationFrom(axiUnburst.w)
    axiNoSize.w.payload.assignSomeByName(axiUnburst.w.payload)

    axiUnburst.b.arbitrationFrom(axiNoSize.b)
    axiUnburst.b.payload.assignSomeByName(axiNoSize.b.payload)
    axiNoSize
  } else {
    axiUnburst
  }
  
  val axiWidthAdapted = if (axiliteConfig.dataWidth != axiNoSize.config.dataWidth) {
    val intermediateConfig = axiNoSize.config.copy(useLen = true)
    val adapted = if (axiNoSize.config.dataWidth > axiliteConfig.dataWidth) {
      val adapted = Axi4WriteOnlyDownsizer(intermediateConfig, Axi4toAxiLite4.toAxiConfig(axiliteConfig))
      // Connect
      adapted.io.input.aw.arbitrationFrom(axiNoSize.aw)
      adapted.io.input.aw.payload.assignSomeByName(axiNoSize.aw.payload)
      adapted.io.input.aw.len := 0
      adapted.io.input.w.arbitrationFrom(axiNoSize.w)
      adapted.io.input.w.payload.assignSomeByName(axiNoSize.w.payload)
      axiNoSize.b.arbitrationFrom(adapted.io.input.b)
      axiNoSize.b.payload.assignSomeByName(adapted.io.input.b.payload)
      // Return output
      adapted.io.output
    } else {
      val adapted = Axi4WriteOnlyUpsizer(intermediateConfig, Axi4toAxiLite4.toAxiConfig(axiliteConfig))
      // Connect
      adapted.io.input.aw.arbitrationFrom(axiNoSize.aw)
      adapted.io.input.aw.payload.assignSomeByName(axiNoSize.aw.payload)
      adapted.io.input.aw.len := 0
      adapted.io.input.w.arbitrationFrom(axiNoSize.w)
      adapted.io.input.w.payload.assignSomeByName(axiNoSize.w.payload)
      axiNoSize.b.arbitrationFrom(adapted.io.input.b)
      axiNoSize.b.payload.assignSomeByName(adapted.io.input.b.payload)
      // Return output
      adapted.io.output
    }
    adapted
  } else {
    axiNoSize
  }

  val axiMinimal = Axi4WriteOnly(Axi4toAxiLite4.toAxiConfig(axiliteConfig))

  axiWidthAdapted >> axiMinimal

  io.axilite.aw.arbitrationFrom(axiMinimal.aw)
  io.axilite.aw.payload.assignSomeByName(axiMinimal.aw.payload)

  io.axilite.w.arbitrationFrom(axiMinimal.w)
  io.axilite.w.payload.assignSomeByName(axiMinimal.w.payload)

  axiMinimal.b.arbitrationFrom(io.axilite.b)
  axiMinimal.b.payload.assignSomeByName(io.axilite.b.payload)

}

