package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.CodecInspectorScreen
import com.example.ui.screens.JitterBufferScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfilerScreen
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.NovaStreamTheme
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioCard
import com.example.ui.theme.TextMuted

enum class AppNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    PLAYER("Monitor", Icons.Default.PlayArrow, "nav_player"),
    PROFILER("Profiler", Icons.Default.Analytics, "nav_profiler"),
    JITTER_BUFFER("Network", Icons.Default.Router, "nav_jitter"),
    CODECS("Hardware", Icons.Default.Memory, "nav_codecs")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NovaStreamTheme {
                NovaStreamApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NovaStreamApp(viewModel: MainViewModel) {
    var selectedDestination by rememberSaveable { mutableStateOf(AppNavDestination.PLAYER) }
    val streams by viewModel.streams.collectAsState()

    // Auto-start playback on initial launch
    LaunchedEffect(Unit) {
        viewModel.engine.startPlayback()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = StudioBlack,
        bottomBar = {
            NavigationBar(
                containerColor = StudioCard,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                tonalElevation = 6.dp
            ) {
                AppNavDestination.values().forEach { destination ->
                    val isSelected = selectedDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(text = destination.title, fontSize = 11.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioBlack,
                            selectedTextColor = InstrumentCyan,
                            indicatorColor = InstrumentCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioBlack)
        ) {
            when (selectedDestination) {
                AppNavDestination.PLAYER -> {
                    PlayerScreen(
                        engine = viewModel.engine,
                        streamList = streams,
                        onSelectStream = { config ->
                            viewModel.selectStream(config)
                        }
                    )
                }
                AppNavDestination.PROFILER -> {
                    ProfilerScreen(
                        engine = viewModel.engine
                    )
                }
                AppNavDestination.JITTER_BUFFER -> {
                    JitterBufferScreen(
                        engine = viewModel.engine
                    )
                }
                AppNavDestination.CODECS -> {
                    CodecInspectorScreen(
                        viewModel = viewModel,
                        onStreamSelected = { config ->
                            viewModel.selectStream(config)
                            selectedDestination = AppNavDestination.PLAYER
                        }
                    )
                }
            }
        }
    }
}
