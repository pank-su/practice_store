package us.panks.ui.users

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import us.panks.data.ServiceLocator
import us.panks.data.api.ApiException
import us.panks.domain.model.User
import us.panks.domain.model.UserFilter

class UsersViewModel {

    var users by mutableStateOf<List<User>>(emptyList())
    var total by mutableStateOf(0L)
    var page by mutableStateOf(1)
    val limit = 10

    var minAge by mutableStateOf("")
    var maxAge by mutableStateOf("")

    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    fun load(scope: CoroutineScope, targetPage: Int, onUnauthorized: () -> Unit) {
        page = targetPage
        loading = true
        error = null
        scope.launch {
            try {
                val filter = UserFilter(
                    page = targetPage,
                    limit = limit,
                    minAge = minAge.toIntOrNull(),
                    maxAge = maxAge.toIntOrNull(),
                )
                val result = ServiceLocator.listUsersUseCase(filter)
                users = result.users
                total = result.total
                page = result.page
            } catch (e: ApiException) {
                if (e.statusCode == 401) onUnauthorized()
                else error = e.message
            } catch (e: Throwable) {
                error = "Network error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun applyFilter(scope: CoroutineScope, onUnauthorized: () -> Unit) {
        load(scope, 1, onUnauthorized)
    }

    val totalPages: Long get() = (total + limit - 1) / limit
}
