package com.example.ui.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.Bookmark
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.Highlight
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksHighlightsScreen(
    viewModel: BookmarksHighlightsViewModel,
    onBackClick: () -> Unit,
    onJumpToPosition: (chapterId: Long, pageNumber: Int, scrollAnchor: Int) -> Unit
) {
    val book by viewModel.book.collectAsStateWithLifecycle()
    val chapters by viewModel.chapters.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val highlights by viewModel.highlights.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Bookmarks (${bookmarks.size})", "Highlights (${highlights.size})")

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bookmarks & Highlights",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = EditorialSerif,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            )
                        )
                        book?.let {
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ObsidianBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBackground
                )
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CreamBackground,
                contentColor = ObsidianBlack,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = ObsidianBlack,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = if (selectedTabIndex == index) ObsidianBlack else TextMuted
                                )
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> BookmarksTab(
                    bookmarks = bookmarks,
                    chapters = chapters,
                    dateFormat = dateFormat,
                    onDelete = { viewModel.deleteBookmark(it) },
                    onJump = { bm ->
                        onJumpToPosition(bm.chapterId, bm.pageNumber, bm.scrollAnchor)
                    }
                )
                1 -> HighlightsTab(
                    highlights = highlights,
                    chapters = chapters,
                    dateFormat = dateFormat,
                    onDelete = { viewModel.deleteHighlight(it) },
                    onJump = { hl ->
                        onJumpToPosition(hl.chapterId, 0, hl.paragraphIndex)
                    }
                )
            }
        }
    }
}

@Composable
private fun BookmarksTab(
    bookmarks: List<Bookmark>,
    chapters: List<Chapter>,
    dateFormat: SimpleDateFormat,
    onDelete: (Bookmark) -> Unit,
    onJump: (Bookmark) -> Unit
) {
    if (bookmarks.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.BookmarkBorder,
            title = "No Bookmarks Saved",
            description = "Tap the bookmark icon in the top toolbar while reading to save your position."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bookmarks, key = { it.id }) { bookmark ->
                val chapter = chapters.find { it.id == bookmark.chapterId }
                val chapterName = chapter?.title ?: "Chapter ${chapter?.number ?: 1}"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onJump(bookmark) }
                        .testTag("bookmark_item_${bookmark.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ObsidianBlack
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = chapterName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextMuted,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateFormat.format(Date(bookmark.createdAt)),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        IconButton(onClick = { onJump(bookmark) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Jump to page",
                                tint = ObsidianBlack
                            )
                        }

                        IconButton(onClick = { onDelete(bookmark) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete bookmark",
                                tint = Color(0xFFEF4444).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightsTab(
    highlights: List<Highlight>,
    chapters: List<Chapter>,
    dateFormat: SimpleDateFormat,
    onDelete: (Highlight) -> Unit,
    onJump: (Highlight) -> Unit
) {
    if (highlights.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.FormatQuote,
            title = "No Text Highlights",
            description = "Long-press and select text while reading to highlight memorable passages and add notes."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(highlights, key = { it.id }) { highlight ->
                val chapter = chapters.find { it.id == highlight.chapterId }
                val chapterName = chapter?.title ?: "Chapter ${chapter?.number ?: 1}"
                val badgeColor = try {
                    Color(android.graphics.Color.parseColor(highlight.colorHex))
                } catch (e: Exception) {
                    Color(0xFFFEF08A)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onJump(highlight) }
                        .testTag("highlight_item_${highlight.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color indicator pill
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = chapterName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = dateFormat.format(Date(highlight.createdAt)),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Highlighted snippet text
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeColor.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“${highlight.highlightedText}”",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = EditorialSerif,
                                    fontSize = 15.sp,
                                    lineHeight = 21.sp,
                                    color = ObsidianBlack
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        // Optional Note attached to highlight
                        if (!highlight.note.isNullObscureOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = highlight.note.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ObsidianBlack,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onJump(highlight) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Jump to position",
                                    tint = ObsidianBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { onDelete(highlight) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete highlight",
                                    tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNullObscureOrBlank(): Boolean = this.isNullOrBlank()

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ObsidianBlack
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
