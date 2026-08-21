package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    indices = [Index("bookId"), Index("chapterId")]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val chapterId: Long,
    val pageNumber: Int = 0,
    val scrollAnchor: Int = 0,
    val label: String,
    val createdAt: Long = System.currentTimeMillis()
)
