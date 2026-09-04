package com.vignesh.uniquepersoncollage.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.vignesh.uniquepersoncollage.data.model.Person
import com.vignesh.uniquepersoncollage.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    person: Person,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = person.name,
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
        containerColor = BackgroundDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Best Representative Portrait
            item {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(3.dp, PrimaryIndigo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (person.representativeFace != null) {
                        Image(
                            bitmap = person.representativeFace.asImageBitmap(),
                            contentDescription = person.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${person.appearanceCount}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = PrimaryIndigo
                            )
                            Text(
                                text = "Appearances",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = person.screenTimeFormatted,
                                style = MaterialTheme.typography.headlineMedium,
                                color = AccentPink
                            )
                            Text(
                                text = "Screen Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryDark
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.0f%%", person.representativeScore * 100),
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "Quality Score",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }

            // All Extracted Shots Carousel
            if (person.allFaceThumbnails.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Detected Face Shots",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(person.allFaceThumbnails) { thumb ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SurfaceDark)
                                ) {
                                    Image(
                                        bitmap = thumb.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Appearance Timeline Segments
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Video Appearances",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(person.appearances) { appearance ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (appearance.bestShot != null) {
                            Image(
                                bitmap = appearance.bestShot.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            val startSec = appearance.startTimeMs / 1000
                            val endSec = appearance.endTimeMs / 1000
                            Text(
                                text = "Time: ${startSec}s - ${endSec}s (${appearance.frameCount} frames)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Pose: ${appearance.dominantPose}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }
        }
    }
}
