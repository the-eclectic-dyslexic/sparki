/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.background.delegates

import android.content.Context

/**
 * Interface used primarily in the main receiver, for the purpose of abstracting
 * any task related to performing an action upon battery target being reached
 */

interface ChargeTargetReachedDelegate {
    /**
     * Will be called upon charge target being reached
     *
     * An implementer should check that the user actually wants this delegate running,
     * by checking user settings
     *
     * This function should not throw any exceptions
     */
    fun delegate(context: Context)

    /**
     * Will be called upon power being disconnected, or user input dismissing any actions
     * related to charge target being reached
     *
     * This function should cancel any routines/alarms/receivers/etc set in motion by "delegate"
     *
     * This function should be able to be called without problem regardless of whether "delegate"
     * has been called previously
     *
     * This function should not throw any exceptions
     */
    fun cancel(context: Context)
}