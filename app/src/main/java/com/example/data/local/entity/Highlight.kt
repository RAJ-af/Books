package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "highlights",
    indices = [Index("bookId"), Index("chapterId")]
)
data class Highlight(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val chapterId: Long,
    val paragraphIndex: Int = 0,
    val startOffset: Int = 0,
    val endOffset: Int = 0,
    val highlightedText: String,
    val colorHex: String,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
