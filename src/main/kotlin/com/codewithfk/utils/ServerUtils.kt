package com.codewithfk.utils

import com.codewithfk.model.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun ApplicationCall.respondError( message: String,status: HttpStatusCode) {
    respond(status, ErrorResponse(status.value, message))
}