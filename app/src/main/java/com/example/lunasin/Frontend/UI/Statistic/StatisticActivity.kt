package com.example.lunasin.Frontend.UI.Statistic

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.lunasin.R // Pastikan ini mengarah ke R.java proyek Anda
import com.example.lunasin.Backend.Data.profile_data.ProfileRepository
import com.example.lunasin.Backend.Data.management_data.HutangRepository
import com.example.lunasin.Backend.model.Hutang
import com.example.lunasin.Backend.model.Tempo // Pastikan ini diimpor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class StatisticActivity : AppCompatActivity() {

    private lateinit var profileRepository: ProfileRepository
    private lateinit var hutangRepository: HutangRepository
    private lateinit var lineChart: LineChart
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvDateRange: TextView
    private lateinit var llCategoryContainer: LinearLayout

    // Formatter untuk tampilan tanggal di UI (misal: "1 Mei 2025")
    private val displayDateFormat = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
    // Formatter untuk tanggal di X-axis grafik (hanya hari, misal: "21")
    private val dayOnlyFormatter = SimpleDateFormat("dd", Locale.getDefault())
    // Formatter untuk key Map harian (penting untuk konsistensi key, misal: "2025-06-15")
    private val dailyMapKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        profileRepository = ProfileRepository()
        hutangRepository = HutangRepository(FirebaseFirestore.getInstance())

        lineChart = findViewById(R.id.lineChart)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        tvDateRange = findViewById(R.id.tvDateRange)
        llCategoryContainer = findViewById(R.id.llCategoryContainer)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "DANA Statement"

        loadAndDisplayStatistics()
    }

    private fun loadAndDisplayStatistics() {
        CoroutineScope(Dispatchers.IO).launch { // Coroutine untuk operasi I/O
            val calendar = Calendar.getInstance()
            // Set start date of the month (e.g., 1 Juni 2025, 00:00:00.000)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time

            // Set end date of the month (e.g., 30 Juni 2025, 23:59:59.999)
            calendar.add(Calendar.MONTH, 1) // Pindah ke bulan berikutnya
            calendar.add(Calendar.DAY_OF_MONTH, -1) // Kembali ke hari terakhir bulan saat ini
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endDate = calendar.time

            // Update rentang tanggal di UI (harus di Main thread)
            withContext(Dispatchers.Main) {
                tvDateRange.text = "${displayDateFormat.format(startDate)} - ${displayDateFormat.format(endDate)}"
            }

            // Ambil data profil (monthlyIncome)
            profileRepository.getUserProfile { profile ->
                val monthlyIncome = profile?.monthlyIncome ?: 0.0

                // Ambil SEMUA data hutang pengguna (operasi I/O, pakai Coroutine)
                CoroutineScope(Dispatchers.IO).launch {
                    val allHutangList = hutangRepository.getDaftarHutangForUser()

                    // Setelah data diambil, proses dan update UI (harus di Main thread)
                    withContext(Dispatchers.Main) {
                        processStatisticData(monthlyIncome, allHutangList, startDate, endDate)
                    }
                }
            }
        }
    }

    private fun processStatisticData(
        monthlyIncome: Double,
        hutangList: List<Hutang>,
        startDate: Date,
        endDate: Date
    ) {
        // --- Total Pemasukan & Pengeluaran ---
        tvIncome.text = "Rp${String.format("%,.0f", monthlyIncome)}"

        var totalExpense = 0.0
        // Map untuk menyimpan total pengeluaran per hari (Key: "yyyy-MM-dd" String, Value: Double)
        val dailyExpense = mutableMapOf<String, Double>()
        // Map untuk menyimpan total pengeluaran per kategori
        val expenseByCategory = mutableMapOf<String, Double>()

        // Iterasi melalui setiap dokumen Hutang
        for (hutang in hutangList) {
            // Iterasi melalui setiap angsuran (Tempo) dalam Hutang tersebut
            for (tempo in hutang.listTempo) {
                // Periksa apakah angsuran sudah dibayar dan memiliki tanggal pembayaran
                if (tempo.paid && tempo.paymentDate != null) {
                    val paymentDate = tempo.paymentDate

                    // Filter hanya angsuran yang tanggal pembayarannya berada dalam periode yang diminta
                    if (!paymentDate.before(startDate) && !paymentDate.after(endDate)) {
                        totalExpense += tempo.amount

                        // Tambahkan ke pengeluaran harian
                        val dateKey = dailyMapKeyFormatter.format(paymentDate) // Contoh: "2025-06-15"
                        dailyExpense[dateKey] = dailyExpense.getOrDefault(dateKey, 0.0) + tempo.amount

                        // Tambahkan ke pengeluaran per kategori.
                        // Anda bisa menggunakan hutang.namapinjaman sebagai kategori,
                        // atau field lain jika ada (misal hutang.category jika Anda tambahkan)
                        val category = hutang.namapinjaman // Atau bisa "Pembayaran Angsuran"
                        expenseByCategory[category] = expenseByCategory.getOrDefault(category, 0.0) + tempo.amount
                    }
                }
            }
        }
        tvExpense.text = "Rp${String.format("%,.0f", totalExpense)}"

        // --- Data untuk Grafik ---
        val incomeEntries = mutableListOf<Entry>()
        val expenseEntries = mutableListOf<Entry>()
        val datesForXAxis = mutableListOf<String>() // Label untuk sumbu X (hari)

        val calendar = Calendar.getInstance()
        calendar.time = startDate // Mulai dari tanggal awal periode

        var index = 0 // Indeks untuk posisi X di grafik (0, 1, 2, ...)

        // Loop melalui setiap hari dalam periode yang dipilih
        while (!calendar.time.after(endDate)) {
            val currentDate = calendar.time
            val dateKey = dailyMapKeyFormatter.format(currentDate)

            // Tambahkan label hari untuk sumbu X
            datesForXAxis.add(dayOnlyFormatter.format(currentDate))

            // Pemasukan: Asumsikan monthlyIncome terjadi di hari pertama bulan
            if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                incomeEntries.add(Entry(index.toFloat(), monthlyIncome.toFloat()))
            } else {
                // Untuk hari lain, pemasukan dianggap 0 untuk tujuan grafik ini
                incomeEntries.add(Entry(index.toFloat(), 0f))
            }

            // Pengeluaran: Ambil total pengeluaran untuk hari ini dari map
            expenseEntries.add(Entry(index.toFloat(), dailyExpense.getOrDefault(dateKey, 0.0).toFloat()))

            // Pindah ke hari berikutnya
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            index++
        }

        // Pastikan chart diupdate di Main thread
        updateChart(incomeEntries, expenseEntries, datesForXAxis)
        updateCategories(expenseByCategory)
    }

    // --- Metode untuk Update Chart (Tidak Berubah dari Sebelumnya) ---
    private fun updateChart(
        incomeEntries: List<Entry>,
        expenseEntries: List<Entry>,
        datesForXAxis: List<String>
    ) {
        val incomeDataSet = LineDataSet(incomeEntries, "Pemasukan").apply {
            color = Color.parseColor("#4CAF50") // Hijau
            setDrawCircles(true)
            circleRadius = 3f
            circleColors = listOf(Color.parseColor("#4CAF50"))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#404CAF50") // Semi-transparan hijau
            fillAlpha = 100
        }

        val expenseDataSet = LineDataSet(expenseEntries, "Pengeluaran").apply {
            color = Color.parseColor("#FF5722") // Oranye/Merah
            setDrawCircles(true)
            circleRadius = 3f
            circleColors = listOf(Color.parseColor("#FF5722"))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#40FF5722") // Semi-transparan oranye
            fillAlpha = 100
        }

        val dataSets = arrayListOf<ILineDataSet>()
        dataSets.add(incomeDataSet)
        dataSets.add(expenseDataSet)

        val data = LineData(dataSets)
        lineChart.data = data

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(datesForXAxis)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.setLabelCount(datesForXAxis.size, true)

        lineChart.axisRight.isEnabled = false
        lineChart.axisLeft.setDrawGridLines(true)
        lineChart.axisLeft.granularity = 100000f // Sesuaikan dengan skala nominal Anda

        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.invalidate()
    }

    // --- Metode untuk Update Kategori (Tidak Berubah dari Sebelumnya) ---
    private fun updateCategories(expenseByCategory: Map<String, Double>) {
        llCategoryContainer.removeAllViews()

        val sortedCategories = expenseByCategory.entries.sortedByDescending { it.value }

        for ((category, amount) in sortedCategories) {
            val categoryView = LayoutInflater.from(this).inflate(R.layout.item_category_summary, llCategoryContainer, false)
            val tvCategoryName = categoryView.findViewById<TextView>(R.id.tvCategoryName)
            val tvCategoryAmount = categoryView.findViewById<TextView>(R.id.tvCategoryAmount)
            val ivCategoryIcon = categoryView.findViewById<ImageView>(R.id.ivCategoryIcon)

            tvCategoryName.text = category
            tvCategoryAmount.text = "Rp${String.format("%,.0f", amount)}"

            when (category) {
                // Tambahkan mapping ikon untuk kategori Anda
                "Gaji" -> ivCategoryIcon.setImageResource(R.drawable.ic_salary)
                "Belanja" -> ivCategoryIcon.setImageResource(R.drawable.ic_shopping)
                "Transportasi" -> ivCategoryIcon.setImageResource(R.drawable.ic_transport)
                "Makanan" -> ivCategoryIcon.setImageResource(R.drawable.ic_food)
                "Tagihan" -> ivCategoryIcon.setImageResource(R.drawable.ic_bills)
                "Edukasi" -> ivCategoryIcon.setImageResource(R.drawable.ic_education)
                // Jika namapinjaman Anda sangat spesifik, Anda mungkin perlu ikon yang lebih generik
                "alganana" -> ivCategoryIcon.setImageResource(R.drawable.ic_person) // Contoh ikon untuk nama orang
                else -> ivCategoryIcon.setImageResource(R.drawable.ic_default_category) // Ikon default
            }

            llCategoryContainer.addView(categoryView)
        }
    }
}