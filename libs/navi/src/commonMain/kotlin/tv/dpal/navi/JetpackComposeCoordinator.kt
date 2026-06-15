package tv.dpal.navi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class NavCoordinatorSaveable<T>(
    val pages: List<T>,
    val currentPage: T,
)

@Composable
inline fun <reified T> rememberNavCoordinator(
    initialDestination: T,
): NavCoordinator<T> = rememberSaveable(
    saver = object: Saver<NavCoordinator<T>, String> {
        override fun SaverScope.save(value: NavCoordinator<T>): String {
            return Json.encodeToString(
                NavCoordinatorSaveable(
                    pages = value.pages,
                    currentPage = value.currentPage,
                )
            )
        }

        override fun restore(value: String): NavCoordinator<T> {
            return with(Json.decodeFromString<NavCoordinatorSaveable<T>>(value)) {
                JetpackComposeCoordinator(
                    initialState = pages.toTypedArray(),
                    currentPage = currentPage
                )
            }
        }
    },
    init = {
        JetpackComposeCoordinator(initialDestination)
    }
)

class JetpackComposeCoordinator<T>(
    vararg initialState: T,
    currentPage: T = initialState.last()
): NavCoordinator<T> {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableStateList = MutableStateFlow(mutableStateListOf(elements = initialState))
    private val currentState = MutableStateFlow(currentPage)
    private val currentIndex get() = pages.indexOf(currentPage)

    override val pages: List<T>
        get() = mutableStateList.value
    override val pagesAsFlow: StateFlow<List<T>>
        get() = mutableStateList.asStateFlow()
    override val currentPage:T
        get() = currentState.value
    override val currentPageAsFlow: StateFlow<T>
        get() = currentState.asStateFlow()
    override val currentPageIndex: Int
        get() = currentIndex

    override fun onBackPressed(keepStack: Boolean): Boolean {
        return if (currentIndex == 0) {
            false
        } else {
            currentState.value = pages[pages.indexOf(currentPage) - 1]
            if (!keepStack) {
                scope.launch {
                    mutableStateList.value.removeAt(mutableStateList.value.lastIndex)
                }
            }
            true
        }
    }

    override fun popToRoot(keepStack: Boolean): Boolean {
        currentState.value = pages.first()
        if (!keepStack) {
            mutableStateList.value.clear()
            mutableStateList.value.add(currentState.value)
        }
        return true
    }

    override fun setRoot(state: T) {
        mutableStateList.value.clear()
        mutableStateList.value.add(state)
        currentState.value = state
    }

    override fun present(state: T, animate: Boolean) {
        // if we are not the past page, remove all "future" state and then continue
        if (currentIndex < mutableStateList.value.size - 1) {
            mutableStateList.value.removeRange(currentIndex + 1, mutableStateList.value.size)
        }
        mutableStateList.value.add(state)
        navigateTo(state)
    }

    override fun navigateTo(state: T) {
        currentState.value = state
    }

    override fun updateCurrentIndex(index: Int) {
        currentState.value = mutableStateList.value[index]
    }
}
