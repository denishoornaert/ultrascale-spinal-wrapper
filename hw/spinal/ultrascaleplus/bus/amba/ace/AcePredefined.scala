package ultrascaleplus.bus.amba.ace


import spinal.core._
import spinal.lib._


import ultrascaleplus.parameters._


object ACE extends AceMappedInstanceTemplate() {

  override val name = "ace"

  override val domain = "fpd"

  override val id = ""

  override val config = AceConfig(
    addressWidth              =    44,
    dataWidth                 =   128,
    idWidth                   =     6,
    useId                     =  true,
    useRegion                 =  true,
    useBurst                  =  true,
    useLock                   =  true,
    useCache                  =  true,
    useSize                   =  true,
    useQos                    =  true,
    useLen                    =  true,
    useLast                   =  true,
    useResp                   =  true,
    useProt                   =  true,
    useStrb                   =  true,
    useAllStrb                =  true,
    forceAxi4Len              =  true,
    useDomain                 =  true,
    useSnoop                  =  true,
    useUnique                 = false,
    useBar                    =  true,
    useAck                    =  true,
    arUserWidth               =    16,
    awUserWidth               =    16,
    rUserWidth                =     0,
    wUserWidth                =     0,
    bUserWidth                =     0,
    readIssuingCapability     =    32,
    writeIssuingCapability    =    32,
    combinedIssuingCapability =    64,
    readDataReorderingDepth   =    -1
  )

}
