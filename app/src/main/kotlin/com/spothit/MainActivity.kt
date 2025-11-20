package com.spothit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spothit.ui.theme.SpotHitTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy { AppContainer(applicationContext) }
    private val viewModel: GameViewModel by viewModels { appContainer.viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleAuthRedirect(intent)
        observeAuthorizationRequests()
        setContent { SpotHitTheme { SpotHitApp(appContainer, viewModel) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthRedirect(intent)
    }

    private fun handleAuthRedirect(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme != "spothit") return
        val result = appContainer.spotifyAuthManager.parseRedirect(data)
        viewModel.handleAuthRedirect(result)
    }

    private fun observeAuthorizationRequests() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.authorizationRequest?.let {
                        startActivity(it.intent)
                        viewModel.onAuthorizationRequestLaunched()
                    }
                }
            }
        }
    }
}
