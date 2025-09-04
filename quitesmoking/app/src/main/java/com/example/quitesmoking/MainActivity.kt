package com.example.quitesmoking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quitesmoking.auth.LoginScreen
import com.example.quitesmoking.auth.RegisterScreen
import com.example.quitesmoking.chat.CommunityScreen
import com.example.quitesmoking.chat.GPTChatScreen
import com.example.quitesmoking.chat.LeaderboardScreen
import com.example.quitesmoking.navigation.Routes
import com.example.quitesmoking.ui.MorningCheckInScreen
import com.example.quitesmoking.ui.NightCheckInScreen
import com.example.quitesmoking.ui.WeeklyGoalBuilderScreen
import com.example.quitesmoking.ui.theme.QuitesmokingTheme
import com.example.quitesmoking.urge.CravingTipsScreen
import com.example.quitesmoking.urge.UrgeTabScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            QuitesmokingTheme {
                val navController = rememberNavController()
                val startDest = if (FirebaseAuth.getInstance().currentUser != null)
                    "main" else Routes.LOGIN

                NavHost(
                    navController = navController,
                    startDestination = startDest
                ) {
                    // bottom nav container
                    composable("main") { MainScreen(navController) }

                    // auth
                    composable(Routes.LOGIN)    { LoginScreen(navController) }
                    composable(Routes.REGISTER) { RegisterScreen(navController) }

                    // other non-bottom routes
                    composable(Routes.COMMUNITY)     { CommunityScreen(navController) }
                    composable(Routes.LEAD)          { LeaderboardScreen() }
                    composable(Routes.MORNING_CHECK) { MorningCheckInScreen(navController) }
                    composable(Routes.NIGHT_CHECK)   { NightCheckInScreen(navController) }
                    composable("craving_tips")       { CravingTipsScreen(navController) }
                }
            }
        }
    }
}

/**
 * Bottom nav scaffold with nested NavHost
 */
@Composable
fun MainScreen(rootNavController: NavController) {
    /*Use two different navigation Contollers*/
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME)     { HomeScreen(rootNavController) }
                composable(Routes.URGE)     { UrgeTabScreen(rootNavController) }
                composable(Routes.PROGRESS) { StatsScreen(rootNavController) }
                composable(Routes.GPT_CHAT) { GPTChatScreen(rootNavController) }
                composable(Routes.WEEKLY_GOAL_EDIT) { WeeklyGoalBuilderScreen(rootNavController) }
            }
        }
    }
}


