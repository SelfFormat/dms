package com.selfformat.deadmanswitch

import android.os.Bundle
import androidx.fragment.app.transaction
import com.selfformat.deadmanswitch.base.CustomActivity
import kotlinx.android.synthetic.main.activity_buy_premium.*

class BuyPremiumActivity : CustomActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_premium)

        closeButton.setOnClickListener { onBackPressed() }
        noThanksButton.setOnClickListener { onBackPressed() }
        restorePurchasedItemsButton.setOnClickListener {
            //TODO: run buypremium fragment/activity
        }

        supportFragmentManager.transaction(allowStateLoss = true) {
            add(
                R.id.premiumFrame,
                BuyPremiumFragment.newInstance()
            )
        }
    }
}
