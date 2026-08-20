package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.remote.ArchiveOrgRepository
import com.example.data.repository.BookRepository
import com.example.data.settings.ReaderSettingsRepository
import com.example.data.source.BookSourceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReaderApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val bookRepository by lazy {
        BookRepository(
            bookDao = database.bookDao(),
            chapterDao = database.chapterDao(),
            readingProgressDao = database.readingProgressDao()
        )
    }

    val readerSettingsRepository by lazy {
        ReaderSettingsRepository(this)
    }

    val archiveOrgRepository by lazy {
        ArchiveOrgRepository(this)
    }

    val bookSourceManager by lazy {
        BookSourceManager(this)
    }

    val librivoxSource by lazy {
        com.example.data.audio.LibrivoxSource(com.example.data.source.BookSourceManager.createSharedClient())
    }

    val audioPlayerManager by lazy {
        com.example.ui.audio.AudioPlayerManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Ensure database and seed data are initialized
        applicationScope.launch {
            AppDatabase.populateSeedData(database)
        }
    }
}
