package spinal.lib.tools

import spinal.core._
import spinal.lib._
import example.Config
import scala.collection.mutable.ArrayBuffer

case class StreamToElements() {
    
    def apply(target: Stream[_ <: Data]): Array[(String, Data)] = {

        val payloadElements = if (target.payload.isInstanceOf[Bundle]) {
                val tmp = target.payload.asInstanceOf[Bundle].elements.toArray

                if (tmp.filter({case (name, target) => name == "valid" || name == "ready"}).isEmpty)
                    tmp
                else
                    tmp.map({case (name, target) => (f"payload_${name}", target)})

            } else
                Array(("payload", target.payload))

        payloadElements ++ Array(("valid", target.valid), ("ready", target.ready))
    }
}

object StreamToElements extends StreamToElements {}
