package com.example.quitesmoking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.quitesmoking.auth.LoginScreen
import com.example.quitesmoking.auth.RegisterScreen
import com.example.quitesmoking.chat.ChatScreen
import com.example.quitesmoking.chat.CommunityScreen
import com.example.quitesmoking.chat.GPTChatScreen
import com.example.quitesmoking.chat.LeaderboardScreen
import com.example.quitesmoking.navigation.Routes
import com.example.quitesmoking.ui.MorningCheckInScreen
import com.example.quitesmoking.ui.NightCheckInScreen
import com.example.quitesmoking.ui.WeeklyGoalBuilderScreen
//import com.example.quitesmoking.ui.LogPurchaseScreen
import com.example.quitesmoking.ui.theme.QuitesmokingTheme
import com.example.quitesmoking.HomeScreen
import com.example.quitesmoking.StatsScreen
import com.example.quitesmoking.urge.CravingTipsScreen
import com.example.quitesmoking.urge.UrgeTabScreen
import com.example.quitesmoking.urge.MindfulnessVideoPlayerScreen
import com.example.quitesmoking.urge.WithdrawalReliefTipsScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            QuitesmokingTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = if (FirebaseAuth.getInstance().currentUser != null)
                         "main" else Routes.LOGIN
                ) {

                    composable("main") { MainScreen(navController) }

                    // auth
                    composable(Routes.LOGIN)    { LoginScreen(navController) }
                    composable(Routes.REGISTER) { RegisterScreen(navController) }

                   
                    
 
                    composable(Routes.LEAD)          { LeaderboardScreen() }
                    composable(Routes.COMMUNITY)     { CommunityScreen(navController) }
                    /* ---------- NEW morning / night / weekly ---------- */
                    composable(Routes.WEEKLY_GOAL_EDIT) { WeeklyGoalBuilderScreen(navController) }
                    composable(Routes.MORNING_CHECK) { MorningCheckInScreen(navController) }
                    composable(Routes.NIGHT_CHECK)   { NightCheckInScreen(navController) }
                    /* ---------- Purchase tracking ---------- */
//                    composable(Routes.LOG_PURCHASE)     { LogPurchaseScreen(navController) }
                    
                    /* ---------- craving tips ---------- */
                    composable("craving_tips") {
                        CravingTipsScreen(navController)
                    }

                    /* ---------- mindfulness video ---------- */
                    composable(
                        route = "mindfulness_video_screen/{videoUrl}",
                        arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                        MindfulnessVideoPlayerScreen(navController = navController, videoUrl = videoUrl)
                    }

                    /* ---------- withdrawal relief tips ---------- */
                    composable("withdrawal_relief_tips") {
                        WithdrawalReliefTipsScreen(navController)
                    }
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

