package com.simon.historyquiz

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.allViews
import androidx.core.view.setPadding


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
        getSupportActionBar()!!.setTitle("${resources.getString(R.string.app_name)} - Level ${level}")
        historyDate  = resources.getStringArray(R.array.level_date_1)
        historyEvent = resources.getStringArray(R.array.level_event_1)

        //Lösungen befüllen
        setSolutionMap()

        dragArea = findViewById(R.id.DragArea)
        dropArea = findViewById(R.id.dropArea)

        initDragArea(dragArea)
        initDragListener(dragArea)

        initDropArea(this, dropArea)
        initDropListener(dropArea)


    }

    private fun dpToPx(dp: Int) :Int {
        return (dp * resources.displayMetrics.density).toInt()
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
                                findViewById<LinearLayout>(R.id.DragArea).addView(draggedView)
                                v.addView(TextView(this).apply {
                                    text = historyDate[i]
                                    v.setBackgroundColor(getColor(R.color.teal_700))
                                })
                            }
                            // evaluate drag & drop
                            val event = (draggedView as TextView).text.toString()
                            val year = historyDate[i]

                            if(solutions[year] == event) {
                                v.setBackgroundColor(Color.GREEN)
                            } else {
                                v.setBackgroundColor(Color.RED)
                            }
                            if (owner.childCount == 0){
                                Log.v("empty","list is empty")
                                val allTextViews = (v.parent as LinearLayout).allViews.toList().filterIsInstance<TextView>()
                                var flag = true
                                repeat(allTextViews.size) { i ->
                                    if (allTextViews[i].text != solutions[historyDate[i]]){
                                        flag = false
                                    }
                                }
                                if (flag){
                                    var dialog: AlertDialog
                                    val builder = AlertDialog.Builder(this)
                                    if (level == 1){
                                        builder.setTitle("Nächstes Level")
                                        builder.setMessage("Möchtest du weiterspielen?")
                                        builder.setPositiveButton("JA, weiter"){ _, _ ->
                                            level++
                                            historyDate = resources.getStringArray(R.array.level_date_2)
                                            historyEvent = resources.getStringArray(R.array.level_event_2)
                                            solutions.clear()
                                            dropArea.removeAllViews()
                                            dropArea.removeAllViews()
                                            setSolutionMap()
                                            initDragArea(findViewById(R.id.DragArea))
                                            initDragListener(findViewById(R.id.DragArea))
                                            initDropArea(this,findViewById( R.id.dropArea))
                                            initDropListener(findViewById( R.id.dropArea))
                                            getSupportActionBar()!!.setTitle("${resources.getString(R.string.app_name)} - Level ${level}")
                                        }
                                    }else {
                                        builder.setTitle("Danke fürs Spielen")
                                        builder.setMessage("Aktuell sind keine weitere Level erhältlich")
                                    }
                                    builder.setNeutralButton("Spiel Beenden" ){_,_ ->
                                        this.finish()
                                    }
                                    dialog = builder.create()
                                    dialog.show()
                                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                                        setTextColor(resources.getColor(R.color.red_500))
                                        textAlignment = left
                                    }
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                                        setTextColor(resources.getColor(R.color.green))
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
                                    v.setBackgroundColor(getColor(R.color.teal_700))
                                })
                            }
                        }
                        true
                    }
                    else -> {
                        false
                    }
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

    private fun initDragArea(dragArea: LinearLayout) {
        var shuffleHistoryEvents  = historyEvent.toMutableList().shuffled().toTypedArray()
    // Erstelle Textviews
        val spacingInPixels = dpToPx(20) // 16dp
        repeat(6) { i ->
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(150), // width in dp
                    dpToPx(150), // height in dp
                    1f // weight
                )
                text = shuffleHistoryEvents[i] // historyEvent[i]
                id = View.generateViewId() // Generate unique ID für jede TextView
                gravity = Gravity.CENTER
                setBackgroundColor(getColor(R.color.purple_200))
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
                setBackgroundColor(getColor(R.color.teal_700))
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
