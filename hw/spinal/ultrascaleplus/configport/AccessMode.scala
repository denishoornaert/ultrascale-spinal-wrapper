package ultrascaleplus.configport

import spinal.core.Data

sealed case class AccessMode(canRead: Boolean = false, canWrite: Boolean = false) {

    // def canRead(): Boolean = canRead
    // def canWrite(): Boolean = canWrite

    private def getMode(canRead: Boolean, canWrite: Boolean) {
        (canRead, canWrite) match {
            case (true, true) => RW
            case (true, false) => R
            case (false, true) => W
            case (false, false) => NoAccess
        }
    }

    def apply[T <: Data] (that: T) = (that, this)
    def apply[T <: Data, M <: AccessMode] (that: (T, M)) = {
        val (_that, mode) = that

        (_that, getMode(mode.canRead | canRead, mode.canWrite | canWrite))
    }

    override def toString() = f"${if (canRead) 'R' else ""}${if (canWrite) 'W' else ""}"

}

final object RW extends AccessMode(canRead = true, canWrite = true)
final object R extends AccessMode(canRead = true)
final object W extends AccessMode(canWrite = true)
final object NoAccess extends AccessMode()