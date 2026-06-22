package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ViverDatabase
import com.example.data.repository.ViverRepository
import com.example.ui.screens.ViverMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ViverViewModel
import com.example.ui.viewmodel.ViverViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Repository
        val database = ViverDatabase.getDatabase(applicationContext)
        val repository = ViverRepository(database.dao)

        setContent {
            var isDarkTheme by androidx.compose.runtime.remember { 
                androidx.compose.runtime.mutableStateOf(androidx.compose.foundation.isSystemInDarkTheme()) 
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val viewModel: ViverViewModel = viewModel(
                        factory = ViverViewModelFactory(repository)
                    )
                    ViverMainScreen(
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}
