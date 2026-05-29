package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CableAnalyzerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.UsbViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: UsbViewModel = viewModel()
        CableAnalyzerScreen(viewModel = viewModel)
      }
    }
  }
}
