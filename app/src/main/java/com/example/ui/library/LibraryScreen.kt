package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddBookBottomSheet
import com.example.ui.components.BookCoverItem
import com.example.ui.theme.ContentSerif
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftSepiaSurface
import com.example.ui.theme.SystemSans
import com.example.ui.theme.TextLightMuted
import com.example.ui.theme.TextMuted

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookClick: (Long) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    val categorySections by viewModel.categorySections.collectAsStateWithLifecycle()
    val totalBooks by viewModel.totalBookCount.collectAsStateWithLifecycle()
    val availableGenres by viewModel.availableGenres.collectAsStateWithLifecycle()
    var showAddBookSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_scroll_column"),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 100.dp // extra padding for bottom floating pill button
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Header Section
            item {
                LibraryHeader(
                    onSearchClick = onSearchClick
                )
            }

            // Category Sections (Design, Psychology, Novels, etc.)
            items(
                items = categorySections,
                key = { it.genre }
            ) { section ->
                CategoryShelfSection(
                    section = section,
                    onBookClick = onBookClick,
                    onCategoryClick = { onCategoryClick(section.genre) }
                )
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Floating Bottom Pill "+ Add Books"
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            CreamBackground.copy(alpha = 0.85f),
                            CreamBackground
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { showAddBookSheet = true },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(percent = 50), spotColor = ObsidianBlack.copy(alpha = 0.3f))
                    .testTag("add_books_button"),
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
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Books",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontFamily = SystemSans
                        )
                    )
                }
            }
        }

        // Add Book Bottom Sheet Modal
        if (showAddBookSheet) {
            AddBookBottomSheet(
                availableGenres = availableGenres,
                onDismiss = { showAddBookSheet = false },
                onAddManualBook = { title, author, genre, description, colorHex ->
                    viewModel.addNewBook(title, author, genre, description, colorHex)
                    showAddBookSheet = false
                },
                onAddPdfBook = { title, author, genre, description, pdfPath, coverPath, pageCount, colorHex ->
                    viewModel.importPdfBook(title, author, genre, description, pdfPath, coverPath, pageCount, colorHex)
                    showAddBookSheet = false
                }
            )
        }
    }
}

@Composable
fun LibraryHeader(
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // Circular Black Search Button in Top-Right corner (Apple Books style)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 4.dp)
                .size(44.dp)
                .shadow(elevation = 4.dp, shape = CircleShape, spotColor = ObsidianBlack.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(ObsidianBlack)
                .clickable(onClick = onSearchClick)
                .testTag("search_icon_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Books",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Centered Editorial Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Favourite",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    color = ObsidianBlack.copy(alpha = 0.85f),
                    fontFamily = EditorialSerif
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "BOOKS",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    letterSpacing = 1.5.sp,
                    color = ObsidianBlack
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("library_title_books")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Classic editorial flourish divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(1.dp)
                        .background(TextMuted.copy(alpha = 0.35f))
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(TextMuted.copy(alpha = 0.6f))
                )
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(1.dp)
                        .background(TextMuted.copy(alpha = 0.35f))
                )
            }
        }
    }
}

@Composable
fun CategoryShelfSection(
    section: CategorySection,
    onBookClick: (Long) -> Unit,
    onCategoryClick: () -> Unit
) {
    val listState = rememberLazyListState()

    // Determine tray background gradient based on category tint
    val baseTint = try {
        Color(android.graphics.Color.parseColor(section.trayTintHex))
    } catch (_: Exception) {
        Color(0xFFE89A5A)
    }

    val trayGradient = Brush.verticalGradient(
        colors = listOf(
            baseTint.copy(alpha = 0.12f),
            baseTint.copy(alpha = 0.22f),
            baseTint.copy(alpha = 0.08f)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .testTag("category_section_${section.genre}")
    ) {
        // Section Header Row: "Design" + "16 books >" (Clickable to open Category Grid)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onCategoryClick)
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .testTag("category_header_${section.genre}"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.genre,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = ObsidianBlack
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${section.count} books",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = SystemSans,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ObsidianBlack.copy(alpha = 0.7f)
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View all ${section.genre} books",
                    tint = ObsidianBlack.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Frosted / Tinted Shelf Tray Container (Apple Books style)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = baseTint.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(trayGradient)
                    .border(
                        width = 1.dp,
                        color = baseTint.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(top = 18.dp, bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Horizontally Scrollable LazyRow of Book Covers
                    LazyRow(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shelf_lazyrow_${section.genre}"),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = section.books,
                            key = { it.book.id }
                        ) { bookItem ->
                            BookCoverItem(
                                book = bookItem.book,
                                progress = bookItem.progress,
                                width = 104.dp,
                                height = 156.dp,
                                onClick = { onBookClick(bookItem.book.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shelf Page Dots Indicator (Apple Books / Warm Editorial style)
                    val activeIndex by remember {
                        derivedStateOf { listState.firstVisibleItemIndex }
                    }
                    val totalItems = section.books.size
                    val dotCount = minOf(4, totalItems)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        repeat(dotCount) { index ->
                            val isActive = (activeIndex % dotCount) == index
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 6.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) baseTint.copy(alpha = 0.9f)
                                        else baseTint.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
