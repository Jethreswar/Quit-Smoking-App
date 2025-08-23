package com.example.quitesmoking.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quitesmoking.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }

    Column(Modifier.padding(24.dp)) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = nickname, onValueChange = { nickname = it }, label = { Text("Nickname") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(userId)
                                .set(mapOf("nickname" to nickname))
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Routes.LOGIN)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Registered but failed to save nickname.", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Failed to get user ID.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }) {
            Text("Register")
        }

        TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
            Text("Already have an account? Log in")
        }
    }
}
