package com.vela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.vela.data.FakeAssetDataSource
import com.vela.ui.common.components.mapper.toUiModel
import com.vela.ui.screens.home.HomeScreen
import com.vela.ui.theme.VelaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VelaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    HomeScreen(
                        assets = FakeAssetDataSource.assets.map { it.toUiModel() },
                        date = LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
                    )
                }
            }
        }
    }
}