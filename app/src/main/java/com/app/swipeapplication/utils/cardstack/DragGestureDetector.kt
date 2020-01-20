package com.app.swipeapplication.utils.cardstack

import android.content.Context
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.MotionEventCompat


/**
 * @author Shubham
 * 21/1/20
 */
class DragGestureDetector(
    context: Context?,
    myDragListener: DragListener?
) {
    private val mGestureDetector: GestureDetectorCompat
    private val mListener: DragListener?
    private var mStarted = false
    private var mOriginalEvent: MotionEvent? = null

    interface DragListener {
        fun onDragStart(
            e1: MotionEvent?, e2: MotionEvent?, distanceX: Float,
            distanceY: Float
        ): Boolean

        fun onDragContinue(
            e1: MotionEvent?, e2: MotionEvent?, distanceX: Float,
            distanceY: Float
        ): Boolean

        fun onDragEnd(e1: MotionEvent?, e2: MotionEvent?): Boolean
        fun onTapUp(): Boolean
    }

    fun onTouchEvent(event: MotionEvent?) {
        mGestureDetector.onTouchEvent(event)
        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_UP -> {
                Log.d(DEBUG_TAG, "Action was UP")
                if (mStarted) {
                    mListener!!.onDragEnd(mOriginalEvent, event)
                }
                mStarted = false
                //need to set this, quick tap will not generate drap event, so the
//originalEvent may be null for case action_up
//which lead to null pointer
                mOriginalEvent = event
            }
            MotionEvent.ACTION_DOWN -> mOriginalEvent = event
        }
    }

    internal inner class MyGestureListener : SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent, e2: MotionEvent, distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (mListener == null) return true
            if (!mStarted) {
                mListener.onDragStart(e1, e2, distanceX, distanceY)
                mStarted = true
            } else {
                mListener.onDragContinue(e1, e2, distanceX, distanceY)
            }
            mOriginalEvent = e1
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return mListener!!.onTapUp()
        }
    }

    companion object {
        var DEBUG_TAG = "DragGestureDetector"
    }

    init {
        mGestureDetector = GestureDetectorCompat(context, MyGestureListener())
        mListener = myDragListener
    }
}