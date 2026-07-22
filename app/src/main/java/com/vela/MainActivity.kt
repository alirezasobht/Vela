package com.vela

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vela.data.platform.DeepLinkIntentFactory
import com.vela.ui.VelaApp
import com.vela.ui.navigation.deeplink.DeepLinkHandler
import com.vela.ui.theme.VelaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler

    @Inject
    lateinit var deepLinkIntentFactory: DeepLinkIntentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            VelaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VelaApp(deepLinkHandler = deepLinkHandler)
                }
            }
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        deepLinkIntentFactory.fromIntent(intent)?.let { deepLinkHandler.emit(it) }
    }
}
