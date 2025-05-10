# Set Vivado version for HDL generation

## Target vivado version is locally installed

The easiest way is to source the proper vivado version and keep the `Config.vivado` set to "auto".
In other words, make sure to:
```bash
source /vivado/install/folder/version/settings.sh
```

Otherwise, see next section.

## Target vivado version is not locally installed

As early as possible in your top module descrtiption, add
```bash
Config.vivado = "2022.2"
```
for instance.
