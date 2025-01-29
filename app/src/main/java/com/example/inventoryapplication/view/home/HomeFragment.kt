package com.example.inventoryapplication.view.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.inventoryapplication.databinding.FragmentHomeBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
    }

    private fun setupPieChart() {
        val donutChart = binding.donutChart

        // Sample data
        val entries = listOf(
            PieEntry(36f, "Aset Keluar"),
            PieEntry(30f, "Aset Pinjaman"),
            PieEntry(19f, "Aset Perusahaan"),
            PieEntry(15f, "Total Aset Peminjaman")
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = listOf(
            Color.BLUE, Color.rgb(255, 165, 0), Color.GREEN, Color.MAGENTA
        )
        dataSet.sliceSpace = 3f
//        dataSet.valueTextColor = Color.BLACK
//        dataSet.valueTextSize = 14f
        dataSet.setDrawValues(false) // Hilangkan angka pada setiap slice

        val pieData = PieData(dataSet)
        donutChart.data = pieData
        donutChart.description.isEnabled = false
        donutChart.isDrawHoleEnabled = true
        donutChart.holeRadius = 60f
        donutChart.centerText = "100 Aset"
        donutChart.animateY(1000)
        donutChart.setDrawEntryLabels(false)

        val legend = donutChart.legend
        legend.isWordWrapEnabled = true // Memungkinkan teks legend untuk dibungkus ke baris berikutnya
        legend.textSize = 12f // Sesuaikan ukuran teks jika perlu
        legend.xEntrySpace = 10f // Atur jarak horizontal antar item legend
        legend.yEntrySpace = 5f // Atur jarak vertikal antar item legend
        legend.orientation = Legend.LegendOrientation.HORIZONTAL // Pastikan legend tetap horizontal
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // Posisikan di tengah



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
