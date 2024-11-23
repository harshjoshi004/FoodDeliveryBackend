package com.codewithfk.database


import io.ktor.http.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.text.insert

object DatabaseFactory {
    fun init() {
        val dbUrl = "jdbc:mysql://localhost:3306/food_delivery"
        val dbUser = "root"
        val dbPassword = "root"

        Database.connect(
            url = dbUrl,
            driver = "com.mysql.cj.jdbc.Driver",
            user = dbUser,
            password = dbPassword
        )

        transaction {
            SchemaUtils.create(
                UsersTable,
                CategoriesTable,
                RestaurantsTable,
//                MenuItemsTable,
//                OrdersTable,
//                ReviewsTable
            )
        }

    }
}


fun Application.seedDatabase() {
    environment.monitor.subscribe(ApplicationStarted) {
        transaction {
            // Seed users
            if (UsersTable.selectAll().empty()) {
                println("Seeding users...")
                val users = listOf(
                    Triple(UUID.randomUUID(), "owner1@example.com", "Restaurant Owner"),
                    Triple(UUID.randomUUID(), "owner2@example.com", "Another Owner")
                )

                users.forEach { user ->
                    UsersTable.insert {
                        it[id] = user.first
                        it[email] = user.second
                        it[name] = user.third
                        it[passwordHash] = "hashed-password" // Placeholder for hashed password
                        it[role] = "owner"
                        it[authProvider] = "email"
                    }
                }

                println("Users seeded: ${users.map { it.second }}")
            } else {
                println("Users already exist.")
            }

            // Fetch user IDs
            val userIds = UsersTable.selectAll().map { it[UsersTable.id] }

            // Seed categories
            if (CategoriesTable.selectAll().empty()) {
                println("Seeding categories...")
                val categories = listOf(
                    UUID.randomUUID() to "Fast Food",
                    UUID.randomUUID() to "Beverages",
                    UUID.randomUUID() to "Desserts",
                    UUID.randomUUID() to "Healthy Food",
                    UUID.randomUUID() to "Pizza",
                    UUID.randomUUID() to "Asian Cuisine"
                )
                CategoriesTable.batchInsert(categories) { category ->
                    this[CategoriesTable.id] = category.first
                    this[CategoriesTable.name] = category.second
                }

                println("Categories seeded: ${categories.map { it.second }}")
            } else {
                println("Categories already exist.")
            }

            // Get seeded category IDs
            val categoryIds = CategoriesTable.selectAll().associate { it[CategoriesTable.name] to it[CategoriesTable.id] }

            // Seed restaurants
            if (RestaurantsTable.selectAll().empty()) {
                println("Seeding restaurants...")
                val restaurants = listOf(
                    Triple("Pizza Palace", "123 Main St, New York, NY", Triple(40.712776, -74.005974, "Pizza")),
                    Triple("Burger Haven", "456 Elm St, Los Angeles, CA", Triple(34.052235, -118.243683, "Fast Food")),
                    Triple("Dessert Delight", "789 Pine St, Chicago, IL", Triple(41.878113, -87.629799, "Desserts")),
                    Triple("Healthy Bites", "321 Oak St, Miami, FL", Triple(25.761681, -80.191788, "Healthy Food")),
                    Triple("Sushi Express", "654 Maple St, Seattle, WA", Triple(47.606209, -122.332069, "Asian Cuisine")),
                    Triple("Coffee Corner", "987 Cedar St, San Francisco, CA", Triple(37.774929, -122.419418, "Beverages"))
                )
                RestaurantsTable.batchInsert(restaurants) { restaurant ->
                    this[RestaurantsTable.id] = UUID.randomUUID()
                    this[RestaurantsTable.ownerId] = userIds.randomOrNull() ?: error("No users found to assign as owner.")
                    this[RestaurantsTable.name] = restaurant.first
                    this[RestaurantsTable.address] = restaurant.second
                    this[RestaurantsTable.latitude] = restaurant.third.first
                    this[RestaurantsTable.longitude] = restaurant.third.second
                    this[RestaurantsTable.categoryId] = categoryIds[restaurant.third.third] ?: error("Category not found: ${restaurant.third.third}")
                    this[RestaurantsTable.createdAt] = org.jetbrains.exposed.sql.javatime.CurrentTimestamp()
                }

                println("Restaurants seeded: ${restaurants.map { it.first }}")
            } else {
                println("Restaurants already exist.")
            }
        }
    }
}