# UltraScale+ SpinalHDL Wrapper

**NOTE:** The content of the repository does not standalone: it only provides the support to ease and speed-up the generation of SpinalHDL written modules on AMD-Xilinx UltraScale+ platforms.
To get started, follow the guidelines provided hereunder and have a look at the example.

## Cite us

If you have found this project helpful and used it in your project, feel free to cite us via:
```bibtex
@inproceedings{UltraScaleplusSpinalHDLWrapper,
  title={UltraScale+ SpinalHDL Wrapper: Streamlining Ideas to Bitstream on UltraScale+ platforms},
  author={Hoornaert, Denis and Corradi, Giulio and Mancuso, Renato and Caccamo, Marco},
  booktitle={In Proceedings of the 19th Workshop on Operating Systems Platforms for Embedded Real-Time Applications (OSPERT 2025)},
  year={2025}
}
```

## Documentation

The API documentation of the library can be found [here](https://denishoornaert.github.io/ultrascale-spinal-wrapper/) (work in progress!).

## List of supported boards and Vivado version

In theory, any board and Vivado version can be supported. However, you can find here the board and vivado version supported (if listed) and tested (if checked).

|        | Kria KV260 | Kria KR260 | ZCU102 |
|:------:|:----------:|:----------:|:------:|
| 2024.2 |         ✔️  |         ✔️  |     ❓ |
| 2024.1 |         ✔️  |         ✔️  |     ❓ |
| 2023.2 |         ✔️  |         ✔️  |     ❓ |
| 2022.2 |         ✔️  |         ✔️  |     ❓ |
| 2019.2 |         ⛔ |         ⛔ |     ✔️  |

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
    libraryDependencies += "de.tum" %% "ultrascale-spinal-wrapper" % "1.0"
```
2. Create a variable for the dependency, after the SpinalHDL ones
```scala
val ultrascaleLib = "de.tum" %% "ultrascale-spinal-wrapper" % "1.0"
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


