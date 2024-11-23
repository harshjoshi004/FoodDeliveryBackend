package com.codewithfk.services


import com.codewithfk.JwtConfig
import com.codewithfk.database.UsersTable
import com.codewithfk.model.AuthProvider
import com.codewithfk.model.UserRole
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

object AuthService {
    private val httpClient = HttpClient(CIO)

    fun register(name: String, email: String, passwordHash: String, role: String): String {
        return transaction {
            val userId = UUID.randomUUID()
            UsersTable.insert {
                it[id] = userId
                it[this.name] = name
                it[this.email] = email
                it[this.passwordHash] = passwordHash
                it[this.role] = role
                it[this.authProvider] = "email"
            }
            JwtConfig.generateToken(userId.toString())
        }
    }

    fun login(email: String, passwordHash: String): String? {
        return transaction {
            val user = UsersTable.select {
                (UsersTable.email eq email) and (UsersTable.passwordHash eq passwordHash)
            }.singleOrNull()

            user?.let {
                val userId = it[UsersTable.id].toString()
                JwtConfig.generateToken(userId)
            }
        }
    }

    // Google OAuth User Info
    /**
     * Validate Google ID Token and get user information.
     */
    suspend fun validateGoogleToken(idToken: String): Map<String, String>? {
        val response: HttpResponse = httpClient.get("https://oauth2.googleapis.com/tokeninfo") {
            parameter("id_token", idToken)
        }
        return if (response.status == HttpStatusCode.OK) {
            val userInfo = response.body<JsonObject>()
            mapOf(
                "email" to userInfo["email"]?.jsonPrimitive?.content.orEmpty(),
                "name" to userInfo["name"]?.jsonPrimitive?.content.orEmpty()
            )
        } else {
            null
        }
    }

    /**
     * Validate Facebook Access Token and get user information.
     */
    suspend fun validateFacebookToken(accessToken: String): Map<String, String>? {
        val response: HttpResponse = httpClient.get("https://graph.facebook.com/me") {
            parameter("fields", "id,name,email")
            parameter("access_token", accessToken)
        }
        return if (response.status == HttpStatusCode.OK) {
            val userInfo = response.body<JsonObject>()
            mapOf(
                "email" to userInfo["email"]?.jsonPrimitive?.content.orEmpty(),
                "name" to userInfo["name"]?.jsonPrimitive?.content.orEmpty()
            )
        } else {
            null
        }
    }

    /**
     * Handle user registration or login based on OAuth provider.
     */
    fun oauthLoginOrRegister(email: String, name: String, provider: String, userType: String): String {
        return transaction {
            val user = UsersTable.select { UsersTable.email eq email }.singleOrNull()

            if (user == null) {
                // Register a new user
                val userId = UUID.randomUUID()
                UsersTable.insert {
                    it[id] = userId
                    it[this.email] = email
                    it[this.name] = name
                    it[this.authProvider] = provider
                    it[this.role] = userType
                }
                JwtConfig.generateToken(userId.toString())
            } else {
                // Generate token for existing user
                val userId = user[UsersTable.id]
                JwtConfig.generateToken(userId.toString())
            }
        }
    }

}