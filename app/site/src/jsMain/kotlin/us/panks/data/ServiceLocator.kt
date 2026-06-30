package us.panks.data

import us.panks.data.repository.AuthRepositoryImpl
import us.panks.data.repository.OrderRepositoryImpl
import us.panks.data.repository.UserRepositoryImpl
import us.panks.domain.repository.AuthRepository
import us.panks.domain.repository.OrderRepository
import us.panks.domain.repository.UserRepository
import us.panks.domain.usecase.CreateOrderUseCase
import us.panks.domain.usecase.DeleteUserUseCase
import us.panks.domain.usecase.GetUserUseCase
import us.panks.domain.usecase.ListOrdersUseCase
import us.panks.domain.usecase.ListUsersUseCase
import us.panks.domain.usecase.LoginUseCase
import us.panks.domain.usecase.RegisterUseCase
import us.panks.domain.usecase.UpdateUserUseCase
import us.panks.generated.store.api.AuthApi
import us.panks.generated.store.api.OrdersApi
import us.panks.generated.store.api.UsersApi

object ServiceLocator {

    private val authRepo: AuthRepository = AuthRepositoryImpl(AuthApi())
    private val userRepo: UserRepository = UserRepositoryImpl(UsersApi(), authRepo)
    private val orderRepo: OrderRepository = OrderRepositoryImpl(OrdersApi(), authRepo)

    val auth: AuthRepository get() = authRepo

    val loginUseCase = LoginUseCase(authRepo)
    val registerUseCase = RegisterUseCase(userRepo, authRepo)
    val listUsersUseCase = ListUsersUseCase(userRepo)
    val getUserUseCase = GetUserUseCase(userRepo)
    val updateUserUseCase = UpdateUserUseCase(userRepo)
    val deleteUserUseCase = DeleteUserUseCase(userRepo)
    val createOrderUseCase = CreateOrderUseCase(orderRepo)
    val listOrdersUseCase = ListOrdersUseCase(orderRepo)
}
