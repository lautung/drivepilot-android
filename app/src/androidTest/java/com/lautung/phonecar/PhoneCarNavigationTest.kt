package com.lautung.phonecar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lautung.phonecar.data.local.InMemoryDemoStateStore
import com.lautung.phonecar.ui.PhoneCarApp
import com.lautung.phonecar.ui.PhoneCarViewModel
import com.lautung.phonecar.ui.theme.PhoneCarTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PhoneCarNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNavigation_switchesBetweenFourPrimaryScreens() {
        launchApp()

        composeRule.onNodeWithText("Model S - 2026版").assertIsDisplayed()
        composeRule.onNodeWithText("发现").performClick()
        composeRule.onNodeWithText("搜索自驾路线、车友活动").assertIsDisplayed()
        composeRule.onNodeWithText("服务").performClick()
        composeRule.onNodeWithText("定制您的座驾").assertIsDisplayed()
        composeRule.onNodeWithText("我的").performClick()
        composeRule.onNodeWithText("智能驾驶官").assertIsDisplayed()
    }

    @Test
    fun homeQuickAction_opensCabinAndBackReturnsHome() {
        launchApp()

        composeRule.onNodeWithText("空调").performClick()
        composeRule.onNodeWithText("智能座舱").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("返回").performClick()
        composeRule.onNodeWithText("车辆健康度").assertIsDisplayed()
    }

    @Test
    fun discoverFeatureMetadata_staysInsideRoundedCard() {
        launchApp()
        composeRule.onNodeWithText("发现").performClick()

        val cardBounds = composeRule.onNodeWithTag(
            "discover_feature_card",
            useUnmergedTree = true,
        ).fetchSemanticsNode().boundsInRoot
        val metadataBounds = composeRule.onNodeWithText(
            "智驾先锋   1.2k",
            useUnmergedTree = true,
        ).fetchSemanticsNode().boundsInRoot

        assertTrue("Metadata must start inside the card's left edge", metadataBounds.left > cardBounds.left + 1f)
        assertTrue("Metadata must end above the card's bottom edge", metadataBounds.bottom < cardBounds.bottom - 1f)
    }

    private fun launchApp() {
        val viewModel = PhoneCarViewModel(InMemoryDemoStateStore())
        composeRule.setContent {
            PhoneCarTheme {
                PhoneCarApp(viewModel = viewModel)
            }
        }
    }
}
