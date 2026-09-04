package com.vignesh.uniquepersoncollage.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.vignesh.uniquepersoncollage.ui.theme.PrimaryIndigo
import com.vignesh.uniquepersoncollage.ui.theme.SurfaceCard
import com.vignesh.uniquepersoncollage.ui.theme.SurfaceCardBorder
import com.vignesh.uniquepersoncollage.ui.theme.TextMutedDark

@Composable
fun CollagePreview(
    collageBitmap: Bitmap?,
    isRegenerating: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (collageBitmap != null) {
            Image(
                bitmap = collageBitmap.asImageBitmap(),
                contentDescription = "Unique Person Collage",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = "No Collage Generated",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedDark
            )
        }

        if (isRegenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        }
    }
}
