package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number ASC")
    fun getChaptersForBook(bookId: Long): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number ASC")
    suspend fun getChaptersForBookDirect(bookId: Long): List<Chapter>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    fun getChapterById(chapterId: Long): Flow<Chapter?>

    @Query("SELECT * FROM chapters WHERE id = :chapterId LIMIT 1")
    suspend fun getChapterByIdDirect(chapterId: Long): Chapter?

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number ASC LIMIT 1")
    suspend fun getFirstChapterForBook(bookId: Long): Chapter?

    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId")
    suspend fun getChapterCountForBook(bookId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>): List<Long>

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersForBook(bookId: Long)

    @Query("UPDATE chapters SET content = :content WHERE id = :chapterId")
    suspend fun updateChapterContent(chapterId: Long, content: String)

    @Query("UPDATE chapters SET content = :content WHERE bookId = :bookId AND number = :pageNumber")
    suspend fun updateChapterContentByPage(bookId: Long, pageNumber: Int, content: String)
}
