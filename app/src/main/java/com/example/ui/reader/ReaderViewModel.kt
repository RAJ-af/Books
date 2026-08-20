package com.example.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.ReadingProgress
import com.example.data.settings.ReaderFontStyle
import com.example.data.settings.ReaderLineSpacing
import com.example.data.settings.ReaderSettings
import com.example.data.settings.ReaderThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReaderViewModel(
    application: Application,
    val bookId: Long,
    initialChapterId: Long
) : AndroidViewModel(application) {

    private val readerApp = application as ReaderApplication
    private val bookRepository = readerApp.bookRepository
    private val settingsRepository = readerApp.readerSettingsRepository

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

    init {
        // Initial progress record for this chapter if opened
        selectChapter(initialChapterId)
    }

    fun selectChapter(chapterId: Long) {
        _currentChapterId.value = chapterId
        val chapterList = chapters.value
        val currentIndex = chapterList.indexOfFirst { it.id == chapterId }.let { if (it == -1) 0 else it }
        val total = if (chapterList.isNotEmpty()) chapterList.size else 1
        val percent = ((currentIndex + 1).toFloat() / total.toFloat()) * 100f
        updateProgress(chapterId, percent)
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
