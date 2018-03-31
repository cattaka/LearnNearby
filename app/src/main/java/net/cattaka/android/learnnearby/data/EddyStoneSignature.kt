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
        val eventId = (bs[13].toInt() shl 24) or (bs[12].toInt() shl 16) or (bs[11].toInt() shl 8) or (bs[10].toInt())
        val beaconId = (bs[15].toInt() shl 8) or (bs[14].toInt())
        return EddyStoneSignature(eventId, beaconId)
    } else {
        // error case
        return EddyStoneSignature(0, 0)
    }
}
