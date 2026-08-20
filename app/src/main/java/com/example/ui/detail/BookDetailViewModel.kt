package com.example.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.ReadingProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookDetailUiState(
    val book: Book? = null,
    val chapters: List<Chapter> = emptyList(),
    val progress: ReadingProgress? = null,
    val isLoading: Boolean = true
)

class BookDetailViewModel(
    application: Application,
    private val bookId: Long
) : AndroidViewModel(application) {

    private val repository = (application as ReaderApplication).bookRepository

    val book: StateFlow<Book?> = repository.getBookById(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val chapters: StateFlow<List<Chapter>> = repository.getChaptersForBook(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val progress: StateFlow<ReadingProgress?> = repository.getProgressForBook(bookId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun markChapterProgress(chapterId: Long, percent: Float) {
        viewModelScope.launch {
            repository.updateReadingProgress(
                bookId = bookId,
                chapterId = chapterId,
                percentComplete = percent
            )
        }
    }
}
