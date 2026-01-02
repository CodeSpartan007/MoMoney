package com.kp.momoney.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryPercentage(
    val categoryName: String,
    val colorHex: String,
    val totalAmount: Double,
    val percentage: Float
)

data class ReportsUiState(
    val items: List<CategoryPercentage> = emptyList(),
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true
)

data class IncomeExpenseState(
    val income: Double,
    val expense: Double
)

data class DailyPoint(
    val day: Int,
    val amount: Double
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _incomeExpenseState = MutableStateFlow(IncomeExpenseState(0.0, 0.0))
    val incomeExpenseState: StateFlow<IncomeExpenseState> = _incomeExpenseState.asStateFlow()

    private val _dailyTrendState = MutableStateFlow<List<DailyPoint>>(emptyList())
    val dailyTrendState: StateFlow<List<DailyPoint>> = _dailyTrendState.asStateFlow()

    init {
        observeTransactions()
        observeIncomeVsExpense()
        observeDailyTrend()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                val expenseTransactions = transactions.filter {
                    it.type.equals("Expense", ignoreCase = true)
                }

                val totalExpense = expenseTransactions.sumOf { it.amount }

                if (totalExpense <= 0.0) {
                    _uiState.value = ReportsUiState(
                        items = emptyList(),
                        totalExpense = 0.0,
                        isLoading = false
                    )
                    return@collect
                }

                val grouped = expenseTransactions
                    .groupBy { it.categoryName.ifBlank { "Uncategorized" } }

                val items = grouped.map { (categoryName, list) ->
                    val total = list.sumOf { it.amount }
                    val rawPercentage = if (totalExpense > 0.0) {
                        (total / totalExpense * 100.0)
                    } else {
                        0.0
                    }

                    val colorHex = list.firstOrNull()?.categoryColor.orEmpty()

                    CategoryPercentage(
                        categoryName = categoryName,
                        colorHex = colorHex,
                        totalAmount = total,
                        percentage = rawPercentage.toFloat()
                    )
                }.sortedByDescending { it.totalAmount }

                _uiState.value = ReportsUiState(
                    items = items,
                    totalExpense = totalExpense,
                    isLoading = false
                )
            }
        }
    }

    private fun observeIncomeVsExpense() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                val currentMonthTransactions = transactions.filter { transaction ->
                    val transactionCalendar = Calendar.getInstance().apply {
                        time = transaction.date
                    }
                    transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                            transactionCalendar.get(Calendar.YEAR) == currentYear
                }

                val totalIncome = currentMonthTransactions
                    .filter { it.type.equals("Income", ignoreCase = true) }
                    .sumOf { it.amount }

                val totalExpense = currentMonthTransactions
                    .filter { it.type.equals("Expense", ignoreCase = true) }
                    .sumOf { it.amount }

                _incomeExpenseState.value = IncomeExpenseState(
                    income = totalIncome,
                    expense = totalExpense
                )
            }
        }
    }

    private fun observeDailyTrend() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { transactions ->
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                val currentMonthExpenses = transactions.filter { transaction ->
                    val transactionCalendar = Calendar.getInstance().apply {
                        time = transaction.date
                    }
                    transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                            transactionCalendar.get(Calendar.YEAR) == currentYear &&
                            transaction.type.equals("Expense", ignoreCase = true)
                }

                val dailyMap = mutableMapOf<Int, Double>()
                
                // Initialize all days with 0.0
                for (day in 1..daysInMonth) {
                    dailyMap[day] = 0.0
                }

                // Group expenses by day
                currentMonthExpenses.forEach { transaction ->
                    val transactionCalendar = Calendar.getInstance().apply {
                        time = transaction.date
                    }
                    val day = transactionCalendar.get(Calendar.DAY_OF_MONTH)
                    dailyMap[day] = dailyMap.getOrDefault(day, 0.0) + transaction.amount
                }

                val dailyPoints = dailyMap.map { (day, amount) ->
                    DailyPoint(day = day, amount = amount)
                }.sortedBy { it.day }

                _dailyTrendState.value = dailyPoints
            }
        }
    }
}


