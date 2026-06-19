package com.lautung.phonecar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lautung.phonecar.data.local.InMemoryDemoStateStore
import com.lautung.phonecar.ui.PhoneCarApp
import com.lautung.phonecar.ui.PhoneCarViewModel
import com.lautung.phonecar.ui.theme.PhoneCarTheme
import org.junit.Rule
import org.junit.Test

class AllScreensReachableTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun allSixteenPrototypeScreens_haveDiscoverableRoutes() {
        val viewModel = PhoneCarViewModel(InMemoryDemoStateStore())
        composeRule.setContent { PhoneCarTheme { PhoneCarApp(viewModel) } }

        assertScreen("Model S - 2026版")
        openAndBack("空调", "智能座舱")
        openAndBack("哨兵", "检测到 1 次异常靠近事件")
        openByDescriptionAndBack("车辆图片，进入车体控制", "全窗已锁定")
        openAndBack("寻车", "超级充电站 - 望京SOHO站")
        openAndBack("Model S - 2026版", "My Smart Key")

        composeRule.onNodeWithText("发现").performClick()
        assertScreen("搜索自驾路线、车友活动")
        openAndBack("2026款全场景智驾实测，这次真的惊艳到我了...", "A-PILOT 3.0")
        openAndBack("车友会年度盛典", "官方旗舰直播间")

        composeRule.onNodeWithText("服务").performClick()
        assertScreen("定制您的座驾")

        composeRule.onNodeWithText("我的").performClick()
        assertScreen("智能驾驶官")
        openAndBack("维保预约详情", "建议保养")
        openAndBack("帮助与客服中心", "救援车辆接运中")
        openAndBack("订阅服务", "v4.2.0-release")
        openAndBack("行车日志报告", "今日报告")
        openAndBack("安全与隐私设置", "账户安全等级：高")
    }

    private fun openAndBack(entry: String, destination: String) {
        composeRule.onNodeWithText(entry).performClick()
        assertScreen(destination)
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    private fun openByDescriptionAndBack(entry: String, destination: String) {
        composeRule.onNodeWithContentDescription(entry).performClick()
        assertScreen(destination)
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    private fun assertScreen(text: String) {
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }
}
