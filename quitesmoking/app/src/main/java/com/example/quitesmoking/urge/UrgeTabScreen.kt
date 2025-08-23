package com.example.quitesmoking.urge

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrgeTabScreen(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Handle Cravings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CravingOptionButton("Tips") {
                navController.navigate("craving_tips")
            }

            // ✅ Pass encoded video URL
            CravingOptionButton("Mindfulness Videos") {
//                val videoUrl = "android.resource://${context.packageName}/raw/sample"
//                navController.navigate("mindfulness_video_screen/${Uri.encode(videoUrl)}")
                val videoUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
                val encodedUrl = Uri.encode(videoUrl)
                navController.navigate("mindfulness_video_screen/$encodedUrl")
            }

            CravingOptionButton("Reach Out to Someone") {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
                context.startActivity(intent)
            }

            CravingOptionButton("Withdrawal Relief Tips") {
                // TODO: Implement in the future
            }
        }
    }
}

@Composable
fun CravingOptionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}


//// === UrgeTabScreen.kt ===
//package com.example.quitesmoking.urge
//
//import android.content.Context
//import android.net.Uri
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import android.content.Intent
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UrgeTabScreen(navController: NavController) {
//    val context = LocalContext.current
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Handle Cravings") },
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
//                .padding(24.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            CravingOptionButton("Tips") {
//                navController.navigate("craving_tips")
//            }
//            CravingOptionButton("Mindfulness Videos") {
//                navController.navigate("video_player")
//            }
//            CravingOptionButton("Reach Out to Someone") {
//                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
//                context.startActivity(intent)
//            }
//            CravingOptionButton("Withdrawal Relief Tips") {
//                // Future implementation
//            }
//        }
//    }
//}
//
//@Composable
//fun CravingOptionButton(text: String, onClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(60.dp)
//            .clip(RoundedCornerShape(24.dp))
//            .background(
//                Brush.horizontalGradient(
//                    listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
//                )
//            )
//            .clickable { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = text,
//            color = Color.White,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}

//package com.example.quitesmoking.urge
//
//import android.content.Intent
//import android.net.Uri
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UrgeTabScreen(navController: NavController) {
//    val context = LocalContext.current
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Handle Cravings") },
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
//                .padding(24.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            CravingOptionButton("Tips") {
//                navController.navigate("craving_tips")
//            }
//            CravingOptionButton("Mindfulness Videos") {
//                val url = "https://www.youtube.com/results?search_query=mindfulness+videos"
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                context.startActivity(intent)
//            }
//            CravingOptionButton("Reach Out to Someone") {
//                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
//                context.startActivity(intent)
//            }
//            CravingOptionButton("Withdrawal Relief Tips") {
//                // Future implementation
//            }
//        }
//    }
//}
//
//@Composable
//fun CravingOptionButton(text: String, onClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(60.dp)
//            .clip(RoundedCornerShape(24.dp))
//            .background(
//                Brush.horizontalGradient(
//                    listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
//                )
//            )
//            .clickable { onClick() },
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = text,
//            color = Color.White,
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}
