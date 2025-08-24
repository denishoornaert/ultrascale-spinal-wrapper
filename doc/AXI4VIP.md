# Advance eXtended Interface (AXI4) Verification IP

**For simulation only**

The modules are all available in the `ultrascaleplus.bus.amba.axi4.sim` package.

A quick example is availale in [Bleacher](hw/spinal/example/kv260/Bleacher.scala).

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

The `parent` field is important as it creates a dependency between the W job and the corresponding AW job.
This is required to satisfy the property that `w` handshake cannot happen before the corresponding addressing phase (i.e., a handshake on `aw`).

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

There are two drivers provided (They can each be modified and tailored to specific use cases **WiP**).
The drivers are created using an AXI4 bus and a clockdomain.

```scala
val primary = new Axi4CheckerPrimary(dut.io.m.axi, dut.clockDomain)
val secondary = new Axi4CheckerSecondary(dut.io.s.axi, dut.clockDomain)
```

As provided, most user interaction is on the `primary` driver.
The user can add an arbitrary amount of read and write jobs that will be scheduled and place ont he bus by the `primary` driver.

### Adding write jobs

To add a write request, the user can call the `addWrite` method on the `primary` driver.
The request is described as the combination of a AW job and a W job.
Note that the W job must have for `parent` the AW job.

```scala
primary.addWrite(Axi4AWJob: aw, Axi4WJob: w)
```

Here is an example:

```scala
val aw = new Axi4AWJob(
  channel = dut.io.fpd.hpm0.aw,
  addr    = AddressMap.FPD_HPM0.base,
  id      = 0x6800,
  len     = 1,
  size    = 4 // or 128 bits, 16 bytes
)
val w = new Axi4WJob(
  channel = dut.io.fpd.hpm0.w,
  data    = Seq(BigInt("00001111222233334444555566667777", 16), BigInt("88889999AAAABBBBCCCCDDDDEEEEFFFF", 16)),
  strb    = Seq(BigInt("FFFF", 16), BigInt("FFFF", 16)),
  parent  = aw
)
primary.addWrite(aw, w)
```

### Adding read jobs

To add a write request, the user can call the `addRead` method on the `primary` driver.
The request is described as the combination of a AR job.

```scala
Axi4ARJob(
  channel = dut.io.fpd.hpm0.ar,
  addr    = AddressMap.FPD_HPM0.base,
  id      = 0x6800,
  len     = 3, // 4 beats
  size    = 4
)
primary.addRead(ar)
```

### Waiting for job to be emitted and served

The `primary` driver indicates when pending transactions have been emitted **and** served via two functions:
 - `allWritesCompleted()`
 - `allreadsCompleted()`

These methods can be used in combination to [SpinalHDL "Wait API"](https://spinalhdl.github.io/SpinalDoc-RTD/dev/SpinalHDL/Simulation/clock.html#wait-api) (e.g., `waitRisingEdgeWhere`) to pace the simulation.

```scala
dut.clockDomain.waitRisingEdgeWhere(primary.allWritesCompleted())
dut.clockDomain.waitRisingEdgeWhere(primary.allReadsCompleted())
```
