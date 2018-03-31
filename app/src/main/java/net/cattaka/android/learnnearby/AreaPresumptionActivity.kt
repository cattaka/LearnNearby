package net.cattaka.android.learnnearby

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import net.cattaka.android.learnnearby.data.*
import net.cattaka.android.learnnearby.databinding.ActivityAreaPresumptionBinding
import kotlin.math.max

/**
 * Created by cattaka on 18/03/31.
 */
class AreaPresumptionActivity : AppCompatActivity() {
    companion object {
        const val TIMEOUT_INTERVAL_MS = 5000L
    }

    lateinit var mEddystoneUidNamespace: String

    val mMessageListenerForBle: MessageListener = object : MessageListener() {
        override fun onLost(message: Message) {
            super.onLost(message)
            if (Message.MESSAGE_TYPE_EDDYSTONE_UID == message.type) {
                mBeaconHistories[message.toEddyStoneSignature()]?.clear()
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

        override fun onBleSignalChanged(message: Message?, signal: BleSignal?) {
            super.onBleSignalChanged(message, signal)
            if (message != null && signal != null) {
                mSignals[message.toEddyStoneSignature()] = signal.toEddyStoneSignal(SystemClock.elapsedRealtime())
                updatePosition()
            }
        }
    }

    lateinit var mBinding: ActivityAreaPresumptionBinding
    val mBeaconHistories = HashMap<EddyStoneSignature, MutableList<EddyStoneValue>>()
    val mSignals = HashMap<EddyStoneSignature, EddyStoneSignal>()
    lateinit var mMessagesClient: MessagesClient
    lateinit var mMessageFilter: MessageFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_area_presumption)
        mBinding.activity = this

        mEddystoneUidNamespace = getString(R.string.eddystone_uid_namespace)
        mMessageFilter = MessageFilter.Builder()
                .includeEddystoneUids(mEddystoneUidNamespace, null /* any instance */)
                .build()
        mMessagesClient = Nearby.getMessagesClient(this)
    }

    override fun onStart() {
        super.onStart()
        val subscribeOptions = SubscribeOptions.Builder()
                .setFilter(mMessageFilter)
                .setStrategy(Strategy.BLE_ONLY)
                .build()
        mMessagesClient.subscribe(mMessageListenerForBle, subscribeOptions)
    }

    override fun onStop() {
        super.onStop()
        mMessagesClient.unsubscribe(mMessageListenerForBle)
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
            var signal = mSignals[ess]
            val textView = mBinding.root.findViewWithTag<View>(ess.toTagString() + "_DISTANCE")
            if (signal != null && textView is TextView) {
                textView.text = "distance = %02f m\nrssi=%d\ntxPower=%d"
                        .format(d / n, signal.rssi, signal.txPower)
            }
        }

        // calc center of view position
        var maxWeight = 0.0
        for ((_, weight) in ess2Weights) {
            maxWeight = max(weight, maxWeight)
        }
        var totalWeight = 0.0
        var cx = 0.0
        var cy = 0.0
        for ((ess, weight) in ess2Weights) {
            val view = mBinding.root.findViewWithTag<View>(ess.toTagString()) ?: continue
            val w = (maxWeight - weight)
            totalWeight += w
            cx += ((view.left + view.right) / 2) * w
            cy += ((view.top + view.bottom) / 2) * w
        }

        if (totalWeight > 0) {
            cx /= totalWeight
            cy /= totalWeight

            val params = mBinding.viewMarker.layoutParams as ConstraintLayout.LayoutParams
            params.leftMargin = cx.toInt() - mBinding.viewMarker.width / 2
            params.topMargin = cy.toInt() - mBinding.viewMarker.height / 2
            mBinding.viewMarker.layoutParams = params
        }
    }
}