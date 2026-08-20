package com.example.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.audio.LibrivoxAudiobook
import com.example.data.audio.LibrivoxAudioTrack
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.ReadingProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookDetailViewModel(
    application: Application,
    private val bookId: Long
) : AndroidViewModel(application) {

    private val app = application as ReaderApplication
    private val repository = app.bookRepository
    private val librivoxSource = app.librivoxSource
    val audioPlayerManager = app.audioPlayerManager

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

    private val _audiobooks = MutableStateFlow<List<LibrivoxAudiobook>>(emptyList())
    val audiobooks: StateFlow<List<LibrivoxAudiobook>> = _audiobooks.asStateFlow()

    private val _isCheckingAudio = MutableStateFlow(false)
    val isCheckingAudio: StateFlow<Boolean> = _isCheckingAudio.asStateFlow()

    private val _showAudioPicker = MutableStateFlow(false)
    val showAudioPicker: StateFlow<Boolean> = _showAudioPicker.asStateFlow()

    private val _isLoadingTracks = MutableStateFlow(false)
    val isLoadingTracks: StateFlow<Boolean> = _isLoadingTracks.asStateFlow()

    private var checkedBookId: Long? = null

    init {
        viewModelScope.launch {
            book.collect { currentBook ->
                if (currentBook != null && checkedBookId != currentBook.id) {
                    checkedBookId = currentBook.id
                    checkAudiobookAvailability(currentBook)
                }
            }
        }
    }

    private fun checkAudiobookAvailability(book: Book) {
        viewModelScope.launch {
            _isCheckingAudio.value = true
            try {
                // Strip common extra tags from title for better LibriVox match
                val cleanedTitle = book.title
                    .replace(Regex("(?i)\\(.*\\)"), "")
                    .replace(Regex("(?i)\\[.*\\]"), "")
                    .trim()

                val results = librivoxSource.searchAudiobooks(cleanedTitle)
                _audiobooks.value = results
            } catch (e: Exception) {
                _audiobooks.value = emptyList()
            } finally {
                _isCheckingAudio.value = false
            }
        }
    }

    fun onListenButtonClicked() {
        val currentBook = book.value ?: return
        if (_audiobooks.value.isEmpty()) {
            checkAudiobookAvailability(currentBook)
        }
        _showAudioPicker.value = true
    }

    fun dismissAudioPicker() {
        _showAudioPicker.value = false
    }

    fun selectAudiobookAndPlay(audiobook: LibrivoxAudiobook) {
        val currentBook = book.value ?: return
        viewModelScope.launch {
            _isLoadingTracks.value = true
            _showAudioPicker.value = false
            try {
                val tracks = librivoxSource.fetchAudioTracks(audiobook.id)
                if (tracks.isNotEmpty()) {
                    audioPlayerManager.playAudiobook(
                        bookTitle = currentBook.title,
                        authorName = currentBook.author,
                        coverUri = currentBook.coverImageUri,
                        tracks = tracks
                    )
                }
            } catch (e: Exception) {
                // Error fetching tracks
            } finally {
                _isLoadingTracks.value = false
            }
        }
    }

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
