package com.example.quitesmoking.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import com.example.quitesmoking.*
import com.example.quitesmoking.ui.*
import com.example.quitesmoking.chat.ChatScreen           // ← adjust if package differs
import com.example.quitesmoking.chat.GPTChatScreen       // …
import com.example.quitesmoking.urge.UrgeTabScreen
import com.example.quitesmoking.auth.LoginScreen
import com.example.quitesmoking.auth.RegisterScreen
import com.example.quitesmoking.*             // launcher / flow / success
import com.example.quitesmoking.chat.CommunityScreen
import com.example.quitesmoking.chat.LeaderboardScreen

/**
 * Centralised list of route names so they’re type-safe and discoverable.
 * Feel free to split them by feature if this grows too big.
 */
object Routes {
    // ───────── Auth ─────────
    const val LOGIN    = "login"
    const val REGISTER = "register"

    // ───────── Core tabs / home ─────────
    const val HOME     = "home"
    const val PROGRESS = "stats"          // Progress / Stats screen
    const val URGE     = "urge"

    // ───────── Community chat ─────────
    const val CHAT         = "community_chat"
    const val GPT_CHAT     = "gpt_chat"
    const val COMMUNITY    = "community"
    const val LEAD         = "lead"

    // ───────── New spec: purchase tracker lives in PROGRESS route (no extra)

    // ───────── New spec: Morning / Night / Weekly goal ─────────
    const val MORNING_CHECK     = "morning_check"
    const val NIGHT_CHECK       = "night_check"
    const val WEEKLY_GOAL_EDIT  = "weekly_goal"

    const val MINDFULNESS_LIST = "mindfulness_list"

    
    // ───────── Purchase tracking ─────────
    const val LOG_PURCHASE      = "log_purchase"
    
    // ───────── Settings ─────────
    const val SETTINGS          = "settings"

    // ───────── ONBOARDING ─────────
    const val ONBOARDING= "onboarding"
    const val SPLASH = "splash"

    const val ONBOARDING_SUMMARY = "onboarding_summary"
}