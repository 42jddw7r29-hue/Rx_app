package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.database.AppDatabase
import com.example.database.PrescriptionRepository
import com.example.ui.AuthState
import com.example.ui.DashboardScreen
import com.example.ui.LoginScreen
import com.example.ui.MainViewModel
import com.example.ui.ViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure perfect high-contrast dynamic margins & EdgeToEdge layouts
        enableEdgeToEdge()

        // Setup local storage database
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = PrescriptionRepository(database.prescriptionDao())

        // ViewModel compilation
        val viewModel: MainViewModel by viewModels {
            ViewModelFactory(repository)
        }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val authState by viewModel.authState.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Route views depending on authentications state
                    when (authState) {
                        is AuthState.Authenticated -> {
                            val activeUser = (authState as AuthState.Authenticated).phoneNumber
                            DashboardScreen(
                                viewModel = viewModel,
                                userName = activeUser
                            )
                        }
                        else -> {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { phone ->
                                    // Successfully logged in
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
