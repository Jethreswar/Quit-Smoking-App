package com.example.quitesmoking.urge

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import com.example.quitesmoking.navigation.Routes
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.quitesmoking.navigation.goHomeInTabs
import androidx.navigation.NavGraph.Companion.findStartDestination

fun NavController.goHomeInTabs() {
    // This NavController is the bottomNavController
    navigate(Routes.HOME) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrgeTabScreen(navController: NavController, bottomNav: NavController) {

    // System back: always jump to Home tab
    androidx.activity.compose.BackHandler {
        bottomNav.goHomeInTabs()
    }
    val context = LocalContext.current
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Handle Cravings") },
                navigationIcon = {

//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
                    IconButton(onClick = { bottomNav.goHomeInTabs() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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

            // ✅ Use local mindfulness video resource
            CravingOptionButton("Mindfulness Videos") {
                val videoUrl = "local_sample" // Special identifier for local video
                navController.navigate("mindfulness_video_screen/$videoUrl")
            }

            CravingOptionButton("Reach Out to Someone") {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
                context.startActivity(intent)
            }

            CravingOptionButton("Withdrawal Relief Tips") {
                navController.navigate("withdrawal_relief_tips")
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

