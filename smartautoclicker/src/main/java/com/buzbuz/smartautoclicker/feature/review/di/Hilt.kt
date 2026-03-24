/*
 * Hilt DI module for no-op ReviewRepository.
 */
package com.buzbuz.smartautoclicker.feature.review.di

import com.buzbuz.smartautoclicker.feature.review.ReviewRepository
import com.buzbuz.smartautoclicker.feature.review.ReviewRepositoryImpl

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReviewHiltModule {

    @Provides
    @Singleton
    fun providesReviewRepository(reviewRepository: ReviewRepositoryImpl): ReviewRepository =
        reviewRepository
}
