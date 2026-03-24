/*
 * No-op RevenueRepository for personal build.
 * Always returns PURCHASED state (no ads, no billing).
 */
package com.buzbuz.smartautoclicker.feature.revenue

import android.app.Activity
import android.content.Context

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

import javax.inject.Inject
import kotlin.time.Duration


class RevenueRepository @Inject constructor() : IRevenueRepository {
    override val userConsentState: Flow<UserConsentState> = flowOf(UserConsentState.CANNOT_REQUEST_ADS)
    override val isPrivacySettingRequired: Flow<Boolean> = flowOf(false)
    override val userBillingState: StateFlow<UserBillingState> = MutableStateFlow(UserBillingState.PURCHASED)
    override val isBillingFlowInProgress: Flow<Boolean> = flowOf(false)

    override fun startUserConsentRequestUiFlowIfNeeded(activity: Activity) = Unit
    override fun startPrivacySettingUiFlow(activity: Activity) = Unit
    override fun loadAdIfNeeded(context: Context) = Unit
    override fun startPaywallUiFlow(context: Context) = Unit
    override fun refreshPurchases() = Unit
    override fun startPurchaseUiFlow(context: Context) = Unit
    override fun consumeTrial(): Duration? = null
}
