package com.app.swipeapplication.utils.cardstack


import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import com.app.swipeapplication.utils.animation.AnimatorListenerAdapter
import com.app.swipeapplication.utils.animation.RelativeLayoutParamsEvaluator
import com.app.swipeapplication.utils.cardstack.CardUtils.cloneParams
import com.app.swipeapplication.utils.cardstack.CardUtils.getMoveParams
import com.app.swipeapplication.utils.cardstack.CardUtils.move
import com.app.swipeapplication.utils.cardstack.CardUtils.moveFrom
import com.app.swipeapplication.utils.cardstack.CardUtils.scale
import com.app.swipeapplication.utils.cardstack.CardUtils.scaleFrom
import kotlin.math.abs

/**
 * @author Shubham
 * 21/1/20
 */

class CardAnimator(
    var mCardCollection: ArrayList<View>,
    private val mBackgroundColor: Int,
    private var mStackMargin: Int
) {
    private var mRotation: Float = 0.toFloat()
    private var mLayoutsMap: HashMap<View, LayoutParams>? = null
    private val mRemoteLayouts = arrayOfNulls<LayoutParams>(4)
    private var baseLayout: LayoutParams? = null
    private var mGravity = BOTTOM
    var isEnableRotation: Boolean = false // Whether to allow rotation

    private val topView: View
        get() = mCardCollection[mCardCollection.size - 1]

    init {
        setup()
    }

    private fun setup() {
        mLayoutsMap = HashMap()

        for (v in mCardCollection) {
            //setup basic layout
            val params = v.layoutParams as LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            params.width = LayoutParams.MATCH_PARENT
            params.height = LayoutParams.WRAP_CONTENT

            if (mBackgroundColor != -1) {
                v.setBackgroundColor(mBackgroundColor)
            }

            v.layoutParams = params
        }

        baseLayout = mCardCollection[0].layoutParams as LayoutParams?
        baseLayout = cloneParams(baseLayout!!)

    }

    fun initLayout() {
        val size = mCardCollection.size
        for (v in mCardCollection) {
            var index = mCardCollection.indexOf(v)
            if (index != 0) {
                index -= 1
            }
            val params = cloneParams(baseLayout!!)
            v.layoutParams = params

            scale(v, -(size - index - 1) * 5, mGravity)

            val margin = index * mStackMargin
            move(v, if (mGravity == TOP) -margin else margin, 0)
            v.rotation = 0F

            val paramsCopy = cloneParams(v.layoutParams as LayoutParams)
            mLayoutsMap!![v] = paramsCopy
        }

        setupRemotes()
    }

    /**
     * Set direction，Support、under.
     * Called after setting[.initLayout] To reinitialize the layout
     *
     * @param gravity [.TOP] Improvement [.BOTTOM] down，Defaults
     */
    fun setGravity(gravity: Int) {
        mGravity = gravity
    }

    private fun setupRemotes() {
        val topView = topView
        mRemoteLayouts[0] = getMoveParams(topView, REMOTE_DISTANCE, -REMOTE_DISTANCE)
        mRemoteLayouts[1] = getMoveParams(topView, REMOTE_DISTANCE, REMOTE_DISTANCE)
        mRemoteLayouts[2] = getMoveParams(topView, -REMOTE_DISTANCE, -REMOTE_DISTANCE)
        mRemoteLayouts[3] = getMoveParams(topView, -REMOTE_DISTANCE, REMOTE_DISTANCE)

    }

    private fun moveToBack(child: View) {
        val parent = child.parent as ViewGroup
        parent.removeView(child)
        parent.addView(child, 0)
    }


    private fun reorder() {

        val temp = topView
        //RelativeLayout.LayoutParams tempLp = mLayoutsMap.get(mCardCollection.get(0));
        //mLayoutsMap.put(temp,tempLp);
        moveToBack(temp)

        for (i in mCardCollection.size - 1 downTo 1) {
            //View next = mCardCollection.get(i);
            //RelativeLayout.LayoutParams lp = mLayoutsMap.get(next);
            //mLayoutsMap.remove(next);
            val current = mCardCollection[i - 1]

            //current replace next
            mCardCollection[i] = current
            //mLayoutsMap.put(current,lp);

        }

        mCardCollection[0] = temp
    }

    // Destroy card
    fun discard(direction: Int, al: AnimatorListener?) {
        val animatorSet = AnimatorSet()
        val aCollection = ArrayList<Animator>()


        val topView = topView
        val topParams = topView.layoutParams as LayoutParams
        val layout = cloneParams(topParams)
        val discardAnim = ValueAnimator.ofObject(
            RelativeLayoutParamsEvaluator(),
            layout,
            mRemoteLayouts[direction]
        )

        discardAnim.addUpdateListener { value ->
            topView.layoutParams = value.animatedValue as LayoutParams
        }

        discardAnim.duration = 250
        aCollection.add(discardAnim)

        for (i in 0 until mCardCollection.size) {
            val v = mCardCollection[i]

            if (v === topView) continue
            val nv = mCardCollection[i + 1]
            val layoutParams = v.layoutParams as LayoutParams
            val endLayout = cloneParams(layoutParams)
            val layoutAnim = ValueAnimator.ofObject(
                RelativeLayoutParamsEvaluator(), endLayout,
                mLayoutsMap!![nv]
            )
            layoutAnim.duration = 250
            layoutAnim.addUpdateListener { value ->
                v.layoutParams = value.animatedValue as LayoutParams
            }
            aCollection.add(layoutAnim)
        }

        animatorSet.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(arg0: Animator?) {
                reorder()
                al?.onAnimationEnd(arg0)
                mLayoutsMap = HashMap()
                for (v in mCardCollection) {
                    val params = v.layoutParams as LayoutParams
                    val paramsCopy = cloneParams(params)
                    mLayoutsMap!![v] = paramsCopy
                }
            }

        })


        animatorSet.playTogether(aCollection)
        animatorSet.start()
    }

    /**
     * Restore card position
     */
    fun reverse(e1: MotionEvent, e2: MotionEvent) {
        val topView = topView
        val rotationAnim = ValueAnimator.ofFloat(mRotation, 0f)
        rotationAnim.duration = 250
        rotationAnim.addUpdateListener { v ->
            topView.rotation = (v.animatedValue as Float).toFloat()
        }

        rotationAnim.start()

        for (v in mCardCollection) {
            val layoutParams = v.layoutParams as LayoutParams
            val endLayout = cloneParams(layoutParams)
            val layoutAnim = ValueAnimator.ofObject(
                RelativeLayoutParamsEvaluator(), endLayout,
                mLayoutsMap!![v]
            )
            layoutAnim.duration = 100
            layoutAnim.addUpdateListener { value ->
                v.layoutParams = value.animatedValue as LayoutParams
            }
            layoutAnim.start()
        }

    }

    fun drag(
        e1: MotionEvent, e2: MotionEvent, distanceX: Float,
        distanceY: Float
    ) {

        val topView = topView
        val xDiff = (e2.rawX - e1.rawX).toInt()
        val yDiff = (e2.rawY - e1.rawY).toInt()
        val rotationCoefficient = 20f
        val layoutParams = topView.layoutParams as LayoutParams
        val topViewLayouts = mLayoutsMap!![topView]
        layoutParams.leftMargin = topViewLayouts!!.leftMargin + xDiff
        layoutParams.rightMargin = topViewLayouts.rightMargin - xDiff
        layoutParams.topMargin = topViewLayouts.topMargin + yDiff
        layoutParams.bottomMargin = topViewLayouts.bottomMargin - yDiff

        if (isEnableRotation) {
            mRotation = xDiff / rotationCoefficient
            topView.rotation = mRotation
            topView.layoutParams = layoutParams
        }

        //animate secondary views.
        for (v in mCardCollection) {
            val index = mCardCollection.indexOf(v)
            if (v !== topView && index != 0) {
                val l = scaleFrom(
                    v,
                    mLayoutsMap!![v]!!,
                    (abs(xDiff) * 0.05).toInt(), mGravity
                )
                moveFrom(
                    v,
                    l,
                    0,
                    (abs(xDiff).toDouble() * index.toDouble() * 0.05).toInt(), mGravity

                )
            }
        }
    }

    fun setStackMargin(margin: Int) {
        mStackMargin = margin
    }

    companion object {
        private val DEBUG_TAG = "CardAnimator"

        const val TOP = 48
        const val BOTTOM = 80


        private const val REMOTE_DISTANCE = 1000
    }
}