package tv.dpal.navi

import kotlinx.coroutines.flow.StateFlow

interface NavCoordinator {

    val pages: List<Destination>

    val pagesAsFlow: StateFlow<List<Destination>>

    val currentPage: Destination

    val currentPageIndex: Int

    val currentPageAsFlow: StateFlow<Destination>

    /**
     * Appends the state to the current set of pages and navigates the user to that page
     */
    fun present(state: Destination, animate: Boolean = true)

    /**
     * Navigates the user to the state provided. no-op if the state is not within the current set of pages
     */
    fun navigateTo(state: Destination)

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
    fun setRoot(state: Destination)
}
