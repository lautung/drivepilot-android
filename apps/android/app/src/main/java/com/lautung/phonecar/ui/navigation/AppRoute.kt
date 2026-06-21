package com.lautung.phonecar.ui.navigation

import androidx.annotation.DrawableRes
import com.lautung.phonecar.R

enum class AppRoute(
    val route: String,
    val label: String,
    @param:DrawableRes val icon: Int? = null,
) {
    HOME("home", "首页", R.drawable.ic_solar_home_2_bold),
    CABIN("cabin", "智能座舱"),
    SENTRY("sentry", "哨兵模式"),
    BODY_CONTROL("body_control", "车体控制"),
    DISCOVER("discover", "发现", R.drawable.ic_solar_compass_bold),
    ADAS("adas", "智驾系统介绍"),
    LIVE("live", "官方直播间"),
    SERVICE("service", "服务", R.drawable.ic_solar_cart_large_minimalistic_bold),
    CHARGE_MAP("charge_map", "充电站地图"),
    MAINTENANCE("maintenance", "维保预约"),
    RESCUE("rescue", "道路救援"),
    SOFTWARE("software", "软件升级订阅"),
    PROFILE("profile", "我的", R.drawable.ic_solar_user_bold),
    DRIVING_LOG("driving_log", "行车日志"),
    PRIVACY("privacy", "安全与隐私"),
    DIGITAL_KEY("digital_key", "数字钥匙"),
    ;

    companion object {
        val topLevel = listOf(HOME, DISCOVER, SERVICE, PROFILE)
    }
}
