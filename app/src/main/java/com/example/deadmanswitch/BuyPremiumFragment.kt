package com.example.deadmanswitch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.deadmanswitch.components.FeatureAdapter
import com.example.deadmanswitch.data.SingleFeature

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
        arrayOfFeatures.add(SingleFeature("No ads"))
        arrayOfFeatures.add(SingleFeature("More alarm sounds"))
        arrayOfFeatures.add(SingleFeature("Emergency sms"))
        arrayOfFeatures.add(SingleFeature("Dark mode"))

        val listView = view.findViewById<ListView>(R.id.listOfFeatures)
        listView.adapter = FeatureAdapter(view.context, arrayOfFeatures)
    }
}
