package com.vignesh.uniquepersoncollage.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.vignesh.uniquepersoncollage.ui.theme.*

@Composable
fun ProcessingProgress(
    stageTitle: String,
    stageDetail: String,
    progressFraction: Float,
    framesProcessed: Int,
    totalFrames: Int,
    facesFound: Int,
    latestFaceBitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        label = "progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceCardBorder))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live face thumbnail or processing icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(BackgroundDark)
                    .border(2.dp, PrimaryIndigo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (latestFaceBitmap != null) {
                    Image(
                        bitmap = latestFaceBitmap.asImageBitmap(),
                        contentDescription = "Detected Face",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Face,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = PrimaryIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stageTitle,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stageDetail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Smooth progress indicator
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryIndigo,
                trackColor = SurfaceDark,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Frames: $framesProcessed/$totalFrames",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark
                )
                Text(
                    text = "Faces Found: $facesFound",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentCyan
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPink
                )
            }
        }
    }
}
