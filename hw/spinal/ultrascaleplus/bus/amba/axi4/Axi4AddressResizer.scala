package ultrascaleplus.bus.amba.axi4

import spinal.core._
import spinal.lib._


import spinal.lib.bus.amba4.axi._


import ultrascaleplus.utils.Log


object Axi4WriteOnlyAddressResizer {
  
  def apply(inputConfig: Axi4Config, outputConfig: Axi4Config): Axi4WriteOnlyAddressResizer = new Axi4WriteOnlyAddressResizer(inputConfig, outputConfig)

  def apply(axi: Axi4WriteOnly, outputConfig: Axi4Config): Axi4WriteOnly = {
    val resizer = new Axi4WriteOnlyAddressResizer(axi.config, outputConfig)
    resizer.io.input << axi
    resizer.io.output
  }
}


class Axi4WriteOnlyAddressResizer(inputConfig: Axi4Config, outputConfig: Axi4Config) extends Component {
  
  val io = new Bundle {
    val input  =  slave(Axi4WriteOnly(inputConfig))
    val output = master(Axi4WriteOnly(outputConfig))
  }

  // Connect
  //// AW
  io.output.aw <> io.input.aw
  // Override
  io.output.aw.addr.removeAssignments() := io.input.aw.addr.resized
  if (this.inputConfig.addressWidth > this.outputConfig.addressWidth)
    Log.info(f"[Axi4 Address Resizing] Addrress size mismatch: truncated by ${this.inputConfig.addressWidth-this.outputConfig.addressWidth} bits (${this.inputConfig.addressWidth} bits -> ${this.outputConfig.addressWidth} bits)")
  if (this.inputConfig.addressWidth < this.outputConfig.addressWidth)
    Log.info(f"[Axi4 Address Resizing] Addrress size mismatch: extended by ${this.outputConfig.addressWidth-this.inputConfig.addressWidth} bits (${this.outputConfig.addressWidth} bits -> ${this.inputConfig.addressWidth} bits)")
  //// W
  io.output.w <> io.input.w
  //// B
  io.input.b <> io.output.b
}


object Axi4ReadOnlyAddressResizer {
  
  def apply(inputConfig: Axi4Config, outputConfig: Axi4Config): Axi4ReadOnlyAddressResizer = new Axi4ReadOnlyAddressResizer(inputConfig, outputConfig)

  def apply(axi: Axi4ReadOnly, outputConfig: Axi4Config): Axi4ReadOnly = {
    val resizer = new Axi4ReadOnlyAddressResizer(axi.config, outputConfig)
    resizer.io.input << axi
    resizer.io.output
  }
}


class Axi4ReadOnlyAddressResizer(inputConfig: Axi4Config, outputConfig: Axi4Config) extends Component {
  
  val io = new Bundle {
    val input  =  slave(Axi4ReadOnly(inputConfig))
    val output = master(Axi4ReadOnly(outputConfig))
  }

  // Connect
  //// AR
  io.output.ar <> io.input.ar
  // Override
  io.output.ar.addr.removeAssignments() := io.input.ar.addr.resized
  if (this.inputConfig.addressWidth > this.outputConfig.addressWidth)
    Log.info(f"[Axi4 Address Resizing] Addrress size mismatch: truncated by ${this.inputConfig.addressWidth-this.outputConfig.addressWidth} bits (${this.inputConfig.addressWidth} bits -> ${this.outputConfig.addressWidth} bits)")
  if (this.inputConfig.addressWidth < this.outputConfig.addressWidth)
    Log.info(f"[Axi4 Address Resizing] Addrress size mismatch: extended by ${this.outputConfig.addressWidth-this.inputConfig.addressWidth} bits (${this.outputConfig.addressWidth} bits -> ${this.inputConfig.addressWidth} bits)")
  //// R
  io.input.r <> io.output.r
}


object Axi4AddressResizer {
  
  def apply(inputConfig: Axi4Config, outputConfig: Axi4Config): Axi4AddressResizer = new Axi4AddressResizer(inputConfig, outputConfig)

  def apply(axi: Axi4, outputConfig: Axi4Config): Axi4 = {
    val resizer = new Axi4AddressResizer(axi.config, outputConfig)
    resizer.io.input << axi
    resizer.io.output
  }
}


class Axi4AddressResizer(inputConfig: Axi4Config, outputConfig: Axi4Config) extends Component {
  
  val io = new Bundle {
    val input = slave(Axi4(inputConfig))
    val output = master(Axi4(outputConfig))
  }

  val readOnly = Axi4ReadOnlyAddressResizer(inputConfig, outputConfig)
  val writeOnly = Axi4WriteOnlyAddressResizer(inputConfig, outputConfig)

  readOnly.io.input.ar <> io.input.ar
  readOnly.io.input.r <> io.input.r
  writeOnly.io.input.aw <> io.input.aw
  writeOnly.io.input.w <> io.input.w
  writeOnly.io.input.b <> io.input.b

  readOnly.io.output.ar <> io.output.ar
  readOnly.io.output.r <> io.output.r
  writeOnly.io.output.aw <> io.output.aw
  writeOnly.io.output.w <> io.output.w
  writeOnly.io.output.b <> io.output.b
}
