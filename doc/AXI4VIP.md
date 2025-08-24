# Advance eXtended Interface (AXI4) Verification IP

**For simulation only**

The modules are all available in the `ultrascaleplus.bus.amba.axi4.sim` package.

The offered VIP relies on 2 key concepts:
 - `primary` and `secondary` object to drive the AXI4 channels (i.e., `ar`, `r`, `aw`, `w`, and `b`)
 - Channels' job to describe transactions activity on each channel.

## AXI4 channels' job

JObs aim at describing a transaction's activity on its associated channel.
For instance, `Axi4RJob` describes the payload read from the memory target (i.e., `secondary`).
Typically, this means that it contains an array of data (the length depends ont he amount of beats requested) and an id.
The jobs are created and maintained by their responsible drivers (`primary` and `secondary`).

### AW job

Created on the primary's side. Mainted/queued on the secondary's side.
```scala
Axi4AWJob(channel: Axi4Aw, addr: BigInt, id: Int, len: Int, size: Int, burst: Int)
```

### W job

Created on the primary's side. Mainted/queued on the secondary's side.
```scala
Axi4WJob(channel: Axi4W, data: Seq[BigInt], strb: Seq[BigInt], parent: Axi4AWJob)
```

**NOTE:** Marking the parent job `burst` type as `WRAP` does not imply that the `data` and `strb` fields will be re-ordered for you.
IN other word, you are still responsible to order the data according to the `wrap` described int he parent job.

**TIP:** To described large bus width data/payload, populate the `data` field with `BigInt("00000000000000000000000000000000", 16)` for a 128 bits wide bus.

### B job

Created on the secondary's side. Mainted/queued on the primary's side.
```scala
Axi4BJob(channel: Axi4B, id: Int, resp: Int)
```

### AR job

Created on the primary's side. Mainted/queued on the secondary's side.
```scala
Axi4ARJob(channel: Axi4Ar, addr: BigInt, id: Int, len: Int, size: Int, burst: Int)
```

### R job

Created on the secondary's side. Mainted/queued on the primary's side.
```scala
Axi4RJob(channel: Axi4R, id: Int, resp: Int)
```

Data can be attached to the R job by enqueueing it to the job.
This is done via the `enqueue` method which can take either:
 - a fixed data value `enqueue(value: BigInt)` (i.e., `BigInt`)
 - a function to be executed when scheduled for placement on the bus `enqueue(func: () => BigInt)` (i.e., `() => BigInt`)

**NOTE:** Marking the parent job `burst` type as `WRAP` does not imply that the `data` and `strb` fields will be re-ordered for you.
IN other word, you are still responsible to order the data according to the `wrap` described int he parent job.

**TIP:** To described large bus width data/payload, populate the `data` field with `BigInt("00000000000000000000000000000000", 16)` for a 128 bits wide bus.

## AXI4 Driver

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
