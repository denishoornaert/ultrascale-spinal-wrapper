# Advance eXtended Interface (AXI4) Verification IP

**For simulation only**

The modules are all available in the `ultrascaleplus.bus.amba.axi4.sim` package.

```scala
val primary = new Axi4CheckerPrimary(dut.io.axi, dut.clockDomain)
val secondary = new Axi4CheckerSecondary(dut.io.fpd.hp0, dut.clockDomain)
```

```scala
val aw = new Axi4AWJob(
  channel = dut.io.fpd.hpm0.aw,
  addr    = AddressMap.FPD_HPM0.base+(t*64),
  id      = 0x6800+(t*0x0020),
  len     = expectedData.length-1,
  size    = log2Up(dut.io.fpd.hpm0.aw.config.bytePerWord)
)
val w = new Axi4WJob(
  channel = dut.io.fpd.hpm0.w,
  data    = expectedData,
  strb    = strobeBits,
  parent  = aw
)
primary.addWrite(aw, w)
```

```scala
primary.addRead(
  new Axi4ARJob(
    channel = dut.io.fpd.hpm0.ar,
    addr    = AddressMap.FPD_HPM0.base+(t*64),
    id      = 0x6800+(t*0x0020),
    len     = 4-1,
    size    = log2Up(dut.io.fpd.hpm0.ar.config.dataWidth/8)
  )
)
```

```scala
dut.clockDomain.waitRisingEdgeWhere(primary.allWritesCompleted())
```
