package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey
    val bookId: Long,
    val currentChapterId: Long = 0L,
    val percentComplete: Float = 0f, // 0.0 to 100.0
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val currentScrollOffset: Int = 0
)
