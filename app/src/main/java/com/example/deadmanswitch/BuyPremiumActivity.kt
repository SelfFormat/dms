package com.example.deadmanswitch

import android.os.Bundle
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.activity_buy_premium.*

class BuyPremiumActivity : CustomStatusBarActivity() {

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
}
