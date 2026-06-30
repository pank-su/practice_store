package us.panks.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import us.panks.data.ServiceLocator
import us.panks.data.api.ApiException
import us.panks.domain.model.LoginInput

class LoginViewModel {

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false)

    fun login(scope: CoroutineScope, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            error = "Please fill in all fields"
            return
        }
        loading = true
        error = null
        scope.launch {
            try {
                val token = ServiceLocator.loginUseCase(LoginInput(email, password))
                ServiceLocator.auth.saveToken(token.token, email)
                onSuccess()
            } catch (e: ApiException) {
                error = e.message
            } catch (e: Throwable) {
                error = "Network error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }
}
