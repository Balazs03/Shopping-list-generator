package com.example.shoppinglistgenerator.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglistgenerator.R
import com.example.shoppinglistgenerator.model.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "AuthGate"

enum class AuthState { Loading, SignedIn, Failed }

/**
 * Indításkor (névtelenül) bejelentkezteti a felhasználót. A Firestore szabályok csak
 * bejelentkezett felhasználónak engednek olvasni, ezért a képernyők csak ezután indulhatnak.
 */
class AuthGateViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState.Loading)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        signIn()
    }

    fun signIn() {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            _state.value = runCatching { authRepository.ensureAuthenticated() }
                .fold(
                    onSuccess = { AuthState.SignedIn },
                    onFailure = { error ->
                        Log.e(TAG, "Névtelen bejelentkezés sikertelen", error)
                        AuthState.Failed
                    }
                )
        }
    }
}

@Composable
fun AuthGate(
    modifier: Modifier = Modifier,
    viewModel: AuthGateViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        AuthState.SignedIn -> content()
        AuthState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        AuthState.Failed -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.auth_failed),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Button(onClick = viewModel::signIn) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}
