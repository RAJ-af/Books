package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BookDao
import com.example.data.local.dao.ChapterDao
import com.example.data.local.dao.ReadingProgressDao
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.ReadingProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Book::class,
        Chapter::class,
        ReadingProgress::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun readingProgressDao(): ReadingProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "books_app.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateSeedData(database)
                    }
                }
            }
        }

        suspend fun populateSeedData(database: AppDatabase) {
            val bookDao = database.bookDao()
            val chapterDao = database.chapterDao()
            val progressDao = database.readingProgressDao()

            if (bookDao.getBookCount() == 0) {
                val seedData = SeedData.getSeedBooks()
                for (item in seedData) {
                    bookDao.insertBook(item.book)
                    chapterDao.insertChapters(item.chapters)
                    item.initialProgress?.let { progress ->
                        progressDao.upsertProgress(progress)
                    }
                }
            }
        }
    }
}
