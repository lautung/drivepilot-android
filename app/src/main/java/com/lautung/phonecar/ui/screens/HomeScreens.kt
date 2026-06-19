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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.lautung.phonecar.ui.components.PrototypeTopBar
import com.lautung.phonecar.ui.components.QuickAction
import com.lautung.phonecar.ui.components.SettingsToggleCard
import com.lautung.phonecar.ui.theme.BrandBlue
import com.lautung.phonecar.ui.theme.Slate100
import com.lautung.phonecar.ui.theme.Slate400
import com.lautung.phonecar.ui.theme.Slate500
import com.lautung.phonecar.ui.theme.Slate900

@Composable
fun VehicleHomeScreen(
    state: DemoState,
    onVehicleLock: (Boolean) -> Unit,
    onCabin: () -> Unit,
    onSentry: () -> Unit,
    onFindCar: () -> Unit,
    onBodyControl: () -> Unit,
    onDigitalKey: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.clickable(onClick = onDigitalKey),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_solar_user_circle_bold), null, Modifier.size(28.dp), tint = BrandBlue)
                    Text("Model S - 2026版", Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                }
                Icon(painterResource(R.drawable.ic_solar_bell_bing_bold), "通知", Modifier.size(26.dp))
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth().height(210.dp).clickable(onClick = onBodyControl),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.vehicle_sedan),
                    contentDescription = "车辆图片，进入车体控制",
                    modifier = Modifier.fillMaxWidth(0.64f).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickAction(if (state.vehicleLocked) "解锁" else "锁车", R.drawable.ic_solar_lock_keyhole_bold, state.vehicleLocked) { onVehicleLock(!state.vehicleLocked) }
                QuickAction("空调", R.drawable.ic_solar_wind_bold, state.acEnabled, onCabin)
                QuickAction("哨兵", R.drawable.ic_solar_ghost_bold, state.sentryEnabled, onSentry)
                QuickAction("寻车", R.drawable.ic_solar_map_point_wave_bold, true, onFindCar)
            }
        }
        item {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFF8FAFC)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_solar_shield_check_bold), null, Modifier.size(24.dp), tint = BrandBlue)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("车辆健康度", fontWeight = FontWeight.SemiBold)
                    Text("2026-02-09 状态良好", style = MaterialTheme.typography.bodySmall, color = Slate500)
                }
                Icon(painterResource(R.drawable.ic_solar_alt_arrow_right_bold), null, tint = Slate400)
            }
        }
    }
}

@Composable
fun CabinScreen(
    state: DemoState,
    onBack: () -> Unit,
    onAcEnabled: (Boolean) -> Unit,
    onPurification: (Boolean) -> Unit,
    onTemperature: (Float) -> Unit,
    onFanLevel: (Int) -> Unit,
    onDriverHeating: (Boolean) -> Unit,
    onPassengerHeating: (Boolean) -> Unit,
    onVentilation: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Color.White)) {
        PrototypeTopBar("智能座舱", onBack = onBack) {
            Icon(painterResource(R.drawable.ic_solar_settings_bold), "设置", tint = BrandBlue)
        }
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(172.dp).clip(CircleShape).background(Color(0xFFF8FAFC)), contentAlignment = Alignment.Center) {
                        Text("${state.cabinTemperature}°C", fontSize = 42.sp, fontWeight = FontWeight.Light, color = Slate900)
                    }
                }
                Slider(value = state.cabinTemperature, onValueChange = onTemperature, valueRange = 16f..30f)
            }
            item { SettingsToggleCard("AC 制冷", if (state.acEnabled) "运行中" else "已关闭", state.acEnabled, R.drawable.ic_solar_snowflake_bold, onAcEnabled) }
            item { SettingsToggleCard("自动净化", if (state.airPurificationEnabled) "运行中" else "已关闭", state.airPurificationEnabled, R.drawable.ic_solar_wind_bold, onPurification) }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFF8FAFC)).padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("风量调节", fontWeight = FontWeight.SemiBold)
                        Text("Level ${state.fanLevel}", color = BrandBlue)
                    }
                    Slider(value = state.fanLevel.toFloat(), onValueChange = { onFanLevel(it.toInt()) }, valueRange = 1f..5f, steps = 3)
                }
            }
            item { Text("座椅调节", style = MaterialTheme.typography.titleMedium) }
            item { SettingsToggleCard("主驾加热", "三档温控", state.driverSeatHeating, R.drawable.ic_solar_chair_bold, onDriverHeating) }
            item { SettingsToggleCard("副驾加热", "三档温控", state.passengerSeatHeating, R.drawable.ic_solar_chair_bold, onPassengerHeating) }
            item { SettingsToggleCard("座椅通风", "前排座椅", state.seatVentilation, R.drawable.ic_solar_wind_bold, onVentilation) }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
