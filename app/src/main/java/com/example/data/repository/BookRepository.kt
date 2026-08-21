package com.example.data.repository

import com.example.data.local.dao.BookDao
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.ChapterDao
import com.example.data.local.dao.HighlightDao
import com.example.data.local.dao.ReadingProgressDao
import com.example.data.local.entity.Book
import com.example.data.local.entity.Bookmark
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.Highlight
import com.example.data.local.entity.ReadingProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BookWithDetails(
    val book: Book,
    val progress: ReadingProgress? = null,
    val totalChapters: Int = 0
)

class BookRepository(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val readingProgressDao: ReadingProgressDao,
    private val bookmarkDao: BookmarkDao,
    private val highlightDao: HighlightDao
) {
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allGenres: Flow<List<String>> = bookDao.getAllGenres()
    val allProgress: Flow<List<ReadingProgress>> = readingProgressDao.getAllProgress()

    fun getBooksByGenre(genre: String): Flow<List<Book>> = bookDao.getBooksByGenre(genre)

    fun getBookById(bookId: Long): Flow<Book?> = bookDao.getBookById(bookId)

    suspend fun getBookByIdDirect(bookId: Long): Book? = bookDao.getBookByIdDirect(bookId)

    fun searchBooks(query: String): Flow<List<Book>> = bookDao.searchBooks(query)

    fun getChaptersForBook(bookId: Long): Flow<List<Chapter>> = chapterDao.getChaptersForBook(bookId)

    suspend fun getChaptersForBookDirect(bookId: Long): List<Chapter> = chapterDao.getChaptersForBookDirect(bookId)

    fun getChapterById(chapterId: Long): Flow<Chapter?> = chapterDao.getChapterById(chapterId)

    suspend fun getChapterByIdDirect(chapterId: Long): Chapter? = chapterDao.getChapterByIdDirect(chapterId)

    suspend fun getFirstChapterForBook(bookId: Long): Chapter? = chapterDao.getFirstChapterForBook(bookId)

    fun getProgressForBook(bookId: Long): Flow<ReadingProgress?> = readingProgressDao.getProgressForBook(bookId)

    // Bookmarks
    fun getBookmarksForBook(bookId: Long): Flow<List<Bookmark>> = bookmarkDao.getBookmarksForBook(bookId)

    fun getBookmarksForChapter(bookId: Long, chapterId: Long): Flow<List<Bookmark>> =
        bookmarkDao.getBookmarksForChapter(bookId, chapterId)

    suspend fun insertBookmark(bookmark: Bookmark): Long = bookmarkDao.insertBookmark(bookmark)

    suspend fun deleteBookmark(bookmark: Bookmark) = bookmarkDao.deleteBookmark(bookmark)

    suspend fun deleteBookmarkById(id: Long) = bookmarkDao.deleteBookmarkById(id)

    suspend fun deleteBookmarkAt(bookId: Long, chapterId: Long, pageNumber: Int, scrollAnchor: Int) =
        bookmarkDao.deleteBookmarkAt(bookId, chapterId, pageNumber, scrollAnchor)

    // Highlights
    fun getHighlightsForBook(bookId: Long): Flow<List<Highlight>> = highlightDao.getHighlightsForBook(bookId)

    fun getHighlightsForChapter(bookId: Long, chapterId: Long): Flow<List<Highlight>> =
        highlightDao.getHighlightsForChapter(bookId, chapterId)

    suspend fun insertHighlight(highlight: Highlight): Long = highlightDao.insertHighlight(highlight)

    suspend fun updateHighlight(highlight: Highlight) = highlightDao.updateHighlight(highlight)

    suspend fun deleteHighlight(highlight: Highlight) = highlightDao.deleteHighlight(highlight)

    suspend fun deleteHighlightById(id: Long) = highlightDao.deleteHighlightById(id)

    fun getBooksWithProgressByGenre(genre: String): Flow<List<BookWithDetails>> {
        return combine(
            bookDao.getBooksByGenre(genre),
            readingProgressDao.getAllProgress()
        ) { books, progressList ->
            val progressMap = progressList.associateBy { it.bookId }
            books.map { book ->
                BookWithDetails(
                    book = book,
                    progress = progressMap[book.id]
                )
            }
        }
    }

    suspend fun updateReadingProgress(
        bookId: Long,
        chapterId: Long,
        percentComplete: Float,
        scrollOffset: Int = 0
    ) {
        val progress = ReadingProgress(
            bookId = bookId,
            currentChapterId = chapterId,
            percentComplete = percentComplete.coerceIn(0f, 100f),
            lastReadTimestamp = System.currentTimeMillis(),
            currentScrollOffset = scrollOffset
        )
        readingProgressDao.upsertProgress(progress)
    }

    suspend fun insertBookWithChapters(book: Book, chapters: List<Chapter>): Long {
        val bookId = bookDao.insertBook(book)
        val chaptersWithBookId = chapters.map { it.copy(bookId = bookId) }
        chapterDao.insertChapters(chaptersWithBookId)
        return bookId
    }

    suspend fun updateChapterContent(chapterId: Long, content: String) {
        chapterDao.updateChapterContent(chapterId, content)
    }

    suspend fun updateChapterContentByPage(bookId: Long, pageNumber: Int, content: String) {
        chapterDao.updateChapterContentByPage(bookId, pageNumber, content)
    }

    suspend fun deleteBook(book: Book) {
        readingProgressDao.deleteProgress(book.id)
        chapterDao.deleteChaptersForBook(book.id)
        bookDao.deleteBook(book)
    }
}
