package us.panks.data.repository

import kotlinx.browser.localStorage
import us.panks.data.api.ApiException
import us.panks.domain.model.AuthToken
import us.panks.domain.model.CreateUserInput
import us.panks.domain.model.LoginInput
import us.panks.domain.model.UpdateUserInput
import us.panks.domain.model.User
import us.panks.domain.model.UserFilter
import us.panks.domain.model.UserList
import us.panks.domain.repository.AuthRepository
import us.panks.domain.repository.OrderRepository
import us.panks.domain.repository.UserRepository
import us.panks.generated.store.api.AuthApi
import us.panks.generated.store.api.OrdersApi
import us.panks.generated.store.api.UsersApi
import us.panks.generated.store.infrastructure.HttpResponse
import us.panks.generated.store.model.InternalHandlerscreateOrderRequest
import us.panks.generated.store.model.InternalHandlerscreateUserRequest
import us.panks.generated.store.model.InternalHandlersloginRequest
import us.panks.generated.store.model.InternalHandlersupdateUserRequest
import us.panks.generated.store.model.Practice1InternalModelsOrder
import us.panks.generated.store.model.Practice1InternalModelsUser

private const val TOKEN_KEY = "store_token"
private const val EMAIL_KEY = "store_email"

private suspend fun <T : Any> HttpResponse<T>.bodyOrThrow(): T {
    if (!success) {
        throw ApiException(status, "request failed with status $status")
    }
    return body()
}

private fun Practice1InternalModelsUser.toDomain() = User(
    id = id ?: 0,
    name = name.orEmpty(),
    email = email.orEmpty(),
    age = age ?: 0,
)

private fun Practice1InternalModelsOrder.toDomain() = us.panks.domain.model.Order(
    id = id ?: 0,
    userId = userId ?: 0,
    product = product.orEmpty(),
    quantity = quantity ?: 0,
    price = price ?: 0.0,
    createdAt = createdAt.orEmpty(),
)

class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {

    override suspend fun login(input: LoginInput): AuthToken {
        val body = api.login(InternalHandlersloginRequest(input.email, input.password)).bodyOrThrow()
        return AuthToken(body.token.orEmpty())
    }

    override fun saveToken(token: String, email: String) {
        localStorage.setItem(TOKEN_KEY, token)
        localStorage.setItem(EMAIL_KEY, email)
    }

    override fun getToken(): String? = localStorage.getItem(TOKEN_KEY)
    override fun getEmail(): String? = localStorage.getItem(EMAIL_KEY)
    override fun isLoggedIn(): Boolean = getToken() != null
    override fun logout() {
        localStorage.removeItem(TOKEN_KEY)
        localStorage.removeItem(EMAIL_KEY)
    }
}

class UserRepositoryImpl(
    private val api: UsersApi,
    private val auth: AuthRepository,
) : UserRepository {

    private fun authorize() {
        auth.getToken()?.let { token ->
            api.setApiKey(token)
            api.setApiKeyPrefix("Bearer")
        }
    }

    override suspend fun create(input: CreateUserInput): User {
        val body = api.createUser(
            InternalHandlerscreateUserRequest(input.age, input.email, input.name, input.password)
        ).bodyOrThrow()
        return body.toDomain()
    }

    override suspend fun list(filter: UserFilter): UserList {
        authorize()
        val body = api.listUsers(filter.page, filter.limit, filter.minAge, filter.maxAge).bodyOrThrow()
        return UserList(
            page = body.page ?: filter.page,
            limit = body.limit ?: filter.limit,
            total = body.total?.toLong() ?: 0L,
            users = body.users.orEmpty().map { it.toDomain() },
        )
    }

    override suspend fun get(id: Int): User {
        authorize()
        return api.getUser(id).bodyOrThrow().toDomain()
    }

    override suspend fun update(id: Int, input: UpdateUserInput): User {
        authorize()
        val body = api.updateUser(id, InternalHandlersupdateUserRequest(input.age, input.email, input.name)).bodyOrThrow()
        return body.toDomain()
    }

    override suspend fun delete(id: Int) {
        authorize()
        api.deleteUser(id).bodyOrThrow()
    }
}

class OrderRepositoryImpl(
    private val api: OrdersApi,
    private val auth: AuthRepository,
) : OrderRepository {

    private fun authorize() {
        auth.getToken()?.let { token ->
            api.setApiKey(token)
            api.setApiKeyPrefix("Bearer")
        }
    }

    override suspend fun create(userId: Int, product: String, quantity: Int, price: Double): us.panks.domain.model.Order {
        authorize()
        val body = api.createOrder(userId, InternalHandlerscreateOrderRequest(price, product, quantity)).bodyOrThrow()
        return body.toDomain()
    }

    override suspend fun listByUserId(userId: Int): List<us.panks.domain.model.Order> {
        authorize()
        return api.listOrders(userId).bodyOrThrow().map { it.toDomain() }
    }
}
