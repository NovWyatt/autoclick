/*
 * No-op ReviewRepository implementation for personal build.
 */
package com.buzbuz.smartautoclicker.feature.review

import android.content.Context
import android.content.Intent

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor() : ReviewRepository {
    override fun isUserCandidateForReview(): Boolean = false
    override fun getReviewActivityIntent(context: Context): Intent? = null
    override fun onUserSessionStarted(): Unit = Unit
    override fun onUserSessionStopped(): Unit = Unit
}
