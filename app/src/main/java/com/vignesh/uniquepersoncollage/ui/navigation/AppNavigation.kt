package com.vignesh.uniquepersoncollage.ui.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.data.model.Person
import com.vignesh.uniquepersoncollage.data.model.ProcessingResult
import com.vignesh.uniquepersoncollage.ui.screens.HomeScreen
import com.vignesh.uniquepersoncollage.ui.screens.PersonDetailScreen
import com.vignesh.uniquepersoncollage.ui.screens.ProcessingScreen
import com.vignesh.uniquepersoncollage.ui.screens.ResultScreen
import com.vignesh.uniquepersoncollage.viewmodel.HomeViewModel
import com.vignesh.uniquepersoncollage.viewmodel.ProcessingViewModel
import com.vignesh.uniquepersoncollage.viewmodel.ResultViewModel

object Routes {
    const val HOME = "home"
    const val PROCESSING = "processing"
    const val RESULT = "result"
    const val PERSON_DETAIL = "person_detail"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel(),
    processingViewModel: ProcessingViewModel = viewModel(),
    resultViewModel: ResultViewModel = viewModel()
) {
    var activeProcessingParams by remember {
        mutableStateOf<Tuple4<Uri, Float, Float, CollageStyle>?>(null)
    }
    var currentProcessingResult by remember {
        mutableStateOf<ProcessingResult?>(null)
    }
    var selectedPerson by remember {
        mutableStateOf<Person?>(null)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToProcessing = { uri, fps, threshold, style ->
                    activeProcessingParams = Tuple4(uri, fps, threshold, style)
                    navController.navigate(Routes.PROCESSING)
                }
            )
        }

        composable(Routes.PROCESSING) {
            val params = activeProcessingParams
            if (params != null) {
                ProcessingScreen(
                    videoUri = params.first,
                    sampleFps = params.second,
                    clusteringThreshold = params.third,
                    collageStyle = params.fourth,
                    viewModel = processingViewModel,
                    onProcessingFinished = { result ->
                        currentProcessingResult = result
                        navController.navigate(Routes.RESULT) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Routes.RESULT) {
            val result = currentProcessingResult
            if (result != null) {
                ResultScreen(
                    result = result,
                    viewModel = resultViewModel,
                    onNavigateBack = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    },
                    onPersonClick = { person ->
                        selectedPerson = person
                        navController.navigate(Routes.PERSON_DETAIL)
                    }
                )
            }
        }

        composable(Routes.PERSON_DETAIL) {
            val person = selectedPerson
            if (person != null) {
                PersonDetailScreen(
                    person = person,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
