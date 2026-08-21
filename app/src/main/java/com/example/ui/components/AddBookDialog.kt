package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.ContentSerif
import com.example.ui.theme.EditorialSerif
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SoftSepiaSurface
import com.example.ui.theme.SystemSans
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmOffWhite
import com.example.util.PdfHelper
import com.example.util.PdfImportResult
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookBottomSheet(
    availableGenres: List<String> = listOf("Design", "Psychology", "Novels"),
    onDismiss: () -> Unit,
    onBrowseInternetArchive: () -> Unit,
    onAddManualBook: (title: String, author: String, genre: String, description: String, colorHex: String) -> Unit,
    onAddPdfBook: (title: String, author: String, genre: String, description: String, pdfPath: String, coverPath: String, pageCount: Int, colorHex: String, isScanned: Boolean, pageTexts: List<String>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isProcessingPdf by remember { mutableStateOf(false) }
    var importedPdfResult by remember { mutableStateOf<PdfImportResult?>(null) }

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(availableGenres.firstOrNull() ?: "Design") }
    var customGenre by remember { mutableStateOf("") }
    var isAddingNewGenre by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#285698") }

    val colorPalettes = listOf("#285698", "#E04423", "#F2CA38", "#2B4C5F", "#27548A", "#163E32", "#492F5C", "#2A5F63", "#D97706")

    // Storage Access Framework Picker for PDFs
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingPdf = true
            scope.launch {
                val result = PdfHelper.processAndImportPdf(context, uri)
                isProcessingPdf = false
                result.onSuccess { info ->
                    importedPdfResult = info
                    title = info.guessedTitle
                    if (author.isBlank()) {
                        author = "Imported Author"
                    }
                    description = "PDF document with ${info.pageCount} pages (${info.fileSizeFormatted})."
                }.onFailure { err ->
                    Toast.makeText(context, "Failed to load PDF: ${err.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmOffWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(TextMuted.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (importedPdfResult != null) "Import PDF Book" else "Add Books",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = EditorialSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = ObsidianBlack
                        )
                    )
                    Text(
                        text = if (importedPdfResult != null) "Review metadata and confirm import" else "Discover public-domain books or import local PDF",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = SystemSans,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftSepiaSurface)
                        .clickable(onClick = onDismiss)
                        .testTag("add_book_close_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ObsidianBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option 1: "Browse Internet Archive" Banner (Featured)
            if (importedPdfResult == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            onDismiss()
                            onBrowseInternetArchive()
                        }
                        .testTag("browse_internet_archive_button"),
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianBlack,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Internet Archive",
                                tint = Color(0xFFE89A5A),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Browse Internet Archive",
                                    fontFamily = SystemSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF2E6F40)
                                ) {
                                    Text(
                                        text = "FREE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = SystemSans,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Search & import millions of public-domain books",
                                fontFamily = ContentSerif,
                                fontSize = 12.5.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // PDF Import Action Card / Preview
            if (isProcessingPdf) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SoftSepiaSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = ObsidianBlack,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Analyzing PDF & rendering cover...",
                            fontFamily = SystemSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ObsidianBlack
                        )
                    }
                }
            } else if (importedPdfResult == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        }
                        .testTag("select_pdf_button"),
                    shape = RoundedCornerShape(18.dp),
                    color = SoftSepiaSurface.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD3C4))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                tint = Color(0xFFE89A5A),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Upload PDF from Device",
                                fontFamily = SystemSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ObsidianBlack
                            )
                            Text(
                                text = "Choose document via Android file picker",
                                fontFamily = SystemSans,
                                fontSize = 12.5.sp,
                                color = TextMuted
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = ObsidianBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                // PDF Loaded Card with Cover Thumbnail Preview
                val pdf = importedPdfResult!!
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = SoftSepiaSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD3C4))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rendered cover thumbnail
                        Surface(
                            modifier = Modifier
                                .width(56.dp)
                                .height(80.dp)
                                .shadow(4.dp, RoundedCornerShape(6.dp)),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White
                        ) {
                            AsyncImage(
                                model = File(pdf.coverImagePath),
                                contentDescription = "PDF Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (pdf.isScanned) Color(0xFFC86C20) else Color(0xFFB82C1B)
                                ) {
                                    Text(
                                        text = if (pdf.isScanned) "SCANNED PDF" else "PDF READY",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = SystemSans,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${pdf.pageCount} Pages • ${pdf.fileSizeFormatted}",
                                    fontSize = 12.sp,
                                    fontFamily = SystemSans,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = pdf.guessedTitle,
                                fontFamily = EditorialSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ObsidianBlack,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            shape = RoundedCornerShape(percent = 50),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "Change",
                                fontSize = 12.sp,
                                fontFamily = SystemSans,
                                color = ObsidianBlack
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Book Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Book / Document Title") },
                placeholder = { Text("e.g. Design Systems & Architecture") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_book_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ObsidianBlack,
                    unfocusedBorderColor = SoftSepiaSurface,
                    focusedLabelColor = ObsidianBlack
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Author Name Input
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author / Publisher") },
                placeholder = { Text("e.g. Robin Williams") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_book_author_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ObsidianBlack,
                    unfocusedBorderColor = SoftSepiaSurface,
                    focusedLabelColor = ObsidianBlack
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector (Chips + "Add new")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category / Shelf",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                )

                if (!isAddingNewGenre) {
                    Text(
                        text = "+ Add new",
                        fontSize = 12.5.sp,
                        fontFamily = SystemSans,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianBlack,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { isAddingNewGenre = true }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips Row
            val allGenreOptions = remember(availableGenres, customGenre) {
                val list = availableGenres.toMutableList()
                if (customGenre.isNotBlank() && !list.contains(customGenre)) {
                    list.add(customGenre)
                }
                list.distinct()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allGenreOptions.take(4).forEach { genre ->
                    val isSelected = selectedGenre == genre
                    Surface(
                        onClick = {
                            selectedGenre = genre
                            isAddingNewGenre = false
                        },
                        shape = RoundedCornerShape(percent = 50),
                        color = if (isSelected) ObsidianBlack else SoftSepiaSurface,
                        modifier = Modifier.testTag("genre_chip_$genre")
                    ) {
                        Text(
                            text = genre,
                            color = if (isSelected) Color.White else ObsidianBlack,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Inline New Category Input if clicked "+ Add new"
            AnimatedVisibility(visible = isAddingNewGenre) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customGenre,
                            onValueChange = {
                                customGenre = it
                                selectedGenre = it
                            },
                            label = { Text("New Category Name") },
                            placeholder = { Text("e.g. Architecture") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ObsidianBlack,
                                unfocusedBorderColor = SoftSepiaSurface
                            )
                        )

                        Button(
                            onClick = {
                                if (customGenre.isNotBlank()) {
                                    selectedGenre = customGenre.trim()
                                    isAddingNewGenre = false
                                }
                            },
                            enabled = customGenre.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
                        ) {
                            Text("Set")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cover Color Theme Palette
            Text(
                text = "Accent Color Palette",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SystemSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colorPalettes.forEach { hex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (_: Exception) {
                        Color(0xFF285698)
                    }
                    val isSelected = selectedColor == hex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description / Synopsis
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Synopsis / Reading Notes (Optional)") },
                placeholder = { Text("A brief overview or summary...") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_book_desc_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ObsidianBlack,
                    unfocusedBorderColor = SoftSepiaSurface,
                    focusedLabelColor = ObsidianBlack
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            val isFormValid = title.isNotBlank() && author.isNotBlank() && selectedGenre.isNotBlank()

            Button(
                onClick = {
                    if (isFormValid) {
                        val pdf = importedPdfResult
                        if (pdf != null) {
                            onAddPdfBook(
                                title.trim(),
                                author.trim(),
                                selectedGenre.trim(),
                                description.trim(),
                                pdf.pdfPath,
                                pdf.coverImagePath,
                                pdf.pageCount,
                                selectedColor,
                                pdf.isScanned,
                                pdf.pageTexts
                            )
                        } else {
                            onAddManualBook(
                                title.trim(),
                                author.trim(),
                                selectedGenre.trim(),
                                description.trim(),
                                selectedColor
                            )
                        }
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("add_book_submit_button"),
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ObsidianBlack,
                    contentColor = Color.White,
                    disabledContainerColor = TextMuted.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (importedPdfResult != null) Icons.Default.PictureAsPdf else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (importedPdfResult != null) "Import PDF to Shelf" else "Save Book to Library",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SystemSans
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
