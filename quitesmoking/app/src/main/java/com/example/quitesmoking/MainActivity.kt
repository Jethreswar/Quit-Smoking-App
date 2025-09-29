package com.example.quitesmoking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quitesmoking.auth.LoginScreen
import com.example.quitesmoking.auth.RegisterScreen
import com.example.quitesmoking.chat.CommunityScreen
import com.example.quitesmoking.chat.GPTChatScreen
import com.example.quitesmoking.chat.LeaderboardScreen
import com.example.quitesmoking.navigation.Routes
import com.example.quitesmoking.onboarding.OnboardingConfigRepo
import com.example.quitesmoking.onboarding.OnboardingRepository
import com.example.quitesmoking.onboarding.OnboardingScreen
import com.example.quitesmoking.onboarding.OnboardingSummaryScreen
import com.example.quitesmoking.onboarding.OnboardingViewModel
import com.example.quitesmoking.ui.LogPurchaseScreen
import com.example.quitesmoking.ui.MorningCheckInScreen
import com.example.quitesmoking.ui.NightCheckInScreen
import com.example.quitesmoking.ui.SettingsScreen
import com.example.quitesmoking.ui.WeeklyGoalBuilderScreen
import com.example.quitesmoking.ui.theme.QuitesmokingTheme
import com.example.quitesmoking.HomeScreen
import com.example.quitesmoking.StatsScreen
import com.example.quitesmoking.navigation.Routes.MINDFULNESS_LIST
import com.example.quitesmoking.urge.CravingTipsScreen
import com.example.quitesmoking.urge.MindfulnessVideoPlayerScreen
import com.example.quitesmoking.urge.UrgeTabScreen
import com.example.quitesmoking.urge.MindfulnessVideosScreen
import com.example.quitesmoking.urge.WithdrawalReliefTipsScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        val configRepo = OnboardingConfigRepo(
            appContext = applicationContext,
            db = firestore,
            localResId = R.raw.onboarding,
            firestoreDocPath = "configs/onboarding",
            preferLocalFirst = true
        )

        val answersRepo = OnboardingRepository(db = firestore, auth = auth)

        setContent {
            QuitesmokingTheme {
                val navController = rememberNavController()

                // ✅ ONE shared VM for the whole onboarding flow
                val onboardingVm = remember { OnboardingViewModel(configRepo, answersRepo) }

                NavHost(
                    navController = navController,
                    startDestination = Routes.SPLASH
                ) {
                    /* ---------- SPLASH gate ---------- */
                    composable(Routes.SPLASH) {
                        SplashGate(
                            auth = auth,
                            onGoLogin = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            },
                            onGoOnboarding = {
                                navController.navigate(Routes.ONBOARDING) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            },
                            onGoMain = {
                                navController.navigate("main") {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            }
                        )
                    }

                    /* ---------- ONBOARDING flow ---------- */
                    composable(Routes.ONBOARDING) {
                        OnboardingScreen(
                            vm = onboardingVm,                 // ✅ shared VM
                            onCompleted = {
                                navController.navigate(Routes.ONBOARDING_SUMMARY)
                            }
                        )
                    }

                    /* ---------- Summary: review + save once, then Home ---------- */
                    composable(Routes.ONBOARDING_SUMMARY) {
                        OnboardingSummaryScreen(
                            vm = onboardingVm,
                            onEdit = {
                                // Go to the onboarding screen at the question VM was set to
                                navController.navigate(Routes.ONBOARDING) {
                                    launchSingleTop = true
                                    // Keep Summary in the back stack so we can come back
                                }
                            },// ✅ same shared VM
                            onGoHome = {
                                navController.navigate("main") {
                                    popUpTo(0) { inclusive = true } // clear stack after saving
                                }
                            }
                        )
                    }

                    /* ---------- MAIN (bottom nav shell) ---------- */
                    composable("main") { MainScreen(navController) }

                    /* ---------- Auth ---------- */
                    composable(Routes.LOGIN)    { LoginScreen(navController) }
                    composable(Routes.REGISTER) { RegisterScreen(navController) }

                    /* ---------- Feature routes ---------- */
                    composable(Routes.LEAD)          { LeaderboardScreen(navController) }
                    composable(Routes.COMMUNITY)     { CommunityScreen(navController) }
                    composable(Routes.MORNING_CHECK) { MorningCheckInScreen(navController) }
                    composable(Routes.NIGHT_CHECK)   { NightCheckInScreen(navController) }

                    /* ---------- Purchase tracking ---------- */
                    composable(Routes.LOG_PURCHASE)     { LogPurchaseScreen(navController) }
                    
                    /* ---------- craving tips ---------- */
                    composable("craving_tips") {
                        CravingTipsScreen(navController)
                    }
                    composable(MINDFULNESS_LIST) {
                        MindfulnessVideosScreen(navController)
                    }

                    composable(
                        route = "mindfulness_video_screen/{videoUrl}",
                        arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
                        MindfulnessVideoPlayerScreen(navController = navController, videoUrl = videoUrl)
                    }

                    composable("withdrawal_relief_tips") { WithdrawalReliefTipsScreen(navController) }
                    composable(Routes.SETTINGS) { SettingsScreen(navController) }
                }
            }
        }
    }
}

/* -------------------- SPLASH gate -------------------- */
@Composable
private fun SplashGate(
    auth: FirebaseAuth,
    onGoLogin: () -> Unit,
    onGoOnboarding: () -> Unit,
    onGoMain: () -> Unit
) {
    // tiny spinner while we check Firestore
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

    LaunchedEffect(Unit) {
        val fbUser = auth.currentUser
        if (fbUser == null) {
            onGoLogin()
            return@LaunchedEffect
        }

        val db = FirebaseFirestore.getInstance()
        val uid = fbUser.uid
        val docRef = db.collection("users").document(uid)

        // Ensure doc exists for first-time sign-in
        var snap = docRef.get().await()
        if (!snap.exists()) {
            docRef.set(
                mapOf(
                    "id" to uid,
                    "email" to fbUser.email,
                    "name" to (fbUser.displayName ?: ""),
                    "completedOnboarding" to false
                ),
                SetOptions.merge()
            ).await()
            snap = docRef.get().await()
        }

        val completed = snap.getBoolean("completedOnboarding") == true
        Log.d(
            "AUTH_USER",
            "DB → path=${docRef.path}, docId=${snap.id}, email=${snap.getString("email")}, " +
                    "name=${snap.getString("name")}, completedOnboarding=$completed"
        )

        if (completed) onGoMain() else onGoOnboarding()
    }
}

/* -------------------- Main shell -------------------- */
@Composable
fun MainScreen(rootNavController: NavController) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = bottomNavController,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME)             { HomeScreen(rootNavController) }
                composable(Routes.URGE)             { UrgeTabScreen(rootNavController, bottomNavController) }
                composable(Routes.PROGRESS)         { StatsScreen(rootNavController, bottomNavController) }
                composable(Routes.GPT_CHAT)         { GPTChatScreen(rootNavController, bottomNavController) }
                composable(Routes.WEEKLY_GOAL_EDIT) { WeeklyGoalBuilderScreen(rootNavController, bottomNavController) }
            }
        }
    }
}
