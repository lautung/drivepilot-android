package com.lautung.phonecar.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lautung.phonecar.R
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.ui.components.ConfirmActionDialog
import com.lautung.phonecar.ui.components.PrototypeTopBar
import com.lautung.phonecar.ui.theme.AlertRed
import com.lautung.phonecar.ui.theme.BrandBlue
import com.lautung.phonecar.ui.theme.Slate100
import com.lautung.phonecar.ui.theme.Slate200
import com.lautung.phonecar.ui.theme.Slate400
import com.lautung.phonecar.ui.theme.Slate500
import com.lautung.phonecar.ui.theme.Slate900
import com.lautung.phonecar.ui.theme.SuccessGreen

@Composable
fun DrivingLogScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        PrototypeTopBar("行车日志", onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Slate100).padding(4.dp)) {
                    listOf("今日报告", "最近7天", "2026年2月").forEachIndexed { index, text ->
                        Text(text, Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(if (index == 0) Color.White else Color.Transparent).padding(10.dp), color = if (index == 0) BrandBlue else Slate500, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("里程 (km)", "42.5", Modifier.weight(1f))
                    MetricCard("能耗 (kWh/100km)", "14.2", Modifier.weight(1f))
                }
            }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Slate100).padding(16.dp)) {
                    Text("驾驶行为分析", fontWeight = FontWeight.Bold)
                    Canvas(Modifier.fillMaxWidth().height(120.dp).padding(top = 12.dp)) {
                        val path = Path().apply {
                            moveTo(0f, size.height * .75f); lineTo(size.width * .15f, size.height * .55f); lineTo(size.width * .3f, size.height * .66f); lineTo(size.width * .48f, size.height * .32f); lineTo(size.width * .68f, size.height * .46f); lineTo(size.width, size.height * .18f)
                        }
                        drawPath(path, BrandBlue, style = Stroke(5f, cap = StrokeCap.Round))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("急加速   1 次", color = Slate500); Text("急刹车   3 次", color = Slate500) }
                    Text("今日超越 88% 的同型号车主，驾驶风格：稳健型。", Modifier.padding(top = 10.dp), color = SuccessGreen)
                }
            }
            item { Text("行程记录", style = MaterialTheme.typography.titleMedium) }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).border(1.dp, Slate200, RoundedCornerShape(20.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.ic_solar_map_point_wave_bold), null, tint = BrandBlue) }
                    Column(Modifier.padding(start = 12.dp)) { Text("望京 SOHO - 环球影城", fontWeight = FontWeight.SemiBold); Text("09:12 - 10:05 | 28km", color = Slate500, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFEFF6FF)).padding(16.dp)) {
        Text(label, color = Slate500, style = MaterialTheme.typography.bodySmall)
        Text(value, color = BrandBlue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PrivacyScreen(
    state: DemoState,
    onBack: () -> Unit,
    onLocation: (Boolean) -> Unit,
    onCamera: (Boolean) -> Unit,
    onClear: () -> Unit,
) {
    var showClear by remember { mutableStateOf(false) }
    var showUnlink by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        PrototypeTopBar("安全与隐私", onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xFFECFDF5)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_solar_shield_check_bold), null, Modifier.size(34.dp), tint = SuccessGreen)
                    Column(Modifier.padding(start = 12.dp)) { Text("账户安全等级：高", fontWeight = FontWeight.Bold); Text("已开启生物识别与双重验证", color = Slate500) }
                }
            }
            item { Text("权限管理", style = MaterialTheme.typography.titleMedium) }
            item { PrivacyToggle("位置共享", "仅在使用App时访问", state.locationSharingEnabled, onLocation) }
            item { PrivacyToggle("车内摄像头预览", "用于疲劳驾驶检测", state.cabinCameraEnabled, onCamera) }
            item { Text("数据管理", style = MaterialTheme.typography.titleMedium) }
            item { DangerRow(if (state.drivingCacheCleared) "行车记录缓存已清除" else "清除行车记录缓存", AlertRed) { showClear = true } }
            item { DangerRow("注销车辆关联数据", AlertRed) { showUnlink = true } }
        }
    }
    if (showClear) ConfirmActionDialog("清除缓存", "确认清除本地行车记录缓存？", { showClear = false; onClear() }, { showClear = false })
    if (showUnlink) ConfirmActionDialog("注销车辆数据", "演示版不会执行真实注销，此操作仅展示确认流程。", { showUnlink = false }, { showUnlink = false })
}

@Composable
private fun PrivacyToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Slate100).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Slate500, style = MaterialTheme.typography.bodySmall) }
        Switch(checked, onCheckedChange = onChange)
    }
}

@Composable
private fun DangerRow(text: String, color: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_solar_danger_bold), null, tint = color)
        Text(text, Modifier.padding(start = 12.dp), color = color, fontWeight = FontWeight.SemiBold)
    }
}
