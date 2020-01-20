package com.app.swipeapplication.utils.cardstack

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.RelativeLayout
import kotlin.math.abs


/**
 * @author Sumit Pratap Singh
 * 20/1/20
 */
class CardStack : RelativeLayout {

    /**
     * Whether to allow rotation
     *
     * @return
     */
    /**
     * Whether to allow rotation
     *
     *
     * Called after setting[.reset] To reinitialize the layout
     *
     * @param enableRotation
     */
    var isEnableRotation: Boolean = false
    /**
     * Get the current direction
     *
     * @return
     */
    /**
     * Set direction，Support、under.
     * Called after setting[.reset] To reinitialize the layout
     *
     * @param gravity [CardAnimator.TOP] 向上 [CardAnimator.BOTTOM] 向下，默认值
     */
    var stackGravity: Int = 0
    private var mColor = -1
    //sync?
    var currIndex = 0
        private set
    private var mNumVisible = 4
    private var canSwipe = true
    private var mAdapter: ArrayAdapter<*>? = null
    private var mOnTouchListener: OnTouchListener? = null
    private var mCardAnimator: CardAnimator? = null
    /**
     * Whether to scroll
     *
     * @return
     */
    /**
     * Whether to scroll
     * Called after setting[.reset] To reinitialize the layout
     *
     * @param enableLoop
     */
    var isEnableLoop: Boolean = false // Whether to allow cyclic scrolling


    private var mEventListener: CardEventListener = DefaultStackEventListener(300)
    private var mContentResource = 0
    private var mMargin: Int = 0

    var stackMargin: Int
        get() = mMargin
        set(margin) {
            mMargin = margin
            mCardAnimator!!.setStackMargin(mMargin)
            mCardAnimator!!.initLayout()
        }

    private val mOb = object : DataSetObserver() {
        override fun onChanged() {
            reset(false)
        }
    }


    //ArrayList

    internal var viewCollection = ArrayList<View>()

    val adapter
        get() = mAdapter

    val topView: View
        get() = (viewCollection[viewCollection.size - 1] as ViewGroup).getChildAt(0)

    private val contentView: View?
        get() {
            var contentView: View? = null
            if (mContentResource != 0) {
                val lf = LayoutInflater.from(context)
                contentView = lf.inflate(mContentResource, null)
            }
            return contentView

        }

    /**
     * Get the number of visible cards
     *
     * @return
     */
    var visibleCardNum: Int
        get() = mNumVisible
        set(visiableNum) {
            mNumVisible = visiableNum
            if (mNumVisible >= mAdapter!!.count) {
                mNumVisible = mAdapter!!.count
            }
            reset(false)
        }


    interface CardEventListener {
        //section
        // 0 | 1
        //--------
        // 2 | 3
        // swipe distance, most likely be used with height and width of a view ;

        fun swipeEnd(section: Int, distance: Float): Boolean

        fun swipeStart(section: Int, distance: Float): Boolean

        fun swipeContinue(section: Int, distanceX: Float, distanceY: Float): Boolean

        fun discarded(mIndex: Int, direction: Int)

        fun topCardTapped()
    }

    fun discardTop(direction: Int) {
        mCardAnimator!!.discard(direction, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(arg0: Animator) {
                mCardAnimator!!.initLayout()
                currIndex++
                loadLast()

                viewCollection[0].setOnTouchListener(null)
                viewCollection[viewCollection.size - 1].setOnTouchListener(mOnTouchListener)
                mEventListener.discarded(currIndex - 1, direction)
            }
        })
    }

    //only necessary when I need the attrs from xml, this will be used when inflating layout
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        if (attrs != null) {
            val array = context.obtainStyledAttributes(
                attrs,
                com.app.swipeapplication.R.styleable.CardStack
            )

            mColor = array.getColor(
                com.app.swipeapplication.R.styleable.CardStack_card_backgroundColor,
                mColor
            )
            stackGravity = array.getInteger(
                com.app.swipeapplication.R.styleable.CardStack_card_gravity,
                Gravity.BOTTOM
            )
            isEnableRotation = array.getBoolean(
                com.app.swipeapplication.R.styleable.CardStack_card_enable_rotation,
                false
            )
            mNumVisible = array.getInteger(
                com.app.swipeapplication.R.styleable.CardStack_card_stack_size,
                mNumVisible
            )
            isEnableLoop = array.getBoolean(
                com.app.swipeapplication.R.styleable.CardStack_card_enable_loop,
                isEnableLoop
            )
            mMargin = array.getDimensionPixelOffset(
                com.app.swipeapplication.R.styleable.CardStack_card_margin,
                20
            )
            array.recycle()
        }

        //get attrs assign minVisiableNum
        for (i in 0 until mNumVisible) {
            addContainerViews(false)
        }
        setupAnimation()
    }

    private fun addContainerViews(anim: Boolean) {
        val v = FrameLayout(context)
        viewCollection.add(v)
        addView(v)
        if (anim) {
            val animation =
                AnimationUtils.loadAnimation(context, com.app.swipeapplication.R.anim.undo_anim)
            v.startAnimation(animation)
        }
    }


    fun setContentResource(res: Int) {
        mContentResource = res
    }

    fun setCanSwipe(can: Boolean) {
        this.canSwipe = can
    }


    fun reset(resetIndex: Boolean) {
        reset(resetIndex, false)
    }

    private fun reset(resetIndex: Boolean, animFirst: Boolean) {
        if (resetIndex) currIndex = 0
        removeAllViews()
        viewCollection.clear()
        for (i in 0 until mNumVisible) {
            addContainerViews(i == mNumVisible - 1 && animFirst)
        }
        setupAnimation()
        loadData()
    }

    fun setThreshold(t: Int) {
        mEventListener = DefaultStackEventListener(t)
    }

    fun setListener(cel: CardEventListener) {
        mEventListener = cel
    }

    private fun setupAnimation() {
        val cardView = viewCollection[viewCollection.size - 1]
        mCardAnimator = CardAnimator(viewCollection, mColor, mMargin)
        mCardAnimator!!.setGravity(stackGravity)
        mCardAnimator!!.isEnableRotation = isEnableRotation
        //mCardAnimator.setStackMargin(mMargin);
        mCardAnimator!!.initLayout()

        val dd =
            DragGestureDetector(this@CardStack.context, object : DragGestureDetector.DragListener {


                override fun onDragStart(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    if (canSwipe) {
                        mCardAnimator!!.drag(e1!!, e2!!, distanceX, distanceY)
                    }
                    val x1 = e1!!.rawX
                    val y1 = e1.rawY
                    val x2 = e2!!.rawX
                    val y2 = e2.rawY
                    val direction = CardUtils.direction(x1, y1, x2, y2)
                    val distance = CardUtils.distance(x1, y1, x2, y2)
                    mEventListener.swipeStart(direction, distance)
                    return true
                }

                override fun onDragContinue(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    val x1 = e1!!.rawX
                    val y1 = e1.rawY
                    val x2 = e2!!.rawX
                    val y2 = e2.rawY
                    val direction = CardUtils.direction(x1, y1, x2, y2)
                    if (canSwipe) {
                        mCardAnimator!!.drag(e1, e2, distanceX, distanceY)
                    }
                    mEventListener.swipeContinue(direction, abs(x2 - x1), abs(y2 - y1))
                    return true
                }

                override fun onDragEnd(e1: MotionEvent?, e2: MotionEvent?): Boolean {
                    //reverse(e1,e2);
                    val x1 = e1!!.rawX
                    val y1 = e1.rawY
                    val x2 = e2!!.rawX
                    val y2 = e2.rawY
                    val distance = CardUtils.distance(x1, y1, x2, y2)
                    val direction = CardUtils.direction(x1, y1, x2, y2)

                    val discard = mEventListener.swipeEnd(direction, distance)
                    if (discard) {
                        if (canSwipe) {
                            mCardAnimator!!.discard(direction, object : AnimatorListenerAdapter() {

                                override fun onAnimationEnd(arg0: Animator) {
                                    mCardAnimator!!.initLayout()
                                    currIndex++
                                    mEventListener.discarded(currIndex, direction)

                                    //mIndex = mIndex%mAdapter.getCount();
                                    loadLast()

                                    viewCollection[0].setOnTouchListener(null)
                                    viewCollection[viewCollection.size - 1]
                                        .setOnTouchListener(mOnTouchListener)
                                }

                            })
                        }
                    } else {
                        if (canSwipe) {

                            mCardAnimator!!.reverse(e1, e2)
                        }
                    }
                    return true
                }

                override fun onTapUp(): Boolean {
                    mEventListener.topCardTapped()
                    return true
                }
            }
            )

        mOnTouchListener = object : OnTouchListener {
            private val DEBUG_TAG = "MotionEvents"

            override fun onTouch(arg0: View, event: MotionEvent): Boolean {
                dd.onTouchEvent(event)
                return true
            }
        }
        cardView.setOnTouchListener(mOnTouchListener)
    }

    constructor(context: Context) : super(context)

    fun setAdapter(adapter: ArrayAdapter<*>) {
        if (mAdapter != null) {
            mAdapter!!.unregisterDataSetObserver(mOb)
        }
        mAdapter = adapter
        adapter.registerDataSetObserver(mOb)

        loadData()
    }

    private fun loadData() {
        for (i in mNumVisible - 1 downTo 0) {
            val parent = viewCollection[i] as ViewGroup
            val index = currIndex + mNumVisible - 1 - i
            if (index > mAdapter!!.count - 1) {
                parent.visibility = View.GONE

            } else {
                val child = mAdapter!!.getView(index, contentView, this)
                parent.addView(child)
                parent.visibility = View.VISIBLE
            }
        }
    }

    // Load next
    private fun loadLast() {
        val parent = viewCollection[0] as ViewGroup
        var lastIndex = mNumVisible - 1 + currIndex

        // Out of index
        if (lastIndex > mAdapter!!.count - 1) {
            if (isEnableLoop) {
                // Loop processing
                lastIndex %= mAdapter!!.count
            } else {
                parent.visibility = View.GONE
                return
            }
        }

        val child = mAdapter!!.getView(lastIndex, contentView, parent)
        parent.removeAllViews()
        parent.addView(child)
    }

    fun undo() {
        if (currIndex == 0) return
        currIndex--
        reset(resetIndex = false, animFirst = true)
    }
}