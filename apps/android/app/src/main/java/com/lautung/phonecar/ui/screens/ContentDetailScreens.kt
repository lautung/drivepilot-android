package com.lautung.phonecar.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lautung.phonecar.R
import com.lautung.phonecar.ui.components.PrimaryActionButton
import com.lautung.phonecar.ui.components.PrototypeTopBar
import com.lautung.phonecar.ui.theme.BrandBlue
import com.lautung.phonecar.ui.theme.Slate300
import com.lautung.phonecar.ui.theme.Slate400
import com.lautung.phonecar.ui.theme.Slate500
import com.lautung.phonecar.ui.theme.Slate800
import com.lautung.phonecar.ui.theme.Slate900

@Composable
fun AdasIntroScreen(onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A8A)))), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
        item { DarkContentTopBar(onBack) }
        item {
            Column(Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(22.dp))
                Text("A-PILOT 3.0", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text("极致智能 触手可及", color = Color(0xFF93C5FD), fontSize = 19.sp)
                Text("感知、预测、决策的完美闭环", Modifier.padding(top = 8.dp), color = Slate300)
                Image(painterResource(R.drawable.vehicle_sedan), "智能驾驶车辆", Modifier.fillMaxWidth().height(210.dp).padding(top = 18.dp).clip(RoundedCornerShape(24.dp)), contentScale = ContentScale.Crop)
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Metric("360°", "环境感知")
                Metric("800Tops", "算力")
                Metric("激光雷达", "高精地图")
            }
        }
        item { FeatureBlock("全场景代客泊车", "支持 1000m 超长距离学习，自动寻找车位并自主泊入，告别停车难题。") }
        item { FeatureBlock("主动安全防御", "毫秒级风险预测，全时守护您与家人的出行安全。") }
        item { PrimaryActionButton("一键自动泊车", onClick = {}, modifier = Modifier.padding(horizontal = 20.dp)) }
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, color = Slate300, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FeatureBlock(title: String, body: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 7.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = .1f)).padding(18.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Text(body, Modifier.padding(top = 6.dp), color = Slate300, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LiveRoomScreen(followed: Boolean, onBack: () -> Unit, onFollow: (Boolean) -> Unit) {
    Box(Modifier.fillMaxSize().background(Slate900)) {
        Image(painterResource(R.drawable.wheel_performance), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = .42f)
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_solar_alt_arrow_left_bold), "返回", Modifier.size(28.dp).clickable(onClick = onBack), tint = Color.White)
                Image(painterResource(R.drawable.service_avatar), null, Modifier.padding(start = 12.dp).size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    Text("官方旗舰直播间", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("正在讲解：21寸轮毂", color = Slate300, style = MaterialTheme.typography.bodySmall)
                }
                Text(if (followed) "已关注" else "关注", Modifier.clip(RoundedCornerShape(16.dp)).background(if (followed) Slate500 else BrandBlue).clickable { onFollow(!followed) }.padding(horizontal = 14.dp, vertical = 7.dp), color = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                ChatLine("智能控车手", "新款的续航实测多少？")
                ChatLine("爱车达人007", "想要一张专属定制图片！")
                ChatLine("李先生", "欢迎来到直播间~")
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.Black.copy(alpha = .35f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("说点什么...", Modifier.weight(1f), color = Slate300)
                    Icon(painterResource(R.drawable.ic_solar_share_bold), "分享", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChatLine(name: String, message: String) {
    Text("$name:  $message", color = Color.White, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = .3f)).padding(8.dp), style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun DarkContentTopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_solar_alt_arrow_left_bold), "返回", Modifier.size(28.dp).clickable(onClick = onBack), tint = Color.White)
        Spacer(Modifier.weight(1f))
        Icon(painterResource(R.drawable.ic_solar_share_bold), "分享", tint = Color.White)
    }
}
