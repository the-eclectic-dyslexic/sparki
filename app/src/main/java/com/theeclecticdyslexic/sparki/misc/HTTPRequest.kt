/**
 * This code is licensed under GPLv3, a copy of the license can be found at the root of this project
 * alternatively see the license online here https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package com.theeclecticdyslexic.sparki.misc

/**
 * Container used to hold pairs of SSIDs and URLs
 *
 * Used primarily by anything that needs to read or write to user settings related to
 * what http requests to send upon reaching charge target
 */

data class HTTPRequest(
    val ssid: String,
    val url: String) {

    companion object {
        val EMPTY_OBJECT = HTTPRequest("", "")
    }
}