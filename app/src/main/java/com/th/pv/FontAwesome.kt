package com.th.pv

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

import android.widget.TextView


class FontAwesome : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        val tf = Typeface.createFromAsset(context.assets, "fa-solid-900.ttf")
        typeface = tf
    }
}