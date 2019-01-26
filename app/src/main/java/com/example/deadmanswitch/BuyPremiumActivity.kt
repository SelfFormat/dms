package com.example.deadmanswitch

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_buy_premium.*

class BuyPremiumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_premium)
        setUpStatusBarAppearance()
        closeButton.setOnClickListener { onBackPressed() }
        noThanksButton.setOnClickListener { onBackPressed() }
        supportFragmentManager.transaction(allowStateLoss = true) {
            add(R.id.premiumFrame, BuyPremiumFragment.newInstance())
        }
    }

    private fun setUpStatusBarAppearance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.WHITE
        }
    }
}
