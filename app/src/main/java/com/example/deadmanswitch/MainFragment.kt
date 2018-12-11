package com.example.deadmanswitch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.startActivity


class MainFragment : Fragment() {

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        premiumBar.setOnClickListener {
            activity!!.startActivity<BuyPremiumActivity>()
        }
    }

    fun newFrag() {
        activity!!.supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.frame, BuyPremiumFragment.newInstance()).addToBackStack("BUY_PREMIUM")
        }
    }
}
