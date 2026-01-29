package tv.dpal.compose

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp


/*
 * Modifies a CornerBasedShape based on the index of a list
 *
 * if the index is the first index of the list it will round the top corners
 * if the index is the last index of the list it will round the bottom corners
 * otherwise it will not round any corners
 *
 * This allows you to create long lists of carded items!
 */
@Composable
fun CornerBasedShape.listCorners(
    index: Int,
    items: List<Any>,
) = this.copy(
    topStart = if (index == 0) this.topStart else CornerSize(0.dp),
    topEnd = if (index == 0) this.topEnd else CornerSize(0.dp),
    bottomEnd = if (index == items.lastIndex) this.bottomEnd else CornerSize(0.dp),
    bottomStart = if (index == items.lastIndex) this.bottomStart else CornerSize(0.dp),
)
