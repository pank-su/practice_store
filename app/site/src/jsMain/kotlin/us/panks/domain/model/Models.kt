package us.panks.domain.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int,
)

data class UserList(
    val page: Int,
    val limit: Int,
    val total: Long,
    val users: List<User>,
)

data class Order(
    val id: Int,
    val userId: Int,
    val product: String,
    val quantity: Int,
    val price: Double,
    val createdAt: String,
)

data class CreateUserInput(
    val name: String,
    val email: String,
    val age: Int,
    val password: String,
)

data class UpdateUserInput(
    val name: String,
    val email: String,
    val age: Int,
)

data class CreateOrderInput(
    val product: String,
    val quantity: Int,
    val price: Double,
)

data class LoginInput(
    val email: String,
    val password: String,
)

data class AuthToken(
    val token: String,
)

data class UserFilter(
    val page: Int = 1,
    val limit: Int = 10,
    val minAge: Int? = null,
    val maxAge: Int? = null,
)
