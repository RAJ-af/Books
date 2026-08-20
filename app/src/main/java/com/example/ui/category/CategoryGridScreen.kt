package com.example.ui.category

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
fun CategoryGridScreen(
    genre: String,
    viewModel: LibraryViewModel,
    onBackClick: () -> Unit,
    onBookClick: (Long) -> Unit
) {
    val categorySections by viewModel.categorySections.collectAsStateWithLifecycle()
    val section = categorySections.find { it.genre.equals(genre, ignoreCase = true) }
    val books = section?.books ?: emptyList()

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            CategoryTopBar(
                genre = genre,
                bookCount = books.size,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (books.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No books found in $genre",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = ContentSerif,
                        color = TextMuted
                    )
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("category_books_grid"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(
                    items = books,
                    key = { it.book.id }
                ) { bookItem ->
                    CategoryBookGridCard(
                        bookItem = bookItem,
                        onClick = { onBookClick(bookItem.book.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryTopBar(
    genre: String,
    bookCount: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
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
                .testTag("category_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to Library",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = genre,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = ObsidianBlack
                )
            )
            Text(
                text = "$bookCount curated books",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = SystemSans,
                    color = TextMuted,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
fun CategoryBookGridCard(
    bookItem: BookWithDetails,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("category_card_${bookItem.book.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = WarmOffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book Cover
            BookCoverItem(
                book = bookItem.book,
                progress = bookItem.progress,
                width = 110.dp,
                height = 160.dp,
                onClick = onClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = bookItem.book.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    color = ObsidianBlack
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Author
            Text(
                text = bookItem.book.author,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = SystemSans,
                    fontSize = 12.sp,
                    color = TextMuted
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rating & Page count row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD49A3D),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${bookItem.book.rating}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBlack
                        )
                    )
                }

                Text(
                    text = "${bookItem.book.pageCount} pgs",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted
                    )
                )
            }
        }
    }
}
