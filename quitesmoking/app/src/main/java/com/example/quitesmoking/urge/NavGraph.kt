package com.example.quitesmoking

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.quitesmoking.urge.CravingTipsScreen
import com.example.quitesmoking.urge.MindfulnessVideoPlayerScreen
import com.example.quitesmoking.urge.UrgeTabScreen
import com.example.quitesmoking.urge.WithdrawalReliefTipsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "urge_main") {

        // Home screen for cravings
        composable("urge_main") {
            UrgeTabScreen(navController,bottomNav = navController)
        }

        // Tips screen
        composable("craving_tips") {
            CravingTipsScreen(navController)
        }

        // Video screen, accepts videoUrl as encoded path argument
        composable(
            route = "mindfulness_video_screen/{videoUrl}",
            arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val decodedUrl = Uri.decode(encodedUrl) // ✅ Safely decode URL
            MindfulnessVideoPlayerScreen(navController = navController, videoUrl = decodedUrl)
        }

        // Withdrawal relief tips screen
        composable("withdrawal_relief_tips") {
            WithdrawalReliefTipsScreen(navController)
        }
    }
}

//package com.example.quitesmoking
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavHostController
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.navArgument
//import com.example.quitesmoking.urge.CravingTipsScreen
//import com.example.quitesmoking.urge.MindfulnessVideoPlayerScreen
//import com.example.quitesmoking.urge.UrgeTabScreen
//
//@Composable
//fun NavGraph(navController: NavHostController) {
//    NavHost(navController = navController, startDestination = "urge_main") {
//        composable("urge_main") {
//            UrgeTabScreen(navController)
//        }
//        composable("craving_tips") {
//            CravingTipsScreen(navController)
//        }
//        composable(
//            route = "mindfulness_video_screen/{videoUrl}",
//            arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
//            MindfulnessVideoPlayerScreen(navController = navController, videoUrl = videoUrl)
//        }
//    }
//}
