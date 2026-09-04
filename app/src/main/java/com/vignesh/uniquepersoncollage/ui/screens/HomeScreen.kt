package com.vignesh.uniquepersoncollage.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.ui.components.VideoPickerButton
import com.vignesh.uniquepersoncollage.ui.theme.*
import com.vignesh.uniquepersoncollage.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToProcessing: (Uri, Float, Float, CollageStyle) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onVideoSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = AccentPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Unique Person Collage",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryDark
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Intro Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Turn Video into Unique Person Art",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Extract faces, track identities with ML Kit & TFLite, select the sharpest portraits, and create a collage of every unique individual.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Video Picker Button
            VideoPickerButton(
                onClick = { videoPickerLauncher.launch("video/*") },
                modifier = Modifier.fillMaxWidth()
            )

            // Selected Video Details Card
            if (uiState.selectedVideoInfo != null) {
                val info = uiState.selectedVideoInfo!!
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = info.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryDark
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Duration: ${info.durationFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                            Text(
                                text = "Resolution: ${info.width}x${info.height}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pipeline Settings Section
            Text(
                text = "Processing Settings",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimaryDark,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Frame Sampling Rate Slider
                    Text(
                        text = "Sampling Rate: ${String.format("%.1f", uiState.sampleFps)} FPS",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryDark
                    )
                    Slider(
                        value = uiState.sampleFps,
                        onValueChange = { viewModel.updateFps(it) },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryIndigo,
                            activeTrackColor = PrimaryIndigo
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Clustering Similarity Slider
                    Text(
                        text = "Identity Sensitivity: ${String.format("%.2f", uiState.clusteringThreshold)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryDark
                    )
                    Slider(
                        value = uiState.clusteringThreshold,
                        onValueChange = { viewModel.updateClusteringThreshold(it) },
                        valueRange = 0.40f..0.85f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentPink,
                            activeTrackColor = AccentPink
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Collage Style Selector
                    Text(
                        text = "Initial Collage Style",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CollageStyle.values().forEach { style ->
                            val isSelected = uiState.selectedCollageStyle == style
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateCollageStyle(style) },
                                label = { Text(style.title, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryIndigo,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Start Processing Action Button
            Button(
                onClick = {
                    uiState.selectedVideoUri?.let { uri ->
                        onNavigateToProcessing(
                            uri,
                            uiState.sampleFps,
                            uiState.clusteringThreshold,
                            uiState.selectedCollageStyle
                        )
                    }
                },
                enabled = uiState.selectedVideoUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Extracting & Collaging",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
