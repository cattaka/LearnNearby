package net.cattaka.android.learnnearby.data

import com.google.android.gms.nearby.messages.Distance

/**
 * Created by cattaka on 18/03/31.
 */
data class EddyStoneValue(
        val accuracy: Int,
        val meters: Double,
        val eventTime: Long
) {
}

fun Distance.toEddyStoneValue(currTime: Long): EddyStoneValue {
    return EddyStoneValue(this.accuracy, this.meters, currTime)
}
