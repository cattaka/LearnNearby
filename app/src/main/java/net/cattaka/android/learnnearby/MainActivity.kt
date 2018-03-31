package net.cattaka.android.learnnearby

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.CompoundButton
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.*
import net.cattaka.android.learnnearby.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    lateinit var mEddystoneUidNamespace: String
    lateinit var mMessageFilter: MessageFilter
    val mMessageListenerForBle: MessageListener = object : MessageListener() {
        override fun onFound(message: Message) {
            super.onFound(message)
            writeLog("onFound: " + message.content.toString(Charsets.UTF_8));
        }

        override fun onLost(message: Message) {
            super.onLost(message)
            writeLog("onLost: " + message.content.toString(Charsets.UTF_8));
        }

        override fun onDistanceChanged(message: Message, distance: Distance) {
            super.onDistanceChanged(message, distance)
            writeLog("onDistanceChanged: " + message.content.toString(Charsets.UTF_8) + " : " + distance);
        }

        override fun onBleSignalChanged(message: Message, bleSignal: BleSignal) {
            super.onBleSignalChanged(message, bleSignal)
            writeLog("onBleSignalChanged" +
                    ": " + message.content.toString(Charsets.UTF_8) + " : " + bleSignal);
        }
    }
    val mMessageListener: MessageListener = object : MessageListener() {
        override fun onFound(message: Message) {
            super.onFound(message)
            writeLog("onFound: " + message.content.toString(Charsets.UTF_8));
        }

        override fun onLost(message: Message) {
            super.onLost(message)
            writeLog("onLost: " + message.content.toString(Charsets.UTF_8));
        }
    }

    val mMessage = Message(Build.DEVICE.toByteArray(), Message.MESSAGE_TYPE_EDDYSTONE_UID)
    lateinit var mBinding: ActivityMainBinding
    lateinit var mMessagesClient: MessagesClient;
    var mIsPublishing: Boolean = false
    var mIsSubscribe: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mEddystoneUidNamespace = getString(R.string.eddystone_uid_namespace)
        mMessageFilter = MessageFilter.Builder()
                .includeEddystoneUids(mEddystoneUidNamespace, null /* any instance */)
                .build()

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.activity = this

        mMessagesClient = Nearby.getMessagesClient(this);
    }

    override fun onStart() {
        super.onStart()
        mBinding.text.text = ""
        updatePubSub()
    }

    override fun onStop() {
        super.onStop()
        if (mIsPublishing) {
            mIsPublishing = false
            mMessagesClient.unpublish(mMessage)
        }
        if (mIsSubscribe) {
            mIsSubscribe = false
            mMessagesClient.unsubscribe(mMessageListener)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        updatePubSub()
    }

    fun updatePubSub() {
        if (mIsPublishing != mBinding.checkPublish.isChecked) {
            mIsPublishing = mBinding.checkPublish.isChecked
            if (mIsPublishing) {
                val publishOptions = PublishOptions.Builder()
//                        .setStrategy(Strategy.BLE_ONLY)
                        .build()
                mMessagesClient.publish(mMessage, publishOptions)
            } else {
                mMessagesClient.unpublish(mMessage)
            }
        }
        if (mIsSubscribe != mBinding.checkSubscribe.isChecked) {
            mIsSubscribe = mBinding.checkSubscribe.isChecked
            if (mIsSubscribe) {
                val subscribeOptions = SubscribeOptions.Builder()
                        .setFilter(mMessageFilter)
                        .setStrategy(Strategy.BLE_ONLY)
                        .build()
                mMessagesClient.subscribe(mMessageListenerForBle, subscribeOptions)
                mMessagesClient.subscribe(mMessageListener)
            } else {
                mMessagesClient.unsubscribe(mMessageListenerForBle)
                mMessagesClient.unsubscribe(mMessageListener)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun writeLog(str: String) {
        mBinding.text.text = str + "\n" + mBinding.text.text
    }
}
