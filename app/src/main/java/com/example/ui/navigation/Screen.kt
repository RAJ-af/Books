package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object CategoryGrid : Screen("category/{genre}") {
        fun createRoute(genre: String) = "category/$genre"
    }
    object Search : Screen("search")
    object Discover : Screen("discover")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    object BookmarksAndHighlights : Screen("bookmarks_highlights/{bookId}") {
        fun createRoute(bookId: Long) = "bookmarks_highlights/$bookId"
    }
    object Reader : Screen("reader/{bookId}/{chapterId}?page={page}&paragraph={paragraph}") {
        fun createRoute(bookId: Long, chapterId: Long, page: Int = 0, paragraph: Int = 0) =
            "reader/$bookId/$chapterId?page=$page&paragraph=$paragraph"
    }
}
