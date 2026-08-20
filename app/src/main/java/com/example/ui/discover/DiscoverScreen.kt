package com.example.ui.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.remote.ArchiveDoc
import com.example.ui.theme.ContentSerif
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftSepiaSurface
import com.example.ui.theme.SystemSans
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmOffWhite
import kotlinx.coroutines.launch
import java.io.File

private val SUGGESTED_TOPICS = listOf(
    "Sherlock Holmes",
    "Pride and Prejudice",
    "Alice in Wonderland",
    "Frankenstein",
    "The Art of War",
    "Philosophy",
    "Psychology",
    "Design and Typography",
    "Ancient History",
    "Classic Poetry"
)

private val BOOK_PALETTE_COLORS = listOf(
    "#285698" to "Navy",
    "#8B263E" to "Crimson",
    "#2E6F40" to "Emerald",
    "#8C6239" to "Leather",
    "#594A42" to "Sepia",
    "#1C1B1F" to "Obsidian"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onBackClick: () -> Unit,
    onBookImported: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadStatusMap by viewModel.downloadStatusMap.collectAsStateWithLifecycle()
    val pendingImport by viewModel.pendingImport.collectAsStateWithLifecycle()
    val genres by viewModel.availableGenres.collectAsStateWithLifecycle(initialValue = listOf("Design", "Psychology", "Novels"))

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = CreamBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DiscoverTopBar(onBackClick = onBackClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Header
            DiscoverSearchHeader(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onSearchSubmit = { viewModel.performSearch(it) }
            )

            // Suggested Topics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SUGGESTED_TOPICS.forEach { topic ->
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = if (searchQuery.equals(topic, ignoreCase = true)) ObsidianBlack else SoftSepiaSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .clickable {
                                viewModel.onSearchQueryChanged(topic)
                                viewModel.performSearch(topic)
                            }
                    ) {
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SystemSans,
                                fontWeight = FontWeight.Medium,
                                color = if (searchQuery.equals(topic, ignoreCase = true)) Color.White else ObsidianBlack
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Content Area based on UI State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is DiscoverUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = ObsidianBlack,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "Searching Internet Archive…",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = ContentSerif,
                                        color = TextMuted,
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }
                    }

                    is DiscoverUiState.Empty -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SoftSepiaSurface,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = ObsidianBlack.copy(alpha = 0.6f),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "No Public-Domain Books Found",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = EditorialSerif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = ObsidianBlack
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "We couldn't find matches for \"${state.query}\". Try searching for author names, classical titles, or broader topics.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = ContentSerif,
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    is DiscoverUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SoftSepiaSurface,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (state.isNetworkError) Icons.Default.CloudOff else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = ObsidianBlack.copy(alpha = 0.7f),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (state.isNetworkError) "Check Connection" else "Search Error",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = EditorialSerif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        color = ObsidianBlack
                                    )
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = ContentSerif,
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.retrySearch() },
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack),
                                    shape = RoundedCornerShape(percent = 50)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Retry Search", fontFamily = SystemSans, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    is DiscoverUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("discover_books_list"),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                items = state.books,
                                key = { it.identifier }
                            ) { bookDoc ->
                                val status = downloadStatusMap[bookDoc.identifier] ?: ItemDownloadStatus.Idle

                                ArchiveBookRowCard(
                                    book = bookDoc,
                                    downloadStatus = status,
                                    onDownloadClick = {
                                        viewModel.downloadAndImport(bookDoc)
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }

                    DiscoverUiState.Idle -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Search books to explore Internet Archive",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = ContentSerif,
                                    color = TextMuted
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Sheet for Imported Book
    pendingImport?.let { pending ->
        ArchiveImportConfirmationSheet(
            pending = pending,
            availableGenres = genres,
            onDismiss = { viewModel.dismissPendingImport() },
            onConfirm = { title, author, genre, desc, colorHex ->
                viewModel.confirmImportToLibrary(
                    title = title,
                    author = author,
                    genre = genre,
                    description = desc,
                    colorHex = colorHex,
                    onSuccess = { insertedId ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Book \"$title\" added to Library!")
                        }
                        onBookImported(insertedId)
                    }
                )
            }
        )
    }
}

@Composable
fun DiscoverTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, shape = CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(ObsidianBlack)
                .clickable(onClick = onBackClick)
                .testTag("discover_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Discover Books",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = ObsidianBlack
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Internet Archive • Free Public Domain",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = SystemSans,
                        color = TextMuted,
                        fontSize = 11.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun DiscoverSearchHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .testTag("discover_search_input"),
        placeholder = {
            Text(
                text = "Search titles, authors, classics…",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = ContentSerif,
                    color = TextMuted
                )
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = ObsidianBlack.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (searchQuery.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = ObsidianBlack.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = WarmOffWhite,
            unfocusedContainerColor = WarmOffWhite,
            focusedBorderColor = ObsidianBlack,
            unfocusedBorderColor = Color(0xFFDDD3C4),
            focusedTextColor = ObsidianBlack,
            unfocusedTextColor = ObsidianBlack
        )
    )
}

@Composable
fun ArchiveBookRowCard(
    book: ArchiveDoc,
    downloadStatus: ItemDownloadStatus,
    onDownloadClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discover_book_card_${book.identifier}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarmOffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Book Cover Thumbnail
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SoftSepiaSurface,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .width(72.dp)
                        .height(104.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(book.coverThumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = book.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Metadata Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = book.title ?: "Untitled Document",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = EditorialSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 21.sp,
                            color = ObsidianBlack
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = book.creator ?: "Unknown / Public Domain",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = ContentSerif,
                            color = TextMuted,
                            fontSize = 13.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!book.year.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SoftSepiaSurface
                            ) {
                                Text(
                                    text = book.year,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SystemSans,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ObsidianBlack,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE5EDE6)
                        ) {
                            Text(
                                text = "FREE PDF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SystemSans,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E6F40),
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action / Download status banner
            when (downloadStatus) {
                is ItemDownloadStatus.Idle -> {
                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("download_btn_${book.identifier}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ObsidianBlack,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(percent = 50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download & Import",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = SystemSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        )
                    }
                }

                is ItemDownloadStatus.Downloading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Downloading PDF…",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SystemSans,
                                    fontWeight = FontWeight.Medium,
                                    color = ObsidianBlack
                                )
                            )
                            Text(
                                text = "${(downloadStatus.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SystemSans,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { downloadStatus.progress.coerceIn(0.01f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ObsidianBlack,
                            trackColor = SoftSepiaSurface
                        )
                    }
                }

                is ItemDownloadStatus.Processing -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = ObsidianBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rendering cover & indexing pages…",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SystemSans,
                                color = ObsidianBlack
                            )
                        )
                    }
                }

                is ItemDownloadStatus.Unavailable -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFBF2E6),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECD6BE))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF996120),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Not available for direct download (borrow restricted or non-PDF)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = SystemSans,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF7A4A12)
                                )
                            )
                        }
                    }
                }

                is ItemDownloadStatus.Failed -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Download failed: ${downloadStatus.error}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = SystemSans,
                                color = Color(0xFF8B263E),
                                fontSize = 12.sp
                            )
                        )
                        Button(
                            onClick = onDownloadClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftSepiaSurface,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(percent = 50)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Download", fontFamily = SystemSans, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveImportConfirmationSheet(
    pending: PendingImportBook,
    availableGenres: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, genre: String, description: String, colorHex: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(pending.initialTitle) }
    var author by remember { mutableStateOf(pending.initialAuthor) }
    var genre by remember { mutableStateOf(pending.initialGenre) }
    var customGenre by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#285698") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmOffWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFDDD3C4))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Add to Your Library",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = ObsidianBlack
                )
            )
            Text(
                text = "Downloaded from Internet Archive (${pending.importResult.pageCount} pages, ${pending.importResult.fileSizeFormatted})",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = ContentSerif,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Cover Preview & Title/Author Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Cover preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SoftSepiaSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .width(82.dp)
                        .height(120.dp)
                ) {
                    val coverFile = File(pending.importResult.coverImagePath)
                    if (coverFile.exists()) {
                        AsyncImage(
                            model = coverFile,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = pending.coverUrl,
                            contentDescription = "Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Title & Author Inputs
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Book Title", fontFamily = SystemSans) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_book_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CreamBackground,
                            unfocusedContainerColor = CreamBackground,
                            focusedBorderColor = ObsidianBlack,
                            unfocusedBorderColor = Color(0xFFDDD3C4)
                        )
                    )

                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author / Creator", fontFamily = SystemSans) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_book_author_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CreamBackground,
                            unfocusedContainerColor = CreamBackground,
                            focusedBorderColor = ObsidianBlack,
                            unfocusedBorderColor = Color(0xFFDDD3C4)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Genre / Shelf Selector
            Text(
                text = "Select Category / Shelf",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBlack
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            val genreOptions = (availableGenres + listOf("Novels", "Philosophy", "Design", "History")).distinct()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genreOptions.forEach { g ->
                    val isSelected = genre.equals(g, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { genre = g },
                        label = { Text(g, fontFamily = SystemSans, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(percent = 50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ObsidianBlack,
                            selectedLabelColor = Color.White,
                            containerColor = SoftSepiaSurface,
                            labelColor = ObsidianBlack
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Book Color Accent Picker
            Text(
                text = "Book Theme Color",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBlack
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BOOK_PALETTE_COLORS.forEach { (hex, name) ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = selectedColorHex == hex

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColorHex = hex }
                            .border(
                                width = if (isSelected) 2.5.dp else 0.dp,
                                color = if (isSelected) ObsidianBlack else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = name,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text("Cancel", fontFamily = SystemSans, fontWeight = FontWeight.SemiBold, color = ObsidianBlack)
                }

                Button(
                    onClick = {
                        val finalGenre = if (genre.isNotBlank()) genre else if (customGenre.isNotBlank()) customGenre else "Novels"
                        onConfirm(
                            title.trim(),
                            author.trim(),
                            finalGenre.trim(),
                            "Internet Archive public-domain document (${pending.importResult.pageCount} pages)",
                            selectedColorHex
                        )
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                        .testTag("confirm_import_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack),
                    shape = RoundedCornerShape(percent = 50)
                ) {
                    Text("Add to Library", fontFamily = SystemSans, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
