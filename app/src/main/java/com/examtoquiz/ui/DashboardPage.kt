package com.examtoquiz.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.examtoquiz.data.StatsResponse
import com.examtoquiz.network.ApiService

@Composable
fun DashboardPage(api: ApiService, scope: kotlinx.coroutines.CoroutineScope, snackbar: SnackbarHostState) {
    var stats by remember { mutableStateOf<StatsResponse?>(null) }
    LaunchedEffect(Unit) {
        try { stats = api.getStats() } catch (_: Exception) {}
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFFEEF2F8), Color(0xFFF0F4F8), Color(0xFFF4F6FA)))
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // Goal ring card — Liquid Glass
        item {
            val goal = stats?.dailyGoal
            val pct = if (goal != null && goal.target > 0) (goal.completed.toFloat() / goal.target) else 0f
            val animPct by animateFloatAsState(targetValue = pct, label = "goal")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                // Refraction light spots
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF2563EB).copy(alpha = 0.06f), Color.Transparent),
                                centerX = 0.22f,
                                centerY = 0.20f
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.04f), Color.Transparent),
                                centerX = 0.75f,
                                centerY = 0.80f
                            )
                        )
                )

                // Fresnel top edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(1.5.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.10f),
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.85f),
                                    Color.White.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Goal ring
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(82.dp)) {
                        CircularProgressIndicator(
                            progress = { animPct },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 7.dp,
                            color = Color(0xFF2563EB),
                            trackColor = Color(0xFF2563EB).copy(alpha = 0.06f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${(animPct * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2563EB), lineHeight = 18.sp)
                            Text("完成", fontSize = 9.sp, color = Color(0xFF64748B))
                        }
                    }

                    Spacer(Modifier.width(18.dp))

                    // Three-column data with dividers
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎯", fontSize = 16.sp)
                            Spacer(Modifier.height(2.dp))
                            Text("${goal?.completed ?: 0}/${goal?.target ?: 20}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                            Text("今日目标", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF94A3B8).copy(alpha = 0.15f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥", fontSize = 16.sp)
                            Spacer(Modifier.height(2.dp))
                            Text("${goal?.streak ?: 0}天", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                            Text("连续打卡", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color(0xFF94A3B8).copy(alpha = 0.15f)))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⭐", fontSize = 16.sp)
                            Spacer(Modifier.height(2.dp))
                            Text("${stats?.total ?: 0}", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF1E293B))
                            Text("总题量", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }

        // Stats grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidGlassStatCard("📝", "${stats?.total ?: 0}", "总题量", Modifier.weight(1f))
                LiquidGlassStatCard("📥", "${stats?.todayCount ?: 0}", "今日新增", Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidGlassStatCard("❌", "${stats?.wrongCount ?: 0}", "错题本", Modifier.weight(1f))
                LiquidGlassStatCard("⭐", "${stats?.favoriteCount ?: 0}", "收藏", Modifier.weight(1f))
            }
        }

        // Subject distribution
        item {
            val subjects = stats?.subjects ?: emptyList()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                                centerX = 0.25f,
                                centerY = 0.15f
                            )
                        )
                )
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("学科分布", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${subjects.size} 科", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    Spacer(Modifier.height(10.dp))
                    val maxCount = subjects.maxOfOrNull { it.count } ?: 1
                    subjects.forEach { stat ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stat.name, modifier = Modifier.width(42.dp), fontSize = 11.sp, color = Color(0xFF64748B))
                            Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF2563EB).copy(alpha = 0.06f))) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction = stat.count.toFloat() / maxCount).clip(RoundedCornerShape(3.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF60A5FA)))))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("${stat.count}", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF2563EB))
                        }
                    }
                }
            }
        }

        // Recent additions
        item {
            Text("最近添加", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.padding(vertical = 2.dp))
        }
        stats?.recent?.forEach { q ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
                        .clickable { }
                        .padding(12.dp)
                ) {
                    Row {
                        Text("[${q.type}] ", fontSize = 11.sp, color = Color(0xFF2563EB))
                        Text(q.content, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF64748B))
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun LiquidGlassStatCard(icon: String, value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .clickable { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                        centerX = 0.25f,
                        centerY = 0.15f
                    )
                )
        )
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2563EB).copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
                Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}