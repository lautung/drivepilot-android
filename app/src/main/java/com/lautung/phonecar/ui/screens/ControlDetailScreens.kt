package com.lautung.phonecar.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lautung.phonecar.R
import com.lautung.phonecar.data.model.CameraAngle
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.ui.components.PrimaryActionButton
import com.lautung.phonecar.ui.components.PrototypeTopBar
import com.lautung.phonecar.ui.components.QuickAction
import com.lautung.phonecar.ui.components.SettingsToggleCard
import com.lautung.phonecar.ui.theme.AlertRed
import com.lautung.phonecar.ui.theme.BrandBlue
import com.lautung.phonecar.ui.theme.Slate100
import com.lautung.phonecar.ui.theme.Slate300
import com.lautung.phonecar.ui.theme.Slate400
import com.lautung.phonecar.ui.theme.Slate500
import com.lautung.phonecar.ui.theme.Slate700
import com.lautung.phonecar.ui.theme.Slate800
import com.lautung.phonecar.ui.theme.Slate900
import com.lautung.phonecar.ui.theme.SuccessGreen

@Composable
fun SentryScreen(
    state: DemoState,
    onBack: () -> Unit,
    onEnabled: (Boolean) -> Unit,
    onCamera: (CameraAngle) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Slate900)) {
        DarkTopBar("哨兵模式", onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                val cameras = listOf(
                    CameraAngle.FRONT to ("前" to R.drawable.sentry_front),
                    CameraAngle.REAR to ("后" to R.drawable.road_scene),
                    CameraAngle.LEFT to ("左" to R.drawable.sentry_left),
                    CameraAngle.RIGHT to ("右（运行中）" to R.drawable.sentry_right),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cameras.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (angle, content) ->
                                val (label, image) = content
                                Box(
                                    modifier = Modifier.weight(1f).aspectRatio(1.35f).clip(RoundedCornerShape(18.dp))
                                        .border(if (state.selectedCamera == angle) 2.dp else 0.dp, AlertRed, RoundedCornerShape(18.dp))
                                        .clickable { onCamera(angle) },
                                ) {
                                    Image(painterResource(image), label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = if (state.selectedCamera == angle) 1f else .62f)
                                    Text(label, Modifier.align(Alignment.BottomStart).padding(10.dp), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Slate800).padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(if (state.sentryEnabled) SuccessGreen else Slate400))
                        Text("系统状态", Modifier.padding(start = 8.dp).weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                        Switch(checked = state.sentryEnabled, onCheckedChange = onEnabled)
                    }
                    Text(if (state.sentryEnabled) "全时运行中" else "已暂停", color = SuccessGreen)
                    Spacer(Modifier.height(14.dp))
                    Canvas(Modifier.fillMaxWidth().height(92.dp)) {
                        val path = Path().apply {
                            moveTo(0f, size.height * .7f)
                            lineTo(size.width * .18f, size.height * .82f)
                            lineTo(size.width * .38f, size.height * .5f)
                            lineTo(size.width * .58f, size.height * .74f)
                            lineTo(size.width * .8f, size.height * .18f)
                            lineTo(size.width, size.height * .58f)
                        }
                        drawPath(path, AlertRed, style = Stroke(width = 5f, cap = StrokeCap.Round))
                    }
                    Text("检测到 1 次异常靠近事件", color = Color(0xFFFCA5A5), style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    QuickAction("查看录像", R.drawable.ic_solar_document_bold) {}
                    QuickAction("喊话", R.drawable.ic_solar_megaphone_bold) {}
                    QuickAction("闪灯", R.drawable.ic_solar_lightbulb_bold) {}
                    QuickAction("鸣笛", R.drawable.ic_solar_alarm_add_bold) {}
                }
            }
        }
    }
}

@Composable
fun BodyControlScreen(
    state: DemoState,
    onBack: () -> Unit,
    onAutoLights: (Boolean) -> Unit,
    onWelcomeLight: (Boolean) -> Unit,
    onWindow: (Int) -> Unit,
    onMirrors: (Boolean) -> Unit,
    onTrunk: (Boolean) -> Unit,
    onSunshade: (Boolean) -> Unit,
    onChildLock: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        PrototypeTopBar("车体控制", onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(R.drawable.vehicle_sedan), "车辆俯视图", Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop)
                    Text("全窗已锁定", color = BrandBlue, fontWeight = FontWeight.SemiBold)
                }
            }
            item { Text("外部灯光", style = MaterialTheme.typography.titleMedium) }
            item { SettingsToggleCard("自动大灯", if (state.autoHeadlights) "开启中" else "已关闭", state.autoHeadlights, R.drawable.ic_solar_lightbulb_bold, onAutoLights) }
            item { SettingsToggleCard("迎宾灯", if (state.welcomeLight) "开启中" else "已关闭", state.welcomeLight, R.drawable.ic_solar_magic_stick_3_bold, onWelcomeLight) }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Slate100).padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("车窗百分比", fontWeight = FontWeight.SemiBold)
                        Text("${state.windowOpenPercent}% (通风模式)", color = BrandBlue)
                    }
                    Slider(state.windowOpenPercent.toFloat(), { onWindow(it.toInt()) }, valueRange = 0f..100f)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    QuickAction("折叠后视镜", R.drawable.ic_solar_layers_bold, state.mirrorsFolded) { onMirrors(!state.mirrorsFolded) }
                    QuickAction("后备箱", R.drawable.ic_solar_case_minimalistic_bold, state.trunkOpen) { onTrunk(!state.trunkOpen) }
                    QuickAction("遮阳帘", R.drawable.ic_solar_lightbulb_bold, state.sunshadeOpen) { onSunshade(!state.sunshadeOpen) }
                    QuickAction("童锁", R.drawable.ic_solar_lock_keyhole_bold, state.childLock) { onChildLock(!state.childLock) }
                }
            }
        }
    }
}

@Composable
fun ChargeMapScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFFE8EEF5))) {
        Canvas(Modifier.fillMaxSize()) {
            val road = Path().apply { moveTo(0f, size.height * .28f); cubicTo(size.width * .3f, size.height * .18f, size.width * .65f, size.height * .42f, size.width, size.height * .3f) }
            drawPath(road, Color.White, style = Stroke(width = 30f, cap = StrokeCap.Round))
            val cross = Path().apply { moveTo(size.width * .3f, 0f); lineTo(size.width * .5f, size.height * .72f) }
            drawPath(cross, Color.White, style = Stroke(width = 24f, cap = StrokeCap.Round))
        }
        Column(Modifier.fillMaxSize()) {
            PrototypeTopBar("加能地图", onBack)
            Box(Modifier.weight(1f).fillMaxWidth()) {
                listOf("汶水路" to (.12f to .18f), "中环路" to (.62f to .27f), "静安青年体育公园" to (.17f to .48f), "宁汇广场" to (.68f to .56f)).forEach { (text, point) ->
                    Text(text, Modifier.align(Alignment.TopStart).padding(start = (point.first * 320).dp, top = (point.second * 460).dp), color = Slate700, style = MaterialTheme.typography.bodySmall)
                }
                Box(Modifier.align(Alignment.Center).size(58.dp).clip(CircleShape).background(BrandBlue), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_solar_map_point_wave_bold), "充电站", tint = Color.White)
                }
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).background(Color.White).padding(20.dp)) {
                Text("优质站点：祥腾财富广场", color = BrandBlue, style = MaterialTheme.typography.bodySmall)
                Text("超级充电站 - 望京SOHO站", style = MaterialTheme.typography.titleMedium)
                Text("距离您 2.4km | 12个空闲车位", color = Slate500)
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("800V 快充", color = SuccessGreen)
                    Text("电价  ¥1.25", fontWeight = FontWeight.Bold)
                    Text("服务分  4.9 ★", fontWeight = FontWeight.Bold)
                }
                PrimaryActionButton("一键去这里", onClick = {})
            }
        }
    }
}

@Composable
fun DigitalKeyScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A8A))))) {
        DarkTopBar("数字钥匙", onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White.copy(alpha = .12f)).padding(20.dp)) {
                    Text("UWB 超宽带", color = Color(0xFF93C5FD), style = MaterialTheme.typography.bodySmall)
                    Text("My Smart Key", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("**** **** 8821", color = Slate300)
                    Text("已连接至 Model S", Modifier.padding(top = 18.dp), color = SuccessGreen)
                }
            }
            item { Text("钥匙分享 (2/5)", color = Color.White, style = MaterialTheme.typography.titleMedium) }
            item { KeyPerson("家人-王女士", "永久权限 | 包含寻车、遥控", R.drawable.digital_key_avatar) }
            item { KeyPerson("朋友-李先生", "临时权限 | 2026-02-10 到期", R.drawable.service_avatar) }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = .1f)).padding(16.dp)) {
                    Text("使用日志", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("今日 08:45", Modifier.padding(top = 12.dp), color = Slate400)
                    Text("家人-王女士 使用蓝牙钥匙执行 [锁车]", color = Color.White)
                    Text("2026-02-08 18:20", Modifier.padding(top = 12.dp), color = Slate400)
                    Text("主手机 执行 [蓝牙无感解锁]", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun KeyPerson(name: String, permission: String, image: Int) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = .1f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(image), null, Modifier.size(44.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(permission, color = Slate300, style = MaterialTheme.typography.bodySmall)
        }
        Text("管理权限", color = Color(0xFF93C5FD), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun DarkTopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_solar_alt_arrow_left_bold), "返回", Modifier.size(28.dp).clickable(onClick = onBack), tint = Color.White)
        Text(title, Modifier.weight(1f), color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(28.dp))
    }
}
