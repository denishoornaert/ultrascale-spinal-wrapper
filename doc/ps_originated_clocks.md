# PS-originated clocks

Up to four PS-originated clocks are provided: `plclk0`, `plclk1`, `plclk2`, and `plclk3`.

Each clock and reset signal pair can be enabled by enabling `withPL_CLKX` where `X` is the clock number (`[0, ..., 3]`).
More accurately, it is acheived by specifying a frequency different than `0 Hz`.
Doing so gives access to `io.pl.clkX`; a `ClockResetMapped`.

To create a clock area, use the `PLClockingArea` from the `ultascale.clock.PLClockingArea` package.
Such area where the logic should be declared and defined can be described as follows:
```scala
val area = new PLClockingArea(io.pl.clk0) {
    <sequential logic here>
}
```

### Example
See `example.kv260.Shell.scala` for an example.
