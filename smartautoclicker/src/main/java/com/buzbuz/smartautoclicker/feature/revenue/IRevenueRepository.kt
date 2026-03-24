/*
 * Simplified no-op stub for personal build.
 * Original: feature/revenue module
 */
package com.buzbuz.smartautoclicker.feature.revenue

import android.app.Activity
import android.content.Context

import com.buzbuz.smartautoclicker.core.base.Dumpable
import com.buzbuz.smartautoclicker.core.base.addDumpTabulationLvl
import com.buzbuz.smartautoclicker.core.base.dumpWithTimeout

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

import java.io.PrintWriter


interface IRevenueRepository : Dumpable {

    val userConsentState: Flow<UserConsentState>
    val isPrivacySettingRequired: Flow<Boolean>

    val userBillingState: StateFlow<UserBillingState>
    val isBillingFlowInProgress: Flow<Boolean>

    fun startUserConsentRequestUiFlowIfNeeded(activity: Activity)
    fun startPrivacySettingUiFlow(activity: Activity)

    fun loadAdIfNeeded(context: Context)
    fun startPaywallUiFlow(context: Context)

    fun refreshPurchases()
    fun startPurchaseUiFlow(context: Context)

    fun consumeTrial(): Duration?

    override fun dump(writer: PrintWriter, prefix: CharSequence) {
        val contentPrefix = prefix.addDumpTabulationLvl()

        writer.apply {
            append(prefix).println("* RevenueRepository:")
            append(contentPrefix)
                .append("- userBillingState=${userBillingState.dumpWithTimeout()}; ")
                .append("isPrivacySettingRequired=${isPrivacySettingRequired.dumpWithTimeout()}; ")
                .println()
        }
    }
}
