package com.vignesh.uniquepersoncollage.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.data.model.ProcessingResult
import com.vignesh.uniquepersoncollage.ui.components.ProcessingProgress
import com.vignesh.uniquepersoncollage.ui.theme.BackgroundDark
import com.vignesh.uniquepersoncollage.ui.theme.TextPrimaryDark
import com.vignesh.uniquepersoncollage.viewmodel.ProcessingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    videoUri: Uri,
    sampleFps: Float,
    clusteringThreshold: Float,
    collageStyle: CollageStyle,
    viewModel: ProcessingViewModel,
    onProcessingFinished: (ProcessingResult) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(videoUri) {
        viewModel.startProcessing(videoUri, sampleFps, clusteringThreshold, collageStyle)
    }

    LaunchedEffect(uiState.isCompleted, uiState.result) {
        if (uiState.isCompleted && uiState.result != null) {
            onProcessingFinished(uiState.result!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analyzing Video",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.cancelProcessing()
                        onCancel()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cancel",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onCancel,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Go Back")
                }
            } else {
                ProcessingProgress(
                    stageTitle = uiState.currentStageTitle,
                    stageDetail = uiState.currentStageDetail,
                    progressFraction = uiState.progressFraction,
                    framesProcessed = uiState.framesProcessed,
                    totalFrames = uiState.totalFrames,
                    facesFound = uiState.facesFound,
                    latestFaceBitmap = uiState.latestFaceBitmap
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.cancelProcessing()
                        onCancel()
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel Processing")
                }
            }
        }
    }
}
