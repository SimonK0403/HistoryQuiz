package com.simon.historyquiz

import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.allViews


class MainActivity : AppCompatActivity() {
    private fun dpToPx(dp: Int) :Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    private val solutions = HashMap<String, String>()
    private val historyDate = listOf("1945","1949","1953","1961","1989","1990")
    private val historyEvent = listOf("Ende 2. WK","Gründung BRD&DDR","Aufstand in der DDR","Mauerbau","Mauerfall","Wiedervereinigung")
    override fun onCreate(savedInstanceState: Bundle?) {
        // basic stuff:
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Lösungen befüllen
        repeat(historyDate.size) {i ->
            solutions[historyDate[i]] = historyEvent[i]
        }

        val dragArea: LinearLayout = findViewById(R.id.DragArea)
        val dropArea: LinearLayout = findViewById(R.id.dropArea)
        initDragArea(dragArea)
        initDropArea(this, dropArea)
        for (i in 0 until dragArea.childCount){
            val child = dragArea.getChildAt(i)
            child.setOnLongClickListener { v ->
                val shadow = View.DragShadowBuilder(child)
                v.startDragAndDrop(null, shadow, child, 0) // child as drag data
                false
            }
        }
        for (i in 0 until dropArea.childCount){
            val child = dropArea.getChildAt(i)
            child.setOnDragListener{ v, e ->
                when(e.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        v.setBackgroundColor(getColor(R.color.teal_700))
                        true
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        v.setBackgroundColor(Color.BLUE)
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        v.setBackgroundColor(getColor(R.color.teal_700))
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        val draggedView = e.localState as View
                        val owner = draggedView.parent as LinearLayout
                        (v as LinearLayout)
                        if (historyDate.contains((v.getChildAt(0) as TextView).text)){
                            owner.removeView(draggedView)
                            v.removeView(v.getChildAt(0))
                            v.addView(draggedView)
                            draggedView.setOnClickListener {
                                v.removeView(draggedView)
                                owner.addView(draggedView)
                                v.addView(TextView(this).apply {
                                    text = historyDate[i]
                                    v.setBackgroundColor(getColor(R.color.teal_700))
                                })
                               // Toast.makeText(this, "xx", Toast.LENGTH_SHORT).show()
                            }

                            val event = (draggedView.allViews.toList()[0] as TextView).text.toString()
                            val year = historyDate[i]

                            if(solutions[year] == event) {
                                v.setBackgroundColor(Color.GREEN)
                            } else {
                                v.setBackgroundColor(Color.RED)
                            }

                            true
                        } else{
                            v.invalidate()
                            false
                        }
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        v.invalidate()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }
    }

    private fun initDragArea(dragArea: LinearLayout) {
    // Erstelle Textviews
        repeat(6) { i ->
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(150), // width in dp
                    dpToPx(150), // height in dp
                    1f // weight
                )
                text = historyEvent[i]
                id = View.generateViewId() // Generate unique ID für jede TextView
                gravity = Gravity.CENTER
                setBackgroundColor(getColor(R.color.purple_200))
            }
            dragArea.addView(textView)
        }

        val spacingInPixels = dpToPx(16) // 16dp

        for (i in 0 until dragArea.childCount) {
            val child = dragArea.getChildAt(i)
            val params = child.layoutParams as LinearLayout.LayoutParams
            params.leftMargin = spacingInPixels/2
            params.rightMargin = spacingInPixels/2
            params.bottomMargin = spacingInPixels
            params.topMargin = spacingInPixels
            child.layoutParams = params
        }
    }
    private fun initDropArea(mainActivity: MainActivity, dropArea: LinearLayout) {
        // Erstelle Textviews
        repeat(6) { i ->
            val listView = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(200), // width in dp
                    dpToPx(200), // height in dp
                    1f // weight
                )
                val textView = TextView(mainActivity).apply {
                    text = historyDate[i]
                }
                this.addView(textView)
                id = View.generateViewId() // Generate unique ID für jede TextView
                gravity = Gravity.CENTER
                setBackgroundColor(getColor(R.color.teal_200))
            }
            dropArea.addView(listView)
        }

        val spacingInPixels = dpToPx(16) // 16dp

        for (i in 0 until dropArea.childCount) {
            val child = dropArea.getChildAt(i)
            val params = child.layoutParams as LinearLayout.LayoutParams
            params.leftMargin = spacingInPixels/2
            params.rightMargin = spacingInPixels/2
            params.bottomMargin = spacingInPixels
            params.topMargin = spacingInPixels
            child.layoutParams = params
        }
    }

}
