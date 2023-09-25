import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.lift.bro.di.DependencyContainer
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.Coordinator
import com.lift.bro.presentation.home.HomeScreen

@Composable
fun App(
    coordinator: Coordinator = dependencies.coordinator,
) {
    AppTheme() {
        coordinator.render()
    }
}

object Spacing {
    val two = 32.dp
    val oneAndHalf = 24.dp
    val one = 16.dp
    val half = 8.dp
    val quarter = 4.dp
}

object BudgeyTheme {
    val spacing: Spacing = Spacing
}

val MaterialTheme.spacing get() = BudgeyTheme.spacing

@Composable
fun LiftingTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }
    val typography = Typography()
    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}