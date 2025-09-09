package com.example.quitesmoking.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class HelpSlide(
    val icon: ImageVector,
    val title: String,
    val desc: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HelpInfoOverlay(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false, // ← full-screen
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        // Full-screen scrim + content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                val slides = remember {
                    listOf(
                        HelpSlide(Icons.Default.Home, "Home",
                            "See greeting, daily check-in, days smoke-free, streak, and money saved."),
                        HelpSlide(Icons.Default.WbSunny, "Check-ins",
                            "Tap sun/moon for Morning/Night check-ins. Updates streak & leaderboard."),
                        HelpSlide(Icons.Default.Add, "Log Purchase",
                            "Track purchases to estimate savings. Totals reflect in Money Saved."),
                        HelpSlide(Icons.Default.Groups, "Community & GPT",
                            "Join community chat or ask GPT for tips, mindfulness, and strategies."),
                        HelpSlide(Icons.Default.ShowChart, "Progress",
                            "Review stats and weekly goals to stay on track.")
                    )
                }
                val pagerState = rememberPagerState(pageCount = { slides.size })

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    // Top bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    // Pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        val slide = slides[page]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = slide.icon,
                                contentDescription = slide.title,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(slide.title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                slide.desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pagerState.pageCount) { i ->
                            val selected = pagerState.currentPage == i
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 8.dp)
                                    .size(if (selected) 10.dp else 8.dp)
                                    .background(
                                        color = if (selected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Got it
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("Got it")
                    }
                }
            }
        }
    }

    // Back button also dismisses
    BackHandler(onBack = onDismiss)
}
