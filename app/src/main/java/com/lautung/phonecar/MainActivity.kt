package com.lautung.phonecar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.lautung.phonecar.ui.PhoneCarApp
import com.lautung.phonecar.ui.PhoneCarViewModel
import com.lautung.phonecar.ui.theme.PhoneCarTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PhoneCarViewModel by viewModels {
        (application as PhoneCarApplication).container.viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoneCarTheme {
                PhoneCarApp(viewModel = viewModel)
            }
        }
    }
}
