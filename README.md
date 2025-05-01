# SpinalHDL Base Project

**NOTE:** The content of the repository does not standalone: it only provides the support to ease and speed-up the generation of SpinalHDL written modules on AMD-Xilinx UltraScale+ platforms.
To get started, follow the guidelines provided hereunder and have a look at the example.

## List of supported boards

In theory most boards can be supported. However, you can find here a list of board supported (if listed) and tested (if checked).

 - [x] AMD-Xilinx Kria KV260
 - [x] AMD-Xilinx Kria KR260
 - [ ] AMD-Xilinx ZCU102

## Vivado version tested

 - [x] Vivado 2024.1
 - [x] Vivado 2022.2

**Let us know** if it works for you with another version!

See [How to add suport for a Vivado version](doc/add_vivado_version_support.md) section for further information on how to extend the library.

## How to use the library (guidelines)

Soon to be added!

### Generate and publish locally the library (to include in other projects)

___PAY ATTENTION TO VERSIONS!___

From the root foolder

```bash
sbt clean compile publishLocal

# or

make library
```

This will create the `.jar` files in `/target/scala-2.12/` and install them under `${HOME}/.ivy2/local/`;
the library can now be included as a dependency on other projects.

To add it as a dependency:

1. Directly add it with following line in the project settings
```scala
    libraryDependencies += "de.tum" %% "ultrascale-spinal-wrapper" % "0.1"
```
2. Create a variable for the dependency, after the SpinalHDL ones
```scala
val ultrascaleLib = "de.tum" %% "ultrascale-spinal-wrapper" % "0.1"
```
and add it to the main list of dependencies
```scala
libraryDependencies ++= Seq(spinalCore, spinalLib, spinalIdslPlugin, ultrascaleLib)
```

### Generate bitstream

Must be done from the root folder!
```bash
vivado -mode batch -source vivado/MyModule.tcl
```
You will end up with `MyModule.bit` in the root folder.


### Example

Soon to be added!


