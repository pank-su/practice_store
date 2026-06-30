package us.panks.domain.usecase

import us.panks.domain.model.AuthToken
import us.panks.domain.model.CreateUserInput
import us.panks.domain.model.LoginInput
import us.panks.domain.model.Order
import us.panks.domain.model.UpdateUserInput
import us.panks.domain.model.User
import us.panks.domain.model.UserFilter
import us.panks.domain.model.UserList
import us.panks.domain.repository.AuthRepository
import us.panks.domain.repository.OrderRepository
import us.panks.domain.repository.UserRepository

class LoginUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(input: LoginInput): AuthToken = repo.login(input)
}

class RegisterUseCase(
    private val users: UserRepository,
    private val auth: AuthRepository,
) {
    suspend operator fun invoke(input: CreateUserInput): User {
        val user = users.create(input)
        auth.login(LoginInput(input.email, input.password)).also { token ->
            auth.saveToken(token.token, input.email)
        }
        return user
    }
}

class ListUsersUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(filter: UserFilter): UserList = repo.list(filter)
}

class GetUserUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(id: Int): User = repo.get(id)
}

class UpdateUserUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(id: Int, input: UpdateUserInput): User = repo.update(id, input)
}

class DeleteUserUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(id: Int) = repo.delete(id)
}

class CreateOrderUseCase(private val repo: OrderRepository) {
    suspend operator fun invoke(userId: Int, product: String, quantity: Int, price: Double): Order =
        repo.create(userId, product, quantity, price)
}

class ListOrdersUseCase(private val repo: OrderRepository) {
    suspend operator fun invoke(userId: Int): List<Order> = repo.listByUserId(userId)
}
