package com.app.swipeapplication.utils.cardstack

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.app.swipeapplication.utils.animation.RelativeLayoutParamsEvaluator
import com.app.swipeapplication.utils.cardstack.CardUtils.cloneParams
import com.app.swipeapplication.utils.cardstack.CardUtils.getMoveParams
import com.app.swipeapplication.utils.cardstack.CardUtils.move
import com.app.swipeapplication.utils.cardstack.CardUtils.moveFrom
import com.app.swipeapplication.utils.cardstack.CardUtils.scaleFrom
import java.util.*


class CardAnimator(
    var mCardCollection: ArrayList<View>,
    private val mBackgroundColor: Int,
    private var mStackMargin: Int
) {
    private var mRotation = 0f
    private var mLayoutsMap: HashMap<View, RelativeLayout.LayoutParams>? =
        null
    private val mRemoteLayouts =
        arrayOfNulls<RelativeLayout.LayoutParams>(4)
    private var baseLayout: RelativeLayout.LayoutParams? = null
    private var mGravity = BOTTOM
    var isEnableRotation // 是否允许旋转
            = false

    private fun setup() {
        mLayoutsMap = HashMap()
        for (v in mCardCollection) { //setup basic layout
            val params =
                v.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT
            if (mBackgroundColor != -1) {
                v.setBackgroundColor(mBackgroundColor)
            }
            v.layoutParams = params
        }
        baseLayout = mCardCollection[0].layoutParams as RelativeLayout.LayoutParams
        baseLayout = cloneParams(baseLayout!!)
    }

    fun initLayout() {
        val size = mCardCollection.size
        for (v in mCardCollection) {
            var index = mCardCollection.indexOf(v)
            if (index != 0) {
                index -= 1
            }
            val params: RelativeLayout.LayoutParams = cloneParams(baseLayout!!)
            v.layoutParams = params
            val margin = index * mStackMargin
            move(v, if (mGravity == TOP) -margin else margin, 0)
            v.rotation = 0f
            val paramsCopy: RelativeLayout.LayoutParams =
                cloneParams(v.layoutParams as RelativeLayout.LayoutParams)
            mLayoutsMap!![v] = paramsCopy
        }
        setupRemotes()
    }

    /**
     * 设置方向，支持上、下。
     * 设置后调用[.initLayout] 来重新初始化布局
     *
     * @param gravity [.TOP] 向上 [.BOTTOM] 向下，默认值
     */
    fun setGravity(gravity: Int) {
        mGravity = gravity
    }

    private fun setupRemotes() {
        val topView = topView
        mRemoteLayouts[0] = getMoveParams(
            topView,
            REMOTE_DISTANCE,
            -REMOTE_DISTANCE
        )
        mRemoteLayouts[1] = getMoveParams(
            topView,
            REMOTE_DISTANCE,
            REMOTE_DISTANCE
        )
        mRemoteLayouts[2] = getMoveParams(
            topView,
            -REMOTE_DISTANCE,
            -REMOTE_DISTANCE
        )
        mRemoteLayouts[3] = getMoveParams(
            topView,
            -REMOTE_DISTANCE,
            REMOTE_DISTANCE
        )
    }

    private val topView: View
        private get() = mCardCollection[mCardCollection.size - 1]

    private fun moveToBack(child: View) {
        val parent = child.parent as ViewGroup
        if (null != parent) {
            parent.removeView(child)
            parent.addView(child, 0) // 移到最后一个
        }
    }

    // 卡片排序，抽出一个，底部上来一个
    private fun reorder() {
        val temp = topView
        //RelativeLayout.LayoutParams tempLp = mLayoutsMap.get(mCardCollection.get(0));
//mLayoutsMap.put(temp,tempLp);
        moveToBack(temp)
        for (i in mCardCollection.size - 1 downTo 1) { //View next = mCardCollection.get(i);
//RelativeLayout.LayoutParams lp = mLayoutsMap.get(next);
//mLayoutsMap.remove(next);
            val current = mCardCollection[i - 1]
            //current replace next
            mCardCollection[i] = current
            //mLayoutsMap.put(current,lp);
        }
        mCardCollection[0] = temp
    }

    // 销毁卡片
    fun discard(direction: Int, al: AnimatorListener?) {
        val `as` = AnimatorSet()
        val aCollection =
            ArrayList<Animator>()
        val topView = topView
        val topParams =
            topView.layoutParams as RelativeLayout.LayoutParams
        val layout: RelativeLayout.LayoutParams = cloneParams(topParams)
        val discardAnim = ValueAnimator.ofObject(
            RelativeLayoutParamsEvaluator(),
            layout,
            mRemoteLayouts[direction]
        )
        discardAnim.addUpdateListener { value ->
            topView.layoutParams = value.animatedValue as RelativeLayout.LayoutParams
        }
        discardAnim.duration = 250
        aCollection.add(discardAnim)
        for (i in mCardCollection.indices) {
            val v = mCardCollection[i]
            if (v === topView) continue
            val nv = mCardCollection[i + 1]
            val layoutParams =
                v.layoutParams as RelativeLayout.LayoutParams
            val endLayout: RelativeLayout.LayoutParams = cloneParams(layoutParams)
            val layoutAnim = ValueAnimator.ofObject(
                RelativeLayoutParamsEvaluator(),
                endLayout,
                mLayoutsMap!![nv]
            )
            layoutAnim.duration = 250
            layoutAnim.addUpdateListener { value ->
                v.layoutParams = value.animatedValue as RelativeLayout.LayoutParams
            }
            aCollection.add(layoutAnim)
        }
        `as`.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                reorder()
                al?.onAnimationEnd(animation)
                mLayoutsMap = HashMap()
                for (v in mCardCollection) {
                    val params =
                        v.layoutParams as RelativeLayout.LayoutParams
                    val paramsCopy: RelativeLayout.LayoutParams = cloneParams(params)
                    mLayoutsMap!![v] = paramsCopy
                }
            }
        })
        `as`.playTogether(aCollection)
        `as`.start()
    }

    /**
     * 还原卡片位置
     */
    fun reverse(e1: MotionEvent?, e2: MotionEvent?) {
        val topView = topView
        val rotationAnim = ValueAnimator.ofFloat(mRotation, 0f)
        rotationAnim.duration = 250
        rotationAnim.addUpdateListener { v ->
            topView.rotation = (v.animatedValue as Float).toFloat()
        }
        rotationAnim.start()
        for (v in mCardCollection) {
            val layoutParams =
                v.layoutParams as RelativeLayout.LayoutParams
            val endLayout: RelativeLayout.LayoutParams = cloneParams(layoutParams)
            val layoutAnim = ValueAnimator.ofObject(
                RelativeLayoutParamsEvaluator(),
                endLayout,
                mLayoutsMap!![v]
            )
            layoutAnim.duration = 100
            layoutAnim.addUpdateListener { value ->
                v.layoutParams = value.animatedValue as RelativeLayout.LayoutParams
            }
            layoutAnim.start()
        }
    }

    fun drag(
        e1: MotionEvent, e2: MotionEvent, distanceX: Float,
        distanceY: Float
    ) {
        val topView = topView
        val x_diff = (e2.rawX - e1.rawX).toInt()
        val y_diff = (e2.rawY - e1.rawY).toInt()
        val rotation_coefficient = 20f
        val layoutParams =
            topView.layoutParams as RelativeLayout.LayoutParams
        val topViewLayouts = mLayoutsMap!![topView]
        layoutParams.leftMargin = topViewLayouts!!.leftMargin + x_diff
        layoutParams.rightMargin = topViewLayouts.rightMargin - x_diff
        layoutParams.topMargin = topViewLayouts.topMargin + y_diff
        layoutParams.bottomMargin = topViewLayouts.bottomMargin - y_diff
        if (isEnableRotation) {
            mRotation = x_diff / rotation_coefficient
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
                    (Math.abs(x_diff) * 0.05).toInt(),
                    mGravity
                )
                moveFrom(
                    v,
                    l,
                    0,
                    (Math.abs(x_diff) * index * 0.05).toInt(),
                    mGravity
                )
            }
        }
    }

    fun setStackMargin(margin: Int) {
        mStackMargin = margin
    }

    companion object {
        private const val DEBUG_TAG = "CardAnimator"
        const val TOP = 48
        const val BOTTOM = 80
        private const val REMOTE_DISTANCE = 1000
    }

    init {
        setup()
    }
}