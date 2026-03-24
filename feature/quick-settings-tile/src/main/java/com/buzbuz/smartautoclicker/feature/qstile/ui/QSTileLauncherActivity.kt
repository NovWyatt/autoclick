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
package com.buzbuz.smartautoclicker.feature.qstile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

import com.buzbuz.smartautoclicker.feature.qstile.R

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QSTileLauncherActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_SCENARIO_ID =
            "com.buzbuz.smartautoclicker.feature.qstile.ui.EXTRA_SCENARIO_ID"

        fun getStartIntent(context: Context, scenarioId: Long, isSmartScenario: Boolean): Intent =
            Intent(context, QSTileLauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(EXTRA_SCENARIO_ID, scenarioId)
    }

    private val viewModel: QSTileLauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qstile_launcher)

        val scenarioId = intent?.getLongExtra(EXTRA_SCENARIO_ID, -1)
        if (scenarioId == null || scenarioId == -1L) {
            Log.e(TAG, "Invalid start parameter, finish activity")
            finish()
            return
        }

        Log.i(TAG, "Start dumb scenario from tile...")
        onCreateDumbScenarioLauncher(scenarioId)
    }

    private fun onCreateDumbScenarioLauncher(scenarioId: Long) {
        viewModel.startPermissionFlowIfNeeded(
            activity = this,
            onMandatoryDenied = ::finish,
            onAllGranted = {
                Log.i(TAG, "All permissions are granted, start scenario")
                viewModel.startDumbScenario(scenarioId)
                finish()
            }
        )
    }
}

private const val TAG = "QSTileLauncherActivity"