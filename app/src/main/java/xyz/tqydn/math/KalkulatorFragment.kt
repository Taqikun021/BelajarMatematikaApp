package xyz.tqydn.math

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import xyz.tqydn.math.databinding.FragmentKalkulatorBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import kotlin.math.sqrt

class KalkulatorFragment : Fragment() {

    private var isFutureOperationButtonClicked: Boolean = false
    private var isInstantOperationButtonClicked: Boolean = false
    private var isEqualButtonClicked: Boolean = false
    private var currentNumber: Double = 0.0
    private var currentResult: Double = 0.0
    private var historyText = ""
    private var historyInstantOperationText = ""
    private var historyActionList: ArrayList<String> = ArrayList()
    private var currentOperation = INIT
    private var _binding: FragmentKalkulatorBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button0.setOnClickListener {
            onNumberButtonClick(ZERO)
        }

        binding.button1.setOnClickListener {
            onNumberButtonClick(ONE)
        }

        binding.button2.setOnClickListener {
            onNumberButtonClick(TWO)
        }

        binding.button3.setOnClickListener {
            onNumberButtonClick(THREE)
        }

        binding.button4.setOnClickListener {
            onNumberButtonClick(FOUR)
        }

        binding.button5.setOnClickListener {
            onNumberButtonClick(FIVE)
        }

        binding.button6.setOnClickListener {
            onNumberButtonClick(SIX)
        }

        binding.button7.setOnClickListener {
            onNumberButtonClick(SEVEN)
        }

        binding.button8.setOnClickListener {
            onNumberButtonClick(EIGHT)
        }

        binding.button9.setOnClickListener {
            onNumberButtonClick(NINE)
        }

        binding.buttonAddition.setOnClickListener {
            onFutureOperationButtonClick(ADDITION)
        }

        binding.buttonSubtraction.setOnClickListener {
            onFutureOperationButtonClick(SUBTRACTION)
        }

        binding.buttonMultiplication.setOnClickListener {
            onFutureOperationButtonClick(MULTIPLICATION)
        }

        binding.buttonDivision.setOnClickListener {
            onFutureOperationButtonClick(DIVISION)
        }

        binding.buttonCe.setOnClickListener {
            clearEntry()
        }

        binding.buttonC.setOnClickListener {
            currentNumber = 0.0
            currentResult = 0.0
            currentOperation = INIT
            historyText = ""
            historyInstantOperationText = ""
            binding.numberCurrent.text = formatDoubleToString(currentNumber)
            binding.numberHistory.text = historyText
            isFutureOperationButtonClicked = false
            isEqualButtonClicked = false
            isInstantOperationButtonClicked = false
        }

        binding.buttonBackspace.setOnClickListener {
            if (isFutureOperationButtonClicked || isInstantOperationButtonClicked || isEqualButtonClicked) return@setOnClickListener
            var currentValue = binding.numberCurrent.text.toString()
            val charsLimit = if (currentValue.first().isDigit()) 1 else 2
            currentValue = if (currentValue.length > charsLimit)
                currentValue.substring(0, currentValue.length - 1)
            else
                ZERO
            binding.numberCurrent.text = currentValue
            currentNumber = formatStringToDouble(currentValue)
        }

        binding.buttonPlusMinus.setOnClickListener {
            val currentValue = binding.numberCurrent.text.toString()
            currentNumber = formatStringToDouble(currentValue)

            if (currentNumber == 0.0) return@setOnClickListener
            currentNumber *= -1
            binding.numberCurrent.text = formatDoubleToString(currentNumber)

            if (isInstantOperationButtonClicked) {
                historyInstantOperationText = "($historyInstantOperationText)"
                historyInstantOperationText = StringBuilder().append(NEGATE).append(historyInstantOperationText).toString()
                binding.numberHistory.text = StringBuilder().append(historyText).append(currentOperation).append(historyInstantOperationText).toString()
            }

            if (isEqualButtonClicked) {
                currentOperation = INIT
            }

            isFutureOperationButtonClicked = false
            isEqualButtonClicked = false
        }

        binding.buttonComma.setOnClickListener {
            var currentValue = binding.numberCurrent.text.toString()

            if (isFutureOperationButtonClicked || isInstantOperationButtonClicked || isEqualButtonClicked) {
                currentValue = StringBuilder().append(ZERO).append(COMMA).toString()
                if (isInstantOperationButtonClicked) {
                    historyInstantOperationText = ""
                    binding.numberHistory.text = StringBuilder().append(historyText).append(currentOperation).toString()
                }
                if (isEqualButtonClicked) currentOperation = INIT
                currentNumber = 0.0
            } else if (currentValue.contains(COMMA)) {
                return@setOnClickListener
            } else currentValue = StringBuilder().append(currentValue).append(COMMA).toString()

            binding.numberCurrent.text = currentValue
            isFutureOperationButtonClicked = false
            isInstantOperationButtonClicked = false
            isEqualButtonClicked = false
        }

        binding.buttonEqual.setOnClickListener {
            if (isFutureOperationButtonClicked) {
                currentNumber = currentResult
            }

            val historyAllText = calculateResult()
            Toast.makeText(requireContext(), historyAllText, Toast.LENGTH_LONG).show()
            historyActionList.add(historyAllText)
            historyText = StringBuilder().append(formatDoubleToString(currentResult)).toString()
            binding.numberHistory.text = historyAllText
            isFutureOperationButtonClicked = false
            isEqualButtonClicked = true
        }

        binding.buttonPercentage.setOnClickListener {
            onInstantOperationButtonClick(PERCENTAGE)
        }

        binding.buttonRoot.setOnClickListener {
            onInstantOperationButtonClick(ROOT)
        }

        binding.buttonSquare.setOnClickListener {
            onInstantOperationButtonClick(SQUARE)
        }

        binding.buttonFraction.setOnClickListener {
            onInstantOperationButtonClick(FRACTION)
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun onNumberButtonClick(number: String, isHistory: Boolean = false) {
        var currentValue: String = binding.numberCurrent.text.toString()
        currentValue = if (currentValue == ZERO || isFutureOperationButtonClicked || isInstantOperationButtonClicked || isEqualButtonClicked || isHistory) number else StringBuilder().append(currentValue).append(number).toString()

        try {
            currentNumber = formatStringToDouble(currentValue)
        } catch (e: ParseException) {
            throw IllegalArgumentException("String must be number.")
        }

        binding.numberCurrent.text = currentValue

        if (isEqualButtonClicked) {
            currentOperation = INIT
            historyText = ""
        }

        if (isInstantOperationButtonClicked) {
            historyInstantOperationText = ""
            binding.numberHistory.text = StringBuilder().append(historyText).append(currentOperation).toString()
            isInstantOperationButtonClicked = false
        }

        isFutureOperationButtonClicked = false
        isEqualButtonClicked = false
    }

    private fun onFutureOperationButtonClick(operation: String) {
        if (!isFutureOperationButtonClicked && !isEqualButtonClicked) {
            calculateResult()
        }

        currentOperation = operation

        if (isInstantOperationButtonClicked) {
            isInstantOperationButtonClicked = false
            historyText = binding.numberHistory.text.toString()
        }
        binding.numberHistory.text = StringBuilder().append(historyText).append(operation).toString()
        isFutureOperationButtonClicked = true
        isEqualButtonClicked = false
    }

    private fun onInstantOperationButtonClick(operation: String) {
        var currentValue = binding.numberCurrent.text.toString()
        var thisOperationNumber = formatStringToDouble(currentValue)
        currentValue = "(${formatDoubleToString(thisOperationNumber)})"

        when (operation) {
            PERCENTAGE -> thisOperationNumber /= 100
            ROOT -> thisOperationNumber = thisOperationNumber.sqrt
            SQUARE -> thisOperationNumber *= thisOperationNumber
            FRACTION -> thisOperationNumber = 1 / thisOperationNumber
        }

        when {
            isInstantOperationButtonClicked -> {
                historyInstantOperationText = "($historyInstantOperationText)"
                historyInstantOperationText = StringBuilder().append(operation).append(historyInstantOperationText).toString()
                binding.numberHistory.text = if (isEqualButtonClicked) historyInstantOperationText else StringBuilder().append(historyText).append(currentOperation).append(historyInstantOperationText).toString()
            }
            isEqualButtonClicked -> {
                historyInstantOperationText = StringBuilder().append(operation).append(currentValue).toString()
                binding.numberHistory.text = historyInstantOperationText
            }
            else -> {
                historyInstantOperationText = StringBuilder().append(operation).append(currentValue).toString()
                binding.numberHistory.text = StringBuilder().append(historyText).append(currentOperation).append(historyInstantOperationText).toString()
            }
        }

        binding.numberCurrent.text = formatDoubleToString(thisOperationNumber)
        if (isEqualButtonClicked) currentResult = thisOperationNumber else currentNumber = thisOperationNumber
        isInstantOperationButtonClicked = true
        isFutureOperationButtonClicked = false
    }

    private fun calculateResult(): String {
        when (currentOperation) {
            INIT -> {
                currentResult = currentNumber
                historyText = StringBuilder().append(binding.numberHistory.text.toString()).toString()
            }
            ADDITION -> currentResult += currentNumber
            SUBTRACTION -> currentResult -= currentNumber
            MULTIPLICATION -> currentResult *= currentNumber
            DIVISION -> currentResult /= currentNumber
        }

        binding.numberCurrent.text = formatDoubleToString(currentResult)

        if (isInstantOperationButtonClicked) {
            isInstantOperationButtonClicked = false
            historyText = binding.numberHistory.text.toString()
            if (isEqualButtonClicked) historyText = StringBuilder().append(historyText).append(currentOperation).append(formatDoubleToString(currentNumber)).toString()
        } else {
            historyText = StringBuilder().append(historyText).append(currentOperation).append(formatDoubleToString(currentNumber)).toString()
        }

        return StringBuilder().append(historyText).append(EQUAL).append(formatDoubleToString(currentResult)).toString()
    }

    private fun useNumberFormat(): DecimalFormat {
        val symbols = DecimalFormatSymbols()
        symbols.decimalSeparator = ','
        val format = DecimalFormat("#.##############")
        format.decimalFormatSymbols = symbols
        return format
    }

    private fun formatDoubleToString(number: Double): String {
        return useNumberFormat().format(number)
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun formatStringToDouble(number: String): Double {
        return useNumberFormat().parse(number).toDouble()
    }

    private val Double.sqrt: Double get() = sqrt(this)

    private fun clearEntry(newNumber: Double = 0.0) {
        historyInstantOperationText = ""

        if (isEqualButtonClicked) {
            currentOperation = INIT
            historyText = ""
        }

        if (isInstantOperationButtonClicked) binding.numberHistory.text = StringBuilder().append(historyText).append(currentOperation).toString()

        isInstantOperationButtonClicked = false
        isFutureOperationButtonClicked = false
        isEqualButtonClicked = false

        currentNumber = newNumber
        binding.numberCurrent.text = formatDoubleToString(newNumber)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKalkulatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        private const val ZERO = "0"
        private const val ONE = "1"
        private const val TWO = "2"
        private const val THREE = "3"
        private const val FOUR = "4"
        private const val FIVE = "5"
        private const val SIX = "6"
        private const val SEVEN = "7"
        private const val EIGHT = "8"
        private const val NINE = "9"
        private const val INIT = ""
        private const val ADDITION = " + "
        private const val SUBTRACTION = " − "
        private const val MULTIPLICATION = " × "
        private const val DIVISION = " ÷ "
        private const val PERCENTAGE = ""
        private const val ROOT = "√"
        private const val SQUARE = "sqr"
        private const val FRACTION = "1/"
        private const val NEGATE = "negate"
        private const val COMMA = ","
        private const val EQUAL = " = "
    }
}