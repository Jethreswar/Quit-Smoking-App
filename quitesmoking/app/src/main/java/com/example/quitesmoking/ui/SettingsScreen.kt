package com.example.quitesmoking.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User Profile Section
            SettingsSection(
                title = "Profile",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your personal information",
                        onClick = { /* Navigate to profile edit */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Email,
                        title = "Email Settings",
                        subtitle = "Manage email notifications",
                        onClick = { /* Navigate to email settings */ }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Preferences Section
            SettingsSection(
                title = "Preferences",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Configure push notifications",
                        onClick = { /* Navigate to notification settings */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "Theme",
                        subtitle = "Choose light or dark mode",
                        onClick = { /* Navigate to theme settings */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "Select your preferred language",
                        onClick = { /* Navigate to language settings */ }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data & Privacy Section
            SettingsSection(
                title = "Data & Privacy",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Settings",
                        subtitle = "Manage your data privacy",
                        onClick = { /* Navigate to privacy settings */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.CloudDownload,
                        title = "Export Data",
                        subtitle = "Download your data",
                        onClick = { /* Navigate to data export */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Delete Account",
                        subtitle = "Permanently delete your account",
                        onClick = { /* Navigate to account deletion */ }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Support Section
            SettingsSection(
                title = "Support",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help Center",
                        subtitle = "Get help and support",
                        onClick = { /* Navigate to help center */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Feedback,
                        title = "Send Feedback",
                        subtitle = "Share your thoughts with us",
                        onClick = { /* Navigate to feedback form */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "App version and information",
                        onClick = { /* Navigate to about screen */ }
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Account Actions Section
            SettingsSection(
                title = "Account",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Logout,
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        onClick = { showLogoutDialog = true },
                        textColor = Color.Red
                    )
                )
            )
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text("Sign Out", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsItemRow(
                        item = item,
                        showDivider = index < items.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemRow(
    item: SettingsItem,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { item.onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.textColor ?: MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = item.textColor ?: MaterialTheme.colorScheme.onSurface
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 56.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val onClick: () -> Unit,
    val textColor: Color? = null
)
