package com.example.pyatnaski // Или ваш актуальный пакет

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView // Для таймера
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout

class MainActivity : AppCompatActivity() {

    private lateinit var gameBoardLayout: GridLayout
    private val tiles = mutableListOf<Button>()
    private var emptyTile: Button? = null
    private val TILE_COUNT = 4

    private lateinit var startButton: Button
    private lateinit var timerTextView: TextView

    private var timerSeconds = 0
    private var isTimerRunning = false
    private var hasGameStartedSinceShuffle = false // Флаг, что игра началась (было первое движение)
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable: Runnable = object : Runnable {
        override fun run() {
            timerSeconds++
            val hours = timerSeconds / 3600
            val minutes = (timerSeconds % 3600) / 60
            val secs = timerSeconds % 60
            timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, secs)
            if (isTimerRunning) {
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameBoardLayout = findViewById(R.id.gridLayout_gameBoard)
        startButton = findViewById(R.id.button_start) // Убедитесь, что ID правильный
        timerTextView = findViewById(R.id.textView_timer) // Убедитесь, что ID правильный

        if (::gameBoardLayout.isInitialized) {
            initializeBoard() // Первоначальная инициализация (можно сразу перемешать)
        } else {
            Log.e("MainActivity", "GridLayout not found!")
        }

        startButton.setOnClickListener {
            shuffleAndResetGame()
        }
        // Устанавливаем начальное значение таймера
        timerTextView.text = "00:00:00"
    }

    private fun shuffleAndResetGame() {
        Log.d("MainActivity", "Shuffle and reset requested.")
        // 1. Остановить и сбросить таймер
        stopTimer()
        timerSeconds = 0
        timerTextView.text = "00:00:00"
        hasGameStartedSinceShuffle = false // Сбрасываем флаг начала игры

        // 2. Перемешать доску
        initializeBoard() // Это уже включает перемешивание и расстановку

        // Можно добавить проверку на решаемость здесь, если необходимо
        // checkIfSolvableAndFix()
    }


    private fun initializeBoard() {
        gameBoardLayout.removeAllViews()
        tiles.clear()
        emptyTile = null

        val numbers = (1..(TILE_COUNT * TILE_COUNT - 1)).toMutableList()
        numbers.shuffle() // Перемешиваем числа
        numbers.add(0) // 0 - пустая плитка

        // Для теста можно начать с решенной доски, закомментировав shuffle и add(0) выше
        // и раскомментировав это:
        // val numbers = (1..(TILE_COUNT * TILE_COUNT - 1)).toMutableList()
        // numbers.add(0) // Пустая в конце для решенного состояния


        val inflater = LayoutInflater.from(this)

        for (i in 0 until TILE_COUNT * TILE_COUNT) {
            val row = i / TILE_COUNT
            val col = i % TILE_COUNT
            val tileValue = numbers[i]
            val tileView = inflater.inflate(R.layout.item_tile, gameBoardLayout, false) as Button

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

            if (tileValue == 0) {
                tileView.text = ""
                tileView.visibility = View.INVISIBLE
                emptyTile = tileView
            } else {
                tileView.text = tileValue.toString()
                tileView.visibility = View.VISIBLE
            }

            tileView.setOnClickListener { clickedView ->
                if (clickedView is Button && emptyTile != null) {
                    if (clickedView.visibility == View.VISIBLE) {
                        onTileClick(clickedView, emptyTile!!)
                    }
                }
            }
            gameBoardLayout.addView(tileView)
            tiles.add(tileView)
        }

        if (emptyTile == null && tiles.isNotEmpty()) {
            emptyTile = tiles.last()
            emptyTile?.text = ""
            emptyTile?.visibility = View.INVISIBLE
            Log.w("MainActivity", "Backup: Empty tile was not set, using last tile as empty.")
        }
        Log.d("MainActivity", "Board initialized. Empty: ${getTilePosition(emptyTile!!)}")
    }

    private fun onTileClick(clickedTile: Button, currentEmptyTile: Button) {
        val clickedPos = getTilePosition(clickedTile)
        val emptyPos = getTilePosition(currentEmptyTile)

        if (clickedPos == null || emptyPos == null) {
            Log.e("MainActivity", "Could not get tile positions.")
            return
        }

        if (isAdjacent(clickedPos, emptyPos)) {
            // Если это первое движение после перемешивания и таймер не запущен
            if (!hasGameStartedSinceShuffle && !isTimerRunning) {
                startTimer()
                hasGameStartedSinceShuffle = true
            }

            val tempText = clickedTile.text
            clickedTile.text = ""
            currentEmptyTile.text = tempText

            clickedTile.visibility = View.INVISIBLE
            currentEmptyTile.visibility = View.VISIBLE

            emptyTile = clickedTile // Обновляем пустую плитку

            Log.d("MainActivity", "Moved tile ${currentEmptyTile.text}. New empty: ${getTilePosition(emptyTile!!)}")

             checkIfGameWon() // Проверка на победу
        } else {
            Log.d("MainActivity", "Tile ${clickedTile.text} at $clickedPos not adjacent to empty $emptyPos")
        }
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            timerHandler.postDelayed(timerRunnable, 1000) // Запускаем Runnable
            Log.d("MainActivity", "Timer started.")
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            timerHandler.removeCallbacks(timerRunnable) // Останавливаем Runnable
            Log.d("MainActivity", "Timer stopped.")
        }
    }

    private fun getTilePosition(tile: Button): Pair<Int, Int>? {
        val index = gameBoardLayout.indexOfChild(tile)
        if (index == -1) return null
        return Pair(index / TILE_COUNT, index % TILE_COUNT)
    }

    private fun isAdjacent(pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): Boolean {
        val (r1, c1) = pos1
        val (r2, c2) = pos2
        return (kotlin.math.abs(r1 - r2) == 1 && c1 == c2) || (kotlin.math.abs(c1 - c2) == 1 && r1 == r2)
    }

    // Не забудьте остановить таймер, если Activity уничтожается, чтобы избежать утечек
    override fun onDestroy() {
        super.onDestroy()
        stopTimer() // или timerHandler.removeCallbacks(timerRunnable)
    }

    // Можно добавить функцию проверки на победу
    private fun checkIfGameWon() {
        for (i in 0 until tiles.size - 1) { // Проверяем все плитки, кроме последней (пустой)
            val tile = tiles[i]
            if (tile.visibility == View.INVISIBLE || tile.text.toString() != (i + 1).toString()) {
                return // Игра не закончена
            }
        }
        // Если дошли сюда, все плитки на своих местах
        stopTimer()
        Log.d("MainActivity", "Congratulations! You won in $timerSeconds seconds!")
        // Показать сообщение о победе
        // Можно, например, сделать кнопку Start неактивной или изменить ее текст на "Play Again?"
    }
}