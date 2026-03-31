package com.lift.bro.domain.filter

import com.benasher44.uuid.uuid4

object DefaultFilters {

    val OneRepMaxes = Filter(
        id = uuid4().toString(),
        name = "One Rep Maxes",
        conditions = listOf(
            Condition.Equals(Field.Reps, 1)
        )
    )

    val Favourites = Filter(
        id = uuid4().toString(),
        name = "Only Favs",
        description = "Any movements you have favourited!",
        conditions = listOf(
            Condition.Equals(Field.Favourite, true),
        )
    )

    val BodyWeight = Filter(
        id = uuid4().toString(),
        name = "Body Weight",
        description = "Only Bodies to be found here",
        conditions = listOf(
            Condition.Equals(Field.BodyWeight, true),
        )
    )
}
