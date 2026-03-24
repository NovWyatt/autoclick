/*
 * Hilt DI module for no-op RevenueRepository.
 */
package com.buzbuz.smartautoclicker.feature.revenue.di

import com.buzbuz.smartautoclicker.feature.revenue.IRevenueRepository
import com.buzbuz.smartautoclicker.feature.revenue.RevenueRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingHiltModule {

    @Provides
    @Singleton
    fun providesRevenueRepository(revenueRepository: RevenueRepository): IRevenueRepository =
        revenueRepository
}
