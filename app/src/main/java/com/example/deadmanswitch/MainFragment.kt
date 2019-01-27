package com.example.deadmanswitch

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.transaction
import kotlinx.android.synthetic.main.card_tone_picker.*
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.startActivity
import android.animation.ObjectAnimator
import kotlinx.android.synthetic.main.card_emergency.*

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
        premiumBarMain.setOnClickListener { activity?.startActivity<BuyPremiumActivity>() }
        editEmergency.setOnClickListener { activity?.startActivity<EmergencySmsActivity>() }
        chooseToneButton.setOnClickListener { openSoundPicker(view) }
        currentAlarmName.setOnClickListener { openSoundPicker(view) }

        scrollableMainLayout.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                val animation = ObjectAnimator.ofFloat(fab, "translationY", 400f)
                animation.duration = 100
                animation.start()
            } else {
                val animation = ObjectAnimator.ofFloat(fab, "translationY", 0f)
                animation.duration = 100
                animation.start()
            }
        })
    }

    private fun openSoundPicker(view: View) {
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AlertDialog.Builder(view.context)
        } else {
            AlertDialog.Builder(view.context)
        }

        builder.setTitle("Credits")
            .setMessage("Pick favorite")
            .setPositiveButton(android.R.string.yes) { _, _ -> }
            .setNegativeButton(android.R.string.no) { _, _ -> }
            .setIcon(R.drawable.ic_check_green_24dp)
            .setView(R.layout.single_row_feature) // you could specify your inner layout
            .show()
    }

    fun newFrag(fragment: Fragment) {
        activity!!.supportFragmentManager.transaction(allowStateLoss = true) {
            replace(R.id.mainFrame, fragment).addToBackStack("BUY_PREMIUM")
        }
    }
}
