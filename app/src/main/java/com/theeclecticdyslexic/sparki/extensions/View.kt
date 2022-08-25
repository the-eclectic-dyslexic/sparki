/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.extensions

import android.view.View

fun View.show(shouldShow: Boolean) {
    visibility =
        if (shouldShow) View.VISIBLE
        else            View.GONE
}
