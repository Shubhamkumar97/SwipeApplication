package com.app.swipeapplication.utils.animation


import android.animation.TypeEvaluator
import android.widget.RelativeLayout
import com.app.swipeapplication.utils.cardstack.CardUtils.cloneParams


class RelativeLayoutParamsEvaluator :
    TypeEvaluator<RelativeLayout.LayoutParams> {
    override fun evaluate(
        fraction: Float, start: RelativeLayout.LayoutParams,
        end: RelativeLayout.LayoutParams
    ): RelativeLayout.LayoutParams {
        val result = cloneParams(start)
        result.leftMargin += ((end.leftMargin - start.leftMargin) * fraction).toInt()
        result.rightMargin += ((end.rightMargin - start.rightMargin) * fraction).toInt()
        result.topMargin += ((end.topMargin - start.topMargin) * fraction).toInt()
        result.bottomMargin += ((end.bottomMargin - start.bottomMargin) * fraction).toInt()
        return result
    }
}