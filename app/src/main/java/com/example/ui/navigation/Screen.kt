package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object CategoryGrid : Screen("category/{genre}") {
        fun createRoute(genre: String) = "category/$genre"
    }
    object Search : Screen("search")
    object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: Long) = "book_detail/$bookId"
    }
    object Reader : Screen("reader/{bookId}/{chapterId}") {
        fun createRoute(bookId: Long, chapterId: Long) = "reader/$bookId/$chapterId"
    }
}
