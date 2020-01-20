package com.app.swipeapplication.utils.cardstack

import android.util.Log
import com.app.swipeapplication.utils.cardstack.CardStack.CardEventListener

/**
 * @author Shubham
 * 21/1/20
 */
class DefaultStackEventListener(i: Int) : CardEventListener {
    private val mThreshold: Float = i.toFloat()
    override fun swipeEnd(section: Int, distance: Float): Boolean {
        Log.d("rae", "swipeEnd:$section-$distance")
        return distance > mThreshold
    }

    override fun swipeStart(section: Int, distance: Float): Boolean {
        Log.d("rae", "swipeStart:$section-$distance")
        return false
    }

    override fun swipeContinue(
        section: Int,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d("rae", "swipeContinue:$section-$distanceX-$distanceY")
        return false
    }

    override fun discarded(mIndex: Int, direction: Int) {
        Log.d("rae", "discarded:$mIndex-$direction")
    }

    override fun topCardTapped() {
        Log.d("rae", "topCardTapped")
    }

}