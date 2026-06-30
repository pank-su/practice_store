package us.panks.ui.userdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import us.panks.data.ServiceLocator
import us.panks.data.api.ApiException
import us.panks.domain.model.Order
import us.panks.domain.model.User

class UserDetailViewModel {

    var user by mutableStateOf<User?>(null)
    var orders by mutableStateOf<List<Order>>(emptyList())
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    // Order form
    var product by mutableStateOf("")
    var quantity by mutableStateOf("")
    var price by mutableStateOf("")
    var orderError by mutableStateOf<String?>(null)
    var orderLoading by mutableStateOf(false)

    fun load(scope: CoroutineScope, userId: Int, onUnauthorized: () -> Unit) {
        loading = true
        error = null
        scope.launch {
            try {
                user = ServiceLocator.getUserUseCase(userId)
                orders = ServiceLocator.listOrdersUseCase(userId)
            } catch (e: ApiException) {
                if (e.statusCode == 401) onUnauthorized()
                else if (e.statusCode == 404) error = "User not found"
                else error = e.message
            } catch (e: Throwable) {
                error = "Network error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun createOrder(scope: CoroutineScope, userId: Int) {
        val qty = quantity.toIntOrNull()
        val prc = price.toDoubleOrNull()
        if (product.isBlank() || qty == null || qty <= 0 || prc == null || prc <= 0) {
            orderError = "Fill all order fields correctly"
            return
        }
        orderLoading = true
        orderError = null
        scope.launch {
            try {
                ServiceLocator.createOrderUseCase(userId, product, qty, prc)
                orders = ServiceLocator.listOrdersUseCase(userId)
                product = ""
                quantity = ""
                price = ""
            } catch (e: ApiException) {
                orderError = e.message
            } catch (e: Throwable) {
                orderError = "Network error: ${e.message}"
            } finally {
                orderLoading = false
            }
        }
    }

    fun delete(scope: CoroutineScope, userId: Int, onSuccess: () -> Unit) {
        scope.launch {
            try {
                ServiceLocator.deleteUserUseCase(userId)
                onSuccess()
            } catch (e: ApiException) {
                error = e.message
            } catch (e: Throwable) {
                error = "Network error: ${e.message}"
            }
        }
    }
}
