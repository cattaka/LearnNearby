package net.cattaka.android.learnnearby

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import net.cattaka.android.learnnearby.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    lateinit var mBinding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_menu)
        mBinding.activity = this
    }

    fun onClickSimpleListener(v: View) {
        startActivity(Intent(this, SimpleListenerActivity::class.java));
    }

    fun onClickAreaPresumption(v: View) {
        startActivity(Intent(this, AreaPresumptionActivity::class.java));
    }
}