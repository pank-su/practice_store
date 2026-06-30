package us.panks.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import us.panks.data.ServiceLocator
import us.panks.data.api.ApiException
import us.panks.domain.model.CreateUserInput

class RegisterViewModel {

    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var age by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false)

    fun register(scope: CoroutineScope, onSuccess: () -> Unit) {
        val ageInt = age.toIntOrNull()
        if (name.isBlank() || email.isBlank() || password.isBlank() || ageInt == null || ageInt <= 0) {
            error = "Please fill in all fields correctly"
            return
        }
        loading = true
        error = null
        scope.launch {
            try {
                ServiceLocator.registerUseCase(CreateUserInput(name, email, ageInt, password))
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
