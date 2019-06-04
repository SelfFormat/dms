package com.selfformat.deadmanswitch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.selfformat.deadmanswitch.components.FeatureAdapter
import com.selfformat.deadmanswitch.data.SingleFeature

class BuyPremiumFragment : Fragment() {
    companion object {
        fun newInstance(): BuyPremiumFragment {
            return BuyPremiumFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
            R.layout.fragment_buy_premium, container,
            false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arrayOfFeatures = ArrayList<SingleFeature>()
        arrayOfFeatures.add(SingleFeature(getString(R.string.feature_no_ads)))
        arrayOfFeatures.add(SingleFeature(getString(R.string.feature_custom_alarms)))
        arrayOfFeatures.add(SingleFeature(getString(R.string.feature_emergency_sms)))
        arrayOfFeatures.add(SingleFeature(getString(R.string.feature_dark_mode)))
        arrayOfFeatures.add(SingleFeature(getString(R.string.feature_widget)))
        val listView = view.findViewById<ListView>(R.id.listOfFeatures)
        listView.adapter = FeatureAdapter(view.context, arrayOfFeatures)
    }
}
