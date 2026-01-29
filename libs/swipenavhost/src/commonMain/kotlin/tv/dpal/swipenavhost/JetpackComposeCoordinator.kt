package tv.dpal.swipenavhost

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
data class NavCoordinatorSaveable(
    val pages: List<Destination>,
    val currentPage: Destination,
)

@Composable
fun <T: Destination> rememberNavCoordinator(
    initialDestination: T,
): NavCoordinator = rememberSaveable(
    saver = object: Saver<NavCoordinator, String> {
        override fun SaverScope.save(value: NavCoordinator): String {
            return Json.encodeToString(
                NavCoordinatorSaveable(
                    pages = value.pages,
                    currentPage = value.currentPage,
                )
            )
        }

        override fun restore(value: String): NavCoordinator {
            return with(Json.decodeFromString<NavCoordinatorSaveable>(value)) {
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

class JetpackComposeCoordinator(
    vararg initialState: Destination,
    currentPage: Destination = initialState.last()
): NavCoordinator {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val mutableStateList = MutableStateFlow(mutableStateListOf(elements = initialState))
    private val currentState = MutableStateFlow(currentPage)

    override val pages: List<Destination>
        get() = mutableStateList.value
    override val pagesAsFlow: StateFlow<List<Destination>>
        get() = mutableStateList.asStateFlow()

    override val currentPage: Destination
        get() = currentState.value
    override val currentPageAsFlow: StateFlow<Destination>
        get() = currentState.asStateFlow()

    override val currentPageIndex: Int
        get() = currentIndex

    private val currentIndex get() = pages.indexOf(currentPage)

    override fun onBackPressed(keepStack: Boolean): Boolean {
        return if (currentIndex == 0) {
            false
        } else {
            currentState.value = pages.get(pages.indexOf(currentPage) - 1)
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

    override fun setRoot(state: Destination) {
        mutableStateList.value.clear()
        mutableStateList.value.add(state)
        currentState.value = state
    }

    override fun present(state: Destination, animate: Boolean) {
        // if we are not the past page, remove all "future" state and then continue
        if (currentIndex < mutableStateList.value.size - 1) {
            mutableStateList.value.removeRange(currentIndex + 1, mutableStateList.value.size)
        }
        mutableStateList.value.add(state)
        navigateTo(state)
    }

    override fun navigateTo(state: Destination) {
        currentState.value = state
    }

    override fun updateCurrentIndex(index: Int) {
        currentState.value = mutableStateList.value[index]
    }
}
