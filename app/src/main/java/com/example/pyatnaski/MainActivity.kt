package com.example.pyatnaski

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.gridlayout.widget.GridLayout
import com.bumptech.glide.Glide
import com.example.pyatnaski.ui.theme.PyatnaskiTheme


class MainActivity : AppCompatActivity() {
    private lateinit var themeSwitchButton: ImageButton

    private lateinit var sharedPrefs: SharedPreferences
//    private val PREFS_NAME = "ThemePrefs"
//    private val KEY_THEME = "app_theme_mode"
    private lateinit var languageButton: Button
    private lateinit var thinking_gif: ImageView
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
    private val KEY_ACH_SOLVED_UNDER_1_MIN = "ach_solved_under_1_min"
    private lateinit var achievementsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        LocaleHelper.loadLocale(this)

        super.onCreate(savedInstanceState)

        sharedPrefs = getSharedPreferences(AppPreferences.PREFS_NAME, MODE_PRIVATE)
        val currentTheme = sharedPrefs.getInt(AppPreferences.KEY_THEME, AppCompatDelegate.MODE_NIGHT_YES)
        AppCompatDelegate.setDefaultNightMode(currentTheme)

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

        thinking_gif = findViewById(R.id.imageView_gif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.thinking_gif)
            .into(thinking_gif)

        languageButton = findViewById(R.id.button_lang_switch)
        languageButton.setOnClickListener {
            changeLanguage()
        }

        themeSwitchButton = findViewById(R.id.day_night_switch)
        updateThemeSwitchIcon()

        themeSwitchButton.setOnClickListener {
            toggleTheme()
        }
        val leadersButton = findViewById<Button>(R.id.button_leaders)
        leadersButton.setOnClickListener { showLeadersDialog() }
    }

    private fun showLeadersDialog() {
        // Пока используем тестовые данные. В будущем вы будете загружать их, например, из SharedPreferences.
        val fakeLeaders = listOf(
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85),
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85),
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85),
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85),
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85),
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85),
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85),
            LeaderboardEntry("user@example.com", 120, 340),
            LeaderboardEntry("player_two@game.com", 95, 210),
            LeaderboardEntry("fifteen_master@pro.org", 45, 85)
        ).sortedBy { it.steps } // Сортируем по количеству шагов для примера

        // Создаем диалог, как и для достижений
        val dialog = AlertDialog.Builder(this)
            .create()

        // ВАЖНО: Вместо setView() с XML, мы используем setContent() с Compose
        dialog.setView(
            androidx.compose.ui.platform.ComposeView(this).apply {
                // Говорим ComposeView, что она не должна управлять своим жизненным циклом сама
                setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    // Оборачиваем наш UI в нашу новую тему
                    PyatnaskiTheme {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LeaderboardDialogContent(leaders = fakeLeaders) {
                                // Действие при нажатии на кнопку "Закрыть" в Compose
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }
        )

        // Убираем стандартный фон диалога, т.к. у нас свой фон в Compose
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }

    private fun toggleTheme() {
        val newTheme = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }

        sharedPrefs.edit().putInt(AppPreferences.KEY_THEME, newTheme).apply()

        AppCompatDelegate.setDefaultNightMode(newTheme)
    }

    private fun updateThemeSwitchIcon() {
        val isNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            themeSwitchButton.setImageResource(R.drawable.night_mode)
        } else {
            themeSwitchButton.setImageResource(R.drawable.day_mode)
        }
    }

    private fun changeLanguage(){
        val currentLang = resources.configuration.locales.get(0).language
        val newLang = if (currentLang == "ru") "en" else "ru"

        LocaleHelper.setLocale(this, newLang)
        recreate()
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
        stepsTextView.text = getString(R.string.steps_prefix, stepCount)
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
        }
    }

    private fun onTileClick(clickedTile: Button, currentEmptyTile: Button) {
        val clickedPos = getTilePosition(clickedTile)
        val emptyPos = getTilePosition(currentEmptyTile)
        if (isGamePaused) {
            return
        }
        if (clickedPos == null || emptyPos == null) {
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
        Toast.makeText(this, getString(R.string.toast_pause), Toast.LENGTH_SHORT).show()
    }

    private fun resumeGame() {
        isGamePaused = false
        if (isTimerRunning && hasGameStartedSinceShuffle) { // Возобновляем таймер, если он был активен и игра уже началась
            timerHandler.postDelayed(timerRunnable, 1000)
        }
        setGameBoardEnabled(true)
        Toast.makeText(this, getString(R.string.toast_resume), Toast.LENGTH_SHORT).show()
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

        val hadSolvedOnce = sharedPrefs.getBoolean(KEY_ACH_SOLVED_ONCE, false)
        val hadSolvedUnder1Min = sharedPrefs.getBoolean(KEY_ACH_SOLVED_UNDER_1_MIN, false)
        val hadSolvedUnder5Min = sharedPrefs.getBoolean(KEY_ACH_SOLVED_UNDER_5_MIN, false)
        val hadSolvedUnder50Steps = sharedPrefs.getBoolean(KEY_ACH_SOLVED_UNDER_50_STEPS, false)

        val editor = sharedPrefs.edit()
        editor.putBoolean(KEY_ACH_SOLVED_ONCE, true)
        if (timerSeconds <= 60) {
            editor.putBoolean(KEY_ACH_SOLVED_UNDER_1_MIN, true)
        }
        if (timerSeconds <= 300) {
            editor.putBoolean(KEY_ACH_SOLVED_UNDER_5_MIN, true)
        }
        if (stepCount <= 50) {
            editor.putBoolean(KEY_ACH_SOLVED_UNDER_50_STEPS, true)
        }
        editor.apply()

        val newlyUnlockedAchievements = mutableListOf<String>()

        if (!hadSolvedOnce) {
            newlyUnlockedAchievements.add(getString(R.string.ach_solved_once))
        }
        if (timerSeconds <= 60 && !hadSolvedUnder1Min) {
            newlyUnlockedAchievements.add(getString(R.string.ach_solved_under_1_min))
        }
        if (timerSeconds <= 300 && !hadSolvedUnder5Min) {
            newlyUnlockedAchievements.add(getString(R.string.ach_solved_under_5_min))
        }
        if (stepCount <= 50 && !hadSolvedUnder50Steps) {
            newlyUnlockedAchievements.add(getString(R.string.ach_solved_under_50_steps))
        }

        var finalMessage = getString(R.string.win_message, timerSeconds, stepCount)

        if (newlyUnlockedAchievements.isNotEmpty()) {
            finalMessage += "\n${getString(R.string.new_achievements)} ${newlyUnlockedAchievements.joinToString(", ")}"
        }

        Toast.makeText(this, finalMessage, Toast.LENGTH_LONG).show()
    }

    private fun checkAndUnlockStepAchievements() {
        if (stepCount >= 100 && !sharedPrefs.getBoolean(KEY_ACH_100_STEPS, false)) {
            sharedPrefs.edit().putBoolean(KEY_ACH_100_STEPS, true).apply()
            Toast.makeText(this, getString(R.string.toast_100_step), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAchievementsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievements, null)
        val listView = dialogView.findViewById<ListView>(R.id.listViewAchievements)
        val closeButton = dialogView.findViewById<Button>(R.id.buttonCloseAchievements)

        val achievementItems = mutableListOf<AchievementItem>()
        achievementItems.add(AchievementItem(getString(R.string.ach_solved_once), sharedPrefs.getBoolean(KEY_ACH_SOLVED_ONCE, false)))
        achievementItems.add(AchievementItem(getString(R.string.ach_solved_under_5_min), sharedPrefs.getBoolean(KEY_ACH_SOLVED_UNDER_5_MIN, false)))
        achievementItems.add(AchievementItem(getString(R.string.ach_100_steps), sharedPrefs.getBoolean(KEY_ACH_100_STEPS, false)))
        achievementItems.add(AchievementItem(getString(R.string.ach_solved_under_50_steps), sharedPrefs.getBoolean(KEY_ACH_SOLVED_UNDER_50_STEPS, false)))

        val adapter = AchievementsAdapter(this, achievementItems)
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}