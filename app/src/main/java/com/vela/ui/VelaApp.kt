package com.vela.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.vela.ui.navigation.RootNavGraph
import com.vela.ui.theme.VelaTheme

@Composable
fun VelaApp() {
    val rootNavController = rememberNavController()
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

@Preview(showBackground = true)
@Composable
fun VelaAppPreview() {
    VelaApp()
}
