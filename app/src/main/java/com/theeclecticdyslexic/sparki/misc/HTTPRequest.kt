package com.theeclecticdyslexic.sparki.misc

data class HTTPRequest(
    val ssid: String,
    val url: String) {

    companion object {
        val EMPTY_OBJECT = HTTPRequest("", "")
    }
}