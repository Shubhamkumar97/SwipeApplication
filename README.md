# Swipe_Application


## Change log:

  * provide option to infinitly swipe in a loop
  * card rotation setting
  * card gravity setting
  * undo animation
  
  A tinder like swipeable card stack component. Provide "swipe to like" effects. Easy to customize card views.
### Configuration
Put CardStack in your layout file
  ```
    <com.wenchao.cardstack.CardStack
        android:id="@+id/container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipChildren="false"
        android:clipToPadding="false"
        android:gravity="center"
        android:padding="10dp"
        app:card_enable_loop="true"
        app:card_enable_rotation="true"
        app:card_gravity="top"
        app:card_margin="10dp"
        app:card_stack_size="4"/>
  ```
  Create your card view layout file.
  
  Example: card_layout.xml, contain only a TextView
  ```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="match_parent"
    android:layout_height="match_parent">
    <TextView
        android:id="@+id/content"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
    />

</LinearLayout>
   ```
   Implement your own adapter for the card stack. The CardStack will accept ArrayAdapter. The Following example extends a simple ArrayAdapter, overriding getView() to supply your customized card layout
   ```
    ArrayAdapter<String?>(context!!, R.layout.card_content) {


    override fun getView(position: Int, contentView: View?, parent: ViewGroup
    ): View {
        val v = contentView!!.findViewById<View>(R.id.content) as TextView
        v.text = getItem(position)
        return contentView
    }
   ```
   Get the CardStack instance in your activity
   
  ```
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCardStack = findViewById<View>(R.id.container) as CardStack
        mCardStack!!.setContentResource(R.layout.card_content)
        }
  ```
  Finally, set the adapter
  ```
   mCardAdapter = CardsDataAdapter(applicationContext)
        mCardAdapter!!.add("test1")
        mCardAdapter!!.add("test2")
        mCardAdapter!!.add("test3")
        mCardAdapter!!.add("test4")
        mCardAdapter!!.add("test5")
        mCardAdapter!!.add("test6")
        mCardAdapter!!.add("test7")
        mCardStack!!.setAdapter(mCardAdapter!!)
        if (mCardStack!!.adapter != null) {
            Log.i("MyActivity", "Card Stack size: " + mCardStack!!.adapter!!.count)
  ```
## Listening to card stack event

implement CardStack.CardEventListener, and set it as listener
```
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
     * @param EnableRotation
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


        fun swipeEnd(section: Int, distance: Float): Boolean

        fun swipeStart(section: Int, distance: Float): Boolean

        fun swipeContinue(section: Int, distanceX: Float, distanceY: Float): Boolean

        fun discarded(mIndex: Int, direction: Int)

        fun topCardTapped()
    }
  }
```
