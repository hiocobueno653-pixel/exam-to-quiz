package com.examtoquiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.examtoquiz.data.*
import com.examtoquiz.network.ApiService
import kotlinx.coroutines.launch

@Composable
fun AiPage(api: ApiService, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState) {
    var tab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Tabs
        Surface(tonalElevation = 2.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("生成","解析","问答").forEachIndexed { i, label ->
                    TextButton(onClick = { tab = i }) {
                        Text(label, fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal, color = if (tab == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        when (tab) {
            0 -> AiGenerateTab(api, scope, snackbar)
            1 -> AiParseTab(api, scope, snackbar)
            2 -> AiChatTab(api, scope, snackbar)
        }
    }
}

@Composable
fun AiGenerateTab(api: ApiService, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState) {
    var topic by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("数学") }
    var count by remember { mutableIntStateOf(5) }
    var result by remember { mutableStateOf<List<Question>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("主题") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("学科") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            loading = true
            scope.launch {
                try {
                    val resp = api.aiGenerate(AiGenerateRequest(topic, subject, count, "单选,多选,判断"))
                    result = resp.questions ?: emptyList()
                } catch (e: Exception) { snackbar.showSnackbar("生成失败: ${e.message}") }
                loading = false
            }
        }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), enabled = !loading) {
            Text(if (loading) "生成中..." else "生成题目")
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(result) { q ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("[${q.type}]", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(q.difficulty, fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Text(q.content, fontSize = 14.sp)
                        Text("答案: ${q.answer}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun AiParseTab(api: ApiService, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState) {
    var text by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<List<Question>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("粘贴题目文本") }, modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            loading = true
            scope.launch {
                try {
                    val resp = api.aiParse(AiParseRequest(text))
                    result = resp.questions ?: emptyList()
                } catch (e: Exception) { snackbar.showSnackbar("解析失败: ${e.message}") }
                loading = false
            }
        }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), enabled = !loading) {
            Text(if (loading) "解析中..." else "智能解析")
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(result) { q ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), elevation = CardDefaults.cardElevation(1.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("[${q.type}] ${q.content}", fontSize = 14.sp)
                        Text("答案: ${q.answer}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun AiChatTab(api: ApiService, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState) {
    var messages by remember { mutableStateOf(listOf(ChatMessage("assistant", "你好！我是 AI 学习助手，有什么可以帮助你的？"))) }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (msg.role == "user") Alignment.End else Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (msg.role == "user") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            msg.content,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            color = if (msg.role == "user") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Surface(tonalElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("输入你的问题...") }
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    val msg = input.trim(); if (msg.isEmpty()) return@IconButton
                    input = ""
                    messages = messages + ChatMessage("user", msg)
                    loading = true
                    scope.launch {
                        try {
                            val resp = api.aiChat(AiChatRequest(msg, messages))
                            messages = messages + ChatMessage("assistant", resp.reply ?: "抱歉，AI 回复失败")
                        } catch (e: Exception) {
                            messages = messages + ChatMessage("assistant", "错误: ${e.message}")
                        }
                        loading = false
                    }
                }, enabled = !loading) {
                    Icon(Icons.Filled.Send, "发送")
                }
            }
        }
    }
}
