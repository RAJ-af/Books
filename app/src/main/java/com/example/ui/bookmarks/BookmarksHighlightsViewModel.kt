package com.example.ui.bookmarks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.local.entity.Book
import com.example.data.local.entity.Bookmark
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.Highlight
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksHighlightsViewModel(
    application: Application,
    val bookId: Long
) : AndroidViewModel(application) {

    private val readerApp = application as ReaderApplication
    private val bookRepository = readerApp.bookRepository

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

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookRepository.deleteBookmark(bookmark)
        }
    }

    fun deleteHighlight(highlight: Highlight) {
        viewModelScope.launch {
            bookRepository.deleteHighlight(highlight)
        }
    }
}
