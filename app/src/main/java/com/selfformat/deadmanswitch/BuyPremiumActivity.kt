package com.selfformat.deadmanswitch

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.transaction
import com.android.billingclient.api.*
import com.selfformat.deadmanswitch.base.CustomActivity
import kotlinx.android.synthetic.main.activity_buy_premium.*
import kotlinx.android.synthetic.main.fragment_buy_premium.*

class BuyPremiumActivity : CustomActivity(), PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BuyPremiumActivity"
    }
    private lateinit var billingClient: BillingClient
    private val skuList = listOf("premium")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_premium)
        closeButton.setOnClickListener { onBackPressed() }
        noThanksButton.setOnClickListener { onBackPressed() }
        restorePurchasedItemsButton.setOnClickListener {
            //TODO: implement in-app popup
        }

        setupBillingClient()

        supportFragmentManager.transaction(allowStateLoss = true) {
            add(
                R.id.premiumFrame,
                BuyPremiumFragment.newInstance()
            )
        }
    }

    private fun showBuyPopup(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        billingClient.launchBillingFlow(this, flowParams)
    }

    private fun setupBillingClient() {
        billingClient = BillingClient
            .newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    println("BILLING | startConnection | RESULT OK")
                    loadProduct()
                } else {
                    println("BILLING | startConnection | RESULT: ${billingResult.responseCode}")
                }            }

            override fun onBillingServiceDisconnected() {
                println("BILLING | onBillingServiceDisconnected | DISCONNECTED")
            }
        })
    }

    fun loadProduct() {
        if (billingClient.isReady) {
            val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    println("querySkuDetailsAsync, responseCode: ${billingResult.responseCode}")
                    Log.i(TAG, "SKULIST : $skuDetailsList")
                    noThanksButton.text = skuDetailsList[0].title
                    buyPremiumButton.setOnClickListener {
                        showBuyPopup(skuDetailsList[0])
                    }
                } else {
                    println("Can't querySkuDetailsAsync, responseCode: ${billingResult.responseCode}")
                }
            }
        } else {
            println("Billing Client not ready")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
    }
}
