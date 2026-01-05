package com.lift.bro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun rememberNavCoordinator(
    initialDestination: Destination
): NavCoordinator = remember { JetpackComposeCoordinator(initialState = initialDestination) }

class JetpackComposeCoordinator(
    initialState: Destination,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : NavCoordinator {

    private val mutableStateList = MutableStateFlow(mutableStateListOf(initialState))
    private val currentState = MutableStateFlow(initialState)

    override val pages: List<Destination>
        get() = mutableStateList.value
    override val pagesAsFlow: StateFlow<List<Destination>>
        get() = mutableStateList.asStateFlow()

    override val currentPage: Destination
        get() = currentState.value
    override val currentPageAsFlow: StateFlow<Destination>
        get() = currentState.asStateFlow()

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
