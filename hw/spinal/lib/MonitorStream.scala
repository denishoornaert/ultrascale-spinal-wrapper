/**
 * Author:    Francesco Ciraolo
 * Created:   13.10.2025
 * 
 * Placeholder for MonitorStream.scala as a interface with an ad-hoc WIP library
 * 
 * Note: some API could change
 * 
 **/
package spinal.lib

import spinal.core._

trait IMonitor {}

object MonitorStream {
	def apply[T <: Data](payloadType: HardType[T]): MonitorStream[T] = new MonitorStream(payloadType)
}

class MonitorStream[T <: Data](val payloadType :  HardType[T]) extends Bundle with IMonitor with DataCarrier[T] {

	val valid = Bool()
	val ready = Bool()
	val payload = payloadType()

	/**
	  * Direction is input for all lines.
	  */
	in(valid, ready, payload)

	override def clone: MonitorStream[T] = MonitorStream(payloadType)
	
	override def fire = valid && ready
	
	override def freeRun(): this.type = this

	def toFlow: Flow[T] = {
		val flow = Flow(payloadType())
		flow.valid := fire
		flow.payload := payload
		flow.setCompositeName(this, "toFlow", true)
		flow
	}

}
