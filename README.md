# Swipe_Application

###### Creating Anim Folder
Inside Res Folder Create XML folder, with following sample code
  ```
    <scale
        android:duration="300"
        android:fromXScale="1.5"
        android:fromYScale="1.5"
        android:pivotX="50%"
        android:pivotY="50%"
        android:toXScale="1"
        android:toYScale="1" />

    <alpha
        android:duration="200"
        android:fromAlpha="0"
        android:toAlpha="1" />
  ```
  ### Code
  Below Code is Written in Adapter
  ```
        ArrayAdapter<String?>(context!!, R.layout.card_content) {


    override fun getView(position: Int, contentView: View?, parent: ViewGroup
    ): View {
        val v = contentView!!.findViewById<View>(R.id.content) as TextView
        v.text = getItem(position)
        return contentView
    }
   ```
