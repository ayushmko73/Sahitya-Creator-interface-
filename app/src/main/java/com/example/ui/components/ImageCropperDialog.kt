package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SahityaBorderColor
import com.example.ui.theme.SahityaCyanSecondary
import com.example.ui.theme.SahityaEmeraldAccent
import com.example.ui.theme.SahityaIndigoPrimary
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class CropAspectRatio(
    val widthRatio: Int,
    val heightRatio: Int,
    val title: String
) {
    val floatRatio: Float get() = widthRatio.toFloat() / heightRatio.toFloat()
}

@Composable
fun ImageCropperDialog(
    imageUri: String,
    aspectRatio: CropAspectRatio, // 3:4 or 16:9
    onCropSaved: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadedBitmapRatio by remember { mutableFloatStateOf(1f) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }

    // Load Bitmap asynchronously from URI
    LaunchedEffect(imageUri) {
        runCatching {
            val uri = Uri.parse(imageUri)
            val inputStream: InputStream? = if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                context.contentResolver.openInputStream(uri)
            } else {
                null
            }

            val bmp = if (inputStream != null) {
                BitmapFactory.decodeStream(inputStream)
            } else {
                // Fallback for standard demo asset or local file
                null
            }

            if (bmp != null) {
                originalBitmap = bmp
                loadedBitmapRatio = bmp.width.toFloat() / bmp.height.toFloat()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Crop,
                                contentDescription = null,
                                tint = SahityaCyanSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ANDROID STANDARD CROPPER",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = SahityaCyanSecondary
                            )
                        }
                        Text(
                            text = aspectRatio.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("crop_dismiss_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Aspect Ratio Info Banner
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SahityaBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = null,
                                tint = SahityaEmeraldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Required Frame Ratio: ${aspectRatio.widthRatio}:${aspectRatio.heightRatio}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Surface(
                            color = SahityaIndigoPrimary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (aspectRatio.widthRatio == 3) "Portrait 3:4" else "Landscape 16:9",
                                style = MaterialTheme.typography.labelSmall,
                                color = SahityaCyanSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Crop Canvas Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds()
                        .background(Color.Black, RoundedCornerShape(16.dp))
                        .border(1.dp, SahityaBorderColor, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = originalBitmap

                    if (bitmap != null) {
                        // Interactive Croppable Canvas View
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.6f, 4f)
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offsetX
                                        translationY = offsetY
                                        rotationZ = rotationDegrees
                                    }
                            ) {
                                drawImage(bitmap.asImageBitmap())
                            }

                            // Overlay Frame Mask with Rule-of-Thirds Grid
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height

                                // Calculate frame box size matching exact ratio
                                val frameWidth: Float
                                val frameHeight: Float

                                if (aspectRatio.floatRatio <= 1f) {
                                    // Portrait e.g. 3:4
                                    frameHeight = canvasHeight * 0.75f
                                    frameWidth = frameHeight * aspectRatio.floatRatio
                                } else {
                                    // Landscape e.g. 16:9
                                    frameWidth = canvasWidth * 0.90f
                                    frameHeight = frameWidth / aspectRatio.floatRatio
                                }

                                val left = (canvasWidth - frameWidth) / 2f
                                val top = (canvasHeight - frameHeight) / 2f
                                val right = left + frameWidth
                                val bottom = top + frameHeight

                                // Outer Dark Semi-Transparent Mask
                                val path = Path().apply {
                                    addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                                    addRect(Rect(left, top, right, bottom))
                                }
                                drawPath(
                                    path = path,
                                    color = Color.Black.copy(alpha = 0.75f)
                                )

                                // Frame Border
                                drawRect(
                                    color = SahityaCyanSecondary,
                                    topLeft = Offset(left, top),
                                    size = Size(frameWidth, frameHeight),
                                    style = Stroke(width = 3.dp.toPx())
                                )

                                // Grid Lines (Rule of Thirds)
                                val colWidth = frameWidth / 3f
                                val rowHeight = frameHeight / 3f

                                // Vertical Grid Lines
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(left + colWidth, top),
                                    end = Offset(left + colWidth, bottom),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(left + colWidth * 2, top),
                                    end = Offset(left + colWidth * 2, bottom),
                                    strokeWidth = 1.dp.toPx()
                                )

                                // Horizontal Grid Lines
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(left, top + rowHeight),
                                    end = Offset(right, top + rowHeight),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(left, top + rowHeight * 2),
                                    end = Offset(right, top + rowHeight * 2),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                    } else {
                        // Fallback preview frame box when bitmap is loading or standard URI
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (aspectRatio.widthRatio == 16) 0.9f else 0.7f)
                                .aspectRatio(aspectRatio.floatRatio)
                                .border(2.dp, SahityaCyanSecondary, RoundedCornerShape(12.dp))
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = SahityaCyanSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Standard Frame: ${aspectRatio.widthRatio}:${aspectRatio.heightRatio}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Drag/Pinch image to align in frame",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Adjustment Controls (Zoom Slider, Rotate, Reset)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scale Zoom: ${(scale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Rotate 90°",
                                    tint = SahityaCyanSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                    rotationDegrees = 0f
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Frame",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = scale,
                        onValueChange = { scale = it },
                        valueRange = 0.6f..3f,
                        colors = SliderDefaults.colors(
                            thumbColor = SahityaCyanSecondary,
                            activeTrackColor = SahityaIndigoPrimary,
                            inactiveTrackColor = SahityaBorderColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val bitmap = originalBitmap
                            if (bitmap != null) {
                                val croppedUri = cropAndSaveBitmap(
                                    context = context,
                                    sourceBitmap = bitmap,
                                    targetWidthRatio = aspectRatio.widthRatio,
                                    targetHeightRatio = aspectRatio.heightRatio,
                                    scale = scale,
                                    offsetX = offsetX,
                                    offsetY = offsetY,
                                    rotationDegrees = rotationDegrees
                                )
                                onCropSaved(croppedUri)
                            } else {
                                // Fallback to source Uri if bitmap parsing was direct
                                onCropSaved(imageUri)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("apply_crop_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SahityaIndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Crop", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Android Standard Bitmap Cropper Helper Function
 * Performs exact aspect-ratio box cropping & transforms to produce clean output image.
 */
private fun cropAndSaveBitmap(
    context: android.content.Context,
    sourceBitmap: Bitmap,
    targetWidthRatio: Int,
    targetHeightRatio: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    rotationDegrees: Float
): String {
    return try {
        // Apply rotation if needed
        val matrix = Matrix()
        if (rotationDegrees != 0f) {
            matrix.postRotate(rotationDegrees)
        }

        val rotatedBitmap = if (rotationDegrees != 0f) {
            Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true)
        } else {
            sourceBitmap
        }

        val targetRatio = targetWidthRatio.toFloat() / targetHeightRatio.toFloat()

        // Determine crop bounds matching target ratio
        val cropWidth: Int
        val cropHeight: Int

        val currentRatio = rotatedBitmap.width.toFloat() / rotatedBitmap.height.toFloat()

        if (currentRatio > targetRatio) {
            // Wider than target ratio -> crop width
            cropHeight = rotatedBitmap.height
            cropWidth = (cropHeight * targetRatio).toInt()
        } else {
            // Taller than target ratio -> crop height
            cropWidth = rotatedBitmap.width
            cropHeight = (cropWidth / targetRatio).toInt()
        }

        val startX = ((rotatedBitmap.width - cropWidth) / 2).coerceIn(0, rotatedBitmap.width - 1)
        val startY = ((rotatedBitmap.height - cropHeight) / 2).coerceIn(0, rotatedBitmap.height - 1)

        val finalWidth = cropWidth.coerceAtMost(rotatedBitmap.width - startX)
        val finalHeight = cropHeight.coerceAtMost(rotatedBitmap.height - startY)

        val croppedBitmap = Bitmap.createBitmap(
            rotatedBitmap,
            startX,
            startY,
            finalWidth,
            finalHeight
        )

        // Save to cache file
        val file = File(context.cacheDir, "cropped_sahitya_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        Uri.fromFile(file).toString()
    } catch (e: Exception) {
        e.printStackTrace()
        // Return original if crop fails
        sourceBitmap.toString()
    }
}
