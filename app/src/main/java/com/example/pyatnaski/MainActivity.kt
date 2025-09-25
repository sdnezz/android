package com.example.pyatnaski

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
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
    private var hasGameStartedSinceShuffle = false
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

    private lateinit var stepsTextView: TextView
    private var stepCount = 0

    private lateinit var pauseToggleButton: ToggleButton
    private var isGamePaused = false

    private val ACHIEVEMENTS_PREFS = "GameAchievementsPrefs"
    private val KEY_ACH_SOLVED_ONCE = "ach_solved_once"
    private val KEY_ACH_SOLVED_UNDER_5_MIN = "ach_solved_under_5_min"
    private val KEY_ACH_100_STEPS = "ach_100_steps"
    private val KEY_ACH_SOLVED_UNDER_50_STEPS = "ach_solved_under_50_steps"
    private val KEY_ACH_SOLVED_UNDER_1_MIN = "ach_solved_under_50_steps"
    private lateinit var achievementsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        achievementsButton = findViewById(R.id.button_achievements)
        achievementsButton.setOnClickListener {showAchievementsDialog()}
        gameBoardLayout = findViewById(R.id.gridLayout_gameBoard)
        startButton = findViewById(R.id.button_start)
        timerTextView = findViewById(R.id.textView_timer)

        if (::gameBoardLayout.isInitialized) {initializeBoard()}

        startButton.setOnClickListener {
            shuffleAndResetGame()
        }
        timerTextView.text = "00:00:00"

        stepsTextView = findViewById(R.id.textView_steps)
        updateStepsDisplay()

        pauseToggleButton = findViewById(R.id.toggleButton_pause)
        pauseToggleButton.setOnCheckedChangeListener { _, isChecked ->
            isGamePaused = isChecked
            if (isGamePaused) {
                pauseGame()
            } else {
                resumeGame()
            }
        }
        pauseToggleButton.isChecked = false
    }

    private fun shuffleAndResetGame() {
        stopTimer()
        timerSeconds = 0
        timerTextView.text = "00:00:00"
        hasGameStartedSinceShuffle = false
        stepCount = 0
        updateStepsDisplay()
        initializeBoard()
    }

    private fun updateStepsDisplay(){
        stepsTextView.text = "Шагов: $stepCount"
    }

    private fun initializeBoard() {
        gameBoardLayout.removeAllViews()
        tiles.clear()
        emptyTile = null

        val numbers = (1..(TILE_COUNT * TILE_COUNT - 1)).toMutableList()
        numbers.shuffle()
        numbers.add(0)


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
        if (isGamePaused) {
            return
        }
        if (clickedPos == null || emptyPos == null) {
            Log.e("MainActivity", "Could not get tile positions.")
            return
        }

        if (isAdjacent(clickedPos, emptyPos)) {
            if (!hasGameStartedSinceShuffle && !isTimerRunning) {
                startTimer()
                hasGameStartedSinceShuffle = true
            }

            val tempText = clickedTile.text
            clickedTile.text = ""
            currentEmptyTile.text = tempText

            clickedTile.visibility = View.INVISIBLE
            currentEmptyTile.visibility = View.VISIBLE

            emptyTile = clickedTile

            stepCount++
            updateStepsDisplay()
            checkAndUnlockStepAchievements()

            checkIfGameWon()
        }
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            timerHandler.postDelayed(timerRunnable, 1000)
            Log.d("MainActivity", "Timer started.")
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    private fun pauseGame() {
        isGamePaused = true
        if (isTimerRunning) {
            timerHandler.removeCallbacks(timerRunnable)
        }
        setGameBoardEnabled(false)
        Toast.makeText(this, "Игра приостановлена", Toast.LENGTH_SHORT).show()
    }

    private fun resumeGame() {
        isGamePaused = false
        if (isTimerRunning && hasGameStartedSinceShuffle) { // Возобновляем таймер, если он был активен и игра уже началась
            timerHandler.postDelayed(timerRunnable, 1000)
        }
        setGameBoardEnabled(true)
        Toast.makeText(this, "Игра возобновлена", Toast.LENGTH_SHORT).show()
    }

    private fun setGameBoardEnabled(isEnabled: Boolean) {
        for (i in 0 until gameBoardLayout.childCount) {
            val child = gameBoardLayout.getChildAt(i)
            child.isEnabled = isEnabled
            child.isClickable = isEnabled
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

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    private fun checkIfGameWon() {
        for (i in 0 until tiles.size - 1) {
            val tile = tiles[i]
            if (tile.visibility == View.INVISIBLE || tile.text.toString() != (i + 1).toString()) {
                return
            }
        }
        setGameBoardEnabled(false)
        stopTimer()
        onGameWon()
    }
    private fun onGameWon() {
        stopTimer()

        val prefs = getSharedPreferences(ACHIEVEMENTS_PREFS, MODE_PRIVATE)
        val editor = prefs.edit()

        // 1. "Решить головоломку"
        editor.putBoolean(KEY_ACH_SOLVED_ONCE, true)

        // 2. "Решить не более чем за 5 минут" (5 минут = 300 секунд)
        if (timerSeconds <= 300) {
            editor.putBoolean(KEY_ACH_SOLVED_UNDER_5_MIN, true)
        }

        // 3. "Решить не более чем за 50 шагов"
        if (stepCount <= 50) {
            editor.putBoolean(KEY_ACH_SOLVED_UNDER_50_STEPS, true)
        }

        editor.apply()
        if(prefs.getBoolean(KEY_ACH_SOLVED_ONCE, true)
            or prefs.getBoolean(KEY_ACH_SOLVED_UNDER_5_MIN, true)
            or prefs.getBoolean(KEY_ACH_SOLVED_UNDER_50_STEPS, true))
        {
            Toast.makeText(this, "Победа за ${timerSeconds} секунд и ${stepCount} шагов! Новые достижения!.", Toast.LENGTH_LONG).show()
        }
        else{Toast.makeText(this, "Победа за ${timerSeconds} секунд и ${stepCount} шагов!", Toast.LENGTH_LONG).show()}
    }
    private fun checkAndUnlockStepAchievements() {
        val prefs = getSharedPreferences(ACHIEVEMENTS_PREFS, MODE_PRIVATE)
        if (stepCount >= 100 && !prefs.getBoolean(KEY_ACH_100_STEPS, false)) {
            prefs.edit().putBoolean(KEY_ACH_100_STEPS, true).apply()
            Toast.makeText(this, "Достижение: 100 шагов!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAchievementsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievements, null)
        val listView = dialogView.findViewById<ListView>(R.id.listViewAchievements)
        val closeButton = dialogView.findViewById<Button>(R.id.buttonCloseAchievements)

        val prefs = getSharedPreferences(ACHIEVEMENTS_PREFS, MODE_PRIVATE)
        val achievementItems = mutableListOf<AchievementItem>()
        achievementItems.add(AchievementItem("Решить головоломку", prefs.getBoolean(KEY_ACH_SOLVED_ONCE, false)))
        achievementItems.add(AchievementItem("Решить не более чем за 5 минут", prefs.getBoolean(KEY_ACH_SOLVED_UNDER_5_MIN, false)))
        achievementItems.add(AchievementItem("Сделать 100 шагов", prefs.getBoolean(KEY_ACH_100_STEPS, false)))
        achievementItems.add(AchievementItem("Решить не более чем за 50 шагов", prefs.getBoolean(KEY_ACH_SOLVED_UNDER_50_STEPS, false)))
        // Добавьте другие достижения по аналогии

        val adapter = AchievementsAdapter(this, achievementItems)
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Для полупрозрачности и отсутствия стандартного фона диалога
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}