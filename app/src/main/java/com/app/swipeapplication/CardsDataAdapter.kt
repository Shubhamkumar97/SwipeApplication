package com.app.swipeapplication

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CardsDataAdapter(context: Context?) :
    ArrayAdapter<String?>(context!!, R.layout.card_content) {


    override fun getView(position: Int, contentView: View?, parent: ViewGroup
    ): View {
        val v = contentView!!.findViewById<View>(R.id.content) as TextView
        v.text = getItem(position)
        return contentView
    }
}