import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.Coordinator

@Composable
fun App(
    coordinator: Coordinator = dependencies.coordinator,
) {
    AppTheme() {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            coordinator.render()
        }
    }
}

object Spacing {
    val two = 32.dp
    val oneAndHalf = 24.dp
    val one = 16.dp
    val half = 8.dp
    val quarter = 4.dp
}

object LiftingTheme {
    val spacing: Spacing = Spacing
}

val MaterialTheme.spacing get() = LiftingTheme.spacing
