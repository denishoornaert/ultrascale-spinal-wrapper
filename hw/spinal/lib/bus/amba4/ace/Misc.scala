package spinal.lib.bus.amba4.ace

import spinal.lib.bus.amba4.axi._


object AceUtilities {

    private def toAxiConfig(config: AceConfig) = config.axi4config

    private def toAceConfig(config: Axi4Config) = AceConfig(config, config.addressWidth, config.dataWidth, false)

    implicit class AceRich(ace: Ace) {
        def toAxi(): Axi4 = {
            val axi = new Axi4(toAxiConfig(ace.config))
            
            axi.ar.assignSomeByName(ace.ar)
            axi.r.assignSomeByName(ace.r)

            axi.aw.assignSomeByName(ace.aw)
            axi.w.assignSomeByName(ace.w)
            axi.b.assignSomeByName(ace.b)

            axi
        }
    }
}