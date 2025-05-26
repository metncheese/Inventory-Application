package com.example.inventoryapplication.view.listuserdata

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventoryapplication.databinding.ActivityListUserDataBinding
import com.example.inventoryapplication.models.ViewModelFactory
import com.example.inventoryapplication.view.userdatadetail.UserDataDetailActivity
import kotlinx.coroutines.launch

class ListUserDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListUserDataBinding
    private lateinit var viewModel: ListUserdataViewModel
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListUserDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(this))[ListUserdataViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupToolbar()

        lifecycleScope.launch {
            viewModel.fetchUserData()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            val intent = Intent(this, UserDataDetailActivity::class.java)
            intent.putExtra("user_id", user.id)
            startActivity(intent)
        }
        binding.rvAssets.apply {
            layoutManager = LinearLayoutManager(this@ListUserDataActivity)
            adapter = userAdapter
        }
    }

    private fun setupObservers() {
        viewModel.users.observe(this) { users ->
            userAdapter.submitList(users)
        }
    }
}