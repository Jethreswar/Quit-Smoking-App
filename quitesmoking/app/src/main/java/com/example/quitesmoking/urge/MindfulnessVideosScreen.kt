package com.example.quitesmoking.urge

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// Compose basics
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip

// If you added Coil:
// implementation("io.coil-kt:coil-compose:2.6.0")
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulnessVideosScreen(
    nav: NavController,
    vm: MindfulnessViewModel = remember { MindfulnessViewModel() }
) {
    val videos by vm.videos.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mindfulness Videos") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Column(
                Modifier.fillMaxSize().padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Failed to load videos", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp)); Text(error ?: "")
                Spacer(Modifier.height(12.dp)); Button(onClick = vm::refresh) { Text("Retry") }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
//                itemsIndexed(videos) { i, v ->
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                val encoded = Uri.encode(v.url)
//                                nav.navigate("mindfulness_video_screen/$encoded")
//                            }
//                    ) {
//                        // thumbnail if available; fallback to video url (some CDNs return a frame)
//                        AsyncImage(
//                            model = if (v.thumbnail.isNotBlank()) v.thumbnail else v.url,
//                            contentDescription = v.title,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .aspectRatio(16 / 9f),
//                            contentScale = ContentScale.Crop
//                        )
//                        Column(Modifier.padding(12.dp)) {
//                            Text(v.title.ifBlank { "Video ${i + 1}" },
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold)
////                            Spacer(Modifier.height(4.dp))
////                            Text(v.url.take(70) + if (v.url.length > 70) "…" else "")
//                        }
//                    }
//                }
                itemsIndexed(videos) { i, v ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val encoded = Uri.encode(v.url)
                                nav.navigate("mindfulness_video_screen/$encoded")
                            }
                    ) {
                        AsyncImage(
                            model = if (v.thumbnail.isNotBlank()) v.thumbnail else v.url,
                            contentDescription = v.title.ifBlank { "Mindfulness video ${i + 1}" },
                            modifier = Modifier.matchParentSize(), // fills the Box
                            contentScale = ContentScale.Crop
                        )
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "Play",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                v.title.ifBlank { "Video ${i + 1}" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
