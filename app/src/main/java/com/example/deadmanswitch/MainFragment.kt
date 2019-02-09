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
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainFragment : Fragment() {

    private val valueMin = 5
    private val valueMax = 10
    private var alarm = Alarm()
    private val randomTime = Random()

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
        editEmergency.setOnClickListener { newFrag(EmergencySmsFragment.newInstance()) }
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

        fab.setOnClickListener {
            val random = randomTime()
            val time = System.currentTimeMillis() + random
            val sum = "Alarm" + random / 1000 + " seconds"
            showSnackBar(sum)
            alarm.startAlarm(activity!!.applicationContext, time, Alarm.State.ON)
            fab.setText(R.string.turn_off)
            (activity as MainActivity).notification("Alarm is set", "no body")
        }
    }

    private fun openSoundPicker(view: View) {
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AlertDialog.Builder(view.context)
        } else {
            AlertDialog.Builder(view.context)
        }

        builder.setTitle("Credits")
            .setMessage("Pick favorite")
            .setPositiveButton(android.R.string.yes) { _, _ -> (activity as MainActivity).smsSendMessage() }
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

    private fun randomTime(): Int {
        val initialTime = valueMin * 1000
        val generated = randomTime.nextInt(valueMax + 1 - valueMin) * 1000
        Log.i("init: $initialTime", ", gen: $generated")
        return generated + initialTime
    }

    private fun showSnackBar(text: String) {
        Snackbar.make(view!!.findViewById(com.example.deadmanswitch.R.id.scrollableMainLayout), text, Snackbar.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        val summary = "${(activity as MainActivity).getEmergencyContact().name ?: getString(R.string.sample_contact_name)} (${(activity as MainActivity).getEmergencyContact().number ?: getString(R.string.sample_contact_name)})"
        val textSummary = (activity as MainActivity).getEmergencyContact().message ?: getString(R.string.message)
        contactSummary.text = summary
        messageSummary.text = textSummary
        activity?.toolbarTitle?.text = resources.getString(R.string.app_name)
    }
}
