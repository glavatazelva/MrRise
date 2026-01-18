package com.example.android.tvz.hr.mrrise.ui.puzzle

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.android.tvz.hr.mrrise.R
import com.example.android.tvz.hr.mrrise.databinding.PuzzleSimonSaysBinding

class SimonSaysPuzzle(
    private val container: ViewGroup,
    private val onComplete: () -> Unit
) {

    private val binding = PuzzleSimonSaysBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        true
    )

    private val colors = listOf("RED", "BLUE", "GREEN", "YELLOW")
    private val colorValues = mapOf(
        "RED" to Color.parseColor("#E74C3C"),
        "BLUE" to Color.parseColor("#3498DB"),
        "GREEN" to Color.parseColor("#2ECC71"),
        "YELLOW" to Color.parseColor("#F1C40F")
    )

    private val pattern = mutableListOf<String>()
    private val userInput = mutableListOf<String>()
    private var currentRound = 1
    private val maxRounds = 4
    private val handler = Handler(Looper.getMainLooper())

    init {
        setupButtons()
        startNewRound()
    }

    private fun setupButtons() {
        binding.btnRed.setOnClickListener { onColorClicked("RED") }
        binding.btnBlue.setOnClickListener { onColorClicked("BLUE") }
        binding.btnGreen.setOnClickListener { onColorClicked("GREEN") }
        binding.btnYellow.setOnClickListener { onColorClicked("YELLOW") }
    }

    private fun startNewRound() {
        userInput.clear()

        // Add new random color to pattern
        pattern.add(colors.random())

        // Update UI
        binding.tvProgress.text = binding.root.context.getString(R.string.round, currentRound, maxRounds)
        binding.tvStatus.text = binding.root.context.getString(R.string.watch_pattern)

        // Disable buttons during pattern display
        setButtonsEnabled(false)

        // Show pattern
        showPattern()
    }

    private fun showPattern() {
        var delay = 500L

        pattern.forEachIndexed { index, color ->
            handler.postDelayed({
                flashColor(color)
            }, delay)
            delay += 800L
        }

        // Enable buttons after pattern is shown
        handler.postDelayed({
            binding.tvStatus.text = binding.root.context.getString(R.string.your_turn)
            setButtonsEnabled(true)
        }, delay + 500L)
    }

    private fun flashColor(color: String) {
        val colorValue = colorValues[color] ?: Color.GRAY

        // Show color
        binding.colorIndicator.setBackgroundColor(colorValue)

        // Hide after 400ms
        handler.postDelayed({
            binding.colorIndicator.setBackgroundColor(Color.parseColor("#CCCCCC"))
        }, 400)
    }

    private fun onColorClicked(color: String) {
        userInput.add(color)
        flashColor(color)

        // Check if user input matches pattern so far
        if (userInput.last() != pattern[userInput.size - 1]) {
            // Wrong! Reset
            binding.tvStatus.text = binding.root.context.getString(R.string.wrong_try_again)
            handler.postDelayed({
                startNewRound()
            }, 1000)
            return
        }

        // Check if user completed the full pattern
        if (userInput.size == pattern.size) {
            if (currentRound >= maxRounds) {
                // Puzzle complete!
                binding.tvStatus.text = binding.root.context.getString(R.string.perfect_dismissed)
                setButtonsEnabled(false)
                handler.postDelayed({
                    onComplete()
                }, 1000)
            } else {
                // Next round
                currentRound++
                binding.tvStatus.text = binding.root.context.getString(R.string.correct_next)
                handler.postDelayed({
                    startNewRound()
                }, 1000)
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnRed.isEnabled = enabled
        binding.btnBlue.isEnabled = enabled
        binding.btnGreen.isEnabled = enabled
        binding.btnYellow.isEnabled = enabled
    }
}