package com.example.pyatnaski // Или ваш актуальный пакет

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button // <--- ИЗМЕНИТЬ ЗДЕСЬ
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout

class MainActivity : AppCompatActivity() {

    private lateinit var gameBoardLayout: GridLayout
    // Используем android.widget.Button, так как мы работаем с традиционными Views
    private val tiles = mutableListOf<Button>() // <--- ИЗМЕНИТЬ ЗДЕСЬ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameBoardLayout = findViewById(R.id.gridLayout_gameBoard)

        if (::gameBoardLayout.isInitialized) {
            initializeBoard()
        } else {
            Log.e("MainActivity", "GridLayout not found!")
        }
    }

    private fun initializeBoard() {
        gameBoardLayout.removeAllViews()
        tiles.clear()

        val numbers = (1..15).toMutableList()
        numbers.shuffle()

        val inflater = LayoutInflater.from(this)
        val totalCells = gameBoardLayout.rowCount * gameBoardLayout.columnCount

        for (i in 0 until totalCells) {
            val row = i / gameBoardLayout.columnCount
            val col = i % gameBoardLayout.columnCount

            if (i < numbers.size) {
                // inflater.inflate возвращает View, мы его кастуем к Button (android.widget.Button)
                val tileView = inflater.inflate(R.layout.item_tile, gameBoardLayout, false) as Button
                tileView.text = numbers[i].toString()

                val params = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(row, 1f)
                    columnSpec = GridLayout.spec(col, 1f)
                    width = 0
                    height = 0
                    val marginInDp = 2
                    setMargins(marginInDp, marginInDp, marginInDp, marginInDp)
                }
                tileView.layoutParams = params

                tileView.setOnClickListener {
                    println("Нажата плитка: ${tileView.text}")
                }

                gameBoardLayout.addView(tileView)
                tiles.add(tileView) // Теперь tileView корректно добавляется в List<Button>
            }
        }
    }
}
