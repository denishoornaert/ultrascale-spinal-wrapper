package kv260.interface

import spinal.lib.bus.amba4.ace.Ace
import spinal.core.Data
import spinal.lib.IMasterSlave
import spinal.core.HardType

trait XilinxMS {
  
}

object master {
    def apply[T <: Data with IMasterSlave](data: HardType[T]): T = {
        spinal.lib.master(data)
    }
}
