package com.bookyo.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.bookyo.ui.lightGray
import com.bookyo.ui.whiteGray

@Composable
fun ImageUploadBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Upload your images",
    hasImage: Boolean = false,
    imageUri: Uri? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = lightGray,
                shape = RoundedCornerShape(8.dp)
            )
            .background(whiteGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (hasImage && imageUri != null) {
            // Display the selected image
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Selected book cover",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Draw the X pattern for empty state
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 1.dp.toPx()
                val lineColor = lightGray.copy(alpha = 0.5f)

                // Draw diagonal lines to form an X
                drawLine(
                    color = lineColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = lineColor,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // Overlay text (shown in both states)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Only show text if there's no image or if it's a prompt to change
            if (!hasImage || text.contains("Change")) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasImage) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}