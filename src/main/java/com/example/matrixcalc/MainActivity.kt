package com.example.matrixcalc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.apache.commons.math3.linear.*
import org.apache.commons.math3.linear.MatrixUtils
import java.util.*
import org.apache.commons.math3.linear.EigenDecomposition
import org.apache.commons.math3.linear.LUDecomposition
import kotlin.math.roundToInt




class MainActivity : AppCompatActivity() {


    private lateinit var resultLabel: TextView
    private lateinit var matrixInputA: EditText
    private lateinit var matrixInputB: EditText
    private lateinit var scaler: EditText
    private lateinit var operatorSpinner: Spinner
    private lateinit var calculateButton: Button
    private lateinit var scalarEditText: EditText


    private val operators = arrayOf(
        "Addition", "Subtraction", "Multiplication", "Determinant", "Inverse", "Transpose",
        "Rank", "Multiply by Scalar", "Row Echelon Form", "To the Power of",
        "Solve Linear System"
    )


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultLabel = findViewById(R.id.resultLabel)
        matrixInputA = findViewById(R.id.matrixInputA)
        matrixInputB = findViewById(R.id.matrixInputB)
        operatorSpinner = findViewById(R.id.operatorSpinner)
        calculateButton = findViewById(R.id.calculateButton)
        scaler = findViewById(R.id.scaler)







        operatorSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, operators)
        calculateButton.setOnClickListener { calculate() }
        scalarEditText = findViewById(R.id.scaler)
        scalarEditText.visibility = View.GONE

        operatorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Handle the case where "Multiply by Scalar" is selected
                if (operators[position] == "Multiply by Scalar") {
                    scalarEditText.visibility = View.VISIBLE
                } else if (operators[position] == "To the Power of") {
                    scalarEditText.hint = "Enter the power"
                    scalarEditText.visibility = View.VISIBLE
                } else {
                    scalarEditText.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun calculate() {

        val matrixA = parseMatrix(matrixInputA.text.toString())
        val matrixB = parseMatrix(matrixInputB.text.toString())



        if (matrixA == null) {
            resultLabel.text = "Invalid matrix input"
            return
        }
        val scalar: Double? = if (operatorSpinner.selectedItem == "Multiply by Scalar") {
            scalarEditText.text.toString().toDoubleOrNull()
        } else if (operatorSpinner.selectedItem == "To the Power of") {
            scalarEditText.text.toString().toDoubleOrNull()
        } else {
            null
        }


        val operation = operatorSpinner.selectedItem.toString()


        val resultMatrix = when (operation) {
            "Addition", "Subtraction", "Multiplication" -> matrixB?.let {
                performBinaryOperation(
                    matrixA,
                    it, operation
                )
            }
            "Determinant" -> determinant(matrixA)
            "Inverse" -> inverse(matrixA)
            "Transpose" -> transpose(matrixA)
            "Rank" -> calculateRank(rowEchelonForm(matrixA))
            "Multiply by Scalar" -> multiplyMatrixByScalar(matrixA, scalar ?: 0.0)
            "Row Echelon Form" -> rowEchelonForm(matrixA)
            "To the Power of" -> matrixToPower(matrixA, scalar ?: 0.0)

            "Solve Linear System" -> solveLinearSystem(matrixA)

            else -> null
        }


        try {

            "Result:  ${matrixToString(resultMatrix as Array<DoubleArray>)}".also {
                resultLabel.text = it
            }


        } catch (e: Exception) {


            resultLabel.text = "Result: ${resultMatrix.toString()}"

        }


    }


    private fun performBinaryOperation(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>,
        operation: String
    ): Array<DoubleArray>? {
        return when (operation) {
            "Addition" -> addMatrices(matrixA, matrixB)
            "Subtraction" -> subtractMatrices(matrixA, matrixB)
            "Multiplication" -> multiplyMatrices(matrixA, matrixB)
            else -> null
        }
    }


    private fun parseMatrix(inputText: String): Array<DoubleArray>? {
        try {
            val rows = inputText.trim().split('\n')
            return rows.map { row ->
                row.trim().split(' ').map { it.toDouble() }.toDoubleArray()
            }.toTypedArray()
        } catch (e: Exception) {
            return null
        }
    }


    private fun determinant(matrix: Array<DoubleArray>): Double {
        return try {
            val realMatrix: RealMatrix = MatrixUtils.createRealMatrix(matrix)
            val luDecomposition = LUDecomposition(realMatrix)
            val det = luDecomposition.determinant
            det
        } catch (e: Exception) {
            Double.NaN
        }
    }


    @SuppressLint("SetTextI18n")
    private fun inverse(matrix: Array<DoubleArray>): Array<DoubleArray>? {

        return try {
            if (determinant(matrix) == 0.0) {
                null
            } else {
                val realMatrix: RealMatrix = MatrixUtils.createRealMatrix(matrix)
                val invMatrix = MatrixUtils.inverse(realMatrix).data
                invMatrix
            }
        } catch (e: Exception) {
            null
        }
    }


    @JvmName("transpose1")
    private fun transpose(matrix: Array<DoubleArray>): Array<DoubleArray> {
        return matrix.transpose()
    }


    private fun calculateRank(matrix: Array<DoubleArray>): Int {
        return try {
            val realMatrix = Matrix(matrix)
            val rank = realMatrix.rank()
            rank
        } catch (e: Exception) {
            0
        }
    }

    private class Matrix(private val data: Array<DoubleArray>) {
        fun rank(): Int {
            val rowCount = data.size
            val colCount = data.firstOrNull()?.size ?: 0

            val augmentedMatrix = Array(rowCount) { row ->
                DoubleArray(colCount + 1) { col ->
                    if (col < colCount) data[row][col] else 0.0
                }
            }

            var rank = 0
            var lead = 0

            for (row in 0 until rowCount) {
                if (lead >= colCount) break

                var i = row
                while (i < rowCount && augmentedMatrix[i][lead] == 0.0) {
                    i++
                }

                if (i == rowCount) {

                    lead++
                    continue
                }


                val tempRow = augmentedMatrix[i]
                augmentedMatrix[i] = augmentedMatrix[row]
                augmentedMatrix[row] = tempRow

                val lv = augmentedMatrix[row][lead]
                for (j in 0 until colCount + 1) {
                    augmentedMatrix[row][j] /= lv
                }

                for (i in 0 until rowCount) {
                    if (i != row) {
                        val multiple = augmentedMatrix[i][lead]
                        for (j in 0 until colCount + 1) {
                            augmentedMatrix[i][j] -= multiple * augmentedMatrix[row][j]
                        }
                    }
                }
                lead++
                rank++
            }

            return rank
        }
    }


    private fun multiplyMatrixByScalar(
        matrix: Array<DoubleArray>,
        scalar: Double
    ): Array<DoubleArray> {
        return matrix.map { row ->
            row.map { value -> value * scalar }.toDoubleArray()
        }.toTypedArray()
    }
    private fun matrixToPower(matrix: Array<DoubleArray>, power: Double): Array<DoubleArray>? {
        val powerr: Int = power.roundToInt()
        return try {
            val realMatrix: RealMatrix = MatrixUtils.createRealMatrix(matrix)
            var poweredMatrix = realMatrix

            repeat(powerr - 1) {
                poweredMatrix = poweredMatrix.multiply(realMatrix)
            }

            poweredMatrix.data
        } catch (e: Exception) {
            null
        }
    }


    private fun solveLinearSystem(augmentedMatrix: Array<DoubleArray>): String? {
        return try {
            val realMatrix: RealMatrix = MatrixUtils.createRealMatrix(augmentedMatrix)

            // Check if the matrix is square
            if (realMatrix.rowDimension != realMatrix.columnDimension - 1) {
                return null  // The matrix is not a valid augmented matrix
            }

            val numRows = realMatrix.rowDimension
            val numCols = realMatrix.columnDimension - 1

            // Perform forward elimination
            for (pivotRow in 0 until numRows - 1) {
                for (currentRow in pivotRow + 1 until numRows) {
                    val factor = realMatrix.getEntry(currentRow, pivotRow) / realMatrix.getEntry(
                        pivotRow,
                        pivotRow
                    )
                    for (col in pivotRow until numCols + 1) {
                        realMatrix.addToEntry(
                            currentRow,
                            col,
                            -factor * realMatrix.getEntry(pivotRow, col)
                        )
                    }
                }
            }


            val solution = DoubleArray(numRows)
            for (row in numRows - 1 downTo 0) {
                solution[row] = realMatrix.getEntry(row, numCols) / realMatrix.getEntry(row, row)
                for (k in row - 1 downTo 0) {
                    realMatrix.addToEntry(k, numCols, -realMatrix.getEntry(k, row) * solution[row])
                }
            }


            val s = doubleArrayToString(solution)
            s

        } catch (e: Exception) {
            "null"
        }
    }

    fun doubleArrayToString(doubleArray: DoubleArray): String {
        return doubleArray.joinToString(" ") { it.toString() }
    }


    private fun rowEchelonForm(matrix: Array<DoubleArray>): Array<DoubleArray> {
        val numRows = matrix.size
        val numCols = matrix.firstOrNull()?.size ?: 0

        var lead = 0
        for (row in 0 until numRows) {
            if (lead >= numCols) {
                break
            }

            var i = row
            while (i < numRows && matrix[i][lead] == 0.0) {
                i++
            }

            if (i == numRows) {

                lead++
                continue
            }


            val tempRow = matrix[i]
            matrix[i] = matrix[row]
            matrix[row] = tempRow

            val lv = matrix[row][lead]
            for (j in 0 until numCols) {
                matrix[row][j] /= lv
            }

            for (i in 0 until numRows) {
                if (i != row) {
                    val multiple = matrix[i][lead]
                    for (j in 0 until numCols) {
                        matrix[i][j] -= multiple * matrix[row][j]
                    }
                }
            }
            lead++
        }

        return matrix
    }


    private fun addMatrices(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>
    ): Array<DoubleArray>? {
        return try {
            matrixA.mapIndexed { rowIndex, row ->
                row.mapIndexed { colIndex, value -> value + matrixB[rowIndex][colIndex] }
                    .toDoubleArray()
            }.toTypedArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun subtractMatrices(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>
    ): Array<DoubleArray>? {
        return try {
            matrixA.mapIndexed { rowIndex, row ->
                row.mapIndexed { colIndex, value -> value - matrixB[rowIndex][colIndex] }
                    .toDoubleArray()
            }.toTypedArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun multiplyMatrices(
        matrixA: Array<DoubleArray>,
        matrixB: Array<DoubleArray>
    ): Array<DoubleArray>? {
        return try {
            val result = Array(matrixA.size) { DoubleArray(matrixB[0].size) }

            for (i in matrixA.indices) {
                for (j in matrixB[0].indices) {
                    for (k in matrixB.indices) {
                        result[i][j] += matrixA[i][k] * matrixB[k][j]
                    }
                }
            }
            result
        } catch (e: Exception) {
            null
        }
    }

    private fun matrixToString(matrix: Array<DoubleArray>): String {
        return matrix.joinToString("\n") { row ->
            row.joinToString(" ") { if (it == 0.0) "0.0" else it.toString() }
        }
    }


    private fun DoubleArray.toDoubleArray(): Array<Double> {
        return Array(size) { this[it] }
    }


    private fun Array<DoubleArray>.transpose(): Array<DoubleArray> {
        return if (isEmpty() || this[0].isEmpty()) {

            emptyArray()
        } else {
            val numRows = this.size
            val numCols = this[0].size
            Array(numCols) { col ->
                DoubleArray(numRows) { row ->
                    this[row][col]
                }
            }
        }
    }
}





