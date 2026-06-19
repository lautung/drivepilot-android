package com.lautung.phonecar.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lautung.phonecar.R
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.MaintenanceService
import com.lautung.phonecar.data.model.SubscriptionPlan
import com.lautung.phonecar.ui.components.ConfirmActionDialog
import com.lautung.phonecar.ui.components.PrimaryActionButton
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
fun MaintenanceScreen(
    state: DemoState,
    onBack: () -> Unit,
    onService: (MaintenanceService) -> Unit,
    onDay: (Int) -> Unit,
    onConfirm: () -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color.White)) {
        PrototypeTopBar("维保预约", onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color(0xFFEFF6FF)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_solar_alarm_add_bold), null, tint = BrandBlue)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("建议保养", fontWeight = FontWeight.Bold)
                        Text("剩余续航 5,000km", color = Slate500)
                    }
                    Text("进行中", color = SuccessGreen)
                }
            }
            item { Text("选择服务", style = MaterialTheme.typography.titleMedium) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ServiceChoice("常规保养", "¥899起", state.maintenanceService == MaintenanceService.REGULAR, Modifier.weight(1f)) { onService(MaintenanceService.REGULAR) }
                    ServiceChoice("故障检查", "免费检测", state.maintenanceService == MaintenanceService.DIAGNOSTIC, Modifier.weight(1f)) { onService(MaintenanceService.DIAGNOSTIC) }
                }
            }
            item { Text("预约时间", style = MaterialTheme.typography.titleMedium) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(9 to "周一\n09\n2月", 10 to "周二\n10", 11 to "周三\n11").forEach { (day, text) ->
                        Text(text, Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).clickable { onDay(day) }.background(if (state.maintenanceDay == day) BrandBlue else Slate100).padding(14.dp), color = if (state.maintenanceDay == day) Color.White else Slate900, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Slate100).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriceLine("基础服务费", "¥150.00")
                    PriceLine("配件消耗费", "¥749.00")
                    PriceLine("预估费用", if (state.maintenanceService == MaintenanceService.REGULAR) "¥899.00" else "¥0.00", emphasized = true)
                }
            }
            item { PrimaryActionButton(if (state.maintenanceBooked) "预约成功" else "确认预约", { showConfirm = true }) }
        }
    }
    if (showConfirm) {
        ConfirmActionDialog("确认预约", "提交 ${state.maintenanceDay} 日的维保预约？", onConfirm = { showConfirm = false; onConfirm() }, onDismiss = { showConfirm = false })
    }
}

@Composable
private fun ServiceChoice(title: String, price: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).border(if (selected) 2.dp else 1.dp, if (selected) BrandBlue else Slate200, RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(16.dp)) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(price, color = if (selected) BrandBlue else Slate500)
    }
}

@Composable
private fun PriceLine(label: String, value: String, emphasized: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = if (emphasized) Slate900 else Slate500, fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal)
        Text(value, color = if (emphasized) BrandBlue else Slate900, fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun RescueScreen(state: DemoState, onBack: () -> Unit, onCancel: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(Color(0xFFE8EEF5))) {
        PrototypeTopBar("道路救援", onBack)
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Image(painterResource(R.drawable.road_scene), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = .36f)
            Column(Modifier.align(Alignment.TopStart).padding(24.dp)) {
                Text("汶水路", color = Slate500)
                Text("中环路", Modifier.padding(start = 130.dp, top = 44.dp), color = Slate500)
                Text("大润发", Modifier.padding(top = 80.dp), color = Slate500)
            }
            Box(Modifier.align(Alignment.Center).size(58.dp).clip(CircleShape).background(AlertRed), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_solar_map_point_wave_bold), "救援车辆", tint = Color.White)
            }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).background(Color.White).padding(20.dp)) {
            Text(if (state.rescueCancelled) "救援已取消" else "救援车辆接运中", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.service_avatar), null, Modifier.size(54.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("王师傅", fontWeight = FontWeight.Bold)
                    Text("专业认证", color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
                    Text("京A·D9832 (平板拖车)", color = Slate500)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("12", color = BrandBlue, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("min\n预计到达", color = Slate500, style = MaterialTheme.typography.bodySmall) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryActionButton("消息沟通", {}, Modifier.weight(1f))
                PrimaryActionButton("取消救援", { showConfirm = true }, Modifier.weight(1f), color = AlertRed)
            }
        }
    }
    if (showConfirm) ConfirmActionDialog("取消救援", "确认取消当前道路救援请求？", { showConfirm = false; onCancel() }, { showConfirm = false })
}

@Composable
fun SoftwareScreen(state: DemoState, onBack: () -> Unit, onToggle: (SubscriptionPlan) -> Unit) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        PrototypeTopBar("软件升级订阅", onBack)
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(BrandBlue).padding(18.dp)) {
                    Text("当前版本", color = Color(0xFFBFDBFE))
                    Text("v4.2.0-release", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text("最后检测时间: 2026-02-09 10:30", color = Color(0xFFBFDBFE), style = MaterialTheme.typography.bodySmall)
                }
            }
            item { Text("订阅计划", style = MaterialTheme.typography.titleMedium) }
            item { SubscriptionCard("智驾全功能包 (按月)", "含城市NOA、高速领航", "¥680", SubscriptionPlan.AUTOPILOT in state.subscriptions) { onToggle(SubscriptionPlan.AUTOPILOT) } }
            item { SubscriptionCard("娱乐流量无限包", "车载视听、5G热点", "¥19", SubscriptionPlan.ENTERTAINMENT in state.subscriptions) { onToggle(SubscriptionPlan.ENTERTAINMENT) } }
            item { Text("更新日志", style = MaterialTheme.typography.titleMedium) }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Slate100).padding(16.dp)) {
                    Text("2026-01-20 (新更新)", color = BrandBlue, fontWeight = FontWeight.Bold)
                    Text("优化APA泊车路径算法", Modifier.padding(top = 6.dp), fontWeight = FontWeight.SemiBold)
                    Text("显著提升窄车位泊入成功率，减少揉库次数；新增哨兵模式远程实时预览功耗优化。", color = Slate500)
                }
            }
            item { PrimaryActionButton("检查新版本", {}) }
        }
    }
}

@Composable
private fun SubscriptionCard(title: String, subtitle: String, price: String, checked: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).border(1.dp, Slate200, RoundedCornerShape(22.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = Slate500, style = MaterialTheme.typography.bodySmall); Text(price, color = BrandBlue, fontWeight = FontWeight.Bold) }
        Switch(checked, onCheckedChange = { onClick() })
    }
}
