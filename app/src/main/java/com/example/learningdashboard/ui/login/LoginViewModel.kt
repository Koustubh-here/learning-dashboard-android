package com.example.learningdashboard.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learningdashboard.data.repository.AuthRepository
import com.example.learningdashboard.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(v: String) = _state.update { it.copy(email = v, emailError = null, error = null) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, passwordError = null, error = null) }

    fun onLogin() {
        val s = _state.value
        if (s.isLoading) return
        val emailErr = Validators.email(s.email)
        val passErr = Validators.password(s.password)
        if (emailErr != null || passErr != null) {
            _state.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            auth.login(s.email, s.password).fold(
                onSuccess = { _state.update { it.copy(isLoading = false, loggedIn = true) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Login failed") } }
            )
        }
    }
}
