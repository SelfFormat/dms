package com.selfformat.deadmanswitch.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.selfformat.deadmanswitch.R
import com.selfformat.deadmanswitch.data.SingleFeature
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