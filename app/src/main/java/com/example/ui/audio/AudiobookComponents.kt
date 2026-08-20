package com.example.ui.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.audio.LibrivoxAudiobook
import com.example.data.audio.LibrivoxAudioTrack
import java.util.Locale

private val SerifHeaderFont = FontFamily.Serif
private val ObsidianDarkBg = Color(0xFF141210)
private val CardSurface = Color(0xFF221F1C)
private val WarmAmber = Color(0xFFD97706)
private val SoftSepiaText = Color(0xFFE2D9CC)
private val MutedText = Color(0xFFA1978A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookPickerSheet(
    bookTitle: String,
    audiobooks: List<LibrivoxAudiobook>,
    isLoading: Boolean,
    onAudiobookSelected: (LibrivoxAudiobook) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ObsidianDarkBg,
        contentColor = SoftSepiaText,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MutedText) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("audiobook_picker_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LibriVox Audiobooks",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = SerifHeaderFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Confirm audio version for \"$bookTitle\"",
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedText)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = WarmAmber)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Searching LibriVox public domain audiobooks...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                        )
                    }
                }
            } else if (audiobooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matching audiobook found on LibriVox.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SoftSepiaText)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(audiobooks) { _, book ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAudiobookSelected(book) }
                                .testTag("audiobook_item_${book.id}"),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(WarmAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = WarmAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = book.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "By ${book.author}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF2D2924)
                                        ) {
                                            Text(
                                                text = "${book.numSections} Sections • ${book.totalTime}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = WarmAmber,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF2D2924)
                                        ) {
                                            Text(
                                                text = book.language.uppercase(Locale.ROOT),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = SoftSepiaText,
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Select",
                                    tint = MutedText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookPlayerSheet(
    state: AudioPlayerState,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var showChapterList by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ObsidianDarkBg,
        contentColor = SoftSepiaText,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MutedText) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("audiobook_player_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = MutedText)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = WarmAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIBRIVOX AUDIOBOOK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmAmber,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                IconButton(onClick = { showChapterList = !showChapterList }) {
                    Icon(
                        imageVector = if (showChapterList) Icons.Default.Close else Icons.Default.FormatListNumbered,
                        contentDescription = "Chapter List",
                        tint = if (showChapterList) WarmAmber else MutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showChapterList) {
                // Chapter Selector View
                Text(
                    text = "Audio Chapters",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SerifHeaderFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(state.tracks) { idx, track ->
                        val isSelected = idx == state.currentTrackIndex
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTrack(idx)
                                    showChapterList = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) WarmAmber.copy(alpha = 0.2f) else CardSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${track.sectionNumber}.",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (isSelected) WarmAmber else MutedText,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.width(28.dp)
                                )
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) Color.White else SoftSepiaText,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatDurationMs(track.playtimeSecs * 1000L),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                                )
                            }
                        }
                    }
                }
            } else {
                // Main Player View
                // Cover Art / Artwork Card
                Card(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (state.coverUri.isNotBlank()) {
                            AsyncImage(
                                model = state.coverUri,
                                contentDescription = state.bookTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF332B23), Color(0xFF1E1A16))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = WarmAmber,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        if (state.isPlaying) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(32.dp)
                                    .background(WarmAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title & Author
                Text(
                    text = state.bookTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SerifHeaderFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.authorName,
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedText),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Current Chapter / Track Title
                val currentTrack = state.tracks.getOrNull(state.currentTrackIndex)
                Text(
                    text = currentTrack?.title ?: "Loading Track...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = WarmAmber,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Seek Bar Slider
                val currentPos = state.currentPositionMs.toFloat()
                val totalDur = state.durationMs.coerceAtLeast(1L).toFloat()

                Slider(
                    value = (currentPos / totalDur).coerceIn(0f, 1f),
                    onValueChange = { fraction ->
                        onSeekTo((fraction * totalDur).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = WarmAmber,
                        activeTrackColor = WarmAmber,
                        inactiveTrackColor = CardSurface
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("audiobook_seekbar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDurationMs(state.currentPositionMs),
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                    )
                    Text(
                        text = formatDurationMs(state.durationMs),
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedText)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error message if any
                if (!state.errorMessage.isNullOrBlank()) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Speed Button
                    val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f)
                    Surface(
                        onClick = {
                            val nextSpeedIdx = (speeds.indexOf(state.playbackSpeed) + 1) % speeds.size
                            onSelectSpeed(speeds[nextSpeedIdx])
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurface
                    ) {
                        Text(
                            text = "${state.playbackSpeed}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = WarmAmber
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    // Skip 10s Backward
                    IconButton(onClick = onSkipBackward) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Skip 10 seconds back",
                            tint = SoftSepiaText,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Play/Pause FAB
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(WarmAmber)
                            .clickable(onClick = onTogglePlayPause)
                            .testTag("audiobook_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoadingTrack) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Skip 10s Forward
                    IconButton(onClick = onSkipForward) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Skip 10 seconds forward",
                            tint = SoftSepiaText,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Chapter List Button
                    IconButton(onClick = { showChapterList = true }) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "Chapters",
                            tint = SoftSepiaText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remainingSecs = totalSeconds % 60
    val hours = minutes / 60
    val remainingMins = minutes % 60

    return if (hours > 0) {
        String.format(Locale.ROOT, "%02d:%02d:%02d", hours, remainingMins, remainingSecs)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", remainingMins, remainingSecs)
    }
}
