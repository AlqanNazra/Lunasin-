package com.example.lunasin.Frontend.viewmodel.Statistic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lunasin.Backend.Data.management_data.HutangRepository
import com.example.lunasin.Backend.Data.profile_data.ProfileRepository
import com.example.lunasin.Backend.model.Hutang
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


data class ChartEntry(
    val x: Float,
    val y: Float
)

data class StatisticData(
    val totalMonthlyIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val incomeEntries: List<ChartEntry> = emptyList(),
    val expenseEntries: List<ChartEntry> = emptyList(),
    val datesForXAxis: List<String> = emptyList(),
    val expenseByCategory: Map<String, Double> = emptyMap(),
    val dateRangeText: String = "Memuat...",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class StatisticViewModel(
    private val profileRepository: ProfileRepository,
    private val hutangRepository: HutangRepository
) : ViewModel() {

    private val _statisticData = MutableLiveData<StatisticData>()
    val statisticData: LiveData<StatisticData> = _statisticData

    private val dailyMapKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("d MMM YYYY", Locale("id", "ID")) // Pastikan YYYY kapital untuk tahun 4 digit
    private val dayOnlyFormatter = SimpleDateFormat("dd", Locale.getDefault())

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            // Set state loading saat memulai pemuatan data
            _statisticData.value = StatisticData(
                totalMonthlyIncome = 0.0,
                totalExpense = 0.0,
                incomeEntries = emptyList(),
                expenseEntries = emptyList(),
                datesForXAxis = emptyList(),
                expenseByCategory = emptyMap(),
                dateRangeText = "Memuat data...",
                isLoading = true,
                errorMessage = null
            )
            try {
                // 1. Tentukan Periode Waktu (Bulan ini)
                val calendar = Calendar.getInstance()
                // Atur ke awal bulan
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.time

                // Atur ke akhir bulan
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1) // Kembali ke hari terakhir bulan ini
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endDate = calendar.time

                val dateRangeText = "${displayDateFormat.format(startDate)} - ${displayDateFormat.format(endDate)}"

                // 2. Ambil Data Profil (Monthly Income)
                val profile = profileRepository.getProfile()
                val monthlyIncome = profile?.monthlyIncome ?: 0.0

                // 3. Ambil Semua Data Hutang (akan difilter di sisi client)
                val allHutangList = hutangRepository.getDaftarHutang()

                // 4. Proses Data untuk Statistik
                val (totalExpense, dailyExpense, expenseByCategory) = processExpenseData(allHutangList, startDate, endDate)
                val (incomeEntries, expenseEntries, datesForXAxis) = prepareChartData(monthlyIncome, dailyExpense, startDate, endDate)

                // 5. Update LiveData dengan hasil akhir
                _statisticData.value = StatisticData(
                    totalMonthlyIncome = monthlyIncome,
                    totalExpense = totalExpense,
                    incomeEntries = incomeEntries, // Sekarang List<ChartEntry>
                    expenseEntries = expenseEntries, // Sekarang List<ChartEntry>
                    datesForXAxis = datesForXAxis,
                    expenseByCategory = expenseByCategory,
                    dateRangeText = dateRangeText,
                    isLoading = false,
                    errorMessage = null
                )

            } catch (e: Exception) {
                Log.e("StatisticViewModel", "Error loading statistics: ${e.message}", e)
                _statisticData.value = StatisticData(
                    totalMonthlyIncome = 0.0,
                    totalExpense = 0.0,
                    incomeEntries = emptyList(), // Pastikan ini juga ChartEntry
                    expenseEntries = emptyList(), // Pastikan ini juga ChartEntry
                    datesForXAxis = emptyList(),
                    expenseByCategory = emptyMap(),
                    dateRangeText = "Gagal memuat data",
                    isLoading = false,
                    errorMessage = "Gagal memuat statistik: ${e.localizedMessage}"
                )
            }
        }
    }

    // Fungsi untuk memproses data pengeluaran dari daftar hutang
    private fun processExpenseData(
        hutangList: List<Hutang>,
        startDate: Date,
        endDate: Date
    ): Triple<Double, MutableMap<String, Double>, MutableMap<String, Double>> {
        var totalExpense = 0.0
        val dailyExpense = mutableMapOf<String, Double>()
        val expenseByCategory = mutableMapOf<String, Double>()

        for (hutang in hutangList) {
            for (tempo in hutang.listTempo) {
                if (tempo.paid && tempo.paymentDate != null) {
                    val paymentDate = tempo.paymentDate
                    if (!paymentDate.before(startDate) && !paymentDate.after(endDate)) {
                        totalExpense += tempo.amount
                        val dateKey = dailyMapKeyFormatter.format(paymentDate)
                        dailyExpense[dateKey] = dailyExpense.getOrDefault(dateKey, 0.0) + tempo.amount
                        val category = hutang.namapinjaman
                        expenseByCategory[category] = expenseByCategory.getOrDefault(category, 0.0) + tempo.amount
                    }
                }
            }
        }
        return Triple(totalExpense, dailyExpense, expenseByCategory)
    }

    // Fungsi untuk menyiapkan data Entry untuk grafik
    private fun prepareChartData(
        monthlyIncome: Double,
        dailyExpense: Map<String, Double>,
        startDate: Date,
        endDate: Date
    ): Triple<List<ChartEntry>, List<ChartEntry>, List<String>> { // UBAH TIPE HASIL KEMBALIAN
        val incomeEntries = mutableListOf<ChartEntry>() // UBAH TIPE INI
        val expenseEntries = mutableListOf<ChartEntry>() // UBAH TIPE INI
        val datesForXAxis = mutableListOf<String>()

        val calendar = Calendar.getInstance()
        calendar.time = startDate
        var index = 0

        while (!calendar.time.after(endDate)) {
            val currentDate = calendar.time
            val dateKey = dailyMapKeyFormatter.format(currentDate)

            datesForXAxis.add(dayOnlyFormatter.format(currentDate))

            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                incomeEntries.add(ChartEntry(index.toFloat(), monthlyIncome.toFloat())) // GUNAKAN ChartEntry KUSTOM
            } else {
                incomeEntries.add(ChartEntry(index.toFloat(), 0f)) // GUNAKAN ChartEntry KUSTOM
            }

            expenseEntries.add(ChartEntry(index.toFloat(), dailyExpense.getOrDefault(dateKey, 0.0).toFloat())) // GUNAKAN ChartEntry KUSTOM

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            index++
        }
        return Triple(incomeEntries, expenseEntries, datesForXAxis)
    }
}

class StatisticViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val hutangRepository: HutangRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticViewModel(profileRepository, hutangRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}