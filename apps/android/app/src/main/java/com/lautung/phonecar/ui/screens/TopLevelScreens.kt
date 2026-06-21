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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lautung.phonecar.R
import com.lautung.phonecar.data.model.DemoState
import com.lautung.phonecar.data.model.DiscoveryContent
import com.lautung.phonecar.data.model.DiscoveryTab
import com.lautung.phonecar.data.model.PaintOption
import com.lautung.phonecar.data.model.WheelOption
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.lautung.phonecar.ui.components.ConfirmActionDialog
import com.lautung.phonecar.ui.components.PrimaryActionButton
import com.lautung.phonecar.ui.theme.BrandBlue
import com.lautung.phonecar.ui.theme.Slate100
import com.lautung.phonecar.ui.theme.Slate400
import com.lautung.phonecar.ui.theme.Slate500
import com.lautung.phonecar.ui.theme.Slate900

@Composable
fun CarLifeScreen(state: DemoState, contents: List<DiscoveryContent> = emptyList(), onTab: (DiscoveryTab) -> Unit, onAdas: () -> Unit, onLive: () -> Unit) {
    val remoteFeature = contents.firstOrNull { it.category == state.discoveryTab.name }
    LazyColumn(Modifier.fillMaxSize().background(Color.White), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Slate100).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_solar_compass_bold), null, tint = Slate400)
                Text("搜索自驾路线、车友活动", Modifier.padding(start = 10.dp), color = Slate500)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val labels = listOf(DiscoveryTab.RECOMMENDED to "推荐", DiscoveryTab.LOCAL to "同城", DiscoveryTab.ACTIVITY to "活动", DiscoveryTab.STORE to "商城")
                labels.forEach { (tab, label) ->
                    Text(label, modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onTab(tab) }.background(if (state.discoveryTab == tab) Color(0xFFDBEAFE) else Color.Transparent).padding(horizontal = 14.dp, vertical = 8.dp), color = if (state.discoveryTab == tab) BrandBlue else Slate500, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().testTag("discover_feature_card").clip(RoundedCornerShape(24.dp)).background(Color.White).clickable(onClick = onAdas)) {
                if (remoteFeature?.mediaUrl != null && remoteFeature.mediaId != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(remoteFeature.mediaUrl)
                            .memoryCacheKey(remoteFeature.mediaId)
                            .diskCacheKey(remoteFeature.mediaId)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(190.dp),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.vehicle_configurator),
                    )
                } else {
                    Image(painterResource(R.drawable.vehicle_configurator), null, Modifier.fillMaxWidth().height(190.dp), contentScale = ContentScale.Crop)
                }
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(remoteFeature?.title ?: "2026款全场景智驾实测，这次真的惊艳到我了...", fontWeight = FontWeight.Bold)
                    Text(remoteFeature?.summary ?: "智驾先锋   1.2k", color = Slate500, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFEFF6FF)).clickable(onClick = onLive).padding(18.dp)) {
                Text("车友会年度盛典", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("立即参与报名，赢取联名周边", color = Slate500)
                Text("去参加", Modifier.padding(top = 12.dp), color = BrandBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ConfiguratorScreen(
    state: DemoState,
    onPaint: (PaintOption) -> Unit,
    onWheel: (WheelOption) -> Unit,
    onReserve: () -> Unit,
) {
    var showConfirm by remember { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize().background(Color.White), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("定制您的座驾", style = MaterialTheme.typography.titleLarge) }
        item {
            Box(Modifier.fillMaxWidth().height(230.dp)) {
                Image(painterResource(R.drawable.vehicle_configurator), "定制车辆", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Row(Modifier.align(Alignment.TopStart).padding(12.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .9f)).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_solar_maximize_bold), null, Modifier.size(17.dp), tint = BrandBlue)
                    Text("360° 视图", Modifier.padding(start = 5.dp), color = BrandBlue, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Text("Model Alpha Ultra", style = MaterialTheme.typography.titleLarge) }
        item { Text("¥${"%,d".format(state.totalVehiclePrice)} 起", color = BrandBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        item { Text("搭载全新一代智能驾驶系统，纯电续航跨越 800km。极致性能，优雅美学。", color = Slate500) }
        item { Text("车漆颜色:  极光银 (Aurora Silver)", fontWeight = FontWeight.SemiBold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                listOf(
                    PaintOption.AURORA_SILVER to Color(0xFFCBD5E1),
                    PaintOption.DEEP_BLUE to Color(0xFF1E40AF),
                    PaintOption.OBSIDIAN_BLACK to Color(0xFF111827),
                    PaintOption.PEARL_WHITE to Color.White,
                ).forEach { (paint, color) ->
                    Box(Modifier.size(38.dp).clip(CircleShape).background(color).border(if (state.selectedPaint == paint) 3.dp else 1.dp, if (state.selectedPaint == paint) BrandBlue else Slate400, CircleShape).clickable { onPaint(paint) })
                }
            }
        }
        item { Text("轮毂选配", fontWeight = FontWeight.SemiBold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WheelChoice("20英寸星芒轮毂", "包含在内", R.drawable.wheel_standard, state.selectedWheel == WheelOption.STANDARD_20, Modifier.weight(1f)) { onWheel(WheelOption.STANDARD_20) }
                WheelChoice("21英寸破风轮毂", "+ ¥8,000", R.drawable.wheel_performance, state.selectedWheel == WheelOption.PERFORMANCE_21, Modifier.weight(1f)) { onWheel(WheelOption.PERFORMANCE_21) }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFFEFF6FF)).padding(16.dp)) {
                Text("智能驾驶全功能专享", color = BrandBlue, fontWeight = FontWeight.Bold)
                Text("首任车主获赠 12 个月订阅服务，包含城市动态巡航、自主泊车及高速领航驾驶。", color = Slate500)
            }
        }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("当前选配总价", fontWeight = FontWeight.Bold); Text("¥${"%,d".format(state.totalVehiclePrice)}", color = BrandBlue, fontWeight = FontWeight.Bold) } }
        item { PrimaryActionButton(if (state.reservationConfirmed) "已提交预定" else "立即预定", { showConfirm = true }) }
    }
    if (showConfirm) ConfirmActionDialog("确认预定", "按当前配置提交车辆预定？", { showConfirm = false; onReserve() }, { showConfirm = false })
}

@Composable
private fun WheelChoice(title: String, price: String, image: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).border(if (selected) 2.dp else 1.dp, if (selected) BrandBlue else Slate100, RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(image), null, Modifier.fillMaxWidth().height(90.dp), contentScale = ContentScale.Crop)
        Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
        Text(price, color = if (selected) BrandBlue else Slate500, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ProfileScreen(onDrivingLog: () -> Unit, onMaintenance: () -> Unit, onVehicleOrder: () -> Unit, onSoftware: () -> Unit, onPrivacy: () -> Unit, onRescue: () -> Unit, onLogout: () -> Unit = {}) {
    LazyColumn(Modifier.fillMaxSize().background(Color.White), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.profile_avatar), "用户头像", Modifier.size(74.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Column(Modifier.padding(start = 14.dp)) {
                    Text("智能驾驶官", style = MaterialTheme.typography.titleLarge)
                    Text("至尊合伙人  Lv. 24", color = Slate500)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("12,450\n积分", "5\n勋章", "18\n作品").forEach { Text(it, color = Slate900, fontWeight = FontWeight.SemiBold) }
            }
        }
        item { Text("我的资产", style = MaterialTheme.typography.titleMedium) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                AssetItem("钱包", R.drawable.ic_solar_wallet_bold) {}
                AssetItem("卡券", R.drawable.ic_solar_ticket_bold) {}
                AssetItem("奖励单", R.drawable.ic_solar_chat_round_money_bold) {}
                AssetItem("订阅服务", R.drawable.ic_solar_layers_bold, onSoftware)
            }
        }
        item { ProfileRow("行车日志报告", R.drawable.ic_solar_document_bold, onDrivingLog) }
        item { ProfileRow("维保预约详情", R.drawable.ic_solar_map_point_school_bold, onMaintenance) }
        item { ProfileRow("车载精品订单", R.drawable.ic_solar_case_minimalistic_bold, onVehicleOrder) }
        item { ProfileRow("安全与隐私设置", R.drawable.ic_solar_shield_keyhole_bold, onPrivacy) }
        item { ProfileRow("帮助与客服中心", R.drawable.ic_solar_question_square_bold, onRescue) }
        item { ProfileRow("退出登录", R.drawable.ic_solar_user_circle_bold, onLogout) }
    }
}

@Composable
private fun AssetItem(label: String, icon: Int, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Slate100), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), null, tint = BrandBlue)
        }
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProfileRow(label: String, icon: Int, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, tint = BrandBlue)
        Text(label, Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.Medium)
        Icon(painterResource(R.drawable.ic_solar_alt_arrow_right_bold), null, tint = Slate400)
    }
}
