package com.kp.momoney.presentation.reports

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kp.momoney.data.local.CurrencyPreference
import com.kp.momoney.domain.repository.CurrencyRepository
import com.kp.momoney.domain.repository.TransactionRepository
import com.kp.momoney.util.CsvUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.util.Calendar
import javax.inject.Inject

data class CategoryPercentage(
    val categoryName: String,
    val colorHex: String,
    val totalAmount: Double,
    val percentage: Float
)

data class ReportsUiState(
    val items: List<CategoryPercentage> = emptyList(),
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true,
    val currencyPreference: CurrencyPreference = CurrencyPreference("KES", "KSh", 1.0f)
)

data class IncomeExpenseState(
    val income: Double,
    val expense: Double,
    val currencyPreference: CurrencyPreference = CurrencyPreference("KES", "KSh", 1.0f)
)

data class DailyPoint(
    val day: Int,
    val amount: Double
)

data class DailyTrendState(
    val points: List<DailyPoint>,
    val currencyPreference: CurrencyPreference = CurrencyPreference("KES", "KSh", 1.0f)
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    // Get currency preference flow
    private val currencyPreference = currencyRepository.getCurrencyPreference()

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _incomeExpenseState = MutableStateFlow(IncomeExpenseState(0.0, 0.0))
    val incomeExpenseState: StateFlow<IncomeExpenseState> = _incomeExpenseState.asStateFlow()

    private val _dailyTrendState = MutableStateFlow<List<DailyPoint>>(emptyList())
    val dailyTrendState: StateFlow<List<DailyPoint>> = _dailyTrendState.asStateFlow()

    private val _filterDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val filterDateRange: StateFlow<Pair<Long, Long>?> = _filterDateRange.asStateFlow()

    init {
        observeTransactions()
        observeIncomeVsExpense()
        observeDailyTrend()
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                _filterDateRange.flatMapLatest { dateRange ->
                    if (dateRange == null) {
                        transactionRepository.getAllTransactions()
                    } else {
                        transactionRepository.getTransactionsByDateRange(dateRange.first, dateRange.second)
                    }
                },
                currencyPreference
            ) { transactions, currency ->
                Pair(transactions, currency)
            }.collect { (transactions, currency) ->
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
                    isLoading = false,
                    currencyPreference = currency
                )
            }
        }
    }

    private fun observeIncomeVsExpense() {
        viewModelScope.launch {
            combine(
                _filterDateRange.flatMapLatest { dateRange ->
                    if (dateRange == null) {
                        transactionRepository.getAllTransactions()
                    } else {
                        transactionRepository.getTransactionsByDateRange(dateRange.first, dateRange.second)
                    }
                },
                currencyPreference
            ) { transactions, currency ->
                Pair(transactions, currency)
            }.collect { (transactions, currency) ->
                // If date range is filtered, use all transactions in the range
                // Otherwise, filter to current month (original behavior)
                val filteredTransactions = if (_filterDateRange.value == null) {
                    val calendar = Calendar.getInstance()
                    val currentMonth = calendar.get(Calendar.MONTH)
                    val currentYear = calendar.get(Calendar.YEAR)

                    transactions.filter { transaction ->
                        val transactionCalendar = Calendar.getInstance().apply {
                            time = transaction.date
                        }
                        transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                                transactionCalendar.get(Calendar.YEAR) == currentYear
                    }
                } else {
                    transactions
                }

                val totalIncome = filteredTransactions
                    .filter { it.type.equals("Income", ignoreCase = true) }
                    .sumOf { it.amount }

                val totalExpense = filteredTransactions
                    .filter { it.type.equals("Expense", ignoreCase = true) }
                    .sumOf { it.amount }

                _incomeExpenseState.value = IncomeExpenseState(
                    income = totalIncome,
                    expense = totalExpense,
                    currencyPreference = currency
                )
            }
        }
    }

    private fun observeDailyTrend() {
        viewModelScope.launch {
            combine(
                _filterDateRange.flatMapLatest { dateRange ->
                    if (dateRange == null) {
                        transactionRepository.getAllTransactions()
                    } else {
                        transactionRepository.getTransactionsByDateRange(dateRange.first, dateRange.second)
                    }
                },
                currencyPreference
            ) { transactions, currency ->
                Pair(transactions, currency)
            }.collect { (transactions, currency) ->
                val dateRange = _filterDateRange.value
                
                // If date range is filtered, use the filtered range
                // Otherwise, use current month (original behavior)
                val expenses = if (dateRange == null) {
                    val calendar = Calendar.getInstance()
                    val currentMonth = calendar.get(Calendar.MONTH)
                    val currentYear = calendar.get(Calendar.YEAR)

                    transactions.filter { transaction ->
                        val transactionCalendar = Calendar.getInstance().apply {
                            time = transaction.date
                        }
                        transactionCalendar.get(Calendar.MONTH) == currentMonth &&
                                transactionCalendar.get(Calendar.YEAR) == currentYear &&
                                transaction.type.equals("Expense", ignoreCase = true)
                    }
                } else {
                    transactions.filter { transaction ->
                        transaction.type.equals("Expense", ignoreCase = true)
                    }
                }

                // Determine the day range
                val calendar = Calendar.getInstance()
                val daysInRange = if (dateRange == null) {
                    // Current month
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                } else {
                    // Calculate days between start and end date
                    val startCal = Calendar.getInstance().apply {
                        timeInMillis = dateRange.first
                    }
                    val endCal = Calendar.getInstance().apply {
                        timeInMillis = dateRange.second
                    }
                    val daysDiff = ((dateRange.second - dateRange.first) / (1000 * 60 * 60 * 24)).toInt() + 1
                    daysDiff.coerceAtMost(31) // Cap at 31 days for display
                }

                val dailyMap = mutableMapOf<Int, Double>()
                
                // Initialize all days with 0.0
                for (day in 1..daysInRange) {
                    dailyMap[day] = 0.0
                }

                // Group expenses by day
                expenses.forEach { transaction ->
                    val transactionCalendar = Calendar.getInstance().apply {
                        time = transaction.date
                    }
                    val day = transactionCalendar.get(Calendar.DAY_OF_MONTH)
                    if (day <= daysInRange) {
                        dailyMap[day] = dailyMap.getOrDefault(day, 0.0) + transaction.amount
                    }
                }

                val dailyPoints = dailyMap.map { (day, amount) ->
                    DailyPoint(day = day, amount = amount)
                }.sortedBy { it.day }

                _dailyTrendState.value = dailyPoints
            }
        }
    }

    fun onDateRangeChanged(newRange: Pair<Long, Long>?) {
        _filterDateRange.value = newRange
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                // Fetch current currency preference
                val currencyState = currencyRepository.getCurrencyPreference().first()
                
                // Fetch all transactions
                val transactions = transactionRepository.getAllTransactions().first()
                
                // Generate CSV string with currency conversion
                val csvContent = CsvUtils.generateCsv(transactions, currencyState)
                
                // Write to file
                val file = File(context.cacheDir, "finance_export.csv")
                file.writeText(csvContent)
                
                // Get URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                // Create intent to share
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // Launch share chooser
                context.startActivity(Intent.createChooser(intent, "Export via"))
            } catch (e: Exception) {
                // Error handling - could show a toast or snackbar
                e.printStackTrace()
            }
        }
    }
}


