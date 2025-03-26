package com.an.ridesim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.an.ridesim.ui.screen.HomeScreen
import com.an.ridesim.ui.theme.RideSimTheme
import com.an.ridesim.ui.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RideSimTheme {
                val viewModel = hiltViewModel<RideViewModel>()
                HomeScreen(viewModel)
            }
        }
    }
}
