package com.codewithfk.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()
    val authProvider = varchar("auth_provider", 50) // "google", "facebook", "email"
    val role = varchar("role", 50) // "customer", "rider", "restaurant"
    val createdAt = datetime("created_at").defaultExpression(
        org.jetbrains.exposed.sql.javatime.CurrentTimestamp()
    )

    override val primaryKey: PrimaryKey
        get() = PrimaryKey(id)
}

object CategoriesTable : Table("categories") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255).uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp())
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(CategoriesTable.id)
}

object RestaurantsTable : Table("restaurants") {
    val id = uuid("id").autoGenerate()
    val ownerId = uuid("owner_id").references(UsersTable.id) // User managing the restaurant
    val name = varchar("name", 255)
    val address = varchar("address", 500)
    val categoryId = uuid("category_id").references(CategoriesTable.id)
    val latitude = double("latitude") // Restaurant's latitude
    val longitude = double("longitude") // Restaurant's longitude
    val createdAt = datetime("created_at").defaultExpression(org.jetbrains.exposed.sql.javatime.CurrentTimestamp())
    override val primaryKey: PrimaryKey
        get() = PrimaryKey(UsersTable.id)
}