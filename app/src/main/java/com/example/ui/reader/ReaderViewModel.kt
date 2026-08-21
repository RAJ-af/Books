package com.example.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.local.entity.Book
import com.example.data.local.entity.Bookmark
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.Highlight
import com.example.data.local.entity.ReadingProgress
import com.example.data.settings.ReaderFontStyle
import com.example.data.settings.ReaderLineSpacing
import com.example.data.settings.ReaderSettings
import com.example.data.settings.ReaderThemeMode
import com.example.util.PdfHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ReaderViewModel(
    application: Application,
    val bookId: Long,
    initialChapterId: Long
) : AndroidViewModel(application) {

    private val readerApp = application as ReaderApplication
    private val bookRepository = readerApp.bookRepository
    private val settingsRepository = readerApp.readerSettingsRepository
    val audioPlayerManager = readerApp.audioPlayerManager

    val book: StateFlow<Book?> = bookRepository.getBookById(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val chapters: StateFlow<List<Chapter>> = bookRepository.getChaptersForBook(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentChapterId = MutableStateFlow(initialChapterId)
    val currentChapterId: StateFlow<Long> = _currentChapterId.asStateFlow()

    val progress: StateFlow<ReadingProgress?> = bookRepository.getProgressForBook(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val readerSettings: StateFlow<ReaderSettings> = settingsRepository.readerSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderSettings()
    )

    val bookmarks: StateFlow<List<Bookmark>> = bookRepository.getBookmarksForBook(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val highlights: StateFlow<List<Highlight>> = bookRepository.getHighlightsForBook(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initial progress record for this chapter if opened
        selectChapter(initialChapterId)
    }

    fun toggleBookmark(pageNumber: Int, paragraphIndex: Int) {
        val chapterId = _currentChapterId.value
        val currentBookmarks = bookmarks.value
        val isPdf = book.value?.isImportedPdf == true
        val existing = currentBookmarks.find {
            it.chapterId == chapterId &&
            (if (isPdf) it.pageNumber == pageNumber else it.scrollAnchor == paragraphIndex)
        }

        viewModelScope.launch {
            if (existing != null) {
                bookRepository.deleteBookmark(existing)
            } else {
                val chapter = chapters.value.find { it.id == chapterId }
                val label = if (isPdf) {
                    "Page ${pageNumber + 1}"
                } else {
                    "Chapter ${chapter?.number ?: 1} • Paragraph ${paragraphIndex + 1}"
                }
                bookRepository.insertBookmark(
                    Bookmark(
                        bookId = bookId,
                        chapterId = chapterId,
                        pageNumber = pageNumber,
                        scrollAnchor = paragraphIndex,
                        label = label
                    )
                )
            }
        }
    }

    fun addHighlight(
        paragraphIndex: Int,
        startOffset: Int,
        endOffset: Int,
        highlightedText: String,
        colorHex: String,
        note: String? = null
    ) {
        val chapterId = _currentChapterId.value
        viewModelScope.launch {
            bookRepository.insertHighlight(
                Highlight(
                    bookId = bookId,
                    chapterId = chapterId,
                    paragraphIndex = paragraphIndex,
                    startOffset = startOffset,
                    endOffset = endOffset,
                    highlightedText = highlightedText,
                    colorHex = colorHex,
                    note = note
                )
            )
        }
    }

    fun deleteHighlight(highlightId: Long) {
        viewModelScope.launch {
            bookRepository.deleteHighlightById(highlightId)
        }
    }

    fun selectChapter(chapterId: Long) {
        _currentChapterId.value = chapterId
        val chapterList = chapters.value
        val currentIndex = chapterList.indexOfFirst { it.id == chapterId }.let { if (it == -1) 0 else it }
        val total = if (chapterList.isNotEmpty()) chapterList.size else 1
        val percent = ((currentIndex + 1).toFloat() / total.toFloat()) * 100f
        updateProgress(chapterId, percent)
        triggerOnDemandOcrForCurrentPage()
    }

    fun triggerOnDemandOcrForCurrentPage() {
        val currentBook = book.value ?: return
        val pdfPath = currentBook.pdfFilePath
        if (!currentBook.isImportedPdf || pdfPath.isBlank()) return

        val chapterList = chapters.value
        val currentId = _currentChapterId.value
        val currentChapterIndex = chapterList.indexOfFirst { it.id == currentId }
        if (currentChapterIndex == -1) return
        val currentChapter = chapterList[currentChapterIndex]

        if (currentChapter.content.isNotBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val startPage = currentChapter.pdfPageStart ?: 0
                val endPage = chapterList.getOrNull(currentChapterIndex + 1)?.pdfPageStart ?: currentBook.pageCount
                val contentBuilder = StringBuilder()
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                for (p in startPage until endPage) {
                    val bitmap = PdfHelper.renderPageBitmap(pdfPath, p, 1200) ?: continue
                    val inputImage = InputImage.fromBitmap(bitmap, 0)

                    val recognizedText = suspendCancellableCoroutine<String> { continuation ->
                        recognizer.process(inputImage)
                            .addOnSuccessListener { visionText -> continuation.resume(visionText.text.trim()) }
                            .addOnFailureListener { continuation.resume("") }
                    }

                    if (recognizedText.isNotBlank()) {
                        contentBuilder.append(recognizedText).append("\n\n")
                    }
                    bitmap.recycle()
                }

                val finalSectionText = contentBuilder.toString().trim()
                if (finalSectionText.isNotBlank()) {
                    bookRepository.updateChapterContent(currentChapter.id, finalSectionText)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun navigateToNextChapter(): Boolean {
        val chapterList = chapters.value
        val currentIndex = chapterList.indexOfFirst { it.id == _currentChapterId.value }.let { if (it == -1) 0 else it }
        if (currentIndex < chapterList.size - 1) {
            val nextChapter = chapterList[currentIndex + 1]
            selectChapter(nextChapter.id)
            return true
        }
        return false
    }

    fun navigateToPreviousChapter(): Boolean {
        val chapterList = chapters.value
        val currentIndex = chapterList.indexOfFirst { it.id == _currentChapterId.value }.let { if (it == -1) 0 else it }
        if (currentIndex > 0) {
            val prevChapter = chapterList[currentIndex - 1]
            selectChapter(prevChapter.id)
            return true
        }
        return false
    }

    fun updateProgress(chapterId: Long, percent: Float, scrollOffset: Int = 0) {
        viewModelScope.launch {
            bookRepository.updateReadingProgress(
                bookId = bookId,
                chapterId = chapterId,
                percentComplete = percent,
                scrollOffset = scrollOffset
            )
        }
    }

    fun updateFontSize(fontSizeSp: Float) {
        viewModelScope.launch {
            settingsRepository.updateFontSize(fontSizeSp)
        }
    }

    fun updateTheme(theme: ReaderThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }

    fun updateFontStyle(fontStyle: ReaderFontStyle) {
        viewModelScope.launch {
            settingsRepository.updateFontStyle(fontStyle)
        }
    }

    fun updateLineSpacing(spacing: ReaderLineSpacing) {
        viewModelScope.launch {
            settingsRepository.updateLineSpacing(spacing)
        }
    }

    fun updateBrightness(brightness: Int) {
        viewModelScope.launch {
            settingsRepository.updateBrightness(brightness)
        }
    }
}
