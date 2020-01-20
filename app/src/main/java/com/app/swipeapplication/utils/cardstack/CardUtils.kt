package com.app.swipeapplication.utils.cardstack

import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import kotlin.math.sqrt

/**
 * @author Shubham
 * 21/1/20
 */

object CardUtils {
    private const val DIRECTION_TOP_LEFT = 0
    private const val DIRECTION_TOP_RIGHT = 1
    private const val DIRECTION_BOTTOM_LEFT = 2
    private const val DIRECTION_BOTTOM_RIGHT = 3
    fun scale(v: View, pixel: Int, gravity: Int) {
        val params = v.layoutParams as RelativeLayout.LayoutParams
        params.leftMargin -= pixel * 3
        params.rightMargin -= pixel * 3
        if (gravity == CardAnimator.TOP) {
            params.topMargin += pixel
        } else {
            params.topMargin -= pixel
        }
        params.bottomMargin -= pixel
        v.layoutParams = params
    }

    fun getMoveParams(v: View, upDown: Int, leftRight: Int): RelativeLayout.LayoutParams {
        val original =
            v.layoutParams as RelativeLayout.LayoutParams
        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(original);
        val params = cloneParams(original)
        params.leftMargin += leftRight
        params.rightMargin -= leftRight
        params.topMargin -= upDown
        params.bottomMargin += upDown
        return params
    }

    fun move(v: View, upDown: Int, leftRight: Int) {
        val params: RelativeLayout.LayoutParams = getMoveParams(v, upDown, leftRight)
        v.layoutParams = params
    }

    fun scaleFrom(
        v: View,
        params: RelativeLayout.LayoutParams,
        pixel: Int,
        gravity: Int
    ): RelativeLayout.LayoutParams {
        var params: RelativeLayout.LayoutParams = params
        Log.d("pixel", "onScroll: $pixel")
        params = cloneParams(params)
        params.leftMargin -= pixel
        params.rightMargin -= pixel
        if (gravity == CardAnimator.TOP) {
            params.topMargin += pixel
        } else {
            params.topMargin -= pixel
        }
        params.bottomMargin -= pixel
        v.layoutParams = params
        return params
    }

    fun moveFrom(
        v: View,
        params: RelativeLayout.LayoutParams,
        leftRight: Int,
        upDown: Int,
        gravity: Int
    ): RelativeLayout.LayoutParams {
        var params: RelativeLayout.LayoutParams = params
        params = cloneParams(params)
        params.leftMargin += leftRight
        params.rightMargin -= leftRight
        if (gravity == CardAnimator.BOTTOM) {
            params.bottomMargin += upDown
            params.topMargin -= upDown
        } else {
            params.bottomMargin -= upDown
            params.topMargin += upDown
        }
        v.layoutParams = params
        return params
    }

    //a copy method for RelativeLayout.LayoutParams for backward compartibility
    fun cloneParams(params: RelativeLayout.LayoutParams): RelativeLayout.LayoutParams {
        val copy =
            RelativeLayout.LayoutParams(params.width, params.height)
        copy.leftMargin = params.leftMargin
        copy.topMargin = params.topMargin
        copy.rightMargin = params.rightMargin
        copy.bottomMargin = params.bottomMargin
        val rules = params.rules
        for (i in rules.indices) {
            copy.addRule(i, rules[i])
        }
        //copy.setMarginStart(params.getMarginStart());
//copy.setMarginEnd(params.getMarginEnd());
        return copy
    }

    fun distance(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Float {
        return sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2).toDouble()).toFloat()
    }

    fun direction(x1: Float, y1: Float, x2: Float, y2: Float): Int {
        return if (x2 > x1) { //RIGHT
            if (y2 > y1) { //BOTTOM
                DIRECTION_BOTTOM_RIGHT
            } else { //TOP
                DIRECTION_TOP_RIGHT
            }
        } else { //LEFT
            if (y2 > y1) { //BOTTOM
                DIRECTION_BOTTOM_LEFT
            } else { //TOP
                DIRECTION_TOP_LEFT
            }
        }
    }
}
