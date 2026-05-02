package com.lift.bro

import androidx.compose.runtime.MutableState
import com.lift.bro.config.BuildConfig
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.domain.models.SubscriptionType
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfo
import com.revenuecat.purchases.kmp.models.EntitlementInfos
import com.revenuecat.purchases.kmp.models.OwnershipType
import com.revenuecat.purchases.kmp.models.PeriodType
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.Store
import com.revenuecat.purchases.kmp.models.VerificationResult
import io.sentry.kotlin.multiplatform.Sentry
import kotlin.time.Clock

object AppPurchases {

    val appUserID: String
        get() = if (BuildConfig.isDebug || !Purchases.isConfigured) {
            "debug_user"
        } else {
            Purchases.sharedInstance.appUserID
        }

    suspend fun isUserPro(): Boolean =
        if (BuildConfig.isDebug) {
            true
        } else if (Purchases.isConfigured) {
            Purchases.sharedInstance.awaitCustomerInfo().entitlements.active.contains("pro")
        } else {
            false
        }

    fun getCustomerInfo(
        onError: (PurchasesError) -> Unit = {},
        onSuccess: (CustomerInfo) -> Unit,
    ) {
        if (BuildConfig.isDebug) {
            onSuccess(mockCustomerInfo())
        } else if (Purchases.isConfigured) {
            Purchases.sharedInstance.getCustomerInfo(onError = onError, onSuccess = onSuccess)
        }
    }

    fun instantiate(isAndroid: Boolean, subscriptionType: MutableState<SubscriptionType>) {
        val apiKey = if (isAndroid) BuildKonfig.REVENUE_CAT_API_KEY_AND else BuildKonfig.REVENUE_CAT_API_KEY_IOS
        if (apiKey == "") return
        Purchases.configure(apiKey)
        getCustomerInfo(
            onError = { error ->
                Sentry.captureException(Throwable(message = error.message))
            },
            onSuccess = { success ->
                if (success.entitlements.active.containsKey("pro")) {
                    subscriptionType.value = SubscriptionType.Pro
                }
            }
        )
    }

    private fun mockCustomerInfo(): CustomerInfo {
        val now = Clock.System.now()
        return CustomerInfo(
            entitlements = EntitlementInfos(
                all = mapOf("pro" to dummyEntitlement("pro")),
                verification = VerificationResult.VERIFIED,
            ),
            activeSubscriptions = emptySet(),
            nonSubscriptionTransactions = emptyList(),
            originalAppUserId = "debug_user",
            allExpirationDateMillis = emptyMap(),
            allPurchaseDateMillis = emptyMap(),
            allPurchasedProductIdentifiers = emptySet(),
            firstSeenMillis = now.toEpochMilliseconds(),
            latestExpirationDateMillis = null,
            managementUrlString = null,
            originalApplicationVersion = "1.0",
            originalPurchaseDateMillis = null,
            requestDateMillis = now.toEpochMilliseconds(),
        )
    }

    private fun dummyEntitlement(id: String) = EntitlementInfo(
        identifier = id,
        isActive = true,
        willRenew = true,
        periodType = PeriodType.NORMAL,
        latestPurchaseDateMillis = null,
        originalPurchaseDateMillis = null,
        expirationDateMillis = null,
        store = Store.UNKNOWN_STORE,
        productIdentifier = "debug_product",
        productPlanIdentifier = "debug_plan",
        isSandbox = true,
        unsubscribeDetectedAtMillis = null,
        billingIssueDetectedAtMillis = null,
        ownershipType = OwnershipType.PURCHASED,
        verification = VerificationResult.VERIFIED,
    )
}
