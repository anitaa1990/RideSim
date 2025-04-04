package com.an.ridesim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.an.ridesim.ui.screen.HomeScreen
import com.an.ridesim.ui.screen.RideSummaryScreen
import com.an.ridesim.ui.theme.RideSimTheme
import com.an.ridesim.ui.viewmodel.RideViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val navDestinationHome = "home"
    private val navDestinationSummary = "summary"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RideSimTheme {
                val navController = rememberNavController()
                val viewModel = hiltViewModel<RideViewModel>()

                NavHost(
                    navController = navController,
                    startDestination = navDestinationHome
                ) {
                    composable(navDestinationHome) {
                        HomeScreen(viewModel) {
                            navController.navigate(navDestinationSummary) {
                                popUpTo(navDestinationHome) { inclusive = true }
                            }
                        }
                    }
                    composable(navDestinationSummary) {
                        RideSummaryScreen(viewModel) {
                            viewModel.resetSimulation()
                            navController.navigate(navDestinationHome) {
                                popUpTo(navDestinationSummary) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}
