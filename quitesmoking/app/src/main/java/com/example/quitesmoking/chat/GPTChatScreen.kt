package com.example.quitesmoking.chat

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
 import retrofit2.http.Headers
import retrofit2.http.POST

fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

data class GPTMessage(val role: String, val content: String)
data class ChatMessage(val role: String, val content: String)
data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7
)
data class ChatChoice(val message: ChatMessage)
data class ChatResponse(val choices: List<ChatChoice>)

interface OpenAIService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer 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" // OPENAI_API_KEY
    )
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(@Body request: ChatRequest): retrofit2.Response<ChatResponse>
}

object OpenAIClient {
    val api: OpenAIService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIService::class.java)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GPTChatScreen(navController: NavController) {
    val messages = remember { mutableStateListOf<GPTMessage>() }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(
                GPTMessage(
                    "assistant",
                    "👋 Welcome to Quit GPT! I'm here to support you on your journey. Type your message below to get started."
                )
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quit GPT") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type your message...") },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color(0xFFF0F0F5),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (!isNetworkAvailable(context)) {
                            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                            messages.add(GPTMessage("assistant", "⚠️ No internet connection"))
                            scope.launch { listState.animateScrollToItem(messages.size - 1) }
                            return@IconButton
                        }

                        if (inputText.isNotBlank()) {
                            val userMsg = GPTMessage("user", inputText)
                            messages.add(userMsg)
                            scope.launch { listState.animateScrollToItem(messages.size - 1) }
                            inputText = ""

                            scope.launch {
                                val request = ChatRequest(
                                    messages = listOf(ChatMessage("system", "You are a helpful assistant.")) +
                                            messages.map { ChatMessage(it.role, it.content) }
                                )
                                try {
                                    val response = OpenAIClient.api.getChatCompletion(request)
                                    if (response.isSuccessful) {
                                        val reply = response.body()?.choices?.firstOrNull()?.message
                                        if (reply != null) {
                                            messages.add(GPTMessage(reply.role, reply.content))
                                            listState.animateScrollToItem(messages.size - 1)
                                        } else {
                                            messages.add(GPTMessage("assistant", "⚠️ No response in body."))
                                            listState.animateScrollToItem(messages.size - 1)
                                            Log.e("GPTChat", "Empty response body")
                                        }
                                    } else {
                                        val code = response.code()
                                        val errorBody = response.errorBody()?.string()
                                        val msg = "API Error $code: $errorBody"
                                        messages.add(GPTMessage("assistant", msg))
                                        listState.animateScrollToItem(messages.size - 1)
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        Log.e("GPTChat", msg)
                                    }
                                } catch (e: Exception) {
                                    val errorMsg = "⚠️ Exception: ${e.localizedMessage}"
                                    messages.add(GPTMessage("assistant", errorMsg))
                                    listState.animateScrollToItem(messages.size - 1)
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    Log.e("GPTChat", "Exception during call", e)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF673AB7), shape = CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isUser) Color(0xFF673AB7) else Color(0xFFEFEFF2),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.content,
                            color = if (isUser) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}


//package com.example.quitesmoking.chat
//
//import android.content.Context
//import android.net.ConnectivityManager
//import android.net.NetworkCapabilities
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import kotlinx.coroutines.launch
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Body
//import retrofit2.http.Headers
//import retrofit2.http.POST
//import android.widget.Toast
//
//// ===== 网络可用性检查 =====
//fun isNetworkAvailable(context: Context): Boolean {
//    val connectivityManager =
//        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    val network = connectivityManager.activeNetwork ?: return false
//    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//}
//
//// ===== 数据模型 =====
//data class GPTMessage(val role: String, val content: String)
//data class ChatMessage(val role: String, val content: String)
//data class ChatRequest(
//    val model: String = "gpt-3.5-turbo",
//    val messages: List<ChatMessage>,
//    val temperature: Double = 0.7
//)
//data class ChatChoice(val message: ChatMessage)
//data class ChatResponse(val choices: List<ChatChoice>)
//
//// ===== Retrofit 接口定义 =====
//interface OpenAIService {
//    @Headers(
//        "Content-Type: application/json",
//        "Authorization: Bearer YOUR_OPENAI_API_KEY"
//    )
//    @POST("v1/chat/completions")
//    suspend fun getChatCompletion(@Body request: ChatRequest): retrofit2.Response<ChatResponse>
//}
//
//object OpenAIClient {
//    val api: OpenAIService by lazy {
//        Retrofit.Builder()
//            .baseUrl("https://api.openai.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(OpenAIService::class.java)
//    }
//}
//
//// ===== Compose 聊天界面 =====
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun GPTChatScreen(navController: NavController) {
//    val messages = remember { mutableStateListOf<GPTMessage>() }
//    var inputText by remember { mutableStateOf("") }
//    val scope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Quit GPT") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        },
//        bottomBar = {
//            Row(
//                modifier = Modifier
//                    .padding(12.dp)
//                    .fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TextField(
//                    value = inputText,
//                    onValueChange = { inputText = it },
//                    placeholder = { Text("Type your message...") },
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(56.dp),
//                    shape = RoundedCornerShape(28.dp),
//                    colors = TextFieldDefaults.textFieldColors(
//                        containerColor = Color(0xFFF0F0F5),
//                        unfocusedIndicatorColor = Color.Transparent,
//                        focusedIndicatorColor = Color.Transparent
//                    )
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                IconButton(
//                    onClick = {
//                        if (!isNetworkAvailable(context)) {
//                            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
//                            return@IconButton
//                        }
//
//                        if (inputText.isNotBlank()) {
//                            val userMsg = GPTMessage("user", inputText)
//                            messages.add(userMsg)
//
//                            scope.launch {
//                                val request = ChatRequest(
//                                    messages = listOf(ChatMessage("system", "You are a helpful assistant.")) +
//                                            messages.map { ChatMessage(it.role, it.content) }
//                                )
//                                try {
//                                    val response = OpenAIClient.api.getChatCompletion(request)
//                                    val reply = response.body()?.choices?.firstOrNull()?.message
//                                    if (reply != null) {
//                                        messages.add(GPTMessage(reply.role, reply.content))
//                                    } else {
//                                        messages.add(GPTMessage("assistant", "No response received."))
//                                    }
//                                } catch (e: Exception) {
//                                    messages.add(GPTMessage("assistant", "Error: ${e.localizedMessage}"))
//                                }
//                            }
//
//                            inputText = ""
//                        }
//                    },
//                    modifier = Modifier
//                        .size(48.dp)
//                        .background(Color(0xFF673AB7), shape = CircleShape)
//                ) {
//                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
//                }
//            }
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(padding)
//                .padding(horizontal = 16.dp, vertical = 8.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(messages) { msg ->
//                val isUser = msg.role == "user"
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .background(
//                                if (isUser) Color(0xFF673AB7) else Color(0xFFF0F0F5),
//                                shape = RoundedCornerShape(16.dp)
//                            )
//                            .padding(12.dp)
//                            .widthIn(max = 280.dp)
//                    ) {
//                        Text(
//                            text = msg.content,
//                            color = if (isUser) Color.White else Color.Black
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
