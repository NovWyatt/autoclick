/*
 * No-op ReviewRepository for personal build.
 */
package com.buzbuz.smartautoclicker.feature.review

import android.content.Context
import android.content.Intent

import com.buzbuz.smartautoclicker.core.base.Dumpable
import com.buzbuz.smartautoclicker.core.base.addDumpTabulationLvl

import java.io.PrintWriter


interface ReviewRepository : Dumpable {

    fun isUserCandidateForReview(): Boolean
    fun getReviewActivityIntent(context: Context): Intent?

    fun onUserSessionStarted()
    fun onUserSessionStopped()

    override fun dump(writer: PrintWriter, prefix: CharSequence) {
        val contentPrefix = prefix.addDumpTabulationLvl()

        writer.apply {
            append(prefix).println("* ReviewRepository:")
            append(contentPrefix)
                .append("- isUserCandidateForReview=${isUserCandidateForReview()}; ")
                .println()
        }
    }
}
