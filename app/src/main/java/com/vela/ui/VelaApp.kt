package com.vela.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vela.ui.navigation.RootNavGraph
import com.vela.ui.navigation.deeplink.DeepLinkHandler
import com.vela.ui.navigation.deeplink.navigate
import com.vela.ui.theme.VelaTheme

@Composable
fun VelaApp(deepLinkHandler: DeepLinkHandler) {
    val rootNavController = rememberNavController()
    LaunchedEffect(Unit) {
        deepLinkHandler.flow.collect {
            navigate(navController = rootNavController, deepLink = it)
        }
    }
    VelaTheme {
        Surface {
            RootNavGraph(
                rootNavController = rootNavController,
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
            )
        }
    }
}
