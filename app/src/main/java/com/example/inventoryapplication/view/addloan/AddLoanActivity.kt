package com.example.inventoryapplication.view.addloan

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.inventoryapplication.databinding.ActivityAddLoanBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.utils.AddLoanRequest
import com.example.inventoryapplication.utils.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.inventoryapplication.R
import com.example.inventoryapplication.data.api.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddLoanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddLoanBinding
    private val viewModel: AddLoanViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private var selectedAssetId = -1
    private var selectedAssetMaxQty = 1
    private var quantity = 1
    private var userList: List<User> = emptyList()
    private var selectedUserId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddLoanBinding.inflate(layoutInflater)
        binding.btnSave.isEnabled = false
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupAssetDropdown()
        setupUserSuggestion()
        setupQuantityButtons()
        setupDatePickers()
        setupSaveButton()
        observeAddLoanResult()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupAssetDropdown() {
        lifecycleScope.launch {
            val session = viewModel.getSession().first()
            val token = session.token
            val role = session.role ?: "user"
            viewModel.fetchLoanableAssets(token, role)
            Log.e("TOKEN FROM ALL ASSETS", token);

            viewModel.availableAssets.observe(this@AddLoanActivity) { assetList ->
                val adapter = ArrayAdapter(
                    this@AddLoanActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    assetList.map { it.name }
                )
                binding.stuffNameEditText.setAdapter(adapter)

                binding.stuffNameEditText.setOnItemClickListener { _, _, position, _ ->
                    val selectedAsset = assetList[position]
                    selectedAssetId = selectedAsset.id
                    selectedAssetMaxQty = selectedAsset.quantity.toIntOrNull() ?: 1
                    quantity = 1
                    binding.tvQuantity.text = quantity.toString()
                    binding.categoryEditText.setText(selectedAsset.category)
                }

                binding.stuffNameEditText.setOnClickListener {
                    binding.stuffNameEditText.showDropDown()
                }
            }
        }
    }

    private fun setupUserSuggestion() {
        lifecycleScope.launch {
            val session = viewModel.getSession().first()
            val token = session.token
            if (token.isEmpty()) return@launch
            Log.e("TOKEN FROM ALL USERS", token);

            try {
                val allUsers = viewModel.getAllUsers(token)
                val filteredUsers = allUsers.filter { it.role.equals("user", ignoreCase = true) }

                if (filteredUsers.isEmpty()) {
                    Toast.makeText(this@AddLoanActivity, "Tidak ada data user ditemukan", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = false
                    return@launch
                }

                userList = filteredUsers
                binding.btnSave.isEnabled = true

                val userNames = userList.map { it.username }

                val adapter = ArrayAdapter(
                    this@AddLoanActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    userNames
                )

                binding.borrowerNameEditText.apply {
                    setAdapter(adapter)
                    threshold = 1

                    setOnItemClickListener { _, _, position, _ ->
                        val selected = adapter.getItem(position)
                        selectedUserId = userList.find { it.username == selected }?.id
                        setText(selected ?: "", false)
                    }

                    setOnFocusChangeListener { _, hasFocus ->
                        if (hasFocus) showDropDown()
                    }

                    addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            val input = s?.toString()?.trim()
                            selectedUserId = userList.find { it.username.equals(input, ignoreCase = true) }?.id
                            if (hasFocus()) showDropDown()
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun afterTextChanged(s: Editable?) {}
                    })
                }

            } catch (e: Exception) {
                Log.e("AddLoanActivity", "Gagal ambil user: ${e.message}", e)
                Toast.makeText(this@AddLoanActivity, "Gagal memuat data user", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = false
            }
        }
    }

    private fun setupQuantityButtons() {
        binding.tvQuantity.text = quantity.toString()

        binding.btnIncrement.setOnClickListener {
            if (quantity < selectedAssetMaxQty) {
                quantity++
                binding.tvQuantity.text = quantity.toString()
            }
        }

        binding.btnDecrement.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
            }
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun showPicker(onDatePicked: (String) -> Unit) {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDatePicked(dateFormat.format(calendar.time))
            },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.loanDateEditText.setOnClickListener {
            showPicker { date -> binding.loanDateEditText.setText(date) }
        }

        binding.returnDateEditText.setOnClickListener {
            showPicker { date -> binding.returnDateEditText.setText(date) }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            submitLoan()
        }
    }

    private fun submitLoan() {
        if (userList.isEmpty()) {
            Toast.makeText(this, "Data user belum tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val loanDate = binding.loanDateEditText.text.toString()
        val returnDate = binding.returnDateEditText.text.toString()

        if (selectedAssetId == -1 || loanDate.isEmpty() || returnDate.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua field terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val enteredName = binding.borrowerNameEditText.text.toString().trim()

        if (selectedUserId == null) {
            selectedUserId = userList.find { it.username.equals(enteredName, ignoreCase = true) }?.id
        }

        if (selectedUserId == null) {
            Toast.makeText(this, "Nama peminjam tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val token = viewModel.getSession().first().token
            val request = AddLoanRequest(
                inventory_id = selectedAssetId,
                user_id = selectedUserId!!,
                start_at = "${loanDate}T00:00:00Z",
                expired_at = "${returnDate}T00:00:00Z",
                quantity = quantity
            )
            viewModel.addLoan(token, request)
        }
    }

    private fun observeAddLoanResult() {
        viewModel.addLoanResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(this, "Peminjaman berhasil", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, "Gagal: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddLoanActivity", "AddLoan error: ${result.exception.message}", result.exception)
                }
                else -> {}
            }
        }
    }
}
