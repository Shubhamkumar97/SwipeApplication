package com.app.swipeapplication

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.app.swipeapplication.utils.cardstack.CardAnimator
import com.app.swipeapplication.utils.cardstack.CardStack

class MainActivity : Activity() {
    private var mCardStack: CardStack? = null
    private var mCardAdapter: CardsDataAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCardStack = findViewById<View>(R.id.container) as CardStack
        mCardStack!!.setContentResource(R.layout.card_content)
        //        mCardStack.setStackMargin(20);
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
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.my, menu)
        return true
    }

    /**
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        //Reset
        if (id == R.id.action_reset) {
            mCardStack!!.reset(true)
            return true
        }

        //bottom
        if (id == R.id.action_bottom) {
            mCardStack!!.stackGravity =
                if (mCardStack!!.stackGravity == CardAnimator.TOP) CardAnimator.BOTTOM else CardAnimator.TOP
            mCardStack!!.reset(true)
            return true
        }

        //cycle
        if (id == R.id.action_loop) {
            mCardStack!!.isEnableLoop = !mCardStack!!.isEnableLoop
            mCardStack!!.reset(true)
        }

        //Whether to allow rotation
        if (id == R.id.action_rotation) {
            mCardStack!!.isEnableRotation = !mCardStack!!.isEnableRotation
            mCardStack!!.reset(true)
        }

        //Visible number
        if (id == R.id.action_visibly_size) {
            mCardStack!!.visibleCardNum = mCardStack!!.visibleCardNum + 1
        }

        //interval
        if (id == R.id.action_span) {
            mCardStack!!.stackMargin = mCardStack!!.stackMargin + 10
        }
        if (id == R.id.action_settings) {
            mCardStack!!.undo()
        }
        return super.onOptionsItemSelected(item)
    }
}