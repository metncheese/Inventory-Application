package com.example.inventoryapplication.view.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.inventoryapplication.R
import com.example.inventoryapplication.databinding.FragmentHomeBinding
import com.example.inventoryapplication.models.UserModel
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.Result
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Duration
import com.example.inventoryapplication.utils.TenggatNotificationWorker

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var lastSession: UserModel

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduleTenggatNotification(requireContext())
//        binding.btnTestNotif.setOnClickListener {
//            triggerTenggatNotifWorker(requireContext())
//        }
        setupPieChart()
        observeProfile()
        observeDashboard()

        lifecycleScope.launch {
            viewModel.getSession().collect { session: UserModel ->
                Log.d("HomeFragment", "🟢 Collected session: $session")
                if (session.token.isNotEmpty()) {
                    lastSession = session
                    Log.d("HomeFragment", "👤 Aktif: ${session.username}, role=${session.role}")
                    viewModel.loadUserSessionAndProfile()
                } else {
                    Log.w("HomeFragment", "⚠️ Session belum valid, menunggu...")
                }
            }
        }

        binding.profileImg.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNav.selectedItemId = R.id.navigation_profile
        }
    }

    override fun onResume() {
        super.onResume()
        if (::lastSession.isInitialized && lastSession.token.isNotEmpty()) {
            Log.d("HomeFragment", "🔁 onResume: refreshDashboard dipanggil")
            viewModel.refreshDashboard()
        }
    }

    private fun observeProfile() {
        viewModel.profile.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {}
                is Result.Success -> {
                    val user = result.data
                    binding.txtName.text = user.username
                    binding.txtEmail.text = user.email

                    val fullPhotoUrl = user.photoProfile?.let { photo ->
                        if (photo.startsWith("http")) photo
                        else "https://fauziewan.my.id/fauziewan.my.id/storage/profile_photos/$photo"
                    }

                    Glide.with(this)
                        .load(fullPhotoUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.profileImg)
                }
                is Result.Error -> {
                    Log.e("HomeFragment", "❌ Error loading profile", result.exception)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeDashboard() {
        viewModel.dashboardData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("HomeFragment", "📊 Loading dashboard...")
                }
                is Result.Success -> {
                    val dashboard = result.data
                    binding.tvValue1.text = dashboard.asetTersedia.toString()
                    binding.tvValue2.text = dashboard.asetDipinjam.toString()
                    binding.tvValue3.text = dashboard.totalAset.toString()
                    binding.tvValue4.text = dashboard.asetPerusahaan.toString()
                    binding.tvValue5.text = dashboard.asetPinjaman.toString()

                    // Contoh PieChart update (silakan sesuaikan entry sesuai kebutuhan)
                    val entries = listOf(
                        PieEntry(dashboard.asetTersedia.toFloat(), "Aset Tersedia"),
                        PieEntry(dashboard.asetDipinjam.toFloat(), "Aset Dipinjam"),
                        PieEntry(dashboard.asetPerusahaan.toFloat(), "Aset Perusahaan"),
                        PieEntry(dashboard.asetPinjaman.toFloat(), "Aset Pinjaman")
                    )

                    val dataSet = PieDataSet(entries, "")
                    dataSet.colors = listOf(
                        Color.rgb(255, 165, 0), // orange
                        Color.BLUE,
                        Color.GREEN,
                        Color.GRAY
                    )
                    dataSet.sliceSpace = 3f
                    dataSet.setDrawValues(false)

                    val pieData = PieData(dataSet)
                    binding.donutChart.data = pieData
                    binding.donutChart.centerText = "${dashboard.totalAset} Aset"
                    binding.donutChart.invalidate()
                }
                is Result.Error -> {
                    Log.e("HomeFragment", "❌ Error loading dashboard", result.exception)
                }
            }
        }
    }

    private fun setupPieChart() {
        val donutChart = binding.donutChart
        donutChart.description.isEnabled = false
        donutChart.isDrawHoleEnabled = true
        donutChart.holeRadius = 60f
        donutChart.centerText = "Loading..."
        donutChart.animateY(1000)
        donutChart.setDrawEntryLabels(false)

        val legend = donutChart.legend
        legend.isWordWrapEnabled = true
        legend.textSize = 12f
        legend.xEntrySpace = 10f
        legend.yEntrySpace = 5f
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    private fun triggerTenggatNotifWorker(context: Context) {
//        val testWork = OneTimeWorkRequestBuilder<TenggatNotificationWorker>().build()
//        WorkManager.getInstance(context).enqueue(testWork)
//    }

    private fun scheduleTenggatNotification(context: Context) {
        // Setiap hari sekali (repeatInterval 1 hari)
        val workRequest = PeriodicWorkRequestBuilder<TenggatNotificationWorker>(
            1, TimeUnit.DAYS
        )
            // Tambahkan initialDelay supaya eksekusi pertama tepat jam 8 pagi
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "TenggatNotificationWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = LocalDateTime.now()
        val nextRun = now.withHour(8).withMinute(0).withSecond(0)
        val delay = if (now.isAfter(nextRun)) Duration.between(now, nextRun.plusDays(1)) else Duration.between(now, nextRun)
        return delay.toMillis()
    }
}
