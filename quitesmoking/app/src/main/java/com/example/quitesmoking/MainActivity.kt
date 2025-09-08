package com.example.quitesmoking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
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
import com.example.quitesmoking.ui.LogPurchaseScreen
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

                Scaffold(
                    bottomBar = {
                        if (FirebaseAuth.getInstance().currentUser != null) {
                            BottomBar(navController = navController)
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = if (FirebaseAuth.getInstance().currentUser != null)
                            Routes.HOME else Routes.LOGIN
                    ) {
                    /* ---------- core & auth ---------- */
                    composable(Routes.HOME)     { HomeScreen(navController) }
                    composable(Routes.LOGIN)    { LoginScreen(navController) }
                    composable(Routes.REGISTER) { RegisterScreen(navController) }

                    /* ---------- original routes ---------- */
                    composable(Routes.CHAT)     { ChatScreen(navController) }
                    composable(Routes.URGE)     { UrgeTabScreen(navController) }
                    composable(Routes.PROGRESS) { StatsScreen(navController) }
                    composable(Routes.GPT_CHAT) { GPTChatScreen(navController) }
                    composable(Routes.COMMUNITY)     { CommunityScreen(navController) }
                    composable(Routes.LEAD) { LeaderboardScreen() }

                    /* ---------- NEW morning / night / weekly ---------- */
                    composable(Routes.MORNING_CHECK)    { MorningCheckInScreen(navController) }
                    composable(Routes.NIGHT_CHECK)      { NightCheckInScreen(navController) }
                    composable(Routes.WEEKLY_GOAL_EDIT) { WeeklyGoalBuilderScreen(navController) }
                    
                    /* ---------- Purchase tracking ---------- */
                    composable(Routes.LOG_PURCHASE)     { LogPurchaseScreen(navController) }

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
}

//package com.example.quitesmoking
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.navigation.compose.*
//import com.example.quitesmoking.auth.LoginScreen
//import com.example.quitesmoking.auth.RegisterScreen
//import com.example.quitesmoking.chat.ChatScreen
//import com.example.quitesmoking.chat.CommunityScreen
//import com.example.quitesmoking.chat.GPTChatScreen
//import com.example.quitesmoking.chat.LeaderboardScreen
//import com.example.quitesmoking.navigation.Routes
//import com.example.quitesmoking.ui.MorningCheckInScreen
//import com.example.quitesmoking.ui.NightCheckInScreen
//import com.example.quitesmoking.ui.WeeklyGoalBuilderScreen
//import com.example.quitesmoking.ui.theme.QuitesmokingTheme
//import com.example.quitesmoking.urge.UrgeTabScreen
//import com.google.firebase.FirebaseApp
//import com.google.firebase.auth.FirebaseAuth
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        FirebaseApp.initializeApp(this)
//
//        setContent {
//            QuitesmokingTheme {
//                val navController = rememberNavController()
//
//                NavHost(
//                    navController = navController,
//                    startDestination = if (FirebaseAuth.getInstance().currentUser != null)
//                        Routes.HOME else Routes.LOGIN
//                ) {
//                    /* ---------- core & auth ---------- */
//                    composable(Routes.HOME)     { HomeScreen(navController) }
//                    composable(Routes.LOGIN)    { LoginScreen(navController) }
//                    composable(Routes.REGISTER) { RegisterScreen(navController) }
//
//                    /* ---------- original routes ---------- */
//                    composable(Routes.CHAT)     { ChatScreen(navController) }
//                    composable(Routes.URGE)     { UrgeTabScreen(navController) }
//                    composable(Routes.PROGRESS) { StatsScreen(navController) }
//                    composable(Routes.GPT_CHAT) { GPTChatScreen(navController) }
//                    composable(Routes.COMMUNITY)     { CommunityScreen(navController) }
//                    composable(Routes.LEAD) { LeaderboardScreen() }
//
//                    /* ---------- NEW morning / night / weekly ---------- */
//                    composable(Routes.MORNING_CHECK)    { MorningCheckInScreen(navController) }
//                    composable(Routes.NIGHT_CHECK)      { NightCheckInScreen(navController) }
//                    composable(Routes.WEEKLY_GOAL_EDIT) { WeeklyGoalBuilderScreen(navController) }
//                }
//            }
//        }
//    }
//}
