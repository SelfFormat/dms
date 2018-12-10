package com.example.deadmanswitch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.deadmanswitch.databinding.FragmentBuyPremiumBinding

class BuyPremiumFragment : Fragment() {

    companion object {
        fun newInstance(): BuyPremiumFragment {
            return BuyPremiumFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_buy_premium, container, false)
    }
}
