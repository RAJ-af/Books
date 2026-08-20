package com.example.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.BookWithDetails
import com.example.ui.components.BookCoverItem
import com.example.ui.library.LibraryViewModel
import com.example.ui.theme.ContentSerif
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftSepiaSurface
import com.example.ui.theme.SystemSans
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmOffWhite

@Composable
fun SearchScreen(
    viewModel: LibraryViewModel,
    onBackClick: () -> Unit,
    onBookClick: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            SearchTopBar(
                searchQuery = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onBackClick = onBackClick,
                focusRequester = focusRequester
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick suggested chips when query is empty
            if (searchQuery.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Suggested Searches",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val suggestions = listOf("Design", "Psychology", "Dieter Rams", "Bauhaus", "Novels", "Norman")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 12.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            Surface(
                                onClick = { viewModel.onSearchQueryChange(suggestion) },
                                shape = RoundedCornerShape(percent = 50),
                                color = SoftSepiaSurface,
                                modifier = Modifier.testTag("suggestion_chip_$suggestion")
                            ) {
                                Text(
                                    text = suggestion,
                                    color = ObsidianBlack,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Search by book title or author",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = ContentSerif,
                                    color = TextMuted
                                )
                            )
                        }
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No books found",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = EditorialSerif,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No results matching '$searchQuery'. Try checking the spelling or searching another category.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = ContentSerif,
                                color = TextMuted
                            )
                        )
                    }
                }
            } else {
                Text(
                    text = "${searchResults.size} results for '$searchQuery'",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("search_results_list"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = searchResults,
                        key = { it.book.id }
                    ) { bookItem ->
                        SearchResultCard(
                            bookItem = bookItem,
                            onClick = { onBookClick(bookItem.book.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                .testTag("search_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Library",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Search textfield
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search title, author...",
                    color = TextMuted,
                    fontSize = 15.sp
                )
            },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .testTag("search_input_field"),
            shape = RoundedCornerShape(percent = 50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = WarmOffWhite,
                unfocusedContainerColor = WarmOffWhite,
                focusedBorderColor = ObsidianBlack,
                unfocusedBorderColor = SoftSepiaSurface,
                cursorColor = ObsidianBlack
            ),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun SearchResultCard(
    bookItem: BookWithDetails,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_result_item_${bookItem.book.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmOffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCoverItem(
                book = bookItem.book,
                progress = bookItem.progress,
                width = 68.dp,
                height = 98.dp,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bookItem.book.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = ObsidianBlack
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = bookItem.book.author,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = SystemSans,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = SoftSepiaSurface
                    ) {
                        Text(
                            text = bookItem.book.genre,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ObsidianBlack
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD49A3D),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${bookItem.book.rating}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            )
                        )
                    }
                }
            }
        }
    }
}
