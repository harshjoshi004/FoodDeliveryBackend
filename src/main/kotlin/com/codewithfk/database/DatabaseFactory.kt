package com.codewithfk.database


import com.codewithfk.model.Category
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
        val dbPassword = "godknows"

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
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = "Pizza",
                        imageUrl = "https://images.vexels.com/content/136312/preview/logo-pizza-fast-food-d65bfe.png"
                    ),
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = "Fast Food",
                        imageUrl = "https://www.pngarts.com/files/3/Fast-Food-Free-PNG-Image.png"
                    ),
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = "Beverages",
                        imageUrl = "https://www.pngfind.com/pngs/m/172-1729150_alcohol-drinks-png-mojito-drink-transparent-png.png"
                    ),
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = "Desserts",
                        imageUrl = "https://img.freepik.com/premium-psd/isolated-cake-style-png-with-white-background-generative-ia_209190-251177.jpg"
                    ),
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = "Healthy Food",
                        imageUrl = "https://png.pngtree.com/png-clipart/20190516/original/pngtree-healthy-food-png-image_3776802.jpg"
                    ),
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = "Asian Cuisine",
                        imageUrl = "https://e7.pngegg.com/pngimages/706/98/png-clipart-japanese-cuisine-chinese-cuisine-vietnamese-cuisine-asian-cuisine-dish-cooking-leaf-vegetable-food.png"
                    ),
                    Category(
                        id = UUID.randomUUID().toString(),
                        name = "Burger",
                        imageUrl = "https://png.pngtree.com/png-vector/20231016/ourmid/pngtree-burger-food-png-free-download-png-image_10199386.png"
                    ),
                )
                CategoriesTable.batchInsert(categories) { category ->
                    this[CategoriesTable.id] = UUID.fromString(category.id)
                    this[CategoriesTable.name] = category.name
                    this[CategoriesTable.imageUrl] = category.imageUrl ?: ""

                }

                println("Categories seeded: ${categories.map { it.name }}")
            } else {
                println("Categories already exist.")
            }

            // Get seeded category IDs
            val categoryIds =
                CategoriesTable.selectAll().associate { it[CategoriesTable.name] to it[CategoriesTable.id] }

            // Seed restaurants
            if (RestaurantsTable.selectAll().empty()) {
                println("Seeding restaurants...")
                val restaurants = listOf(
                    Triple(
                        Pair(
                            "Pizza Palace",
                            "https://www.marthastewart.com/thmb/3N-0cJgJfLDyytnCehJd4aVgHJw=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/white-pizza-172-d112100_horiz-c868dcf28ed44b21af90f11797d6d7d6.jpgitokKoRSmCVm"
                        ),
                        "123 Main St, New York, NY",
                        Triple(40.712776, -74.005978, "Pizza")
                    ),
                    Triple(
                        Pair(
                            "Burger Haven",
                            "https://imageproxy.wolt.com/mes-image/43bb7be3-03c2-4337-9d52-99cba2b1650d/85493202-0013-44f0-b7c1-59262d53e9ff"
                        ),
                        "456 Elm St, Los Angeles, CA",
                        Triple(40.712776, -74.005979, "Fast Food")
                    ),
                    Triple(
                        Pair(
                            "Dessert Delight",
                            "https://static.vecteezy.com/system/resources/previews/032/160/853/large_2x/mouthwatering-dessert-heaven-a-tray-of-assorted-creamy-delights-ai-generated-photo.jpg"
                        ),
                        "789 Pine St, Chicago, IL",
                        Triple(40.712776, -74.005973, "Desserts")
                    ),
                    Triple(
                        Pair(
                            "Healthy Bites",
                            "https://i2.wp.com/www.downshiftology.com/wp-content/uploads/2019/04/Cobb-Salad-main.jpg"
                        ),
                        "321 Oak St, Miami, FL",
                        Triple(40.712776, -74.005974, "Healthy Food")
                    ),
                    Triple(
                        Pair(
                            "Sushi Express",
                            "https://tb-static.uber.com/prod/image-proc/processed_images/87baf961b666795ea98160dc3b1d465c/fb86662148be855d931b37d6c1e5fcbe.jpeg"
                        ),
                        "654 Maple St, Seattle, WA",
                        Triple(40.712776, -74.005976, "Asian Cuisine")
                    ),
                    Triple(
                        Pair(
                            "Coffee Corner",
                            "https://insanelygoodrecipes.com/wp-content/uploads/2020/07/Cup-Of-Creamy-Coffee.png"
                        ),
                        "987 Cedar St, San Francisco, CA",
                        Triple(40.712776, -74.005977, "Beverages")
                    )
                )

                RestaurantsTable.batchInsert(restaurants) { restaurant ->
                    this[RestaurantsTable.id] = UUID.randomUUID()
                    this[RestaurantsTable.ownerId] =
                        userIds.randomOrNull() ?: error("No users found to assign as owner.")
                    this[RestaurantsTable.name] = restaurant.first.first
                    this[RestaurantsTable.address] = restaurant.second
                    this[RestaurantsTable.latitude] = restaurant.third.first
                    this[RestaurantsTable.longitude] = restaurant.third.second
                    this[RestaurantsTable.imageUrl] = restaurant.first.second
                    this[RestaurantsTable.categoryId] =
                        categoryIds[restaurant.third.third] ?: error("Category not found: ${restaurant.third.third}")
                    this[RestaurantsTable.createdAt] = org.jetbrains.exposed.sql.javatime.CurrentTimestamp()
                }

                println("Restaurants seeded: ${restaurants.map { it.first }}")
            } else {
                println("Restaurants already exist.")
            }
        }
    }
}