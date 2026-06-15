package tv.dpal.navi

import kotlinx.coroutines.flow.StateFlow

interface NavCoordinator<T> {

    val pages: List<T>

    val pagesAsFlow: StateFlow<List<T>>

    val currentPage:T

    val currentPageIndex: Int

    val currentPageAsFlow: StateFlow<T>

    /**
     * Appends the state to the current set of pages and navigates the user to that page
     */
    fun present(state: T, animate: Boolean = true)

    /**
     * Navigates the user to the state provided. no-op if the state is not within the current set of pages
     */
    fun navigateTo(state: T)

    fun updateCurrentIndex(index: Int)

    /**
     * Navigates the user back one page,
     * returns true if the navigation was handled,
     * false if nothing happens (ie. you need to handle it yourself)
     */
    fun onBackPressed(keepStack: Boolean = true): Boolean

    /**
     * Pops the user to the root of the application (ie. the first page of the app)
     */
    fun popToRoot(keepStack: Boolean = true): Boolean

    /**
     * Clears the current set of pages and shows the user the given state
     */
    fun setRoot(state: T)
}
