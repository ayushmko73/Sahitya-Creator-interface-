package com.example.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Layers
import androidx.compose.runtime.LaunchedEffect
import com.example.util.StoryBlock
import com.example.util.StoryBlockBatch
import com.example.util.StoryBlockParser
import com.example.ui.components.CropAspectRatio
import com.example.ui.components.ImageCropperDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.SeasonEntity
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SahityaAmberGold
import com.example.ui.theme.SahityaBorderColor
import com.example.ui.theme.SahityaCyanSecondary
import com.example.ui.theme.SahityaEmeraldAccent
import com.example.ui.theme.SahityaIndigoPrimary
import com.example.ui.theme.SahityaPinkAccent
import com.example.ui.viewmodel.StoryStudioState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryStudioScreen(
    state: StoryStudioState,
    categories: List<String>,
    autoSaveStatus: String = "Auto-saved in DataStore",
    lastAutoSavedTime: Long = 0L,
    restorableDraft: com.example.data.local.DraftSnapshot? = null,
    onRestoreDraft: (com.example.data.local.DraftSnapshot) -> Unit = {},
    onClearDraft: () -> Unit = {},
    onSetStep: (Int) -> Unit,
    onUpdatePage1: (title: String, subtitle: String, desc: String, category: String, mainThumb: String, banner: String) -> Unit,
    onUpdateSubThumbnails: (List<String>) -> Unit,
    onAddCharacter: () -> Unit,
    onUpdateCharacter: (index: Int, char: CharacterEntity) -> Unit,
    onRemoveCharacter: (index: Int) -> Unit,
    onAddSeason: () -> Unit,
    onUpdateSeason: (index: Int, season: SeasonEntity) -> Unit,
    onRemoveSeason: (index: Int) -> Unit,
    onUpdateMasterTimeline: (String) -> Unit,
    onSaveDraft: () -> Unit,
    onRunPipeline: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        focusManager.clearFocus()
                    }
                )
            }
    ) {
        // Step Indicator Header
        StudioHeader(
            currentStep = state.currentStep,
            onStepClick = { step -> onSetStep(step) }
        )

        // Restorable DataStore Draft Banner
        if (restorableDraft != null && restorableDraft.storyId != state.storyId && restorableDraft.title.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("restorable_draft_banner"),
                colors = CardDefaults.cardColors(containerColor = SahityaIndigoPrimary.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SahityaCyanSecondary)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = SahityaCyanSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Auto-Saved DataStore Draft Found",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        IconButton(onClick = onClearDraft, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Discard Draft", tint = Color.LightGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Title: \"${restorableDraft.title}\" • ${restorableDraft.characters.size} Character(s) • Saved in DataStore Preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onClearDraft,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            modifier = Modifier.testTag("discard_datastore_draft_button")
                        ) {
                            Text("Discard", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onRestoreDraft(restorableDraft) },
                            colors = ButtonDefaults.buttonColors(containerColor = SahityaCyanSecondary, contentColor = Color.Black),
                            modifier = Modifier.testTag("restore_datastore_draft_button")
                        ) {
                            Text("Restore Progress", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state.currentStep) {
                1 -> Page1CoreMetadata(
                    state = state,
                    categories = categories,
                    onUpdatePage1 = onUpdatePage1,
                    onUpdateSubThumbnails = onUpdateSubThumbnails,
                    onNext = { onSetStep(2) }
                )
                2 -> Page2CharacterBuilder(
                    characters = state.characters,
                    onAddCharacter = onAddCharacter,
                    onUpdateCharacter = onUpdateCharacter,
                    onRemoveCharacter = onRemoveCharacter,
                    onBack = { onSetStep(1) },
                    onNext = { onSetStep(3) }
                )
                3 -> Page3ConditionalBranching(
                    selectedCategory = state.selectedCategory,
                    masterTimeline = state.masterTimeline,
                    seasons = state.seasons,
                    characters = state.characters,
                    mainThumbnail = state.mainThumbnail,
                    bannerImage = state.bannerImage,
                    onUpdateMasterTimeline = onUpdateMasterTimeline,
                    onAddSeason = onAddSeason,
                    onUpdateSeason = onUpdateSeason,
                    onRemoveSeason = onRemoveSeason,
                    onBack = { onSetStep(2) },
                    onSaveDraft = onSaveDraft,
                    onPublishPipeline = onRunPipeline
                )
            }
        }
    }
}

@Composable
fun StudioHeader(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SahityaBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SAHITYA STORY CREATION STUDIO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = SahityaCyanSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBadge(
                    stepNumber = 1,
                    title = "1. Metadata",
                    isActive = currentStep == 1,
                    isCompleted = currentStep > 1,
                    onClick = { onStepClick(1) }
                )

                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .background(if (currentStep > 1) SahityaIndigoPrimary else SahityaBorderColor)
                )

                StepBadge(
                    stepNumber = 2,
                    title = "2. Characters",
                    isActive = currentStep == 2,
                    isCompleted = currentStep > 2,
                    onClick = { onStepClick(2) }
                )

                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .background(if (currentStep > 2) SahityaIndigoPrimary else SahityaBorderColor)
                )

                StepBadge(
                    stepNumber = 3,
                    title = "3. Timeline & Arc",
                    isActive = currentStep == 3,
                    isCompleted = false,
                    onClick = { onStepClick(3) }
                )
            }
        }
    }
}

@Composable
fun InlineDeleteConfirmationBox(
    title: String,
    message: String,
    onConfirmDelete: () -> Unit,
    onCancel: () -> Unit
) {
    var secondsLeft by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            secondsLeft--
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("inline_delete_confirmation_box"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1019)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF43F5E)
                    )
                }

                Surface(
                    color = Color(0xFFF43F5E).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (secondsLeft > 0) "${secondsLeft}s safety timer" else "Ready",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF43F5E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5-second progress indicator
            LinearProgressIndicator(
                progress = { (5 - secondsLeft) / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFF43F5E),
                trackColor = Color(0xFF4A1822)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (secondsLeft > 0) "Confirm Delete (${secondsLeft}s)" else "Confirm Delete",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var secondsLeft by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            kotlinx.coroutines.delay(1000L)
            secondsLeft--
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Everything?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete all current story fields, characters, and timelines? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 5-second countdown progress indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE11D48).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        progress = { secondsLeft / 5f },
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFFF43F5E),
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (secondsLeft > 0) "Safety Countdown: $secondsLeft second(s)..." else "Timer finished. Ready to delete.",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF43F5E)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmDelete()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E), contentColor = Color.White),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(
                    text = if (secondsLeft > 0) "Confirm Delete ($secondsLeft s)" else "Confirm Delete All",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelSmall)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun StoryBlockDividerSection(
    blocks: List<StoryBlock>,
    batches: List<StoryBlockBatch>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("story_block_divider_section"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B2E)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SahityaCyanSecondary.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(SahityaIndigoPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DIVIDED STORY BLOCKS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = SahityaCyanSecondary
                        )
                        Text(
                            text = "Divided into 2-block AI execution batches",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Surface(
                    color = SahityaEmeraldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SahityaEmeraldAccent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${blocks.size} Blocks • ${batches.size} Batches",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SahityaEmeraldAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Batches list (2 blocks per batch)
            batches.forEach { batch ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SahityaBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = SahityaIndigoPrimary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "AI BATCH #${batch.batchNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Blocks ${batch.blocks.first().index} & ${batch.blocks.last().index}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Surface(
                                color = SahityaAmberGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${batch.blocks.size}/2 Blocks Payload",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = SahityaAmberGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Render up to 2 blocks inside this batch side by side or stacked
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            batch.blocks.forEach { block ->
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SahityaCyanSecondary.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = SahityaCyanSecondary.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Block #${block.index}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                                        color = SahityaCyanSecondary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Batch #${batch.batchNumber}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = Color.Gray
                                                )
                                            }

                                            Text(
                                                text = "${block.cleanContent.length} chars",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color.LightGray
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = block.cleanContent.ifBlank { "(Empty block content)" },
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "⚡ Batch #${batch.batchNumber} payload ready for AI process turn (2 blocks)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = SahityaEmeraldAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSmartWritingPad(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    maxLength: Int,
    collapsedMinLines: Int = 2,
    expandedMinLines: Int = 6,
    showBlockTemplateButton: Boolean = true,
    showDirectionalPad: Boolean = false,
    testTag: String = ""
) {
    var isFocused by remember { mutableStateOf(false) }
    var showBlockViewer by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            val newPos = textFieldValue.selection.start.coerceAtMost(value.length)
            textFieldValue = TextFieldValue(text = value, selection = TextRange(newPos))
        }
    }

    val blocks = remember(value) { StoryBlockParser.parseBlocks(value) }
    val batches = remember(blocks) { StoryBlockParser.groupIntoBatches(blocks) }

    fun updateText(newValue: TextFieldValue) {
        if (newValue.text.length <= maxLength) {
            textFieldValue = newValue
            onValueChange(newValue.text)
        }
    }

    fun insertBlockTemplate() {
        val snippet = "{  }"
        val curText = textFieldValue.text
        val curPos = textFieldValue.selection.start
        val newText = if (curPos in 0..curText.length) {
            curText.substring(0, curPos) + snippet + curText.substring(curPos)
        } else {
            curText + snippet
        }
        val newSelectionPos = (curPos + 2).coerceAtMost(newText.length)
        updateText(TextFieldValue(text = newText, selection = TextRange(newSelectionPos)))
    }

    fun moveCursorLeft() {
        val curPos = textFieldValue.selection.start
        val newPos = (curPos - 1).coerceAtLeast(0)
        updateText(textFieldValue.copy(selection = TextRange(newPos)))
    }

    fun moveCursorRight() {
        val curPos = textFieldValue.selection.start
        val newPos = (curPos + 1).coerceAtMost(textFieldValue.text.length)
        updateText(textFieldValue.copy(selection = TextRange(newPos)))
    }

    fun moveCursorUp() {
        val text = textFieldValue.text
        val curPos = textFieldValue.selection.start
        val lastLineBreak = text.lastIndexOf('\n', (curPos - 1).coerceAtLeast(0))
        val newPos = if (lastLineBreak >= 0) {
            val prevLineBreak = text.lastIndexOf('\n', (lastLineBreak - 1).coerceAtLeast(0))
            val colOffset = curPos - (lastLineBreak + 1)
            val targetStart = if (prevLineBreak >= 0) prevLineBreak + 1 else 0
            (targetStart + colOffset).coerceAtMost(lastLineBreak)
        } else {
            (curPos - 30).coerceAtLeast(0)
        }
        updateText(textFieldValue.copy(selection = TextRange(newPos)))
    }

    fun moveCursorDown() {
        val text = textFieldValue.text
        val curPos = textFieldValue.selection.start
        val nextLineBreak = text.indexOf('\n', curPos)
        val newPos = if (nextLineBreak >= 0) {
            val lastLineBreak = text.lastIndexOf('\n', (curPos - 1).coerceAtLeast(0))
            val colOffset = curPos - (if (lastLineBreak >= 0) lastLineBreak + 1 else 0)
            val afterNextLine = nextLineBreak + 1
            val followingLineBreak = text.indexOf('\n', afterNextLine)
            val targetEnd = if (followingLineBreak >= 0) followingLineBreak else text.length
            (afterNextLine + colOffset).coerceAtMost(targetEnd)
        } else {
            (curPos + 30).coerceAtMost(text.length)
        }
        updateText(textFieldValue.copy(selection = TextRange(newPos)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFCBD5E1)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Directional D-Pad Cursor Movement Controls
                if (showDirectionalPad) {
                    Surface(
                        color = Color(0xFF1E1B2E),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SahityaIndigoPrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { moveCursorUp() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Cursor Up",
                                    tint = SahityaCyanSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { moveCursorDown() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Cursor Down",
                                    tint = SahityaCyanSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { moveCursorLeft() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Cursor Left",
                                    tint = SahityaCyanSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { moveCursorRight() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Cursor Right",
                                    tint = SahityaCyanSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Quick Insert {--- Block } Button (Shown when showBlockTemplateButton is true)
                if (showBlockTemplateButton) {
                    Surface(
                        color = SahityaIndigoPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SahityaIndigoPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { insertBlockTemplate() }
                            .padding(2.dp)
                            .testTag("insert_block_template_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Insert { Block }",
                                tint = SahityaCyanSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "{ }",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = SahityaCyanSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = "${value.length}/$maxLength",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (value.length >= maxLength) Color(0xFFF43F5E) else SahityaCyanSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Detected Blocks Badge Indicator
        if (blocks.isNotEmpty()) {
            Surface(
                color = Color(0xFF1E1B2E),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SahityaCyanSecondary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { showBlockViewer = !showBlockViewer }
                    .testTag("toggle_block_viewer_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Divided Blocks",
                            tint = SahityaCyanSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Parsed: ${blocks.size} ${if (blocks.size == 1) "Block" else "Blocks"} (${batches.size} ${if (batches.size == 1) "Batch" else "Batches"})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SahityaCyanSecondary
                        )
                    }

                    Text(
                        text = if (showBlockViewer) "Hide Blocks ▲" else "View Divided Blocks ▼",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = SahityaEmeraldAccent
                    )
                }
            }
        }

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { updateText(it) },
            placeholder = { Text(placeholder) },
            minLines = if (isFocused) expandedMinLines else collapsedMinLines,
            maxLines = 14,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
                .testTag(testTag),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SahityaCyanSecondary,
                unfocusedBorderColor = SahityaBorderColor,
                focusedContainerColor = DarkSurface.copy(alpha = 0.95f),
                unfocusedContainerColor = DarkSurface
            )
        )

        if (isFocused) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Enclose story sections in { } to group them into AI blocks",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color(0xFF64748B)
            )
        }

        // Render Divided Story Blocks Viewer
        if (showBlockViewer && blocks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            StoryBlockDividerSection(blocks = blocks, batches = batches)
        }
    }
}

@Composable
fun StepBadge(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> SahityaEmeraldAccent
                        isActive -> SahityaIndigoPrimary
                        else -> DarkSurfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isActive) Color.White else Color(0xFF94A3B8)
        )
    }
}

// -------------------------------------------------------------
// PAGE 1: Core Story Metadata (With Device Media Selection & Limits)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Page1CoreMetadata(
    state: StoryStudioState,
    categories: List<String>,
    onUpdatePage1: (title: String, subtitle: String, desc: String, category: String, mainThumb: String, banner: String) -> Unit,
    onUpdateSubThumbnails: (List<String>) -> Unit,
    onNext: () -> Unit
) {
    var title by remember(state.title) { mutableStateOf(state.title) }
    var subtitle by remember(state.subtitle) { mutableStateOf(state.subtitle) }
    var description by remember(state.description) { mutableStateOf(state.description) }
    var category by remember(state.selectedCategory) { mutableStateOf(state.selectedCategory) }
    var mainThumbnail by remember(state.mainThumbnail) { mutableStateOf(state.mainThumbnail) }
    var bannerImage by remember(state.bannerImage) { mutableStateOf(state.bannerImage) }
    var subThumbnails by remember(state.subThumbnails) { mutableStateOf(state.subThumbnails) }

    var categoryExpanded by remember { mutableStateOf(false) }

    // Interactive Cropper Dialog States
    var activeCropUri by remember { mutableStateOf<String?>(null) }
    var activeCropRatio by remember { mutableStateOf<CropAspectRatio?>(null) }

    // Media Launchers
    val context = LocalContext.current
    fun isImageSizeValid(uri: Uri): Boolean {
        var isValid = true
        try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { fd ->
                val sizeMb = fd.length / (1024.0 * 1024.0)
                if (sizeMb > 5) {
                    isValid = false
                }
            }
        } catch (e: Exception) {}
        if (!isValid) {
            Toast.makeText(context, "Image exceeds maximum 5MB limit.", Toast.LENGTH_LONG).show()
        }
        return isValid
    }

    val mainThumbLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            if (isImageSizeValid(it)) {
                activeCropUri = it.toString()
                activeCropRatio = CropAspectRatio(3, 4, "Main Story Thumbnail (3:4 Ratio Frame)")
            }
        }
    }

    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            if (isImageSizeValid(it)) {
                activeCropUri = it.toString()
                activeCropRatio = CropAspectRatio(9, 16, "Featured Hero Banner (9:16 Ratio Frame)")
            }
        }
    }

    val subThumbLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val validUris = uris.filter { isImageSizeValid(it) }
            val updatedList = (subThumbnails + validUris.map { it.toString() }).take(5)
            subThumbnails = updatedList
            onUpdateSubThumbnails(updatedList)
        }
    }

    // Storage permission launcher for older APIs or custom file access
    var pendingPickerType by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (pendingPickerType) {
                "main_thumb" -> mainThumbLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                "banner" -> bannerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                "sub_thumb" -> subThumbLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
        pendingPickerType = null
    }

    fun requestMediaAccess(type: String) {
        when (type) {
            "main_thumb" -> mainThumbLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            "banner" -> bannerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            "sub_thumb" -> subThumbLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    // Show Android Standard Image Cropper Dialog when user picks image
    if (activeCropUri != null && activeCropRatio != null) {
        ImageCropperDialog(
            imageUri = activeCropUri!!,
            aspectRatio = activeCropRatio!!,
            onCropSaved = { croppedResultUri ->
                if (activeCropRatio?.widthRatio == 3) {
                    mainThumbnail = croppedResultUri
                } else if (activeCropRatio?.widthRatio == 9) {
                    bannerImage = croppedResultUri
                }
                onUpdatePage1(title, subtitle, description, category, mainThumbnail, bannerImage)
                activeCropUri = null
                activeCropRatio = null
            },
            onDismiss = {
                activeCropUri = null
                activeCropRatio = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "STEP 1: CORE STORY METADATA",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = "Define story info, pick media assets from device storage, and select category",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Title Input (Max 30 Chars)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Story Title",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "${title.length}/30",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (title.length >= 30) Color(0xFFF43F5E) else SahityaCyanSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= 30) {
                            title = it
                            onUpdatePage1(title, subtitle, description, category, mainThumbnail, bannerImage)
                        }
                    },
                    placeholder = { Text("e.g. Neon Kismet: Re-Birth") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("story_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SahityaCyanSecondary,
                        unfocusedBorderColor = SahityaBorderColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Subtitle Input (Max 150 Chars)
            ExpandableSmartWritingPad(
                value = subtitle,
                onValueChange = {
                    subtitle = it
                    onUpdatePage1(title, subtitle, description, category, mainThumbnail, bannerImage)
                },
                label = "Subtitle / Catchphrase",
                placeholder = "e.g. Cyberpunk Neo-Mumbai Anime Saga",
                maxLength = 150,
                collapsedMinLines = 1,
                expandedMinLines = 4,
                showBlockTemplateButton = false,
                testTag = "story_subtitle_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Description Input (Max 500 Chars)
            ExpandableSmartWritingPad(
                value = description,
                onValueChange = {
                    description = it
                    onUpdatePage1(title, subtitle, description, category, mainThumbnail, bannerImage)
                },
                label = "Story Description",
                placeholder = "Provide background, lore premise, and central conflicts...",
                maxLength = 500,
                collapsedMinLines = 2,
                expandedMinLines = 7,
                showBlockTemplateButton = false,
                testTag = "story_description_input"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector Dropdown
            Text(
                text = "Category Selector",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("story_category_dropdown"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SahityaCyanSecondary,
                        unfocusedBorderColor = SahityaBorderColor
                    )
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = Color.White) },
                            onClick = {
                                category = cat
                                categoryExpanded = false
                                onUpdatePage1(title, subtitle, description, category, mainThumbnail, bannerImage)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Main Story Thumbnail (3:4 Aspect Ratio Frame)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "1. Main Story Thumbnail",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = SahityaCyanSecondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "3:4 Aspect Ratio Frame",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SahityaCyanSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .width(130.dp)
                    .aspectRatio(3f / 4f)
                    .clickable { requestMediaAccess("main_thumb") }
                    .testTag("select_main_thumbnail_button"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SahityaCyanSecondary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (mainThumbnail.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = mainThumbnail,
                            contentDescription = "Main Thumbnail 3:4",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clickable { requestMediaAccess("main_thumb") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Crop, contentDescription = null, tint = SahityaCyanSecondary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("3:4", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }

                        Surface(
                            color = SahityaCyanSecondary,
                            shape = RoundedCornerShape(topEnd = 6.dp),
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = "3:4 POSTER",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = null, tint = SahityaCyanSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Main Thumbnail", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("(3:4 Poster)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Featured Hero Banner (9:16 Aspect Ratio Frame)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "2. Featured Hero Banner",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = SahityaAmberGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "9:16 Ratio Frame",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = SahityaAmberGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .width(130.dp)
                    .aspectRatio(9f / 16f)
                    .clickable { requestMediaAccess("banner") }
                    .testTag("select_hero_banner_button"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SahityaAmberGold.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (bannerImage.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = bannerImage,
                            contentDescription = "Featured Banner 9:16",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clickable { requestMediaAccess("banner") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Crop, contentDescription = null, tint = SahityaAmberGold, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("9:16", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }

                        Surface(
                            color = SahityaAmberGold,
                            shape = RoundedCornerShape(topEnd = 6.dp),
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = "9:16 STORY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SahityaAmberGold, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Featured Banner", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Text("(9:16 Frame)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF94A3B8))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Sub-Thumbnails Picker (Max 5 Limit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. Sub-Thumbnails (Max 5)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "${subThumbnails.size}/5 images",
                    style = MaterialTheme.typography.labelSmall,
                    color = SahityaCyanSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(subThumbnails) { index, url ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, SahityaBorderColor, RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Sub-Thumbnail ${index + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                val updated = subThumbnails.toMutableList().apply { removeAt(index) }
                                subThumbnails = updated
                                onUpdateSubThumbnails(updated)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                if (subThumbnails.size < 5) {
                    item {
                        Card(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { requestMediaAccess("sub_thumb") }
                                .testTag("add_sub_thumbnail_button"),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SahityaBorderColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = SahityaCyanSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Add Sub", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Next Page Action
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("page1_next_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SahityaIndigoPrimary)
            ) {
                Text("Proceed to Step 2: Character Builder", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

// -------------------------------------------------------------
// PAGE 2: Character Builder Module
// -------------------------------------------------------------
@Composable
fun Page2CharacterBuilder(
    characters: List<CharacterEntity>,
    onAddCharacter: () -> Unit,
    onUpdateCharacter: (index: Int, char: CharacterEntity) -> Unit,
    onRemoveCharacter: (index: Int) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STEP 2: CHARACTER BUILDER MODULE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Define AI character personas, language models, and traits",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Prominent [ + Add Character ] Button
                Button(
                    onClick = onAddCharacter,
                    modifier = Modifier.testTag("add_character_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SahityaCyanSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Character", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        itemsIndexed(characters) { index, char ->
            CharacterCardItem(
                index = index,
                character = char,
                canRemove = characters.size > 1,
                onUpdate = { updated -> onUpdateCharacter(index, updated) },
                onRemove = { onRemoveCharacter(index) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("page2_next_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SahityaIndigoPrimary)
                ) {
                    Text("Proceed to Step 3", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCardItem(
    index: Int,
    character: CharacterEntity,
    canRemove: Boolean,
    onUpdate: (CharacterEntity) -> Unit,
    onRemove: () -> Unit
) {
    var langExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("character_card_$index"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SahityaBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SahityaIndigoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = character.name.ifBlank { "New Character" },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Character",
                            tint = Color(0xFFF43F5E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Character Name Field
            OutlinedTextField(
                value = character.name,
                onValueChange = { onUpdate(character.copy(name = it)) },
                label = { Text("Character Name") },
                placeholder = { Text("e.g. Aarav Varma") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("character_name_input_$index"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SahityaCyanSecondary,
                    unfocusedBorderColor = SahityaBorderColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Backstory / Description Box
            ExpandableSmartWritingPad(
                value = character.backstory,
                onValueChange = { onUpdate(character.copy(backstory = it)) },
                label = "Description / Backstory",
                placeholder = "Enter character origin, motivations, and secret goals...",
                maxLength = 1000,
                collapsedMinLines = 2,
                expandedMinLines = 6,
                showBlockTemplateButton = false,
                testTag = "character_backstory_input_$index"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Language Dropdown (Hinglish - Default, English)
            Text(
                text = "Primary Language Model",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFCBD5E1)
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = langExpanded,
                onExpandedChange = { langExpanded = !langExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = character.language,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Language") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("character_language_dropdown_$index"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SahityaCyanSecondary,
                        unfocusedBorderColor = SahityaBorderColor
                    )
                )

                ExposedDropdownMenu(
                    expanded = langExpanded,
                    onDismissRequest = { langExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Hinglish (Default - Native Indian Roleplay)", color = Color.White) },
                        onClick = {
                            onUpdate(character.copy(language = "Hinglish"))
                            langExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("English (Global Roleplay)", color = Color.White) },
                        onClick = {
                            onUpdate(character.copy(language = "English"))
                            langExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Personality Traits Tags / Field
            OutlinedTextField(
                value = character.traits,
                onValueChange = { onUpdate(character.copy(traits = it)) },
                label = { Text("Nature / Personality Traits") },
                placeholder = { Text("e.g. Tsundere, Loyal, Sarcastic, Tech Wizard") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("character_traits_input_$index"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SahityaCyanSecondary,
                    unfocusedBorderColor = SahityaBorderColor
                )
            )
        }
    }
}

// -------------------------------------------------------------
// PAGE 3: Conditional Branching & Anime Season 50k Limit
// -------------------------------------------------------------
@Composable
fun Page3ConditionalBranching(
    selectedCategory: String,
    masterTimeline: String,
    seasons: List<SeasonEntity>,
    characters: List<CharacterEntity>,
    mainThumbnail: String,
    bannerImage: String,
    onUpdateMasterTimeline: (String) -> Unit,
    onAddSeason: () -> Unit,
    onUpdateSeason: (index: Int, season: SeasonEntity) -> Unit,
    onRemoveSeason: (index: Int) -> Unit,
    onBack: () -> Unit,
    onSaveDraft: () -> Unit,
    onPublishPipeline: () -> Unit
) {
    val isAnime = selectedCategory.equals("Anime", ignoreCase = true)
    val context = androidx.compose.ui.platform.LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "STEP 3: CONDITIONAL TIMELINE & SEASON BRANCHING",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = if (isAnime) "Anime Category Detected: Season Limit is 50,000 characters. AI Formatter operates on Season 1."
                else "Genre Category: Master Narrative Arc & World Building Rules",
                style = MaterialTheme.typography.bodySmall,
                color = SahityaCyanSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!isAnime) {
            // Non-Anime Genre: Master Story Timeline Editor
            item {
                var showMasterTimelineDeleteConfirm by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SahityaBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Master Story Timeline / Narrative Arc",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Detail key plot points, climax hooks, and lore boundaries for this story",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            if (masterTimeline.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFFE11D48).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clickable { showMasterTimelineDeleteConfirm = !showMasterTimelineDeleteConfirm }
                                        .testTag("delete_master_timeline_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Arc",
                                            tint = Color(0xFFF43F5E),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Delete Arc",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFFF43F5E)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { uriHandler.openUri("https://drive.google.com/file/d/1b7t8bH710JOxAgLP-U0z7WuJg3Abj8PJ/view?usp=drivesdk") },
                            colors = CardDefaults.cardColors(containerColor = SahityaEmeraldAccent.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SahityaEmeraldAccent)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = "PDF Template", tint = SahityaEmeraldAccent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Download Formatting Template PDF", color = SahityaEmeraldAccent, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("Use this template with another AI to properly format your narrative arc before pasting.", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        ExpandableSmartWritingPad(
                            value = masterTimeline,
                            onValueChange = onUpdateMasterTimeline,
                            label = "Master Story Timeline / Narrative Arc",
                            placeholder = "Act 1: Discovery -> Act 2: Rising Tension & Rivalry -> Act 3: Climax Battle...",
                            maxLength = 20000,
                            collapsedMinLines = 4,
                            expandedMinLines = 10,
                            showBlockTemplateButton = true,
                            showDirectionalPad = true,
                            testTag = "master_timeline_input"
                        )

                        // Confirmation of user right below ("neeche") with 5s timer
                        if (showMasterTimelineDeleteConfirm) {
                            InlineDeleteConfirmationBox(
                                title = "Confirm Narrative Arc Deletion",
                                message = "Are you sure you want to delete the Master Narrative Arc for this story? All detailed plot outlines and climax hooks will be cleared.",
                                onConfirmDelete = {
                                    onUpdateMasterTimeline("")
                                    showMasterTimelineDeleteConfirm = false
                                },
                                onCancel = {
                                    showMasterTimelineDeleteConfirm = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // Anime Genre: Dynamic Season Builder with 50k limit & character multi-select
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Anime Season Builder",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Construct multiple seasons and assign active characters per season",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Prominent [ + Add Season ] Button
                    Button(
                        onClick = onAddSeason,
                        modifier = Modifier.testTag("add_season_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SahityaCyanSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Season", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            itemsIndexed(seasons) { index, season ->
                SeasonCardItem(
                    index = index,
                    season = season,
                    isAnime = isAnime,
                    availableCharacters = characters,
                    canRemove = seasons.size > 1,
                    onUpdate = { updated -> onUpdateSeason(index, updated) },
                    onRemove = { onRemoveSeason(index) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons: Save Draft & Run Dual-Layer AI Pipeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSaveDraft,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("save_draft_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Draft")
                }

                Button(
                    onClick = {
                        val blocks = StoryBlockParser.parseBlocks(masterTimeline)
                        var isValid = blocks.isNotEmpty()
                        if (isValid) {
                            for (block in blocks) {
                                val content = block.cleanContent
                                if (!content.contains("s:") || !content.contains("t:") || !content.contains("c:") || !content.contains("w:")) {
                                    isValid = false
                                    break
                                }
                            }
                        }
                        if (!isValid) {
                            Toast.makeText(context, "Format must be { s : ... t : ... c : ... w : ... }", Toast.LENGTH_LONG).show()
                        } else if (mainThumbnail.isEmpty() || bannerImage.isEmpty()) {
                            Toast.makeText(context, "Cannot publish without thumbnail and featured hero banner.", Toast.LENGTH_LONG).show()
                        } else {
                            onPublishPipeline()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp)
                        .testTag("run_pipeline_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SahityaEmeraldAccent)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish via AI Pipeline", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SeasonCardItem(
    index: Int,
    season: SeasonEntity,
    isAnime: Boolean,
    availableCharacters: List<CharacterEntity>,
    canRemove: Boolean,
    onUpdate: (SeasonEntity) -> Unit,
    onRemove: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val selectedCharIds = remember(season.characterIdsJson) {
        season.characterIdsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    val maxLimit = if (isAnime) 50000 else 10000

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("season_card_$index"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SahityaIndigoPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SahityaIndigoPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val displayTitle = if (season.seasonName.isNotBlank() && !season.seasonName.startsWith("Season", ignoreCase = true)) {
                        "${if (isAnime) "Season" else "Arc"} ${season.seasonNumber}: ${season.seasonName}"
                    } else if (season.seasonName.isNotBlank()) {
                        season.seasonName
                    } else {
                        "${if (isAnime) "Season" else "Arc"} ${season.seasonNumber}"
                    }
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Delete Season Button
                Surface(
                    color = Color(0xFFE11D48).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { showDeleteConfirm = !showDeleteConfirm }
                        .testTag("delete_season_button_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Season",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF43F5E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Season Name
            OutlinedTextField(
                value = season.seasonName,
                onValueChange = { onUpdate(season.copy(seasonName = it)) },
                label = { Text("Season Title") },
                placeholder = { Text("e.g. Season 1: Prelude to War") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("season_name_input_$index"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SahityaCyanSecondary,
                    unfocusedBorderColor = SahityaBorderColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Season Story Timeline Editor with Character Limit Counter
            ExpandableSmartWritingPad(
                value = season.seasonTimeline,
                onValueChange = {
                    if (it.length <= maxLimit) {
                        onUpdate(season.copy(seasonTimeline = it))
                    }
                },
                label = "Season Story Timeline ${if (isAnime) "(Anime Max 50k)" else ""}",
                placeholder = "Timeline arc for this season...",
                maxLength = maxLimit,
                collapsedMinLines = 3,
                expandedMinLines = 8,
                showBlockTemplateButton = true,
                showDirectionalPad = true,
                testTag = "season_timeline_input_$index"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Character Multi-Select Checklist populated dynamically from Step 2 characters
            Text(
                text = "Assign Characters to Season (Click multiple to assign):",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = SahityaCyanSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (availableCharacters.isEmpty()) {
                Text(
                    text = "No characters created in Step 2 yet. Please add characters first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            } else {
                Column {
                    availableCharacters.forEach { char ->
                        val isChecked = selectedCharIds.contains(char.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSet = selectedCharIds.toMutableSet()
                                    if (isChecked) newSet.remove(char.id) else newSet.add(char.id)
                                    onUpdate(season.copy(characterIdsJson = newSet.joinToString(",")))
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val newSet = selectedCharIds.toMutableSet()
                                    if (checked) newSet.add(char.id) else newSet.remove(char.id)
                                    onUpdate(season.copy(characterIdsJson = newSet.joinToString(",")))
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SahityaCyanSecondary,
                                    uncheckedColor = Color(0xFF64748B)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${char.name} (${char.language})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Inline Confirmation of user right below ("neeche") with 5s timer
            if (showDeleteConfirm) {
                InlineDeleteConfirmationBox(
                    title = if (isAnime) "Confirm Anime Season ${season.seasonNumber} Deletion" else "Confirm Story Arc ${season.seasonNumber} Deletion",
                    message = if (isAnime)
                        "Are you sure you want to delete '${season.seasonName.ifBlank { "Season " + season.seasonNumber }}'? Active characters and timeline arc for this anime season will be removed."
                    else
                        "Are you sure you want to delete '${season.seasonName.ifBlank { "Arc/Season " + season.seasonNumber }}'? Narrative arc timeline details will be reset.",
                    onConfirmDelete = {
                        showDeleteConfirm = false
                        if (canRemove) {
                            onRemove()
                        } else {
                            // If it's the only remaining season, clear its content rather than leaving 0 seasons
                            onUpdate(season.copy(seasonName = "Season 1", seasonTimeline = "", characterIdsJson = ""))
                        }
                    },
                    onCancel = {
                        showDeleteConfirm = false
                    }
                )
            }
        }
    }
}
