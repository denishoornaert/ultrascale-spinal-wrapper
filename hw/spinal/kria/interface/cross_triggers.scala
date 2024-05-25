package kria.interface.crosstrigger

import spinal.core._
import spinal.lib._

import generic.interface.crosstrigger._
import ultrascaleplus.interface.crosstrigger._


object DBG_CTI0 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti0")
  this.setInterfaceAttributes()

}

object DBG_CTI1 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti1")
  this.setInterfaceAttributes()

}

object DBG_CTI2 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti2")
  this.setInterfaceAttributes()

}

object DBG_CTI3 extends AbstractCrossTrigger() {

  override val port = slave(CrossTrigger())

  this.port.setPartialName("dbg_cti3")
  this.setInterfaceAttributes()

}

object DBG_CTO0 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto0")
  this.setInterfaceAttributes()

}

object DBG_CTO1 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto1")
  this.setInterfaceAttributes()

}

object DBG_CTO2 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto2")
  this.setInterfaceAttributes()

}

object DBG_CTO3 extends AbstractCrossTrigger() {

  override val port = master(CrossTrigger())

  this.port.setPartialName("dbg_cto3")
  this.setInterfaceAttributes()

}
