package com.mcvk.flosshabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mcvk.flosshabit.dashboard.DashboardRoute
import com.mcvk.flosshabit.dashboard.DashboardViewModel
import com.mcvk.flosshabit.ui.theme.FlossHabitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlossHabitTheme {
                DashboardRoute(viewModel(factory = dashboardFactory))
            }
        }
    }

    private companion object {
        val dashboardFactory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as FlossHabitApp
                DashboardViewModel(app.habitLogRepository)
            }
        }
    }
}
