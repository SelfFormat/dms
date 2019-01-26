package com.example.deadmanswitch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList

class FeatureAdapter(context: Context, features: ArrayList<SingleFeature>) :
    ArrayAdapter<SingleFeature>(context, 0, features) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val feature = getItem(position)
        if (convertView == null) convertView = LayoutInflater.from(context).inflate(R.layout.single_row_feature, parent, false)
        val featureName = convertView!!.findViewById<View>(R.id.featureName) as TextView
        featureName.text = feature!!.title
        return convertView
    }
}