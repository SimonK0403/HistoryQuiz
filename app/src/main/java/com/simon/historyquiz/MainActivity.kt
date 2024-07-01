package com.simon.historyquiz

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Layout
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.allViews

class MainActivity : AppCompatActivity() {
    private var solutions = HashMap<String, String>()
    private lateinit var historyDate: Array<String>
    private lateinit var historyEvent : Array<String>
    private lateinit var dragArea: LinearLayout
    private lateinit var dropArea: LinearLayout
    private var level : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        // basic stuff:
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.title = "${resources.getString(R.string.app_name)} - Level $level"
        historyDate  = resources.getStringArray(R.array.level_date_1)
        historyEvent = resources.getStringArray(R.array.level_event_1)

        //Lösungen befüllen
        setSolutionMap()

        // init scrollable drag item list
        dragArea = findViewById(R.id.DragArea)
        initDragArea(dragArea, getColor(R.color.purple_200))
        initDragListener(dragArea)

        // init scrollable drop area list
        dropArea = findViewById(R.id.dropArea)
        initDropArea(dropArea)
        initDropListener(dropArea)
    }

    private fun dpToPx(dp: Int) :Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun changeShapeColor(view: View, color: Int) {
        val drawable = view.background as? GradientDrawable
        drawable?.setColor(color)
    }

    private fun setSolutionMap() {
        repeat(historyDate.size) { i ->
            solutions[historyDate[i]] = historyEvent[i]
        }
    }

    private fun initDropListener(dropArea: LinearLayout) {
        for (i in 0 until dropArea.childCount){
            val child = dropArea.getChildAt(i)
            child.setOnDragListener{ v, e ->
                when(e.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        changeShapeColor(v, getColor(R.color.teal_700))
                        true
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        changeShapeColor(v, getColor(R.color.blue_700))
                        v.invalidate()
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        changeShapeColor(v, getColor(R.color.teal_700))
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
                                findViewById<LinearLayout>(R.id.DragArea).addView(draggedView)
                                v.addView(TextView(this).apply {
                                    text = historyDate[i]
                                    changeShapeColor(v, getColor(R.color.teal_700))
                                    textSize = 40.0F
                                })
                            }
                            // evaluate drag & drop
                            val event = (draggedView as TextView).text.toString()
                            val year = historyDate[i]

                            if(solutions[year] == event) {
                                changeShapeColor(v, getColor(R.color.signal_green))
                            } else {
                                changeShapeColor(v, getColor(R.color.red_500))
                            }
                            if (owner.childCount == 0){
                                val allTextViews = (v.parent as LinearLayout).allViews.toList().filterIsInstance<TextView>()
                                var flag = true
                                repeat(allTextViews.size) { i ->
                                    if (allTextViews[i].text != solutions[historyDate[i]]){
                                        flag = false
                                    }
                                }
                                if (flag){// Alertdialog when all is correct
                                    val dialog: AlertDialog
                                    val builder = AlertDialog.Builder(this)
                                    if (level == 1){
                                        builder.setTitle(resources.getString(R.string.next_level))
                                        builder.setMessage(resources.getString(R.string.continue_or_not))
                                        builder.setPositiveButton(resources.getString(R.string.continue_yes)){ _, _ ->
                                            level++
                                            historyDate = resources.getStringArray(R.array.level_date_2)
                                            historyEvent = resources.getStringArray(R.array.level_event_2)
                                            solutions.clear()
                                            setSolutionMap()
                                            //re-init drag item list
                                            dropArea.removeAllViews()
                                            initDragArea(findViewById(R.id.DragArea) , getColor(R.color.purple_200))//different drag item colors possible
                                            initDragListener(findViewById(R.id.DragArea))
                                            // re-init drop areas
                                            dropArea.removeAllViews()
                                            initDropArea(findViewById( R.id.dropArea))
                                            initDropListener(findViewById( R.id.dropArea))
                                            supportActionBar!!.title = "${resources.getString(R.string.app_name)} - Level $level"
                                        }
                                    }else {
                                        builder.setTitle(resources.getString(R.string.thanks_for_playing))
                                        builder.setMessage(resources.getString(R.string.no_levels_available))
                                    }
                                    builder.setNeutralButton(resources.getString(R.string.continue_no)){_,_ ->
                                        this.finish()
                                    }
                                    dialog = builder.create()
                                    dialog.show()
                                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                                        setTextColor(getColor(R.color.red_500))
                                        textAlignment = left
                                    }
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                                        setTextColor(getColor(R.color.green))
                                    }
                                }
                            }
                            true
                        } else{
                            v.invalidate()
                            false
                        }
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        v.invalidate()
                        // Folgender code stellt die Dropview.textview  wieder her
                        val allTextViews= (v.parent as LinearLayout).allViews.toList().filterIsInstance<LinearLayout>()
                        allTextViews.forEachIndexed { index, t ->
                            if(t.getChildAt(0) == null){
                                t.addView(TextView(this).apply {
                                    text = historyDate[index-1]
                                    changeShapeColor(v, getColor(R.color.teal_700))
                                    textSize = 40.0F
                                })
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun initDragListener(dragArea : LinearLayout) {
        for (i in 0 until dragArea.childCount){
            val child = dragArea.getChildAt(i)
            child.setOnLongClickListener { v ->
                val shadow = View.DragShadowBuilder(child)
                v.startDragAndDrop(null, shadow, child, 0) // child as drag data
                false
            }
        }
    }

    private fun initDragArea(dragArea: LinearLayout, color: Int) {
        val shuffleHistoryEvents  = historyEvent.toMutableList().shuffled().toTypedArray()
        // Erstelle Textviews
        val spacingInPixels = dpToPx(20) // 16dp
        repeat(historyEvent.size) { i ->
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(150), // width in dp
                    dpToPx(150), // height in dp
                    1f // weight
                )
                text = shuffleHistoryEvents[i] // historyEvent[i]
                hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_FULL
                id = View.generateViewId() // Generate unique ID für jede TextView
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(this@MainActivity ,R.drawable.rounded_rectangle)
                changeShapeColor(this, color )
                setPadding(-spacingInPixels,-spacingInPixels,-spacingInPixels,-spacingInPixels)
            }
            dragArea.addView(textView)
        }

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
    private fun initDropArea(dropArea: LinearLayout) {
        // Erstelle Textviews
        repeat(historyDate.size) { i ->
            val listView = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(200), // width in dp
                    dpToPx(200), // height in dp
                    1f // weight
                )
                val textView = TextView(this@MainActivity).apply {
                    text = historyDate[i]
                    textSize = 40.0F
                }
                this.addView(textView)
                id = View.generateViewId() // Generate unique ID für jede TextView
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(this@MainActivity ,R.drawable.rounded_rectangle)
                changeShapeColor(this, getColor(R.color.teal_700))
            }
            dropArea.addView(listView)
        }

        val spacingInPixels = dpToPx(16) // 16dp

        for (i in 0 until dropArea.childCount) {
            val child = dropArea.getChildAt(i)
            val params = child.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = spacingInPixels
            params.topMargin = spacingInPixels
            child.layoutParams = params
        }
    }

}
