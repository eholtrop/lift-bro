package com.lift.bro.domain.models

data class User(
    val subscriptionType: SubscriptionType,
)

enum class SubscriptionType {
    None, Pro
}
