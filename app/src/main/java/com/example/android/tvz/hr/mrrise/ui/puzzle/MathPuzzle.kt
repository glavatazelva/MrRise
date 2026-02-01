package com.example.android.tvz.hr.mrrise.ui.puzzle

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.android.tvz.hr.mrrise.R
import com.example.android.tvz.hr.mrrise.databinding.PuzzleMathBinding
import kotlin.random.Random

class MathPuzzle(
    private val container: ViewGroup,
    private val onComplete: () -> Unit
) {

    private val binding = PuzzleMathBinding.inflate(
        LayoutInflater.from(container.context),
        container,
        true
    )

    private var correctX: Int = 0
    private var correctY: Int = 0

    init {
        generateEquations()
        setupSubmitButton()
    }

    private fun generateEquations() {
        correctX = Random.nextInt(-10, 11).let { if (it == 0) 1 else it }
        correctY = Random.nextInt(-10, 11).let { if (it == 0) 1 else it }

        val a = Random.nextInt(1, 4)
        val b = Random.nextInt(1, 3)


        val result1 = a * correctX + b * correctY
        val result2 = correctX - correctY

        val eq1 = buildEquationString(a, "X", b, "Y", result1)
        val eq2 = buildEquationString(1, "X", -1, "Y", result2)

        binding.tvEquation1.text = eq1
        binding.tvEquation2.text = eq2
    }

    private fun buildEquationString(
        coeffX: Int,
        varX: String,
        coeffY: Int,
        varY: String,
        result: Int
    ): String {
        val xPart = if (coeffX == 1) varX else "${coeffX}${varX}"
        val yPart = when {
            coeffY == 1 -> "+ $varY"
            coeffY == -1 -> "- $varY"
            coeffY > 0 -> "+ ${coeffY}${varY}"
            else -> "- ${-coeffY}${varY}"
        }

        return "$xPart $yPart = $result"
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            checkAnswer()
        }
    }

    private fun checkAnswer() {
        val userX = binding.etXValue.text.toString().toIntOrNull()
        val userY = binding.etYValue.text.toString().toIntOrNull()

        if (userX == null || userY == null) {
            binding.tvStatus.text = binding.root.context.getString(R.string.enter_both_values)
            binding.tvStatus.setTextColor(Color.RED)
            return
        }

        if (userX == correctX && userY == correctY) {

            binding.tvStatus.text = binding.root.context.getString(R.string.correct_dismissed)
            binding.tvStatus.setTextColor(Color.parseColor("#2ECC71"))
            binding.btnSubmit.isEnabled = false
            binding.etXValue.isEnabled = false
            binding.etYValue.isEnabled = false

            binding.root.postDelayed({
                onComplete()
            }, 1000)
        } else {
            binding.tvStatus.text = binding.root.context.getString(R.string.wrong_try_again)
            binding.tvStatus.setTextColor(Color.RED)

            binding.etXValue.text?.clear()
            binding.etYValue.text?.clear()
        }
    }
}