package com.example.multitoolx.ui.api

import fi.iki.elonen.NanoHTTPD

/**
 * MyHttpServer.kt
 *
 * Created on: 2025-04-12
 * Author: Suhaib VP
 * Description:
 * - A custom HTTP server that extends NanoHTTPD.
 * - Handles HTTP requests and routes them based on method and path.
 * - Logs each request and its response using a callback function.
 * - Responds with the appropriate status and message for each route.
 *
 * @param port The port on which the server will listen for incoming requests.
 * @param routes A list of predefined routes that map HTTP methods and paths to responses.
 * @param logCallback A callback function for logging request details.
 */
class MyHttpServer(
    port: Int,
    private val routes: List<ServerStatusActivity.Route>, // List of server routes to handle requests
    private val logCallback: (String) -> Unit // Callback to log request details
) : NanoHTTPD(port) {

    /**
     * Handles incoming HTTP requests.
     * It matches the HTTP method and path from the request to predefined routes
     * and responds with the appropriate content or 404 Not Found.
     *
     * @param session The session object representing the HTTP request.
     * @return The HTTP response with the status and response text.
     */
    override fun serve(session: IHTTPSession): Response {
        // Get the HTTP method and requested path from the session
        val method = session.method.name
        val path = session.uri

        // Read the body of the request (if any)
        val body = session.inputStream.bufferedReader().use { it.readText() }

        // Try to find a matching route based on method and path
        val matched = routes.find { it.method.equals(method, true) && it.path == path }

        // Prepare the response text, defaulting to "404 Not Found" if no match is found
        val responseText = matched?.response ?: "404 Not Found"

        // Set the response status based on whether a route was matched
        val status = if (matched != null) Response.Status.OK else Response.Status.NOT_FOUND

        // Log the request details (method, path, body, and response)
        val logEntry = "[$method] $path\nBody: $body\nResponse: $responseText\n"
        logCallback.invoke(logEntry)

        // Return the HTTP response with the appropriate status, content type, and response text
        return newFixedLengthResponse(status, "text/plain", responseText)
    }
}
