/*
 * Copyright (C) 2025 Kevin Buzeau
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
package com.buzbuz.smartautoclicker.scenarios.list

import android.content.Context
import com.buzbuz.smartautoclicker.R
import com.buzbuz.smartautoclicker.core.dumb.domain.IDumbRepository
import com.buzbuz.smartautoclicker.core.dumb.domain.model.DumbAction
import com.buzbuz.smartautoclicker.core.dumb.domain.model.DumbScenario
import com.buzbuz.smartautoclicker.core.dumb.domain.model.Repeatable
import com.buzbuz.smartautoclicker.core.settings.SettingsRepository
import com.buzbuz.smartautoclicker.core.ui.utils.formatDuration
import com.buzbuz.smartautoclicker.scenarios.list.model.ScenarioListUiState
import com.buzbuz.smartautoclicker.scenarios.list.sort.ScenarioSortConfig
import com.buzbuz.smartautoclicker.scenarios.list.sort.ScenarioSortConfigRepository
import com.buzbuz.smartautoclicker.scenarios.list.sort.ScenarioSortType

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class FilteredScenarioListUseCase @Inject constructor(
    @ApplicationContext context: Context,
    dumbRepository: IDumbRepository,
    sortConfigRepository: ScenarioSortConfigRepository,
    settingsRepository: SettingsRepository,
) {

    /** The currently searched action name. Null if no is. */
    private val searchQuery = MutableStateFlow<String?>(null)

    private val refresh: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)

    /** Dumb scenarios only. */
    private val allScenarios: Flow<List<ScenarioListUiState.Item.ScenarioItem>> =
        combine(refresh, dumbRepository.dumbScenarios) { _, dumbList ->
            dumbList.map { it.toItem(context) }
        }

    /** Flow upon the list of Dumb scenarios, filtered with the search query and ordered with the sort config */
    val orderedItems: Flow<List<ScenarioListUiState.Item>> =
        combine(
            allScenarios,
            searchQuery,
            sortConfigRepository.getSortConfig(),
            settingsRepository.isFilterScenarioUiEnabledFlow,
        ) { scenarios, searchQuery, sortConfig, filtersEnabled ->
            if (searchQuery == null) {
                if (filtersEnabled) {
                    val filteredAndSortedItems = scenarios.sortAndFilter(sortConfig)
                    val sortItem = ScenarioListUiState.Item.SortItem(
                        sortType = sortConfig.type,
                        dumbVisible = sortConfig.showDumbScenario,
                        changeOrderChecked = sortConfig.inverted,
                    )

                    buildList {
                        if (scenarios.isNotEmpty()) add(sortItem)
                        addAll(filteredAndSortedItems)
                    }
                } else {
                    scenarios
                }
            } else {
                scenarios.filterByName(searchQuery)
            }
        }

    init {
        refresh.tryEmit(Unit)
    }

    fun updateSearchQuery(query: String?) {
        searchQuery.value = query
    }

    suspend fun refresh() {
        refresh.emit(Unit)
    }

    private fun DumbScenario.toItem(context: Context): ScenarioListUiState.Item.ScenarioItem =
        if (dumbActions.isEmpty()) ScenarioListUiState.Item.ScenarioItem.Empty.Dumb(
            scenario = this,
            lastStartTimestamp = stats?.lastStartTimestampMs ?: 0,
            startCount = stats?.startCount ?: 0
        )
        else ScenarioListUiState.Item.ScenarioItem.Valid.Dumb(
            scenario = this,
            clickCount = dumbActions.count { it is DumbAction.DumbClick },
            swipeCount = dumbActions.count { it is DumbAction.DumbSwipe },
            pauseCount = dumbActions.count { it is DumbAction.DumbPause },
            repeatText = getRepeatDisplayText(context),
            maxDurationText = getMaxDurationDisplayText(context),
            lastStartTimestamp = stats?.lastStartTimestampMs ?: 0,
            startCount = stats?.startCount ?: 0
        )
}

private fun List<ScenarioListUiState.Item.ScenarioItem>.filterByName(
    filter: String
): List<ScenarioListUiState.Item.ScenarioItem> =
    mapNotNull { scenario ->
        if (scenario.displayName.contains(filter, true)) scenario else null
    }

private fun Repeatable.getRepeatDisplayText(context: Context): String =
    if (isRepeatInfinite) context.getString(R.string.item_desc_dumb_scenario_repeat_infinite)
    else context.getString(R.string.item_desc_dumb_scenario_repeat_count, repeatCount)

private fun DumbScenario.getMaxDurationDisplayText(context: Context): String =
    if (isDurationInfinite) context.getString(R.string.item_desc_dumb_scenario_max_duration_infinite)
    else context.getString(
        R.string.item_desc_dumb_scenario_max_duration,
        formatDuration(maxDurationMin.minutes.inWholeMilliseconds),
    )

private fun Collection<ScenarioListUiState.Item.ScenarioItem>.sortAndFilter(
    sortConfig: ScenarioSortConfig,
): Collection<ScenarioListUiState.Item.ScenarioItem> {

    return when (sortConfig.type) {
        ScenarioSortType.NAME ->
            if (sortConfig.inverted) sortedByDescending { it.displayName }
            else sortedBy { it.displayName }

        ScenarioSortType.RECENT ->
            if (sortConfig.inverted) sortedBy { it.lastStartTimestamp }
            else sortedByDescending { it.lastStartTimestamp }

        ScenarioSortType.MOST_USED ->
            if (sortConfig.inverted) sortedBy { it.startCount }
            else sortedByDescending { it.startCount }
    }
}
