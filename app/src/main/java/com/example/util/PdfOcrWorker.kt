package com.example.util

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.AppDatabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class PdfOcrWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val bookId = inputData.getLong(KEY_BOOK_ID, -1L)
        val pdfPath = inputData.getString(KEY_PDF_PATH)

        if (bookId == -1L || pdfPath.isNullOrBlank()) {
            return@withContext Result.failure()
        }

        Log.d(TAG, "Starting OCR background worker for bookId=$bookId, pdfPath=$pdfPath")

        try {
            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
            val chapterDao = db.chapterDao()
            val bookDao = db.bookDao()

            val book = bookDao.getBookByIdDirect(bookId)
            if (book == null) {
                Log.w(TAG, "Book not found for bookId=$bookId")
                return@withContext Result.failure()
            }

            val chapters = chapterDao.getChaptersForBookDirect(bookId)

            if (chapters.isEmpty()) {
                Log.w(TAG, "No chapters found for bookId=$bookId")
                return@withContext Result.failure()
            }

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val totalChaptersCount = chapters.size

            for ((index, chapter) in chapters.withIndex()) {
                if (isStopped) {
                    Log.d(TAG, "OCR worker stopped by WorkManager")
                    break
                }

                // Skip if page already has text saved
                if (chapter.content.isNotBlank()) {
                    setProgress(workDataOf(KEY_PROGRESS to index + 1, KEY_TOTAL to totalChaptersCount, KEY_BOOK_ID to bookId))
                    continue
                }

                val startPage = chapter.pdfPageStart ?: 0
                val endPage = chapters.getOrNull(index + 1)?.pdfPageStart ?: book.pageCount
                val contentBuilder = StringBuilder()

                for (p in startPage until endPage) {
                    val bitmap = PdfHelper.renderPageBitmap(pdfPath, p, targetWidth = 1200)

                    if (bitmap != null) {
                        try {
                            val inputImage = InputImage.fromBitmap(bitmap, 0)
                            val text = recognizeText(recognizer, inputImage)

                            if (text.isNotBlank()) {
                                contentBuilder.append(text).append("\n\n")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error performing OCR on page $p: ${e.message}")
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }

                val sectionText = contentBuilder.toString().trim()
                if (sectionText.isNotBlank()) {
                    chapterDao.updateChapterContent(chapter.id, sectionText)
                    Log.d(TAG, "OCR completed for bookId=$bookId chapter ${chapter.number} (pages $startPage to $endPage, ${sectionText.length} chars)")
                }

                setProgress(workDataOf(KEY_PROGRESS to index + 1, KEY_TOTAL to totalChaptersCount, KEY_BOOK_ID to bookId))
            }

            Log.d(TAG, "OCR worker completed successfully for bookId=$bookId")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "OCR worker failed for bookId=$bookId: ${e.message}", e)
            Result.failure()
        }
    }

    private suspend fun recognizeText(
        recognizer: com.google.mlkit.vision.text.TextRecognizer,
        image: InputImage
    ): String = suspendCancellableCoroutine { continuation ->
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text.trim())
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "ML Kit text recognition failed on page image: ${e.message}")
                continuation.resume("")
            }
    }

    companion object {
        private const val TAG = "PdfOcrWorker"
        const val KEY_BOOK_ID = "key_book_id"
        const val KEY_PDF_PATH = "key_pdf_path"
        const val KEY_PROGRESS = "key_progress"
        const val KEY_TOTAL = "key_total"
    }
}

object OcrManager {
    fun enqueueOcrJob(context: Context, bookId: Long, pdfPath: String) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val inputData = workDataOf(
            PdfOcrWorker.KEY_BOOK_ID to bookId,
            PdfOcrWorker.KEY_PDF_PATH to pdfPath
        )

        val workRequest = OneTimeWorkRequestBuilder<PdfOcrWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            "ocr_book_$bookId",
            ExistingWorkPolicy.KEEP,
            workRequest
        )
    }
}
