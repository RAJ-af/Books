package com.example.ui.reader

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.settings.ReaderFontStyle
import com.example.data.settings.ReaderLineSpacing
import com.example.data.settings.ReaderSettings
import com.example.data.settings.ReaderThemeMode
import com.example.ui.audio.AudiobookPlayerSheet
import com.example.ui.theme.ContentSerif
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SystemSans
import com.example.ui.theme.TextMuted
import com.example.util.PdfHelper

enum class PdfReaderMode {
    ORIGINAL,
    REFLOW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    initialPage: Int = 0,
    initialParagraph: Int = 0
) {
    val book by viewModel.book.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val currentChapterId by viewModel.currentChapterId.collectAsStateWithLifecycle()
    val settings by viewModel.readerSettings.collectAsStateWithLifecycle()
    val playerState by viewModel.audioPlayerManager.playerState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()

    val currentChapter = chapters.find { it.id == currentChapterId } ?: chapters.firstOrNull()
    val currentChapterIndex = chapters.indexOfFirst { it.id == currentChapter?.id }.let { if (it == -1) 0 else it }
    val totalChapters = chapters.size.coerceAtLeast(1)
    val isPdf = book?.isImportedPdf == true || book?.fileType == "PDF" || (book?.pdfFilePath?.isNotBlank() == true)
    val isScanned = book?.isScanned == true

    val listState = rememberLazyListState()
    var showFormatSheet by remember { mutableStateOf(false) }
    var pdfReaderMode by rememberSaveable { mutableStateOf(PdfReaderMode.ORIGINAL) }

    val chapterHighlights = remember(highlights, currentChapterId) {
        highlights.filter { it.chapterId == currentChapterId }
    }

    val firstVisibleIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }

    val isCurrentPositionBookmarked = remember(bookmarks, currentChapterId, currentChapterIndex, firstVisibleIndex.value, isPdf) {
        bookmarks.any { bm ->
            bm.chapterId == currentChapterId &&
            if (isPdf) bm.pageNumber == currentChapterIndex else bm.scrollAnchor == firstVisibleIndex.value
        }
    }

    val clipboardManager = LocalClipboardManager.current
    var selectedTextForHighlight by remember { mutableStateOf<String?>(null) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#FEF08A") }

    val defaultTextToolbar = LocalTextToolbar.current
    val customTextToolbar = remember(defaultTextToolbar, clipboardManager) {
        object : TextToolbar {
            override val status: TextToolbarStatus
                get() = try { defaultTextToolbar.status } catch (_: Throwable) { TextToolbarStatus.Hidden }

            override fun hide() {
                try {
                    defaultTextToolbar.hide()
                } catch (_: Throwable) {}
            }

            override fun showMenu(
                rect: Rect,
                onCopyRequested: (() -> Unit)?,
                onPasteRequested: (() -> Unit)?,
                onCutRequested: (() -> Unit)?,
                onSelectAllRequested: (() -> Unit)?
            ) {
                val wrappedCopy = onCopyRequested?.let { originalCopy ->
                    {
                        originalCopy.invoke()
                        val text = clipboardManager.getText()?.text?.toString()?.trim()
                        if (!text.isNullOrBlank()) {
                            selectedTextForHighlight = text
                        }
                    }
                }
                try {
                    defaultTextToolbar.showMenu(rect, wrappedCopy ?: onCopyRequested, onPasteRequested, onCutRequested, onSelectAllRequested)
                } catch (_: Throwable) {}
            }
        }
    }

    DisposableEffect(currentChapterId, pdfReaderMode) {
        onDispose {
            try {
                customTextToolbar.hide()
            } catch (_: Throwable) {}
        }
    }

    // Jump to initial paragraph if requested
    LaunchedEffect(initialParagraph) {
        if (initialParagraph > 0 && !isPdf) {
            listState.scrollToItem(initialParagraph)
        }
    }

    // Color theme definition matching screenshot palette
    val (backgroundColor, textColor, pillBgColor, bottomBarBorderColor) = when (settings.theme) {
        ReaderThemeMode.LIGHT -> Quadruple(
            Color(0xFFFFFFFF),
            Color(0xFF1E1C1A),
            Color(0xFFF7F5F0).copy(alpha = 0.94f),
            Color(0xFFE8E4DC)
        )
        ReaderThemeMode.SEPIA -> Quadruple(
            Color(0xFFF7F2E8), // Authentic warm parchment tone from image
            Color(0xFF2B2621), // Rich dark espresso charcoal
            Color(0xFFEFE8DA).copy(alpha = 0.92f), // Frosted warm pill
            Color(0xFFE2D8C6)
        )
        ReaderThemeMode.DARK -> Quadruple(
            Color(0xFF161514),
            Color(0xFFE5DFD7),
            Color(0xFF262422).copy(alpha = 0.94f),
            Color(0xFF383532)
        )
    }

    // Dynamic reader font family (for text books)
    val activeFontFamily = when (settings.fontStyle) {
        ReaderFontStyle.SERIF_LORA -> ContentSerif
        ReaderFontStyle.SERIF_PLAYFAIR -> EditorialSerif
        ReaderFontStyle.SYSTEM_SERIF -> FontFamily.Serif
        ReaderFontStyle.SYSTEM_SANS -> SystemSans
    }

    val lineHeight = (settings.fontSizeSp * settings.lineSpacing.valueMultiplier).sp

    // Reset scroll when chapter changes
    LaunchedEffect(currentChapterId) {
        listState.scrollToItem(0)
    }

    // Horizontal swipe gesture detection on page content
    var totalDragX by remember { mutableFloatStateOf(0f) }
    val pageSwipeModifier = Modifier.pointerInput(currentChapterIndex, chapters.size) {
        detectHorizontalDragGestures(
            onDragStart = { totalDragX = 0f },
            onDragEnd = {
                val swipeThreshold = 75f
                if (totalDragX < -swipeThreshold) {
                    viewModel.navigateToNextChapter()
                } else if (totalDragX > swipeThreshold) {
                    viewModel.navigateToPreviousChapter()
                }
                totalDragX = 0f
            },
            onDragCancel = { totalDragX = 0f },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                totalDragX += dragAmount
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .testTag("reader_screen_container")
    ) {
        // Main reading content (PDF Original Page OR Typography LazyColumn for Text/Reflow)
        if (isPdf && pdfReaderMode == PdfReaderMode.ORIGINAL) {
            val pdfPath = book?.pdfFilePath.orEmpty()
            PdfPageView(
                pdfPath = pdfPath,
                pageIndex = currentChapterIndex,
                textColor = textColor,
                modifier = Modifier
                    .fillMaxSize()
                    .then(pageSwipeModifier)
            )
        } else {
            if (currentChapter == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = textColor)
                }
            } else {
                val isReflow = isPdf && pdfReaderMode == PdfReaderMode.REFLOW

                // Trigger on-demand single page OCR if in reflow mode and text is empty
                LaunchedEffect(currentChapter.id, isReflow) {
                    if (isReflow && currentChapter.content.isBlank()) {
                        viewModel.triggerOnDemandOcrForCurrentPage()
                    }
                }

                val paragraphs = remember(currentChapter.content) {
                    currentChapter.content.split("\n\n").filter { it.isNotBlank() }
                }

                CompositionLocalProvider(LocalTextToolbar provides customTextToolbar) {
                    SelectionContainer {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(pageSwipeModifier)
                                .testTag("reader_content_scroll"),
                            contentPadding = PaddingValues(
                                start = 26.dp,
                                end = 26.dp,
                                top = 135.dp,   // Space below top bar & status badge
                                bottom = 150.dp // Generous space so floating bottom bar never obstructs text
                            ),
                            verticalArrangement = Arrangement.spacedBy(22.dp)
                        ) {
                            // Disclaimer Banner for Reflow Mode
                            if (isReflow) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Reflow Mode • OCR-generated text, may contain minor errors",
                                                fontSize = 11.5.sp,
                                                fontFamily = SystemSans,
                                                color = Color(0xFF92400E),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            if (paragraphs.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(36.dp),
                                            color = Color(0xFFD97706),
                                            strokeWidth = 3.dp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Extracting text with ML Kit OCR...",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = EditorialSerif,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "WorkManager background pipeline active",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = SystemSans,
                                                color = textColor.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                }
                            } else {
                                // Paragraphs with book-grade editorial typography and highlights
                                itemsIndexed(paragraphs) { index, paragraph ->
                            val isFirstParagraph = index == 0
                            val rawText = paragraph.trim()

                            val annotatedString = remember(rawText, chapterHighlights) {
                                buildAnnotatedString {
                                    append(rawText)
                                    val paraHighlights = chapterHighlights.filter { it.paragraphIndex == index }
                                    paraHighlights.forEach { hl ->
                                        val start = hl.startOffset.coerceIn(0, rawText.length)
                                        val end = hl.endOffset.coerceIn(start, rawText.length)
                                        if (end > start) {
                                            val color = try {
                                                Color(android.graphics.Color.parseColor(hl.colorHex))
                                            } catch (_: Exception) {
                                                Color(0xFFFEF08A)
                                            }
                                            addStyle(
                                                style = SpanStyle(
                                                    background = color.copy(alpha = 0.85f),
                                                    color = Color(0xFF1E1C1A)
                                                ),
                                                start = start,
                                                end = end
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = annotatedString,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = activeFontFamily,
                                    fontSize = (if (isFirstParagraph) settings.fontSizeSp + 1f else settings.fontSizeSp).sp,
                                    lineHeight = lineHeight,
                                    fontStyle = if (isFirstParagraph) FontStyle.Italic else FontStyle.Normal,
                                    fontWeight = FontWeight.Normal,
                                    color = textColor
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        }

                    // Clean & subtle Chapter End Marker (No ugly giant card or next buttons)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp, bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(1.dp)
                                        .background(textColor.copy(alpha = 0.2f))
                                )
                                Text(
                                    text = "❦",
                                    fontSize = 16.sp,
                                    color = textColor.copy(alpha = 0.45f)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(1.dp)
                                        .background(textColor.copy(alpha = 0.2f))
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (currentChapterIndex < chapters.size - 1) {
                                    "End of Chapter ${currentChapter.number}"
                                } else {
                                    "End of Book"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = EditorialSerif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 14.sp,
                                    color = textColor.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
            }
        }

        // Top Gradient Scrim for readable header overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            backgroundColor.copy(alpha = 0.95f),
                            backgroundColor.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )

        // TOP BAR
        ReaderHeaderTopBar(
            chapterNumber = currentChapter?.number ?: (currentChapterIndex + 1),
            chapterSubtitle = if (isPdf) (book?.title ?: "PDF Document") else (currentChapter?.subtitle?.takeIf { it.isNotBlank() } ?: (book?.title ?: "Feedback")),
            isPdf = isPdf,
            isScanned = isScanned,
            pdfReaderMode = pdfReaderMode,
            onPdfReaderModeChange = { pdfReaderMode = it },
            hasAudioPlayer = playerState.tracks.isNotEmpty(),
            isBookmarked = isCurrentPositionBookmarked,
            textColor = textColor,
            onBackClick = onBackClick,
            onFormatClick = { showFormatSheet = true },
            onBookmarkClick = {
                viewModel.toggleBookmark(
                    pageNumber = currentChapterIndex,
                    paragraphIndex = firstVisibleIndex.value
                )
            },
            onAudioClick = { viewModel.audioPlayerManager.showPlayerSheet() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // BOTTOM FLOATING PILL BAR
        ReaderFloatingBottomBar(
            currentChapterNumber = currentChapterIndex + 1,
            totalChapters = totalChapters,
            isPdf = isPdf,
            hasPrevious = currentChapterIndex > 0,
            hasNext = currentChapterIndex < chapters.size - 1,
            textColor = textColor,
            containerColor = pillBgColor,
            borderColor = bottomBarBorderColor,
            onPreviousClick = { viewModel.navigateToPreviousChapter() },
            onNextClick = { viewModel.navigateToNextChapter() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
        )

        // Floating Text Highlight Selection Bar
        if (!selectedTextForHighlight.isNullOrBlank() && !isPdf) {
            val selText = selectedTextForHighlight!!
            val paragraphs = remember(currentChapter?.content) {
                currentChapter?.content?.split("\n\n")?.filter { it.isNotBlank() } ?: emptyList()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 85.dp, start = 16.dp, end = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF22201E))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("floating_highlight_toolbar")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "“${selText.take(35)}${if (selText.length > 35) "..." else ""}”",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = EditorialSerif,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { selectedTextForHighlight = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close highlight toolbar",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Muted highlight color options (Soft Yellow, Soft Green, Soft Pink, Soft Blue, Soft Peach)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val highlightColors = listOf(
                            "#FEF08A" to Color(0xFFFEF08A), // Soft Yellow
                            "#BBF7D0" to Color(0xFFBBF7D0), // Soft Green
                            "#FBCFE8" to Color(0xFFFBCFE8), // Soft Pink
                            "#BFDBFE" to Color(0xFFBFDBFE), // Soft Blue
                            "#FED7AA" to Color(0xFFFED7AA)  // Soft Peach
                        )

                        highlightColors.forEach { (hex, color) ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable {
                                        var pIndex = paragraphs.indexOfFirst { it.contains(selText) }
                                        if (pIndex == -1) pIndex = 0
                                        val start = if (pIndex < paragraphs.size) paragraphs[pIndex].indexOf(selText).coerceAtLeast(0) else 0
                                        val end = start + selText.length

                                        viewModel.addHighlight(
                                            paragraphIndex = pIndex,
                                            startOffset = start,
                                            endOffset = end,
                                            highlightedText = selText,
                                            colorHex = hex
                                        )
                                        selectedTextForHighlight = null
                                    }
                                    .testTag("highlight_color_$hex")
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Add Note Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable {
                                    selectedColorHex = "#FEF08A"
                                    noteText = ""
                                    showNoteDialog = true
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Add Note",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Note",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Note Input Dialog
        if (showNoteDialog && !selectedTextForHighlight.isNullOrBlank()) {
            val selText = selectedTextForHighlight!!
            val paragraphs = remember(currentChapter?.content) {
                currentChapter?.content?.split("\n\n")?.filter { it.isNotBlank() } ?: emptyList()
            }

            AlertDialog(
                onDismissRequest = { showNoteDialog = false },
                title = {
                    Text(
                        text = "Add Note to Highlight",
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "“${selText.take(60)}${if (selText.length > 60) "..." else ""}”",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = EditorialSerif,
                                color = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text("Write your thoughts...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            var pIndex = paragraphs.indexOfFirst { it.contains(selText) }
                            if (pIndex == -1) pIndex = 0
                            val start = if (pIndex < paragraphs.size) paragraphs[pIndex].indexOf(selText).coerceAtLeast(0) else 0
                            val end = start + selText.length

                            viewModel.addHighlight(
                                paragraphIndex = pIndex,
                                startOffset = start,
                                endOffset = end,
                                highlightedText = selText,
                                colorHex = selectedColorHex,
                                note = noteText
                            )
                            selectedTextForHighlight = null
                            showNoteDialog = false
                        }
                    ) {
                        Text("Save Highlight", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNoteDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = Color.White
            )
        }

        // Dimming overlay based on brightness setting (0-100%)
        if (settings.brightness < 100) {
            val dimAlpha = ((100 - settings.brightness) / 100f) * 0.45f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
            )
        }

        // Reading Settings Modal Bottom Sheet
        if (showFormatSheet) {
            ReaderSettingsBottomSheet(
                settings = settings,
                isPdf = isPdf && pdfReaderMode == PdfReaderMode.ORIGINAL,
                onDismiss = { showFormatSheet = false },
                onFontSizeChange = { viewModel.updateFontSize(it) },
                onFontStyleChange = { viewModel.updateFontStyle(it) },
                onLineSpacingChange = { viewModel.updateLineSpacing(it) },
                onThemeChange = { viewModel.updateTheme(it) },
                onBrightnessChange = { viewModel.updateBrightness(it) }
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
}

/**
 * PDF Page View rendering authentic page bitmaps via PdfRenderer
 */
@Composable
fun PdfPageView(
    pdfPath: String,
    pageIndex: Int,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    var pageBitmap by remember(pdfPath, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(pdfPath, pageIndex) { mutableStateOf(true) }

    LaunchedEffect(pdfPath, pageIndex) {
        isLoading = true
        pageBitmap = PdfHelper.renderPageBitmap(pdfPath, pageIndex, targetWidth = 1080)
        isLoading = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 135.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = textColor,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Rendering Page ${pageIndex + 1}...",
                    fontFamily = SystemSans,
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.65f)
                )
            }
        } else if (pageBitmap != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .shadow(10.dp, RoundedCornerShape(10.dp), spotColor = Color.Black.copy(alpha = 0.22f)),
                shape = RoundedCornerShape(10.dp),
                color = Color.White
            ) {
                Image(
                    bitmap = pageBitmap!!.asImageBitmap(),
                    contentDescription = "PDF Page ${pageIndex + 1}",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Page ${pageIndex + 1} could not be loaded",
                    fontFamily = SystemSans,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Top Reader Bar matching exact reference design:
 * Left: circular black button with back arrow
 * Center: "Chapter 4" (or "Page 4") + Subtitle
 * Right: circular black button with "Aa"
 */
@Composable
fun ReaderHeaderTopBar(
    chapterNumber: Int,
    chapterSubtitle: String,
    isPdf: Boolean = false,
    isScanned: Boolean = false,
    pdfReaderMode: PdfReaderMode = PdfReaderMode.ORIGINAL,
    onPdfReaderModeChange: (PdfReaderMode) -> Unit = {},
    hasAudioPlayer: Boolean = false,
    isBookmarked: Boolean = false,
    textColor: Color,
    onBackClick: () -> Unit,
    onFormatClick: () -> Unit,
    onBookmarkClick: () -> Unit = {},
    onAudioClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Circular dark matte button
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(3.dp, CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.25f))
                .clip(CircleShape)
                .background(Color(0xFF22201E))
                .clickable(onClick = onBackClick)
                .testTag("reader_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        }

        // Center: Chapter/Page title, Status Badge, and Mode Switcher
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = if (isPdf) "Page $chapterNumber" else "Chapter $chapterNumber",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textColor
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isPdf) {
                Spacer(modifier = Modifier.height(2.dp))

                // Clear Status Badge Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isScanned) Color(0xFFD97706).copy(alpha = 0.15f) else Color(0xFF3B82F6).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isScanned) Color(0xFFD97706).copy(alpha = 0.4f) else Color(0xFF3B82F6).copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isScanned) Color(0xFFD97706) else Color(0xFF3B82F6))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isScanned) "Scanned • Reflow Available" else "Digital PDF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SystemSans,
                            color = if (isScanned) Color(0xFFD97706) else Color(0xFF3B82F6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Mode Switcher Pill [ Original | Reflow ]
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF22201E),
                    modifier = Modifier.height(26.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Surface(
                            onClick = { onPdfReaderModeChange(PdfReaderMode.ORIGINAL) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (pdfReaderMode == PdfReaderMode.ORIGINAL) Color(0xFFD97706) else Color.Transparent,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = "Original",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SystemSans,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            onClick = { onPdfReaderModeChange(PdfReaderMode.REFLOW) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (pdfReaderMode == PdfReaderMode.REFLOW) Color(0xFFD97706) else Color.Transparent,
                            modifier = Modifier.height(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = "Reflow",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SystemSans,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = chapterSubtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = ContentSerif,
                        color = textColor.copy(alpha = 0.55f),
                        fontSize = 12.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Right: Bookmark, Audio player, and "Aa" format buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bookmark Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(3.dp, CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(if (isBookmarked) Color(0xFFD97706) else Color(0xFF22201E))
                    .clickable(onClick = onBookmarkClick)
                    .testTag("reader_bookmark_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (hasAudioPlayer) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(3.dp, CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(Color(0xFFD97706))
                        .clickable(onClick = onAudioClick)
                        .testTag("reader_audio_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Audiobook Player",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(3.dp, CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(Color(0xFF22201E))
                    .clickable(onClick = onFormatClick)
                    .testTag("reader_format_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aa",
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Floating bottom pill bar matching screenshot:
 * Frosted pill container, circular left chevron, "Chapter 4 / 12" (or "Page 4 / 12") with black bar, circular right chevron.
 */
@Composable
fun ReaderFloatingBottomBar(
    currentChapterNumber: Int,
    totalChapters: Int,
    isPdf: Boolean = false,
    hasPrevious: Boolean,
    hasNext: Boolean,
    textColor: Color,
    containerColor: Color,
    borderColor: Color,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressFraction = (currentChapterNumber.toFloat() / totalChapters.toFloat()).coerceIn(0.05f, 1f)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(percent = 50),
                spotColor = ObsidianBlack.copy(alpha = 0.18f)
            )
            .border(0.6.dp, borderColor, RoundedCornerShape(percent = 50))
            .testTag("reader_floating_bottom_bar"),
        shape = RoundedCornerShape(percent = 50),
        color = containerColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left Chevron circular target
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(enabled = hasPrevious, onClick = onPreviousClick)
                    .testTag("reader_prev_chapter_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous",
                    tint = if (hasPrevious) textColor else textColor.copy(alpha = 0.25f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center Column: "Chapter 4 / 12" (or "Page 4 / 12") and bottom pill track
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(150.dp)
            ) {
                Text(
                    text = if (isPdf) "Page $currentChapterNumber / $totalChapters" else "Chapter $currentChapterNumber / $totalChapters",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = textColor.copy(alpha = 0.85f)
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Custom Sleek Black Progress Bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(3.5.dp)
                ) {
                    val cornerRadius = CornerRadius(size.height / 2, size.height / 2)
                    // Track background
                    drawRoundRect(
                        color = textColor.copy(alpha = 0.15f),
                        size = size,
                        cornerRadius = cornerRadius
                    )
                    // Filled Progress
                    val progressWidth = size.width * progressFraction
                    drawRoundRect(
                        color = textColor,
                        size = Size(progressWidth, size.height),
                        cornerRadius = cornerRadius
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Chevron circular target
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(enabled = hasNext, onClick = onNextClick)
                    .testTag("reader_next_chapter_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next",
                    tint = if (hasNext) textColor else textColor.copy(alpha = 0.25f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Reading Settings Modal Bottom Sheet matching the exact UI in the user's reference image
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsBottomSheet(
    settings: ReaderSettings,
    isPdf: Boolean = false,
    onDismiss: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onFontStyleChange: (ReaderFontStyle) -> Unit,
    onLineSpacingChange: (ReaderLineSpacing) -> Unit,
    onThemeChange: (ReaderThemeMode) -> Unit,
    onBrightnessChange: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheetBgColor = Color(0xFFF7F3EB)
    val pillInactiveBg = Color(0xFFEBE5DA)
    val pillActiveBg = Color(0xFF22201E)
    val labelColor = Color(0xFF262422)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBgColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFFC7BFB3))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .navigationBarsPadding()
        ) {
            // Header: "Reading Settings" + circular Close "X"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reading Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = labelColor
                    )
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(pillInactiveBg)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = labelColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.6.dp)
                    .background(Color(0xFFE4DCCE))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Informational badge for PDF documents
            if (isPdf) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEBE4D5)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF8C6B38),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "PDF document layout & typography are preserved. Reader theme and brightness can still be adjusted.",
                            fontFamily = SystemSans,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = labelColor.copy(alpha = 0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 1. FONT SIZE SECTION (Disabled for PDF)
            Text(
                text = "Font Size",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isPdf) labelColor.copy(alpha = 0.35f) else labelColor
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A-",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (isPdf) labelColor.copy(alpha = 0.35f) else labelColor
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Slider(
                    value = settings.fontSizeSp,
                    onValueChange = { if (!isPdf) onFontSizeChange(it) },
                    valueRange = 13f..26f,
                    enabled = !isPdf,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = ObsidianBlack,
                        activeTrackColor = ObsidianBlack,
                        inactiveTrackColor = Color(0xFFDDD5C7),
                        disabledThumbColor = Color(0xFFAAA398),
                        disabledActiveTrackColor = Color(0xFFCCC5B8)
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "A+",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isPdf) labelColor.copy(alpha = 0.35f) else labelColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. FONT STYLE SECTION: 4 Pill Options with "Aa"
            Text(
                text = "Font Style",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isPdf) labelColor.copy(alpha = 0.35f) else labelColor
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReaderFontStyle.values().forEach { fontOption ->
                    val isSelected = settings.fontStyle == fontOption

                    val (fontFamilyToUse, fontStyleToUse, fontWeightToUse) = when (fontOption) {
                        ReaderFontStyle.SERIF_LORA -> Triple(ContentSerif, FontStyle.Normal, FontWeight.Bold)
                        ReaderFontStyle.SYSTEM_SANS -> Triple(SystemSans, FontStyle.Normal, FontWeight.Medium)
                        ReaderFontStyle.SERIF_PLAYFAIR -> Triple(EditorialSerif, FontStyle.Italic, FontWeight.Normal)
                        ReaderFontStyle.SYSTEM_SERIF -> Triple(FontFamily.Serif, FontStyle.Normal, FontWeight.Normal)
                    }

                    Surface(
                        onClick = { if (!isPdf) onFontStyleChange(fontOption) },
                        enabled = !isPdf,
                        shape = RoundedCornerShape(percent = 50),
                        color = if (isPdf) pillInactiveBg.copy(alpha = 0.5f) else if (isSelected) pillActiveBg else pillInactiveBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Aa",
                                fontFamily = fontFamilyToUse,
                                fontStyle = fontStyleToUse,
                                fontWeight = fontWeightToUse,
                                fontSize = 16.sp,
                                color = if (isPdf) labelColor.copy(alpha = 0.3f) else if (isSelected) Color.White else labelColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. LINE SPACING SECTION: 3 visual line-icon pills
            Text(
                text = "Line Spacing",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isPdf) labelColor.copy(alpha = 0.35f) else labelColor
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReaderLineSpacing.values().forEach { spacing ->
                    val isSelected = settings.lineSpacing == spacing

                    Surface(
                        onClick = { if (!isPdf) onLineSpacingChange(spacing) },
                        enabled = !isPdf,
                        shape = RoundedCornerShape(percent = 50),
                        color = if (isPdf) pillInactiveBg.copy(alpha = 0.5f) else if (isSelected) pillActiveBg else pillInactiveBg,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            LineSpacingIcon(
                                spacing = spacing,
                                tint = if (isPdf) labelColor.copy(alpha = 0.3f) else if (isSelected) Color.White else labelColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. THEME SECTION: 3 circular swatches (Light, Sepia, Dark)
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = labelColor
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ReaderThemeMode.values().forEach { mode ->
                    val isSelected = settings.theme == mode
                    val swatchColor = when (mode) {
                        ReaderThemeMode.LIGHT -> Color(0xFFFFFFFF)
                        ReaderThemeMode.SEPIA -> Color(0xFFE2C49F)
                        ReaderThemeMode.DARK -> Color(0xFF282624)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onThemeChange(mode) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(if (isSelected) 3.dp else 1.dp, CircleShape)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) ObsidianBlack else Color(0xFFCBC3B5),
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                                .background(swatchColor)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = mode.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SystemSans,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = labelColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. BRIGHTNESS SECTION: Sun icons + Slider
            Text(
                text = "Brightness",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = labelColor
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Low Brightness",
                    tint = labelColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(17.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Slider(
                    value = settings.brightness.toFloat(),
                    onValueChange = { onBrightnessChange(it.toInt()) },
                    valueRange = 25f..100f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = ObsidianBlack,
                        activeTrackColor = ObsidianBlack,
                        inactiveTrackColor = Color(0xFFDDD5C7)
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "High Brightness",
                    tint = labelColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

/**
 * Custom line spacing icons for the 3 pill states (Compact, Normal, Relaxed)
 */
@Composable
private fun LineSpacingIcon(
    spacing: ReaderLineSpacing,
    tint: Color
) {
    val lineGap = when (spacing) {
        ReaderLineSpacing.COMPACT -> 3.dp
        ReaderLineSpacing.NORMAL -> 5.5.dp
        ReaderLineSpacing.RELAXED -> 8.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(lineGap)
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(2.dp)
                .background(tint, CircleShape)
        )
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(tint, CircleShape)
        )
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(2.dp)
                .background(tint, CircleShape)
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
