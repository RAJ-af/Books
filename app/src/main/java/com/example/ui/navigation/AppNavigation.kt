package com.example.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.category.CategoryGridScreen
import com.example.ui.detail.BookDetailScreen
import com.example.ui.detail.BookDetailViewModel
import com.example.ui.discover.DiscoverScreen
import com.example.ui.discover.DiscoverViewModel
import com.example.ui.library.LibraryScreen
import com.example.ui.library.LibraryViewModel
import com.example.ui.reader.ReaderScreen
import com.example.ui.reader.ReaderViewModel
import com.example.ui.search.SearchScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    libraryViewModel: LibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    NavHost(
        navController = navController,
        startDestination = Screen.Library.route
    ) {
        // 1. Library / Home Screen
        composable(Screen.Library.route) {
            LibraryScreen(
                viewModel = libraryViewModel,
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                },
                onCategoryClick = { genre ->
                    navController.navigate(Screen.CategoryGrid.createRoute(genre))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                onDiscoverClick = {
                    navController.navigate(Screen.Discover.route)
                }
            )
        }

        // 2. Discover / Internet Archive Screen
        composable(Screen.Discover.route) {
            val discoverViewModel: DiscoverViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DiscoverViewModel(application) as T
                    }
                }
            )
            DiscoverScreen(
                viewModel = discoverViewModel,
                onBackClick = { navController.popBackStack() },
                onBookImported = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId)) {
                        popUpTo(Screen.Library.route)
                    }
                }
            )
        }

        // 2. Category Grid Screen
        composable(
            route = Screen.CategoryGrid.route,
            arguments = listOf(
                navArgument("genre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val genre = backStackEntry.arguments?.getString("genre") ?: "Design"
            CategoryGridScreen(
                genre = genre,
                viewModel = libraryViewModel,
                onBackClick = { navController.popBackStack() },
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                }
            )
        }

        // 3. Search Screen
        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = libraryViewModel,
                onBackClick = { navController.popBackStack() },
                onBookClick = { bookId ->
                    navController.navigate(Screen.BookDetail.createRoute(bookId))
                }
            )
        }

        // 4. Book Detail Screen
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 1L
            val detailViewModel: BookDetailViewModel = viewModel(
                key = "book_detail_$bookId",
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return BookDetailViewModel(application, bookId) as T
                    }
                }
            )
            BookDetailScreen(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() },
                onReadBookClick = { bId, chapterId ->
                    navController.navigate(Screen.Reader.createRoute(bId, chapterId))
                }
            )
        }

        // 5. Reader Screen
        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.LongType },
                navArgument("chapterId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 1L
            val chapterId = backStackEntry.arguments?.getLong("chapterId") ?: 1L
            val readerViewModel: ReaderViewModel = viewModel(
                key = "reader_${bookId}_$chapterId",
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ReaderViewModel(application, bookId, chapterId) as T
                    }
                }
            )
            ReaderScreen(
                viewModel = readerViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
