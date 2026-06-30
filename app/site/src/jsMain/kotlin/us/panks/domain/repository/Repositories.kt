package us.panks.domain.repository

import us.panks.domain.model.AuthToken
import us.panks.domain.model.CreateUserInput
import us.panks.domain.model.LoginInput
import us.panks.domain.model.Order
import us.panks.domain.model.UpdateUserInput
import us.panks.domain.model.User
import us.panks.domain.model.UserFilter
import us.panks.domain.model.UserList

interface UserRepository {
    suspend fun create(input: CreateUserInput): User
    suspend fun list(filter: UserFilter): UserList
    suspend fun get(id: Int): User
    suspend fun update(id: Int, input: UpdateUserInput): User
    suspend fun delete(id: Int)
}

interface OrderRepository {
    suspend fun create(userId: Int, product: String, quantity: Int, price: Double): Order
    suspend fun listByUserId(userId: Int): List<Order>
}

interface AuthRepository {
    suspend fun login(input: LoginInput): AuthToken
    fun saveToken(token: String, email: String)
    fun getToken(): String?
    fun getEmail(): String?
    fun isLoggedIn(): Boolean
    fun logout()
}
