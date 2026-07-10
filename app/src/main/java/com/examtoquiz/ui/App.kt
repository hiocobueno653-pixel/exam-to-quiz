package com.examtoquiz.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.text.TextStyle

const val DEEPSEEK_API_KEY = "YOUR_API_KEY_HERE" // TODO: 从安全存储读取
const val DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1"

data class NavItem(val label: String, val filled: ImageVector, val outlined: ImageVector)

@Composable
fun App() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { GlassBottomBar(selectedTab) { selectedTab = it } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardPage(scope, snackbarHostState)
                1 -> BankPage(scope, snackbarHostState)
                2 -> PracticePage(scope, snackbarHostState)
            }
        }
    }
}

@Composable
fun GlassBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        NavItem("首页", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("题库", Icons.Filled.Book, Icons.Outlined.Book),
        NavItem("练习", Icons.Filled.Quiz, Icons.Outlined.Quiz),
    )

    var barWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val itemWidth = if (barWidth > 0) barWidth / items.size else 0f
    val indicatorOffset by animateDpAsState(
        targetValue = with(density) { (selected * itemWidth + (itemWidth - 52.dp.toPx()) / 2).toDp() },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f), label = "indicator"
    )

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.18f))), shape = RoundedCornerShape(32.dp))
                .background(brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f), Color.White.copy(alpha = 0.08f))), shape = RoundedCornerShape(32.dp))
                .onSizeChanged { barWidth = it.width }
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(1.5.dp)
                .background(brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.90f), Color.White.copy(alpha = 0.90f), Color.White.copy(alpha = 0.15f), Color.Transparent))))

            Box(modifier = Modifier.offset { IntOffset(indicatorOffset.roundToPx(), 6.dp.roundToPx()) }
                .width(52.dp).fillMaxHeight().padding(vertical = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.42f))), shape = RoundedCornerShape(24.dp)))

            Row(modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                items.forEachIndexed { index, item ->
                    val isSelected = selected == index
                    Column(modifier = Modifier.clickable { onSelect(index) }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = if (isSelected) item.filled else item.outlined, contentDescription = item.label, modifier = Modifier.size(22.dp), tint = if (isSelected) Color(0xFF2563EB) else Color(0xFF94A3B8))
                        Spacer(Modifier.height(1.dp))
                        Text(text = item.label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color(0xFF2563EB) else Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardPage(scope: CoroutineScope, snackbar: SnackbarHostState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("欢迎使用考试助手", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text("本地题库: 0 题", fontSize = 16.sp)
        Text("最近练习: 0 次", fontSize = 16.sp)
    }
}

@Composable
fun BankPage(scope: CoroutineScope, snackbar: SnackbarHostState) {
    var subject by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("题库管理", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("科目") })
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("主题") })
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            // TODO: 实现题库搜索逻辑
                            loading = false
                        }
                    }, enabled = !loading) {
                        Text("搜索题库")
                    }
                }
            }
        }
        questions.forEach { q ->
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(q.content, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PracticePage(scope: CoroutineScope, snackbar: SnackbarHostState) {
    var subTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp)).padding(3.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("选题练习", "AI 助手").forEachIndexed { i, label ->
                val active = subTab == i
                TextButton(onClick = { subTab = i }, modifier = Modifier.weight(1f).then(if (active) Modifier.background(Color.White, RoundedCornerShape(13.dp)) else Modifier), colors = ButtonDefaults.textButtonColors(contentColor = if (active) Color(0xFF2563EB) else Color(0xFF64748B))) {
                    Text(label, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        when (subTab) {
            0 -> QuizPracticeContent(scope, snackbar)
            1 -> AiAssistantContent(scope, snackbar)
        }
    }
}

@Composable
fun QuizPracticeContent(scope: CoroutineScope, snackbar: SnackbarHostState) {
    Text("选题练习功能开发中...", fontSize = 16.sp)
}

@Composable
fun AiAssistantContent(scope: CoroutineScope, snackbar: SnackbarHostState) {
    var message by remember { mutableStateOf("") }
    var chatHistory by remember { mutableStateOf(listOf("你好！我是你的 AI 学习助手。")) }
    var loading by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(chatHistory) { msg ->
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Text(msg, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row {
            OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.weight(1f))
            Button(onClick = {
                scope.launch {
                    if (message.isBlank()) return@launch
                    loading = true
                    chatHistory = chatHistory + "你: $message"
                    try {
                        // TODO: 实现直连 DeepSeek API
                        val response = callDeepSeekAPI(message)
                        chatHistory = chatHistory + response
                    } catch (e: Exception) {
                        snackbar.showSnackbar("请求失败: ${e.message}")
                    }
                    message = ""
                    loading = false
                }
            }, enabled = !loading) {
                Text("发送")
            }
        }
    }
}

// 实际实现已集成 DeepSeekApiService
suspend fun callDeepSeekAPI(message: String): String {
    // TODO: 实现实际的 HTTP 请求
    return "AI 回复: $message"
}
