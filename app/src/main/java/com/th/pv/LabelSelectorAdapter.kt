package com.th.pv

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.th.pv.data.PVData

class LabelSelectorAdapter(
    private val context: Activity,
    private val pvData: PVData
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var labelsList = mutableListOf<String>()

    init {
        for (label in pvData.labels.keys){
            labelsList.add(label)
        }
        labelsList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
    }

    override fun getCount(): Int {
        return labelsList.size
    }

    override fun getItem(position: Int): Any {
        return pvData.labels[labelsList[position]]!!
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null)
            view = layoutInflater.inflate(R.layout.label_selector_list_item, parent, false)

        val labelName = view!!.findViewById<TextView>(R.id.labelName)

        val label = pvData.labels[labelsList[position]]!!
        labelName.text = label.name

        return view
    }
}