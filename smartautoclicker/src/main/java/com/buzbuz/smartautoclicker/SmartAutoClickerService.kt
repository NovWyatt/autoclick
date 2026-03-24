/*
 * Copyright (C) 2024 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

import com.buzbuz.smartautoclicker.core.base.Dumpable
import com.buzbuz.smartautoclicker.core.base.data.AppComponentsProvider
import com.buzbuz.smartautoclicker.core.base.extensions.requestFilterKeyEvents
import com.buzbuz.smartautoclicker.core.base.notifications.NotificationIds
import com.buzbuz.smartautoclicker.core.common.actions.AndroidActionExecutor
import com.buzbuz.smartautoclicker.core.common.overlays.manager.OverlayManager
import com.buzbuz.smartautoclicker.core.common.quality.domain.QualityMetricsMonitor
import com.buzbuz.smartautoclicker.core.common.quality.domain.QualityRepository
import com.buzbuz.smartautoclicker.core.display.config.DisplayConfigManager
import com.buzbuz.smartautoclicker.core.dumb.domain.model.DumbScenario
import com.buzbuz.smartautoclicker.core.dumb.engine.DumbEngine
import com.buzbuz.smartautoclicker.core.settings.SettingsRepository
import com.buzbuz.smartautoclicker.feature.qstile.domain.QSTileActionHandler
import com.buzbuz.smartautoclicker.feature.qstile.domain.QSTileRepository
import com.buzbuz.smartautoclicker.localservice.LocalService
import com.buzbuz.smartautoclicker.localservice.LocalServiceProvider

import dagger.hilt.android.AndroidEntryPoint
import java.io.FileDescriptor
import java.io.PrintWriter
import javax.inject.Inject

/**
 * AccessibilityService implementation for the SmartAutoClicker.
 *
 * Started automatically by Android once the user has defined this service has an accessibility service, it provides
 * an API to start and stop the DumbEngine correctly in order to display the overlay UI and execute auto-click actions.
 * This API is offered through the [LocalService] class, which is instantiated in the [LocalServiceProvider] object.
 */
@AndroidEntryPoint
class SmartAutoClickerService : AccessibilityService() {

    private val localServiceProvider = LocalServiceProvider

    private val localService: LocalService?
        get() = localServiceProvider.localServiceInstance as? LocalService

    @Inject lateinit var overlayManager: OverlayManager
    @Inject lateinit var displayConfigManager: DisplayConfigManager
    @Inject lateinit var dumbEngine: DumbEngine
    @Inject lateinit var qualityRepository: QualityRepository
    @Inject lateinit var qualityMetricsMonitor: QualityMetricsMonitor
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var tileRepository: QSTileRepository
    @Inject lateinit var appComponentsProvider: AppComponentsProvider
    @Inject lateinit var actionExecutor: AndroidActionExecutor

    override fun onServiceConnected() {
        super.onServiceConnected()

        qualityMetricsMonitor.onServiceConnected()
        actionExecutor.init(this)

        tileRepository.setTileActionHandler(
            object : QSTileActionHandler {
                override fun isRunning(): Boolean = localServiceProvider.isServiceStarted()
                override fun startDumbScenario(dumbScenario: DumbScenario) {
                    localServiceProvider.localServiceInstance?.startDumbScenario(dumbScenario)
                }
                override fun stop() {
                    localServiceProvider.localServiceInstance?.stop()
                }
            }
        )

        localServiceProvider.setLocalService(
            LocalService(
                context = this,
                overlayManager = overlayManager,
                appComponentsProvider = appComponentsProvider,
                dumbEngine = dumbEngine,
                settingsRepository = settingsRepository,
                onStart = ::onLocalServiceStarted,
                onStop = ::onLocalServiceStopped,
            )
        )
    }

    override fun onUnbind(intent: Intent?): Boolean {
        localServiceProvider.localServiceInstance?.apply {
            stop()
            release()
        }
        localServiceProvider.setLocalService(null)

        qualityMetricsMonitor.onServiceUnbind()
        actionExecutor.clear()
        return super.onUnbind(intent)
    }

    private fun onLocalServiceStarted(scenarioId: Long, isSmart: Boolean, serviceNotification: android.app.Notification?) {
        qualityMetricsMonitor.onServiceForegroundStart()

        serviceNotification?.let {
            startForeground(NotificationIds.FOREGROUND_SERVICE_NOTIFICATION_ID, it)
        }
        requestFilterKeyEvents(true)

        displayConfigManager.startMonitoring(this)
        tileRepository.setTileScenario(scenarioId = scenarioId, isSmart = isSmart)
    }

    private fun onLocalServiceStopped() {
        qualityMetricsMonitor.onServiceForegroundEnd()
        actionExecutor.resetState()

        requestFilterKeyEvents(false)
        stopForeground(STOP_FOREGROUND_REMOVE)

        displayConfigManager.stopMonitoring()
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean =
        localService?.onKeyEvent(event) ?: super.onKeyEvent(event)

    /**
     * Dump the state of the service via adb.
     * adb shell "dumpsys activity service com.buzbuz.smartautoclicker"
     */
    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        if (writer == null) return

        writer.append("* SmartAutoClickerService:").println()
        writer.append(Dumpable.DUMP_DISPLAY_TAB)
            .append("- isStarted=${localService?.started ?: false}; ")
            .println()

        displayConfigManager.dump(writer)
        overlayManager.dump(writer)
        dumbEngine.dump(writer)
        actionExecutor.dump(writer)
        qualityRepository.dump(writer)
    }

    override fun onInterrupt() { /* Unused */ }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Unused */ }
}

/** Tag for the logs. */
private const val TAG = "SmartAutoClickerService"