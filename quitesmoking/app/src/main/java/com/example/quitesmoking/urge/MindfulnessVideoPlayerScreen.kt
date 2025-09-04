package com.example.quitesmoking.urge

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulnessVideoPlayerScreen(navController: NavController, videoUrl: String) {
    val context = LocalContext.current
    var videoLoadError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ Auto fallback to local resource if requested
    val actualUri = if (videoUrl == "local_sample") {
        Uri.parse("android.resource://${context.packageName}/raw/sample")
    } else {
        Uri.parse(videoUrl)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(actualUri))
            prepare()
            playWhenReady = true
            
            // Add error listener
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    println("MindfulnessVideoPlayer: Playback error: ${error.message}")
                    videoLoadError = true
                    isLoading = false
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        androidx.media3.common.Player.STATE_READY -> {
                            isLoading = false
                        }
                        androidx.media3.common.Player.STATE_BUFFERING -> {
                            isLoading = true
                        }
                    }
                }
            })
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    BackHandler {
        exoPlayer.pause()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mindfulness Video") },
                navigationIcon = {
                    IconButton(onClick = {
                        exoPlayer.pause()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                videoLoadError -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Failed to load video",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Please check your connection or try again later.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = {
                                    videoLoadError = false
                                    isLoading = true
                                    exoPlayer.prepare()
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    AndroidView(
                        factory = {
                            PlayerView(context).apply {
                                player = exoPlayer
                                useController = true
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                    )
                }
            }
        }
    }
}


//package com.example.quitesmoking.urge
//
//import android.net.Uri
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.navigation.NavController
//import androidx.media3.common.MediaItem
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.ui.PlayerView
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MindfulnessVideoPlayerScreen(navController: NavController, videoUrl: String) {
//    val context = LocalContext.current
//
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context).build().apply {
//            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
//            prepare()
//            playWhenReady = true
//        }
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Mindfulness Video") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize()
//        ) {
//            AndroidView(
//                factory = {
//                    PlayerView(context).apply {
//                        player = exoPlayer
//                        useController = true
//                        layoutParams = android.view.ViewGroup.LayoutParams(
//                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
//                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
//                        )
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(16 / 9f)
//            )
//        }
//    }
//
//    // 处理实体返回键
//    BackHandler {
//        navController.popBackStack()
//    }
//}
