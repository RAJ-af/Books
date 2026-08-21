package com.example.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.ReadingProgress
import com.example.ui.components.BookCoverItem
import com.example.ui.audio.AudiobookPickerSheet
import com.example.ui.audio.AudiobookPlayerSheet
import com.example.ui.theme.ContentSerif
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftSepiaSurface
import com.example.ui.theme.SystemSans
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmOffWhite

@Composable
fun BookDetailScreen(
    viewModel: BookDetailViewModel,
    onBackClick: () -> Unit,
    onReadBookClick: (bookId: Long, chapterId: Long) -> Unit,
    onBookmarksClick: (bookId: Long) -> Unit = {}
) {
    val book by viewModel.book.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    val audiobooks by viewModel.audiobooks.collectAsStateWithLifecycle()
    val isCheckingAudio by viewModel.isCheckingAudio.collectAsStateWithLifecycle()
    val showAudioPicker by viewModel.showAudioPicker.collectAsStateWithLifecycle()
    val playerState by viewModel.audioPlayerManager.playerState.collectAsStateWithLifecycle()

    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showEditDetailsDialog by remember { mutableStateOf(false) }

    val baseColor = remember(book?.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(book?.colorHex ?: "#285698"))
        } catch (_: Exception) {
            Color(0xFF285698)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // 1. Frosted / Blurred Full-bleed Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.28f),
                            baseColor.copy(alpha = 0.14f),
                            CreamBackground.copy(alpha = 0.85f),
                            CreamBackground
                        )
                    )
                )
        ) {
        // Blurred decorative atmospheric circles matching book color
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopCenter)
                .blur(50.dp)
                .background(baseColor.copy(alpha = 0.25f), CircleShape)
        )
    }

    if (showEditDetailsDialog && book != null) {
        val currentBook = book!!
        var editTitle by remember { mutableStateOf(currentBook.title) }
        var editAuthor by remember { mutableStateOf(currentBook.author) }
        var editGenre by remember { mutableStateOf(currentBook.genre) }
        var editDesc by remember { mutableStateOf(currentBook.description) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDetailsDialog = false },
            containerColor = WarmOffWhite,
            title = {
                Text(
                    text = "Edit Book Details",
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBlack
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editAuthor,
                        onValueChange = { editAuthor = it },
                        label = { Text("Author") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_author_input")
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editGenre,
                        onValueChange = { editGenre = it },
                        label = { Text("Genre") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_genre_input")
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_desc_input"),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateBookDetails(
                            title = editTitle.trim(),
                            author = editAuthor.trim(),
                            genre = editGenre.trim(),
                            description = editDesc.trim()
                        )
                        showEditDetailsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack),
                    modifier = Modifier.testTag("save_book_details_button")
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEditDetailsDialog = false }
                ) {
                    Text("Cancel", color = ObsidianBlack)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            BookDetailTopBar(
                onBackClick = onBackClick,
                showMenu = showMoreMenu,
                onToggleMenu = { showMoreMenu = it },
                onBookmarksClick = { book?.id?.let { onBookmarksClick(it) } },
                onMarkFinished = {
                    val lastChapter = chapters.lastOrNull()
                    if (lastChapter != null) {
                        viewModel.markChapterProgress(lastChapter.id, 100f)
                    }
                    showMoreMenu = false
                },
                onResetProgress = {
                    val firstChapter = chapters.firstOrNull()
                    if (firstChapter != null) {
                        viewModel.markChapterProgress(firstChapter.id, 0f)
                    }
                    showMoreMenu = false
                },
                onEditDetailsClick = { showEditDetailsDialog = true }
            )
        }
    ) { innerPadding ->
            if (book == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ObsidianBlack)
                }
            } else {
                val currentBook = book!!
                val currentChapterId = progress?.currentChapterId?.takeIf { it > 0 }
                    ?: chapters.firstOrNull()?.id
                    ?: 1L

                val currentChapterIndex = chapters.indexOfFirst { it.id == currentChapterId }.let {
                    if (it == -1) 0 else it
                }
                val currentChapterNumber = if (chapters.isNotEmpty()) currentChapterIndex + 1 else 1
                val totalChapters = chapters.size.coerceAtLeast(1)
                val percentComplete = (progress?.percentComplete ?: 0f).coerceIn(0f, 100f)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .testTag("book_detail_scroll_column"),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Center Hero: Book Cover in White-Bordered Card with Subtle Shadow
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .shadow(
                                        elevation = 14.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        spotColor = ObsidianBlack.copy(alpha = 0.35f)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 8.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(5.dp) // Crisp white border effect
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    BookCoverItem(
                                        book = currentBook,
                                        progress = progress,
                                        width = 136.dp,
                                        height = 202.dp,
                                        onClick = {
                                            onReadBookClick(currentBook.id, currentChapterId)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Large Serif Title
                            Text(
                                text = currentBook.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = EditorialSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    lineHeight = 30.sp,
                                    color = ObsidianBlack
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("detail_book_title")
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Author (Muted Serif)
                            Text(
                                text = currentBook.author,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = ContentSerif,
                                    color = TextMuted,
                                    fontSize = 15.sp
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("detail_book_author")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Pill Row: Rating • Page Count • Genre
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = SoftSepiaSurface.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .border(0.5.dp, Color(0xFFDDD3C4), RoundedCornerShape(percent = 50))
                                    .testTag("detail_meta_pill_row")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Rating
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFD49A3D),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${currentBook.rating}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ObsidianBlack,
                                                fontFamily = SystemSans
                                            )
                                        )
                                    }

                                    Text(
                                        text = "•",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )

                                    // Page count
                                    Text(
                                        text = "${currentBook.pageCount} pages",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = ObsidianBlack.copy(alpha = 0.8f),
                                            fontFamily = SystemSans
                                        )
                                    )

                                    Text(
                                        text = "•",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )

                                    // Genre
                                    Text(
                                        text = currentBook.genre,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = ObsidianBlack,
                                            fontFamily = SystemSans
                                        )
                                    )

                                    if (currentBook.isImportedPdf || currentBook.fileType == "EPUB" || currentBook.source != "local") {
                                        Text(
                                            text = "•",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (currentBook.source) {
                                                "gutenberg" -> Color(0xFFD97706)
                                                "standard_ebooks" -> Color(0xFF059669)
                                                "doab" -> Color(0xFF2E6F40)
                                                "internet_archive" -> Color(0xFF285698)
                                                else -> ObsidianBlack
                                            }
                                        ) {
                                            Text(
                                                text = when (currentBook.source) {
                                                    "gutenberg" -> "PROJECT GUTENBERG EPUB"
                                                    "standard_ebooks" -> "STANDARD EBOOKS EPUB"
                                                    "doab" -> "DOAB OPEN ACCESS"
                                                    "internet_archive" -> "INTERNET ARCHIVE"
                                                    else -> currentBook.fileType.uppercase()
                                                },
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SystemSans,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Buttons Row: Read Book (Black Pill) + Listen (Outline Pill)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Primary Button: "Read Book"
                                Button(
                                    onClick = {
                                        onReadBookClick(currentBook.id, currentChapterId)
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .height(50.dp)
                                        .shadow(6.dp, shape = RoundedCornerShape(percent = 50), spotColor = ObsidianBlack.copy(alpha = 0.25f))
                                        .testTag("read_book_primary_button"),
                                    shape = RoundedCornerShape(percent = 50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ObsidianBlack,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoStories,
                                            contentDescription = null,
                                            modifier = Modifier.size(17.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (percentComplete > 0f) "Resume Book" else "Read Book",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White,
                                                fontFamily = SystemSans
                                            )
                                        )
                                    }
                                }

                                // Secondary Button: "Listen" (Enabled if LibriVox audiobook matches exist)
                                val hasAudioMatch = audiobooks.isNotEmpty()
                                val isAudioButtonEnabled = hasAudioMatch || isCheckingAudio

                                OutlinedButton(
                                    onClick = { viewModel.onListenButtonClicked() },
                                    enabled = isAudioButtonEnabled,
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(50.dp)
                                        .testTag("listen_book_button"),
                                    shape = RoundedCornerShape(percent = 50),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = ObsidianBlack,
                                        disabledContentColor = TextMuted.copy(alpha = 0.4f)
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.linearGradient(
                                            if (isAudioButtonEnabled) listOf(ObsidianBlack, ObsidianBlack)
                                            else listOf(TextMuted.copy(alpha = 0.3f), TextMuted.copy(alpha = 0.3f))
                                        )
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isCheckingAudio) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = ObsidianBlack
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Headphones,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isAudioButtonEnabled) ObsidianBlack else TextMuted.copy(alpha = 0.4f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isCheckingAudio) "Checking..." else "Listen",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                fontFamily = SystemSans,
                                                color = if (isAudioButtonEnabled) ObsidianBlack else TextMuted.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Card 1: "About this book" (Expandable with AnimatedVisibility)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(animationSpec = tween(250))
                                .testTag("card_about_book"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmOffWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "About this book",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = EditorialSerif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = ObsidianBlack
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = currentBook.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = ContentSerif,
                                        fontSize = 14.5.sp,
                                        lineHeight = 22.sp,
                                        color = ObsidianBlack.copy(alpha = 0.85f)
                                    ),
                                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isDescriptionExpanded) "Show less" else "Read more",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = SystemSans,
                                            fontWeight = FontWeight.Bold,
                                            color = ObsidianBlack
                                        )
                                    )
                                    Icon(
                                        imageVector = if (isDescriptionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = ObsidianBlack,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Card 2: "Your Progress"
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_reading_progress"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = WarmOffWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Progress",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = EditorialSerif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = ObsidianBlack
                                        )
                                    )

                                    Text(
                                        text = "${percentComplete.toInt()}%",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = SystemSans,
                                            fontWeight = FontWeight.Bold,
                                            color = ObsidianBlack
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val progressLabel = if (currentBook.isImportedPdf) {
                                    "Page $currentChapterNumber of $totalChapters • Document Page"
                                } else {
                                    "Chapter $currentChapterNumber of $totalChapters • ${chapters.getOrNull(currentChapterIndex)?.title ?: "Intro"}"
                                }

                                Text(
                                    text = progressLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = ContentSerif,
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Black Linear Progress Bar
                                LinearProgressIndicator(
                                    progress = { (percentComplete / 100f).coerceIn(0.01f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = ObsidianBlack,
                                    trackColor = SoftSepiaSurface
                                )
                            }
                        }
                    }

                    // Card 3: "Chapters" Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentBook.isImportedPdf) "Document Pages" else "Chapters",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = EditorialSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = ObsidianBlack
                                )
                            )

                            Text(
                                text = if (currentBook.isImportedPdf) "$totalChapters pages" else "$totalChapters total",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = SystemSans,
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    // Chapters List with status indicators
                    itemsIndexed(
                        items = chapters,
                        key = { _, chapter -> chapter.id }
                    ) { index, chapter ->
                        val isCurrent = chapter.id == currentChapterId
                        val isDone = if (percentComplete >= 100f) true else (index < currentChapterIndex)

                        ChapterDetailRowItem(
                            chapter = chapter,
                            chapterIndex = index + 1,
                            isCurrent = isCurrent,
                            isDone = isDone,
                            onClick = {
                                onReadBookClick(currentBook.id, chapter.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }

        // Audiobook Picker Bottom Sheet
        if (showAudioPicker) {
            AudiobookPickerSheet(
                bookTitle = book?.title.orEmpty(),
                audiobooks = audiobooks,
                isLoading = isCheckingAudio,
                onAudiobookSelected = { selectedAudiobook ->
                    viewModel.selectAudiobookAndPlay(selectedAudiobook)
                },
                onDismiss = { viewModel.dismissAudioPicker() }
            )
        }

        // Audiobook Player Bottom Sheet
        if (playerState.isVisible) {
            AudiobookPlayerSheet(
                state = playerState,
                onTogglePlayPause = { viewModel.audioPlayerManager.togglePlayPause() },
                onSeekTo = { posMs -> viewModel.audioPlayerManager.seekTo(posMs) },
                onSkipForward = { viewModel.audioPlayerManager.skipForward(10) },
                onSkipBackward = { viewModel.audioPlayerManager.skipBackward(10) },
                onNextTrack = { viewModel.audioPlayerManager.nextTrack() },
                onPreviousTrack = { viewModel.audioPlayerManager.previousTrack() },
                onSelectSpeed = { speed -> viewModel.audioPlayerManager.setSpeed(speed) },
                onSelectTrack = { index -> viewModel.audioPlayerManager.playTrackAtIndex(index) },
                onDismiss = { viewModel.audioPlayerManager.hidePlayerSheet() }
            )
        }
    }
}

@Composable
fun BookDetailTopBar(
    onBackClick: () -> Unit,
    showMenu: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onBookmarksClick: () -> Unit,
    onMarkFinished: () -> Unit,
    onResetProgress: () -> Unit,
    onEditDetailsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Black Back Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, shape = CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(ObsidianBlack)
                .clickable(onClick = onBackClick)
                .testTag("detail_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Library",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Circular Black More-Options Button
        Box {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, shape = CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(ObsidianBlack)
                .clickable { onToggleMenu(true) }
                .testTag("detail_more_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More Options",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { onToggleMenu(false) },
                modifier = Modifier.background(WarmOffWhite)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Bookmarks & Highlights",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = ObsidianBlack
                        )
                    },
                    onClick = {
                        onToggleMenu(false)
                        onBookmarksClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Mark as Finished",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = onMarkFinished
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Reset Reading Progress",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = onResetProgress
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            "Edit Details",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = ObsidianBlack
                        )
                    },
                    onClick = {
                        onToggleMenu(false)
                        onEditDetailsClick()
                    }
                )
            }
        }
    }
}

@Composable
fun ChapterDetailRowItem(
    chapter: Chapter,
    chapterIndex: Int,
    isCurrent: Boolean,
    isDone: Boolean,
    onClick: () -> Unit
) {
    // Warm peach tint background for current chapter
    val backgroundColor = when {
        isCurrent -> Color(0xFFF3E5D4) // warm peach tint highlight
        else -> WarmOffWhite
    }

    val borderColor = when {
        isCurrent -> Color(0xFFDDBF9B)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isCurrent) 1.dp else 0.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("chapter_row_${chapter.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 3.dp else 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator Circle:
            // 1. Checkmark for Done
            // 2. Filled Amber Dot for Current
            // 3. Plain Number for Unread
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> ObsidianBlack
                            isCurrent -> Color(0xFFE89A5A)
                            else -> SoftSepiaSurface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isDone -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    isCurrent -> {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                    else -> {
                        Text(
                            text = "$chapterIndex",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack,
                                fontFamily = SystemSans
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Chapter Title & Subtitle
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = EditorialSerif,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.5.sp,
                            color = ObsidianBlack
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = ObsidianBlack
                        ) {
                            Text(
                                text = "NOW",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = SystemSans,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (chapter.subtitle.isNotBlank()) {
                    Text(
                        text = chapter.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = ContentSerif,
                            color = TextMuted,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Estimated read time + chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "${chapter.estimatedReadMinutes}m",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SystemSans,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ObsidianBlack.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
