package com.example.deadmanswitch.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.deadmanswitch.R
import com.example.deadmanswitch.data.SingleFeature
import java.util.*

class FeatureAdapter(context: Context, features: ArrayList<SingleFeature>) :
    ArrayAdapter<SingleFeature>(context, 0, features) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val feature = getItem(position)
        if (view == null) view = LayoutInflater.from(context).inflate(R.layout.single_row_feature, parent, false)
        val featureName = view!!.findViewById<View>(R.id.featureName) as TextView
        featureName.text = feature!!.title
        return view
    }
}