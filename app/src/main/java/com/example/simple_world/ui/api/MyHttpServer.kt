package com.example.simple_world.ui.api

import fi.iki.elonen.NanoHTTPD

class MyHttpServer(
    port: Int,
    private val routes: List<ServerStatusActivity.Route>,
    private val logCallback: (String) -> Unit
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val method = session.method.name
        val path = session.uri

        val body = session.inputStream.bufferedReader().use { it.readText() }

        val matched = routes.find { it.method.equals(method, true) && it.path == path }

        val responseText = matched?.response ?: "404 Not Found"
        val status = if (matched != null) Response.Status.OK else Response.Status.NOT_FOUND

        val logEntry = "[$method] $path\nBody: $body\nResponse: $responseText\n"
        logCallback.invoke(logEntry)

        return newFixedLengthResponse(status, "text/plain", responseText)
    }
}
