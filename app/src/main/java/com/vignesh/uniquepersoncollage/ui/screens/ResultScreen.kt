package com.vignesh.uniquepersoncollage.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vignesh.uniquepersoncollage.collage.CollageStyle
import com.vignesh.uniquepersoncollage.data.model.Person
import com.vignesh.uniquepersoncollage.data.model.ProcessingResult
import com.vignesh.uniquepersoncollage.ui.components.ActionButtons
import com.vignesh.uniquepersoncollage.ui.components.CollagePreview
import com.vignesh.uniquepersoncollage.ui.components.PersonCard
import com.vignesh.uniquepersoncollage.ui.theme.*
import com.vignesh.uniquepersoncollage.viewmodel.ResultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: ProcessingResult,
    viewModel: ResultViewModel,
    onNavigateBack: () -> Unit,
    onPersonClick: (Person) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(result) {
        viewModel.setResult(result)
    }

    // Handle Share Intent trigger
    LaunchedEffect(uiState.shareIntent) {
        uiState.shareIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.clearShareIntent()
        }
    }

    // Handle Toast / Snackbar messages
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val currentResult = uiState.result ?: result

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Collage Result (${currentResult.personCount} People)",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimaryDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Collage Preview Viewport
            item {
                CollagePreview(
                    collageBitmap = currentResult.collageBitmap,
                    isRegenerating = uiState.isRegenerating
                )
            }

            // Collage Style Picker
            item {
                Text(
                    text = "Layout Style",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CollageStyle.values().forEach { style ->
                        val isSelected = uiState.currentStyle == style
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.changeCollageStyle(style) },
                            label = { Text(style.title, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryIndigo,
                                selectedLabelColor = androidx.compose.ui.graphics.Color.White
                            )
                        )
                    }
                }
            }

            // Save and Share Action Buttons
            item {
                ActionButtons(
                    onSaveClick = { viewModel.saveToGallery() },
                    onShareClick = { viewModel.prepareShare() },
                    isSaving = uiState.isSaving,
                    isSharing = uiState.isSharing
                )
            }

            // Unique Persons Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Identified Persons",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "${currentResult.personCount} Unique Individuals",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                }
            }

            // List of Persons
            items(currentResult.uniquePersons) { person ->
                PersonCard(
                    person = person,
                    onClick = { onPersonClick(person) }
                )
            }
        }
    }
}
