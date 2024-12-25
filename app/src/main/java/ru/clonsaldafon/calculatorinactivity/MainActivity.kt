package ru.clonsaldafon.calculatorinactivity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val input = findViewById<EditText>(R.id.input)
        setupButtonClickListeners(input)
    }

    private fun setupButtonClickListeners(input: EditText) {
        val buttonSymbols = mutableListOf<Button>(
            findViewById(R.id.button_zero),
            findViewById(R.id.button_one),
            findViewById(R.id.button_two),
            findViewById(R.id.button_three),
            findViewById(R.id.button_four),
            findViewById(R.id.button_five),
            findViewById(R.id.button_six),
            findViewById(R.id.button_seven),
            findViewById(R.id.button_eight),
            findViewById(R.id.button_nine),
            findViewById(R.id.button_addition),
            findViewById(R.id.button_subtraction),
            findViewById(R.id.button_multiply),
            findViewById(R.id.button_division)
        )

        buttonSymbols.forEach {
            it.setOnClickListener { view ->
                addSymbol((view as Button).text, input)
            }
        }

        findViewById<ImageView>(R.id.image_view_remove_symbol).setOnClickListener {
            removeSymbol(input)
        }

        findViewById<Button>(R.id.button_result).setOnClickListener {
            val expression = input.text.toString()

            if (expression.isNotEmpty()) {
                try {
                    val result: CharSequence = "${calculate(expression)}"
                    input.setText(result)
                } catch (e: Exception) {
                    if (e.message == "Division by zero.")
                        input.setText("Деление на ноль")
                }
            }
        }
    }

    private fun addSymbol(value: CharSequence, input: EditText) {
        val text = input.text

        if (text.toString().isNotEmpty())
            if (text.last().isLetter() || text.toString().contains("."))
                input.setText("")

        if (value in "+-*/")
            if (input.text.isEmpty() || !lastSymbolIsNotOperator(input))
                return

        val newValue: CharSequence = "${input.text}${value}"
        input.setText(newValue)
    }

    private fun lastSymbolIsNotOperator(input: EditText): Boolean {
        val text = input.text

        if (text.isNotEmpty())
            return text.last() !in "+-*/"

        return false
    }

    private fun removeSymbol(input: EditText) {
        val newValue = input.text.dropLast(1)
        input.setText(newValue)
    }

    private fun calculate(expression: String): Double {
        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Char>()
        var currentNumber = ""

        for (char in expression) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else {
                if (currentNumber.isNotEmpty()) {
                    numbers.add(currentNumber.toDouble())
                    currentNumber = ""
                }
                if (char in listOf('*', '/', '+', '-')) {
                    while (operators.isNotEmpty() && precedence(operators.last()) >= precedence(char)) {
                        val operator = operators.removeAt(operators.size - 1)
                        val right = numbers.removeAt(numbers.size - 1)
                        val left = numbers.removeAt(numbers.size - 1)
                        numbers.add(applyOperation(left, right, operator))
                    }
                    operators.add(char)
                }
            }
        }

        if (currentNumber.isNotEmpty()) {
            numbers.add(currentNumber.toDouble())
        }

        while (operators.isNotEmpty()) {
            val operator = operators.removeAt(operators.size - 1)
            val right = numbers.removeAt(numbers.size - 1)
            val left = numbers.removeAt(numbers.size - 1)
            numbers.add(applyOperation(left, right, operator))
        }

        return numbers.first()
    }

    private fun precedence(op: Char): Int {
        return when (op) {
            '+', '-' -> 1
            '*', '/' -> 2
            else -> 0
        }
    }

    private fun applyOperation(left: Double, right: Double, operator: Char): Double {
        return when (operator) {
            '+' -> left + right
            '-' -> left - right
            '*' -> left * right
            '/' -> {
                if (right == 0.0) throw IllegalArgumentException("Division by zero.")
                left / right
            }
            else -> 0.0
        }
    }
}