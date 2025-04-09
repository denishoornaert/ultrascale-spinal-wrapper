package ultrascaleplus.scripts


import java.io.File
import java.io.FileWriter


import spinal.core._
import spinal.lib._


import ultrascaleplus.bus.amba.axi4._


object KernelModule {

  def body(variables: String, functions: String, inits: String, destroys: String): String = s"""
    |#include <asm-generic/errno-base.h>
    |#include <linux/init.h>
    |#include <linux/module.h>
    |#include <linux/kernel.h>
    |#include <linux/fs.h>
    |#include <linux/cdev.h>
    |#include <linux/device.h>
    |#include <linux/mm.h>
    |#include <linux/io.h>
    |#include <linux/uaccess.h>
    |#include <linux/slab.h>
    |#include <linux/types.h>
    |#include <stddef.h>
    |
    |
    |MODULE_LICENSE("GPL");
    |MODULE_AUTHOR("UltraScale+ SpinalHDL Wrapper (auto-generated)");
    |MODULE_DESCRIPTION("Kernel module exposing PL-ports as device targets.");
    |MODULE_VERSION("alpha");
    |
    |
    |#if defined(VERBOSE)
    | #define PR_INFO(fmt, ...) pr_info(fmt, ##__VA__ARGS__)
    |#else
    | #define PR_INFO(fmt, ...)
    |#endif
    |
    |
    |static struct class* class = NULL;
    |${variables}
    |
    |${functions}
    |
    |static int __init m_init(void) {
    | class = class_create(THIS_MODULE, "PL-ports");
    | if (IS_ERR(class)) {
    |   pr_alert("[PL-ports] Failed to create device class.\\n");
    |   return PTR_ERR(class);
    | }
    |
    | int ret = 0;
    |${inits}
    | pr_info("[PL-ports] Devices created.\\n");
    | return 0;
    |}
    |
    |
    |static void __exit m_exit(void) {
    |${destroys}
    | pr_info("[PL-ports] Devices destroyed.\\n");
    |}
    |
    |module_init(m_init);
    |module_exit(m_exit);
    """.stripMargin('|')

  def variables(port_name: String): String = s"""
    |// ${port_name} variables
    |static struct cdev ${port_name}_cdev;
    |static dev_t ${port_name}_dev_t = 0;
    |static struct device* ${port_name}_device;
    """.stripMargin('|')

  // TODO: add checks for size lqrger than the corresponfing port's aperture
  def functions(port_name: String, port_remap: String, port_addr: String): String = s"""
    |static int ${port_name}_open(struct inode* inode, struct file* file) {
    | PR_INFO("[PL-ports] Device for ${port_name} opened.\\n");
    | return 0;
    |}
    |
    |static int ${port_name}_release(struct inode* inode, struct file* file) {
    | PR_INFO("[PL-ports] Device for ${port_name} closed.\\n");
    | return 0;
    |}
    |
    |static int ${port_name}_mmap(struct file* file, struct vm_area_struct* vma) {
    | int ret;
    | long unsigned int size, pfn;
    | size = (long unsigned int)(vma->vm_end-vma->vm_start);
    | pfn = 0x${port_addr}UL >> PAGE_SHIFT;
    | PR_INFO("[PL-ports] ${port_name} mapped at PFN = 0x%lx.\\n", pfn);
    | ret = ${port_remap}
    | if (ret == -1) {
    |   pr_alert("[PL-ports] Function remap_pfn_range failed for 0x%lx to 0x%lx (PFN).\\n", vma->vm_start, pfn);
    |   return ret;
    | }
    | return 0;
    |}
    |
    |static const struct file_operations ${port_name}_dev_fops = {
    | .open    = ${port_name}_open,
    | .release = ${port_name}_release,
    | .mmap    = ${port_name}_mmap,
    | .owner   = THIS_MODULE,
    |};
    """.stripMargin('|')

  def inits(port_name: String): String = s"""
    | ret = alloc_chrdev_region(&${port_name}_dev_t, 0, 1, "${port_name}");
    | if (ret == -1) {
    |   pr_alert("[PL-ports] Failed to alloc_chrdev_region for ${port_name}.\\n");
    | }
    |
    | cdev_init(&${port_name}_cdev, &${port_name}_dev_fops);
    | ret = cdev_add(&${port_name}_cdev, ${port_name}_dev_t, 1);
    | if (ret == -1) {
    |   pr_alert("[PL-ports] Failed to cdev_add for ${port_name}.\\n");
    | }
    |
    | ${port_name}_device = device_create(class, NULL, ${port_name}_dev_t, NULL, "${port_name}");
    | if (${port_name}_device == ERR_PTR) {
    |   pr_alert("[PL-ports] Failed to create device for ${port_name}.\\n");
    |   ret = -EINVAL;
    | }
    """.stripMargin('|')

  def destroys(port_name: String): String = s"""
    | device_destroy(class, ${port_name}_dev_t);
    | cdev_del(&${port_name}_cdev);
    """.stripMargin('|')

  var ports = List[Axi4Mapped]()
  var io_ports = List[Axi4Mapped]()

  def add(port: Axi4Mapped): Unit = {
    ports = port +: ports
  }

  def addIO(port: Axi4Mapped): Unit = {
    io_ports = port +: io_ports
  }

  def generateVariablesSection(): String = {
    var res: String = ""
    for (port <- ports) {
      res = res+variables(port.name)
    }
    for (port <- io_ports) {
      res = res+variables(port.name)
    }
    return res
  }

  def generateFunctionsSection(): String = {
    var res: String = ""
    for (port <- ports) {
      for (aperture <- port.apertures) {
        val remap = "remap_pfn_range(vma, vma->vm_start, pfn, size, vma->vm_page_prot);"
        res = res+functions(port.name, remap, aperture.base.toString(16))
      }
    }
    for (port <- io_ports) {
      for (aperture <- port.apertures) {
        val remap = "io_remap_pfn_range(vma, vma->vm_start, pfn, size, pgprot_noncached(vma->vm_page_prot));"
        res = res+functions(port.name, remap, aperture.base.toString(16))
      }
    }
    return res
  }

  def generateInitsSection(): String = {
    var res: String = ""
    for (port <- ports) {
      res = res+inits(port.name)
    }
    for (port <- io_ports) {
      res = res+inits(port.name)
    }
    return res
  }

  def generateDestroysSection(): String = {
    var res: String = ""
    for (port <- ports) {
      res = res+destroys(port.name)
    }
    for (port <- io_ports) {
      res = res+destroys(port.name)
    }
    return res
  }

  def generateMakefile(): String = """
obj-m += pl_ports.o

PWD := $(CURDIR)
KDIR := /lib/modules/$(shell uname -r)/build

pl_ports:
  make -C $(KDIR) M=$(PWD) modules

clean:
  make -C $(KDIR) M=$(PWD) clean
""" //.stripMargin('|')

  def generate(): Unit = {
    val kernel_module_dir = "hw/gen/kernel_module/"
    // Create kernel module directory
    val dir = new java.io.File(kernel_module_dir).mkdir
    // Generate sources
    val module_source = body(
      variables = generateVariablesSection(),
      functions = generateFunctionsSection(),
      inits     = generateInitsSection(),
      destroys  = generateDestroysSection()
    )
    val makefile_source = generateMakefile()
    // Write to files
    //// Kernel module
    val module = new FileWriter(new File(kernel_module_dir+"pl_ports.c"))
    module.write(module_source)
    module.close()
    //// Makefile
    val makefile = new FileWriter(new File(kernel_module_dir+"Makefile"))
    makefile.write(makefile_source)
    makefile.close()
  }

}
