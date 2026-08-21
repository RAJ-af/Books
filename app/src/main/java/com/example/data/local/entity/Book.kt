package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val coverImageUri: String = "",
    val rating: Float = 4.5f,
    val pageCount: Int = 250,
    val genre: String, // e.g. "Design", "Psychology", "Novels"
    val description: String = "",
    val isImportedPdf: Boolean = false,
    val isScanned: Boolean = false,
    val pdfFilePath: String = "",
    val fileType: String = "TEXT", // "PDF", "EPUB", "TEXT"
    val source: String = "local", // "local", "seed", "internet_archive", "gutenberg", "doab"
    val colorHex: String = "#E89A5A",
    val accentTint: String = "#D97706"
)
