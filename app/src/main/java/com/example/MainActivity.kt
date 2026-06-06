package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AppScaffold
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MedicineWheelViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val mwViewModel: MedicineWheelViewModel = viewModel()
        AppScaffold(viewModel = mwViewModel)
      }
    }
  }
}
