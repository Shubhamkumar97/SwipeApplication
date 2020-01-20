package com.app.swipeapplication.utils.animation

import android.animation.Animator

import android.animation.Animator.AnimatorListener




/**
 * @author Shubham
 * 21/1/20
 */
abstract class AnimatorListenerAdapter : AnimatorListener {
    override fun onAnimationCancel(arg0: Animator?) {}
    override fun onAnimationEnd(arg0: Animator?) {}
    override fun onAnimationRepeat(arg0: Animator?) {}
    override fun onAnimationStart(arg0: Animator?) {}
}