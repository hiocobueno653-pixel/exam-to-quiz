package com.examtoquiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.examtoquiz.data.*
import com.examtoquiz.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PracticePage(api: ApiService, scope: CoroutineScope, snackbar: SnackbarHostState) {
    var subTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp)).padding(3.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("选题练习", "AI 助手").forEachIndexed { i, label ->
                val active = subTab == i
                TextButton(onClick = { subTab = i }, modifier = Modifier.weight(1f).then(if (active) Modifier.background(Color.White, RoundedCornerShape(13.dp)) else Modifier), colors = ButtonDefaults.textButtonColors(contentColor = if (active) Color(0xFF2563EB) else Color(0xFF64748B))) {
                    Text(label, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        when (subTab) { 0 -> QuizContent(api, scope, snackbar); 1 -> AiContent(api, scope, snackbar) }
    }
}

@Composable
fun QuizContent(api: ApiService, scope: CoroutineScope, snackbar: SnackbarHostState) {
    var step by remember { mutableIntStateOf(0) }
    var subjects by remember { mutableStateOf(listOf("数学", "英语", "语文")) }
    var types by remember { mutableStateOf(listOf("单选", "多选", "判断")) }
    var difficulties by remember { mutableStateOf(listOf("简单", "中等", "困难")) }
    var selectedSubject by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("") }
    var questionCount by remember { mutableStateOf("10") }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var answers by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var result by remember { mutableStateOf<QuizResult?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val filters = api.getFilters()
            if (filters.subjects.isNotEmpty()) subjects = filters.subjects
            if (filters.types.isNotEmpty()) types = filters.types
            if (filters.difficulties.isNotEmpty()) difficulties = filters.difficulties
        } catch (_: Exception) {}
    }

    when (step) {
        0 -> QuizStepSelect(api, scope, snackbar, subjects, types, difficulties, selectedSubject, selectedType, selectedDifficulty, questionCount, loading) { s, t, d, c ->
            subjects = s; types = t; difficulties = d
        } { cnt -> questionCount = cnt } {
            scope.launch {
                loading = true
                try {
                    questions = api.generateQuiz(QuizGenerateRequest(
                        subjects = if (selectedSubject.isNotEmpty()) listOf(selectedSubject) else subjects,
                        types = if (selectedType.isNotEmpty()) listOf(selectedType) else types,
                        difficulties = if (selectedDifficulty.isNotEmpty()) listOf(selectedDifficulty) else difficulties,
                        count = questionCount.toIntOrNull() ?: 10
                    ))
                    step = 1
                } catch (e: Exception) { snackbar.showSnackbar("生成失败: ${e.message}") }
                loading = false
            }
        }
        1 -> QuizStepAnswer(api, scope, snackbar, questions, answers, loading) { a -> answers = a } {
            scope.launch {
                loading = true
                try { result = api.gradeQuiz(QuizGradeRequest(answers = answers)); step = 2 }
                catch (e: Exception) { snackbar.showSnackbar("提交失败: ${e.message}") }
                loading = false
            }
        } { step = 0 }
        2 -> QuizStepResult(result, questions, answers) { step = 0 } { result = null; questions = emptyList(); answers = emptyMap(); step = 0 }
    }
}


@Composable
fun QuizStepSelect(
    api: ApiService,
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
    subjects: List<String>,
    types: List<String>,
    difficulties: List<String>,
    selectedSubject: String,
    selectedType: String,
    selectedDifficulty: String,
    questionCount: String,
    loading: Boolean,
    onUpdateLists: (List<String>, List<String>, List<String>) -> Unit,
    onUpdateCount: (String) -> Unit,
    onGenerate: () -> Unit
) {
    var localSubject by remember { mutableStateOf(selectedSubject) }
    var localType by remember { mutableStateOf(selectedType) }
    var localDifficulty by remember { mutableStateOf(selectedDifficulty) }
    var localCount by remember { mutableStateOf(questionCount) }

    LaunchedEffect(subjects, types, difficulties) { onUpdateLists(subjects, types, difficulties) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("选题练习", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(Modifier.height(14.dp))
                    Text("科目", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        subjects.forEach { s ->
                            val sel = localSubject == s
                            Surface(onClick = { localSubject = if (sel) "" else s }, shape = RoundedCornerShape(12.dp), color = if (sel) Color(0xFF2563EB) else Color.White.copy(alpha = 0.4f)) {
                                Text(s, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 13.sp, color = if (sel) Color.White else Color(0xFF64748B))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("题型", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        types.forEach { t ->
                            val sel = localType == t
                            Surface(onClick = { localType = if (sel) "" else t }, shape = RoundedCornerShape(12.dp), color = if (sel) Color(0xFF2563EB) else Color.White.copy(alpha = 0.4f)) {
                                Text(t, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 13.sp, color = if (sel) Color.White else Color(0xFF64748B))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("难度", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        difficulties.forEach { d ->
                            val sel = localDifficulty == d
                            Surface(onClick = { localDifficulty = if (sel) "" else d }, shape = RoundedCornerShape(12.dp), color = if (sel) Color(0xFF2563EB) else Color.White.copy(alpha = 0.4f)) {
                                Text(d, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 13.sp, color = if (sel) Color.White else Color(0xFF64748B))
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(value = localCount, onValueChange = { localCount = it; onUpdateCount(it) }, label = { Text("题目数量") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onGenerate, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(if (loading) "生成中..." else "开始练习")
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}


@Composable
fun QuizStepAnswer(
    api: ApiService,
    scope: CoroutineScope,
    snackbar: SnackbarHostState,
    questions: List<Question>,
    answers: Map<String, String>,
    loading: Boolean,
    onUpdateAnswers: (Map<String, String>) -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("答题中 (${questions.size}题)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                TextButton(onClick = onReset) { Text("重新选择", color = Color(0xFF2563EB)) }
            }
        }
        items(questions.size) { idx ->
            val q = questions[idx]
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("第${idx+1}题", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF2563EB))
                        Text("[${q.type}]", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(q.difficulty, fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(q.content, fontSize = 14.sp, color = Color(0xFF1E293B))
                    Spacer(Modifier.height(8.dp))
                    q.options?.forEach { opt ->
                        val qId = q.id.toString()
                        val selected = answers[qId] == opt.label
                        Surface(onClick = { onUpdateAnswers(answers + (qId to opt.label)) }, shape = RoundedCornerShape(12.dp), color = if (selected) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.Transparent, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (selected) "\u25CF" else "\u25CB", fontSize = 14.sp, color = if (selected) Color(0xFF2563EB) else Color(0xFF94A3B8))
                                Spacer(Modifier.width(8.dp))
                                Text("${opt.label}. ${opt.text}", fontSize = 13.sp, color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = onSubmit, enabled = !loading && answers.size == questions.size, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (loading) "批改中..." else "提交批改")
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}


@Composable
fun QuizStepResult(
    result: QuizResult?,
    questions: List<Question>,
    answers: Map<String, String>,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("答题结果", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("${result?.score ?: 0}分", fontWeight = FontWeight.Bold, fontSize = 36.sp, color = Color(0xFF2563EB))
                    Spacer(Modifier.height(4.dp))
                    Text("正确 ${result?.correct ?: 0}/${result?.total ?: 0} 题", fontSize = 14.sp, color = Color(0xFF64748B))
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        TextButton(onClick = onRetry) { Text("重新练习", color = Color(0xFF2563EB)) }
                        TextButton(onClick = onBack) { Text("返回", color = Color(0xFF64748B)) }
                    }
                }
            }
        }
        result?.results?.forEach { itemResult ->
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (itemResult.isCorrect) "\u2705" else "\u274C", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(itemResult.content, fontSize = 13.sp, color = Color(0xFF1E293B), maxLines = 2)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("你的答案: ${itemResult.userAnswer}", fontSize = 12.sp, color = if (itemResult.isCorrect) Color(0xFF10B981) else Color(0xFFEF4444))
                        if (!itemResult.isCorrect) { Text("正确答案: ${itemResult.correctAnswer}", fontSize = 12.sp, color = Color(0xFF10B981)) }
                        if (itemResult.analysis.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text("解析: ${itemResult.analysis}", fontSize = 12.sp, color = Color(0xFF64748B)) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}


@Composable
fun AiContent(api: ApiService, scope: CoroutineScope, snackbar: SnackbarHostState) {
    var aiTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp)).padding(3.dp)) {
            listOf("题目生成", "智能解析", "AI问答").forEachIndexed { i, label ->
                val active = aiTab == i
                TextButton(onClick = { aiTab = i }, modifier = Modifier.weight(1f).then(if (active) Modifier.background(Color.White, RoundedCornerShape(11.dp)) else Modifier), colors = ButtonDefaults.textButtonColors(contentColor = if (active) Color(0xFF2563EB) else Color(0xFF64748B))) {
                    Text(label, fontSize = 13.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        when (aiTab) { 0 -> AiGenerateTab(api, scope, snackbar); 1 -> AiParseTab(api, scope, snackbar); 2 -> AiChatTab(api, scope, snackbar) }
    }
}


@Composable
fun AiGenerateTab(api: ApiService, scope: CoroutineScope, snackbar: SnackbarHostState) {
    var topic by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("数学") }
    var count by remember { mutableStateOf("5") }
    var types by remember { mutableStateOf("单选,多选,判断") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<List<Question>>(emptyList()) }
    
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("主题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("学科") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("数量") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = types, onValueChange = { types = it }, label = { Text("题型") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            try {
                                val res = api.aiGenerate(AiGenerateRequest(topic = topic, subject = subject, count = count.toIntOrNull() ?: 5, types = types))
                                result = res.questions ?: emptyList()
                            } catch (e: Exception) { snackbar.showSnackbar("生成失败: ${e.message}") }
                            loading = false
                        }
                    }, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(if (loading) "生成中..." else "生成题目")
                    }
                }
            }
        }
        result.forEach { q ->
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("[${q.type}]", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(q.difficulty, fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(q.content, fontSize = 13.sp, color = Color(0xFF1E293B))
                        q.options?.forEach { opt ->
                            Text("${opt.label}. ${opt.text}", fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AiParseTab(api: ApiService, scope: CoroutineScope, snackbar: SnackbarHostState) {
    var text by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<List<Question>>(emptyList()) }
    
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.45f), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("题目文本") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        scope.launch {
                            loading = true
                            try {
                                val res = api.aiParse(AiParseRequest(text = text))
                                result = res.questions ?: emptyList()
                            } catch (e: Exception) { snackbar.showSnackbar("解析失败: ${e.message}") }
                            loading = false
                        }
                    }, enabled = !loading && text.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Text(if (loading) "解析中..." else "智能解析")
                    }
                }
            }
        }
        result.forEach { q ->
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(q.content, fontSize = 13.sp, color = Color(0xFF1E293B))
                        q.options?.forEach { opt ->
                            Text("${opt.label}. ${opt.text}", fontSize = 12.sp, color = Color(0xFF475569), modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AiChatTab(api: ApiService, scope: CoroutineScope, snackbar: SnackbarHostState) {
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf("你好！我是你的 AI 学习助手。有什么可以帮你？")) }
    var loading by remember { mutableStateOf(false) }
    val state = rememberLazyListState()
    
    LaunchedEffect(messages.size) { state.animateScrollToItem(messages.size) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                    Text(msg, fontSize = 13.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(12.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = message, onValueChange = { message = it }, modifier = Modifier.weight(1f))
            Button(onClick = {
                scope.launch {
                    if (message.isBlank()) return@launch
                    loading = true
                    val userMsg = message
                    messages = messages + userMsg
                    message = ""
                    try {
                        val res = api.aiChat(AiChatRequest(message = userMsg, history = messages))
                        messages = messages + (res.reply ?: "抱歉，我暂时无法回答。")
                    } catch (e: Exception) {
                        snackbar.showSnackbar("请求失败: ${e.message}")
                        messages = messages.dropLast(1)
                    }
                    loading = false
                }
            }, enabled = !loading && message.isNotBlank()) {
                Icon(Icons.Filled.Send, contentDescription = "发送", modifier = Modifier.size(20.dp))
            }
        }
    }
}
