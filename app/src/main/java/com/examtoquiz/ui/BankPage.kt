package com.examtoquiz.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.examtoquiz.data.Question
import com.examtoquiz.network.ApiService

@Composable
fun BankPage(api: ApiService, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState) {
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try { questions = api.getQuestions().questions } catch (_: Exception) {}
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(tonalElevation = 2.dp, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = search, onValueChange = { search = it },
                    placeholder = { Text("搜索题目...") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, null) }
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(questions) { q ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)), elevation = CardDefaults.cardElevation(1.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("[${q.type}]", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(q.difficulty, fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(q.content, fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        q.options?.let { opts ->
                            if (opts.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    opts.forEach { Text("${it.label}.${it.text}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
