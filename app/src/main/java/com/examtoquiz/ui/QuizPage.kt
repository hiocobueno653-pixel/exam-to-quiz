package com.examtoquiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.examtoquiz.data.*
import com.examtoquiz.network.ApiService
import kotlinx.coroutines.launch

@Composable
fun QuizPage(api: ApiService, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState) {
    var step by remember { mutableIntStateOf(0) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf(mapOf<Int, String>()) }
    var result by remember { mutableStateOf<QuizResult?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        when (step) {
            0 -> {
                Text("选题答题", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                questions = api.generateQuiz(QuizGenerateRequest(emptyList(), listOf("单选","多选","判断"), listOf("简单","中等","困难"), 10))
                                currentIdx = 0; answers = questions.associate { it.id to "" }; step = 1
                            } catch (e: Exception) { snackbar.showSnackbar("生成失败: ${e.message}") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)
                ) { Text("开始答题", fontSize = 16.sp) }
            }
            1 -> {
                val q = questions.getOrNull(currentIdx)
                if (q != null) {
                    LinearProgressIndicator(progress = { (currentIdx + 1).toFloat() / questions.size }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primaryContainer)
                    Spacer(Modifier.height(12.dp))
                    Text("第 ${currentIdx+1}/${questions.size} 题", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(q.content, fontSize = 16.sp, lineHeight = 24.sp)
                            Spacer(Modifier.height(16.dp))
                            q.options?.forEach { opt ->
                                val sel = answers[q.id] == opt.label
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = if (sel) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { answers = answers + (q.id to opt.label) }
                                ) { Text("${opt.label}. ${opt.text}", modifier = Modifier.padding(12.dp), fontSize = 15.sp) }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (currentIdx > 0) OutlinedButton(onClick = { currentIdx-- }, shape = RoundedCornerShape(12.dp)) { Text("上一题") }
                        else Spacer(Modifier.width(1.dp))
                        Button(onClick = {
                            if (currentIdx < questions.size - 1) currentIdx++
                            else {
                                scope.launch {
                                    try {
                                        result = api.gradeQuiz(QuizGradeRequest(answers))
                                        step = 2
                                    } catch (e: Exception) { snackbar.showSnackbar("批改失败: ${e.message}") }
                                }
                            }
                        }, shape = RoundedCornerShape(12.dp)) { Text(if (currentIdx < questions.size - 1) "下一题" else "交卷") }
                    }
                }
            }
            2 -> {
                val r = result
                if (r != null) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${r.score}", fontWeight = FontWeight.Bold, fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
                            Text("分", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("正确 ${r.correct}/${r.total}", fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(r.results) { item ->
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), elevation = CardDefaults.cardElevation(1.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (item.isCorrect) "✅" else "❌", fontSize = 16.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(item.content, fontSize = 14.sp, maxLines = 2)
                                    }
                                    Text("正确答案: ${item.correctAnswer}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    if (!item.isCorrect) Text("你的答案: ${item.userAnswer}", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                                    if (item.analysis.isNotEmpty()) Text(item.analysis, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { step = 0; questions = emptyList(); result = null }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("返回") }
                }
            }
        }
    }
}
