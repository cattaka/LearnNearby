package net.cattaka.android.learnnearby.data

import com.google.android.gms.nearby.messages.Message

/**
 * Created by cattaka on 18/03/31.
 */
data class EddyStoneSignature(val eventId: Int, val beaconId: Int) {

    fun toTagString(): String {
        return "ESS_TAG_%d_%d".format(eventId, beaconId)
    }
}

fun Message.toEddyStoneSignature(): EddyStoneSignature {
    val bs = this.content
    if (bs.size >= 6) {
        val eventId = (bs[3].toInt() shl 24) or (bs[2].toInt() shl 16) or (bs[1].toInt() shl 8) or (bs[0].toInt())
        val beaconId = (bs[5].toInt() shl 8) or (bs[4].toInt())
        return EddyStoneSignature(eventId, beaconId)
    } else {
        // error case
        return EddyStoneSignature(0, 0)
    }
}
