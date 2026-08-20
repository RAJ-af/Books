package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.Book
import com.example.data.local.entity.ReadingProgress
import com.example.ui.theme.ContentSerif
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SystemSans
import java.io.File

@Composable
fun BookCoverItem(
    book: Book,
    progress: ReadingProgress? = null,
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 150.dp,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(width)
            .clickable(onClick = onClick)
            .testTag("book_item_${book.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Book Spine & Card with soft shadow
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 6.dp, bottomEnd = 6.dp),
                    spotColor = ObsidianBlack.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp, topEnd = 6.dp, bottomEnd = 6.dp))
        ) {
            // Book cover inner canvas
            BookCoverArt(book = book)

            // Book spine fold line effect (left edge shadow)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Spine highlight crease
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 6.dp)
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )

            // PDF Badge on top-right
            if (book.isImportedPdf) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 6.dp),
                    color = ObsidianBlack.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "PDF",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = SystemSans,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Progress badge (if reading)
            if (progress != null && progress.percentComplete > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${progress.percentComplete.toInt()}%",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SystemSans
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = (progress.percentComplete / 100f).coerceIn(0f, 1f))
                                    .background(Color(0xFFE89A5A))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookCoverArt(book: Book) {
    if (book.coverImageUri.isNotBlank()) {
        if (book.coverImageUri.startsWith("http://") || book.coverImageUri.startsWith("https://")) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEDE9DF))
            ) {
                AsyncImage(
                    model = book.coverImageUri,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            return
        }

        val coverFile = File(book.coverImageUri)
        if (coverFile.exists()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEDE9DF))
            ) {
                AsyncImage(
                    model = coverFile,
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            return
        }
    }

    val titleLower = book.title.lowercase()

    when {
        titleLower.contains("bauhaus") -> {
            // Bauhaus iconic geometric cover (Blue, Yellow, Red, Black blocks)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEBE6D8))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "bauhaus",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFF141414),
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Geometric composition
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Blue bar on left
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF194A8D))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            // Yellow bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .background(Color(0xFFE5B537))
                            )
                            // Black block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color(0xFF1B1B1B)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = "1919",
                                    color = Color(0xFFE5B537),
                                    fontSize = 8.sp,
                                    fontFamily = SystemSans,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }

                    // Bottom TASCHEN label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .background(Color(0xFFEBE6D8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TASCHEN",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }

        titleLower.contains("dieter") || titleLower.contains("rams") -> {
            // Dieter Rams iconic orange typographic poster
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE04423))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "rams",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF111111),
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "dieter",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF111111),
                            letterSpacing = (-1).sp
                        )
                    }
                    Text(
                        text = "the complete works",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 7.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        titleLower.contains("design of everyday") -> {
            // Yellow cover with red teapot & Don Norman
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF2CA38))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "The DESIGN",
                            fontFamily = EditorialSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "of EVERYDAY",
                            fontFamily = EditorialSerif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 8.sp,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "THINGS",
                            fontFamily = EditorialSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    // Teapot stylized graphic
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9E2A2B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☕",
                            fontSize = 20.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DON NORMAN",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }

        titleLower.contains("solid product") -> {
            // White clean book with red pen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF9F6))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SOLID\nPRODUCT\nDESIGN",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF1B1B1B)
                        )
                        Text(
                            text = "EXERCISES",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp,
                            color = Color(0xFFB82C1B)
                        )
                    }

                    // Stylized Red pen / marker
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFB82C1B))
                    )

                    Text(
                        text = "WILEY",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 6.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFF444444)
                    )
                }
            }
        }

        titleLower.contains("read people") -> {
            // Dark Teal / Statue silhouette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A3848))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "READ\nPEOPLE\nLIKE A\nBOOK",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFE8C872)
                        )
                    }

                    // Classical statue head emoji / graphic
                    Text(
                        text = "🗿",
                        fontSize = 24.sp
                    )

                    Text(
                        text = "PATRICK KING",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 6.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        titleLower.contains("subconscious") -> {
            // Colorful Mosaic Tile Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2C2F3F))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                    ) {
                        Column {
                            Text(
                                text = "The Power of",
                                fontFamily = ContentSerif,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 7.sp,
                                color = Color(0xFFE2C98F)
                            )
                            Text(
                                text = "Subconscious Mind",
                                fontFamily = EditorialSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Mosaic blocks
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFD49A3D)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF385E72)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF8A3B43)))
                        }
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF285848)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFD87D4A)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF474E68)))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JOSEPH MURPHY",
                            fontFamily = SystemSans,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2C98F)
                        )
                    }
                }
            }
        }

        titleLower.contains("body keeps the score") -> {
            // Blue cover with Matisse cut-out figure
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF27548A))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "THE BODY\nKEEPS THE SCORE",
                            fontFamily = SystemSans,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }

                    // Matisse figure box
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E3D64), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFFD8AA47), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "BESSEL VAN DER KOLK",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 5.5.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        titleLower.contains("paper palace") -> {
            // Scenic painting forest pond
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF4A6B53), Color(0xFF7A6843), Color(0xFF2C3E35))
                        )
                    )
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE\nPAPER\nPALACE",
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFFAF7F2)
                    )

                    Text(
                        text = "🌿",
                        fontSize = 18.sp
                    )

                    Text(
                        text = "MIRANDA COWLEY HELLER",
                        fontFamily = SystemSans,
                        fontSize = 5.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        titleLower.contains("evelyn hugo") -> {
            // Emerald green gown silhouette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF163E32))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE SEVEN\nHUSBANDS\nOF\nEVELYN HUGO",
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFE8D4A2)
                    )

                    Text(
                        text = "💃",
                        fontSize = 18.sp
                    )

                    Text(
                        text = "TAYLOR JENKINS REID",
                        fontFamily = SystemSans,
                        fontSize = 5.sp,
                        color = Color(0xFFE8D4A2)
                    )
                }
            }
        }

        titleLower.contains("vanishing half") -> {
            // Abstract dual head silhouettes in pink/blue/purple
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF492F5C), Color(0xFFB5486F), Color(0xFF2C4378))
                        )
                    )
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE\nVANISHING\nHALF",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Black,
                        fontSize = 8.5.sp,
                        lineHeight = 10.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )

                    Text(
                        text = "🎭",
                        fontSize = 18.sp
                    )

                    Text(
                        text = "BRIT BENNETT",
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 6.sp,
                        color = Color.White
                    )
                }
            }
        }

        titleLower.contains("daisy jones") -> {
            // 70s rock vintage typography
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2A5F63))
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DAISY\nJONES\n& THE SIX",
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFE0CAB5)
                    )

                    Text(
                        text = "🎸",
                        fontSize = 18.sp
                    )

                    Text(
                        text = "TAYLOR JENKINS REID",
                        fontFamily = SystemSans,
                        fontSize = 5.sp,
                        color = Color(0xFFE0CAB5)
                    )
                }
            }
        }

        else -> {
            // Default elegant editorial cover using book's colorHex
            val bgColor = try {
                Color(android.graphics.Color.parseColor(book.colorHex))
            } catch (_: Exception) {
                Color(0xFF333333)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = book.title,
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )

                    Text(
                        text = "📖",
                        fontSize = 18.sp
                    )

                    Text(
                        text = book.author,
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Medium,
                        fontSize = 6.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
