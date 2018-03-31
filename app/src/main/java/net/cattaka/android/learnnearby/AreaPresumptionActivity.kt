package net.cattaka.android.learnnearby

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.nearby.messages.Distance
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import net.cattaka.android.learnnearby.data.EddyStoneSignature
import net.cattaka.android.learnnearby.data.EddyStoneValue
import net.cattaka.android.learnnearby.data.toEddyStoneSignature
import net.cattaka.android.learnnearby.data.toEddyStoneValue
import net.cattaka.android.learnnearby.databinding.ActivityAreaPresumptionBinding

/**
 * Created by cattaka on 18/03/31.
 */
class AreaPresumptionActivity : AppCompatActivity() {
    companion object {
        const val TIMEOUT_INTERVAL_MS = 5000L
    }

    val mMessageListenerForBle: MessageListener = object : MessageListener() {
        override fun onLost(message: Message) {
            super.onLost(message)
            if (Message.MESSAGE_TYPE_EDDYSTONE_UID == message.type) {
                mBeaconHistories.remove(message.toEddyStoneSignature())
                updatePosition()
            }
        }

        override fun onDistanceChanged(message: Message, distance: Distance) {
            super.onDistanceChanged(message, distance)
            if (Message.MESSAGE_TYPE_EDDYSTONE_UID == message.type) {
                val ess = message.toEddyStoneSignature()
                var values = mBeaconHistories[ess]
                if (values == null) {
                    values = mutableListOf()
                    mBeaconHistories[ess] = values
                }
                values.add(distance.toEddyStoneValue(SystemClock.elapsedRealtime()))
                updatePosition()
            }
        }
    }

    lateinit var mBinding: ActivityAreaPresumptionBinding
    val mBeaconHistories = HashMap<EddyStoneSignature, MutableList<EddyStoneValue>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_area_presumption)
        mBinding.activity = this
    }

    fun updatePosition() {
        // Expire old values and calculate average distance
        val currTime = SystemClock.elapsedRealtime()
        val ess2Weights = mutableMapOf<EddyStoneSignature, Double>()
        for ((ess, esvs) in mBeaconHistories) {
            val itr = esvs.iterator()
            var d = 0.0
            var n = 0
            while (itr.hasNext()) {
                val esv = itr.next()
                if ((currTime - esv.eventTime) > TIMEOUT_INTERVAL_MS) {
                    itr.remove()
                } else {
                    d += esv.meters
                    n++
                }
            }
            if (n > 0) {
                ess2Weights[ess] = (d / n)
            }
        }

        // calc center of view position
        var totalWeight = 0.0
        var cx = 0.0
        var cy = 0.0
        for ((ess, weight) in ess2Weights) {
            val view = mBinding.root.findViewWithTag<View>(ess.toTagString()) ?: continue
            cx += (view.top + view.bottom / 2) * weight
            cy += (view.left + view.right / 2) * weight
            totalWeight += weight
        }
        if (totalWeight > 0) {
            cx /= totalWeight
            cy /= totalWeight

            val params = mBinding.viewMarker.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = cx.toInt() - mBinding.viewMarker.width / 2
            params.topMargin = cy.toInt() - mBinding.viewMarker.height / 2
            mBinding.viewMarker.layoutParams = params
        }
    }
}