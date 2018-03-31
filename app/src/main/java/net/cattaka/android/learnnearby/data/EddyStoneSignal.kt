package net.cattaka.android.learnnearby.data

import com.google.android.gms.nearby.messages.BleSignal

/**
 * Created by cattaka on 18/03/31.
 */
data class EddyStoneSignal(
        val rssi: Int,
        val txPower: Int,
        val eventTime: Long
) {
}

fun BleSignal.toEddyStoneSignal(currTime: Long): EddyStoneSignal {
    return EddyStoneSignal(this.rssi, this.txPower, currTime)
}
