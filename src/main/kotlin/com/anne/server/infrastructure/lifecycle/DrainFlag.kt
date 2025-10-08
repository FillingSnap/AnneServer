package com.anne.server.infrastructure.lifecycle

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
class DrainFlag {

    private final val drainFlag = AtomicBoolean(false)

    fun setDrainFlag(flag: Boolean) =
        drainFlag.set(flag)

    fun isDraining(): Boolean = drainFlag.get()

}