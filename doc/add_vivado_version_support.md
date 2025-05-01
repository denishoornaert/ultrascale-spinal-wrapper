# Adding support for other Vivado versions

As we found out, the main difference between one version to another is the IPs versions.
As most of the TCL script generation logic can be kept and re-use, the library uses a simple JSOM file as database to store the relevant IPs versions for each vivado version.
The database in question named `VivadoIPVersion.json` can be found in `hw/ext/`.

The JSON file structure is as follows:
```json
{
    "<vivado-version-1>" : {
        "zynq_ultra_ps_e" : "<zynq-ip-version-1>",
        "proc_sys_reset"  : "<reset-sys-ip-version-1>"
    },
    "<vivado-version-2>" : {
        "zynq_ultra_ps_e" : "<zynq-ip-version-2>",
        "proc_sys_reset"  : "<reset-sys-ip-version-2>"
    },
    ...
    "<vivado-version-n>" : {
        "zynq_ultra_ps_e" : "<zynq-ip-version-n>",
        "proc_sys_reset"  : "<reset-sys-ip-version-n>"
    },
}
```
Where:
 - `<vivado-version-X>` is the version of vivado printed when typing `vivado -version` in your terminal. It should be foru digits (i.e., a year) followed by a signle digit (i.e., a revision number).
 - `<zynq-ip-version-X>` is the version of the zynq IP for the associated vivado version. The information can be found in the vivado's IPs library.
 - `<sys-reset-ip-version-X>` is the version of the system reset IP for the associated vivado version. The information can be found in the vivado's IPs library.

