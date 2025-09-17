package com.example.pyatnaski // Или ваш актуальный пакет

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button // <--- ИЗМЕНИТЬ ЗДЕСЬ
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout

class MainActivity : AppCompatActivity() {

    private lateinit var gameBoardLayout: GridLayout
    // Используем android.widget.Button, так как мы работаем с традиционными Views
    private val tiles = mutableListOf<Button>() // <--- ИЗМЕНИТЬ ЗДЕСЬ
    private var emptyTile: Button? = null // Переменная для пустой плитки
    private val TILE_COUNT = 4
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
        emptyTile = null // Сбрасываем перед инициализацией

        val numbers = (1.. (TILE_COUNT * TILE_COUNT - 1)).toMutableList() // Числа от 1 до 15 для поля 4x4
        numbers.shuffle()
        numbers.add(0) // Добавляем 0 для обозначения пустой плитки (или можно последний элемент сделать пустым)

        val inflater = LayoutInflater.from(this)

        for (i in 0 until TILE_COUNT * TILE_COUNT) {
            val row = i / TILE_COUNT
            val col = i % TILE_COUNT

            val tileValue = numbers[i]

            val tileView = inflater.inflate(R.layout.item_tile, gameBoardLayout, false) as Button
            // Установка LayoutParams (как вы делали ранее, с отступами и весами)
            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(row, 1f)
                columnSpec = GridLayout.spec(col, 1f)
                width = 0
                height = 0
                val marginInDp = 2
                val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
                setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            }
            tileView.layoutParams = params

            if (tileValue == 0) { // Это пустая плитка
                tileView.text = "" // Пустой текст
                tileView.visibility = View.INVISIBLE // Делаем ее невидимой, но она занимает место
                // или можно задать другой фон/стиль для пустой плитки
                // tileView.setBackgroundColor(Color.LTGRAY) // Пример
                emptyTile = tileView
            } else {
                tileView.text = tileValue.toString()
                tileView.visibility = View.VISIBLE
            }

            tileView.setOnClickListener { clickedView ->
                if (clickedView is Button && emptyTile != null) {
                    // Только непустые плитки могут инициировать ход
                    if (clickedView.visibility == View.VISIBLE) {
                        onTileClick(clickedView, emptyTile!!)
                    }
                }
            }

            gameBoardLayout.addView(tileView)
            tiles.add(tileView)
        }

        // Дополнительная проверка, на случай если 0 не попался (хотя должен при такой логике)
        if (emptyTile == null && tiles.isNotEmpty()) {
            // Если по какой-то причине пустая плитка не была назначена,
            // сделаем последнюю плитку пустой (аварийный вариант)
            emptyTile = tiles.last()
            emptyTile?.text = ""
            emptyTile?.visibility = View.INVISIBLE
            Log.w("MainActivity", "Empty tile was not set, using last tile as empty.")
        }
        Log.d("MainActivity", "Board initialized. Empty tile: ${emptyTile?.let { getTilePosition(it) }}")
    }
    private fun onTileClick(clickedTile: Button, currentEmptyTile: Button) {
        val clickedPos = getTilePosition(clickedTile)
        val emptyPos = getTilePosition(currentEmptyTile)

        if (clickedPos == null || emptyPos == null) {
            Log.e("MainActivity", "Could not get tile positions.")
            return
        }

        // Проверяем, являются ли плитки соседями (не по диагонали)
        if (isAdjacent(clickedPos, emptyPos)) {
            // Меняем местами текст и видимость
            val tempText = clickedTile.text
            clickedTile.text = ""
            currentEmptyTile.text = tempText

            clickedTile.visibility = View.INVISIBLE
            currentEmptyTile.visibility = View.VISIBLE

            // Обновляем ссылку на пустую плитку
            emptyTile = clickedTile // Теперь нажатая плитка стала пустой

            Log.d("MainActivity", "Moved tile ${currentEmptyTile.text} to empty. New empty: ${getTilePosition(emptyTile!!)}")

            // Тут можно добавить проверку на победу
            // checkIfGameWon()
        } else {
            Log.d("MainActivity", "Tile ${clickedTile.text} at $clickedPos is not adjacent to empty at $emptyPos")
        }
    }

    // Вспомогательная функция для получения позиции (row, col) плитки в GridLayout
    private fun getTilePosition(tile: Button): Pair<Int, Int>? {
        val params = tile.layoutParams as? GridLayout.LayoutParams ?: return null
        // GridLayout.Spec не дает прямого доступа к row/column после установки,
        // но мы можем найти индекс в списке children и рассчитать
        val index = gameBoardLayout.indexOfChild(tile)
        if (index == -1) return null
        val row = index / TILE_COUNT
        val col = index % TILE_COUNT
        return Pair(row, col)
    }


    // Вспомогательная функция для проверки соседства
    private fun isAdjacent(pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): Boolean {
        val (row1, col1) = pos1
        val (row2, col2) = pos2

        // Разница по строкам и столбцам
        val rowDiff = Math.abs(row1 - row2)
        val colDiff = Math.abs(col1 - col2)

        // Соседние, если одна из разниц равна 1, а другая 0 (не по диагонали)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }
}
