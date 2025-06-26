# Adding new interfaces and IOs

Adding new interfaces and IOs involves creating and modifying files in different places.
Essentially, three steps are needed:
 1. A declaration and definition of the generic logic.
 2. A definition of platform/target specific configurations for the interfaces/IOs.
 3. Adding the interface/IO in the platform/target definition.

## Generic logic definition

Generic definitions are always placed in the ultrascaleplus package.
The designer must determine which sub-package to select.
Typically, if it is a PS-PL interface it should be placed in `package.signal` whereas any IO (e.g., PMOD) shoudl go in `package.io`.
In the new file, code will look as follows:

```scala
package ultrascaleplus.<io,signal>.<intf-name>

import spinal.core._
import spinal.lib._

import ultrascaleplus.utils._

case class <intf-name>(config: <config>) extends Bundle with TCL with XDC {

    val field_1 = UInt(X bits)
    val field_2 = Bool()

    override def getTCL(moduleName: String, clock: String): String = {}

    override def getXDC(): String = {}

}
```

The overarching idea is to keep the definition as abstract as possible.
Generally, it means keeping implementation specific details out (e.g., bus widths, SoC pins, ...)

The class:
 - **must** inherit from a "pure" SpinalHDL data type such as `Bundle` in the example above.
 - **can** inherit form the `TCL` trait. Most interfaces need this to indicate Vivado where to be tethered.
 - **can** inherit from the `XDC` trait. Used for anything requiring constraint; typically for IO pins such as PMOD or any GPIO.

Inheriting the `TCL` class gives access to the `getTCL` method that *must be overriden*.
The method generates the `TCL` code dictating Vivado how to connect the defined interface to another one of the same type.

The same process takes place for `XDC` except the designer must indicate how the related constraints shoud be generated.

## Configuration definition

The configuration is an object containing any form of platform-specific information on how to integrate the instanciated interface/IO with the board.
It is given as a constructor's parameter (see `<config>` in the example above).

For instance, a pmod IO needs to be tethered to a set of board-specific pins.
The configuration given as constructor parameter may look as follows:
```scala
object PMOD0 {

  val names = Seq[String]("H12", "B10", "E10", "E12", "D10", "D11", "C11", "B11")

}
```
with `PMOD0.names` being the sequence of pins stored as strings.
In this case, the pins will be later used in the interface's `getTCL()` and `getXDC()` methods.

## Enabling access to platforms

Adding an interface/IO to a platform is straight forward.
Essentially, in their io defintion add an optional entry.
Typically:
```scala
val io = new Bundle {
    ...
    val <new-interface> = (<condition>) generate (<intf-name>(<config>))
    ...
}
```
where the `<condition>` tells whether the interface/IO is wanted.
It is often made part of a companion configuration class.
For instance, the KV260` is accompanied by a KV260IOConfig` class.
