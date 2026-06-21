package com.lautung.phonecar.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lautung.phonecar.ui.navigation.AppRoute
import com.lautung.phonecar.ui.screens.CabinScreen
import com.lautung.phonecar.ui.screens.CarLifeScreen
import com.lautung.phonecar.ui.screens.AdasIntroScreen
import com.lautung.phonecar.ui.screens.BodyControlScreen
import com.lautung.phonecar.ui.screens.ChargeMapScreen
import com.lautung.phonecar.ui.screens.ConfiguratorScreen
import com.lautung.phonecar.ui.screens.DigitalKeyScreen
import com.lautung.phonecar.ui.screens.DrivingLogScreen
import com.lautung.phonecar.ui.screens.LiveRoomScreen
import com.lautung.phonecar.ui.screens.MaintenanceScreen
import com.lautung.phonecar.ui.screens.PrivacyScreen
import com.lautung.phonecar.ui.screens.ProfileScreen
import com.lautung.phonecar.ui.screens.RescueScreen
import com.lautung.phonecar.ui.screens.SentryScreen
import com.lautung.phonecar.ui.screens.SoftwareScreen
import com.lautung.phonecar.ui.screens.VehicleHomeScreen
import com.lautung.phonecar.ui.theme.BrandBlue
import com.lautung.phonecar.ui.theme.Slate400
import com.lautung.phonecar.data.auth.AuthState
import com.lautung.phonecar.ui.screens.AuthScreen

@Composable
fun PhoneCarApp(viewModel: PhoneCarViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    when (val auth = authState) {
        AuthState.Restoring -> AuthScreen(
            loading = true,
            errorMessage = null,
            onLogin = { _, _ -> },
            onRegister = { _, _ -> },
        )
        AuthState.SignedOut -> AuthScreen(
            loading = false,
            errorMessage = null,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
        )
        is AuthState.Error -> AuthScreen(
            loading = false,
            errorMessage = auth.message,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
        )
        is AuthState.SignedIn -> AuthenticatedPhoneCarApp(viewModel)
    }
}

@Composable
private fun AuthenticatedPhoneCarApp(viewModel: PhoneCarViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val discoveryContents by viewModel.discoveryContents.collectAsStateWithLifecycle()
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(syncError) {
        syncError?.let { snackbarHostState.showSnackbar(it) }
    }
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: AppRoute.HOME.route
    val topLevelRoutes = AppRoute.topLevel.map { it.route }.toSet()

    Scaffold(
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar(containerColor = Color.White) {
                    AppRoute.topLevel.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(requireNotNull(destination.icon)),
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandBlue,
                                selectedTextColor = BrandBlue,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.HOME.route,
            modifier = Modifier.padding(contentPadding),
        ) {
            composable(AppRoute.HOME.route) {
                VehicleHomeScreen(
                    state = state,
                    onVehicleLock = viewModel::setVehicleLocked,
                    onCabin = { navController.navigate(AppRoute.CABIN.route) },
                    onSentry = { navController.navigate(AppRoute.SENTRY.route) },
                    onFindCar = { navController.navigate(AppRoute.CHARGE_MAP.route) },
                    onBodyControl = { navController.navigate(AppRoute.BODY_CONTROL.route) },
                    onDigitalKey = { navController.navigate(AppRoute.DIGITAL_KEY.route) },
                )
            }
            composable(AppRoute.CABIN.route) {
                CabinScreen(
                    state = state,
                    onBack = navController::popBackStack,
                    onAcEnabled = viewModel::setAcEnabled,
                    onPurification = viewModel::setAirPurification,
                    onTemperature = viewModel::setCabinTemperature,
                    onFanLevel = viewModel::setFanLevel,
                    onDriverHeating = viewModel::setDriverSeatHeating,
                    onPassengerHeating = viewModel::setPassengerSeatHeating,
                    onVentilation = viewModel::setSeatVentilation,
                )
            }
            composable(AppRoute.SENTRY.route) {
                SentryScreen(
                    state = state,
                    onBack = navController::popBackStack,
                    onEnabled = viewModel::setSentryEnabled,
                    onCamera = viewModel::setSentryCamera,
                )
            }
            composable(AppRoute.BODY_CONTROL.route) {
                BodyControlScreen(
                    state = state,
                    onBack = navController::popBackStack,
                    onAutoLights = viewModel::setAutoHeadlights,
                    onWelcomeLight = viewModel::setWelcomeLight,
                    onWindow = viewModel::setWindowOpenPercent,
                    onMirrors = viewModel::setMirrorsFolded,
                    onTrunk = viewModel::setTrunkOpen,
                    onSunshade = viewModel::setSunshadeOpen,
                    onChildLock = viewModel::setChildLock,
                )
            }
            composable(AppRoute.CHARGE_MAP.route) {
                ChargeMapScreen(onBack = navController::popBackStack)
            }
            composable(AppRoute.DIGITAL_KEY.route) {
                DigitalKeyScreen(onBack = navController::popBackStack)
            }
            composable(AppRoute.DISCOVER.route) {
                CarLifeScreen(
                    state = state,
                    contents = discoveryContents,
                    onTab = viewModel::selectDiscoveryTab,
                    onAdas = { navController.navigate(AppRoute.ADAS.route) },
                    onLive = { navController.navigate(AppRoute.LIVE.route) },
                )
            }
            composable(AppRoute.ADAS.route) {
                AdasIntroScreen(onBack = navController::popBackStack)
            }
            composable(AppRoute.LIVE.route) {
                LiveRoomScreen(
                    followed = state.followedLiveRoom,
                    onBack = navController::popBackStack,
                    onFollow = viewModel::setLiveRoomFollowed,
                )
            }
            composable(AppRoute.SERVICE.route) {
                ConfiguratorScreen(
                    state = state,
                    onPaint = viewModel::selectPaint,
                    onWheel = viewModel::selectWheel,
                    onReserve = viewModel::confirmReservation,
                )
            }
            composable(AppRoute.PROFILE.route) {
                ProfileScreen(
                    onDrivingLog = { navController.navigate(AppRoute.DRIVING_LOG.route) },
                    onMaintenance = { navController.navigate(AppRoute.MAINTENANCE.route) },
                    onVehicleOrder = { navController.navigate(AppRoute.SERVICE.route) },
                    onSoftware = { navController.navigate(AppRoute.SOFTWARE.route) },
                    onPrivacy = { navController.navigate(AppRoute.PRIVACY.route) },
                    onRescue = { navController.navigate(AppRoute.RESCUE.route) },
                    onLogout = viewModel::logout,
                )
            }
            composable(AppRoute.MAINTENANCE.route) {
                MaintenanceScreen(
                    state = state,
                    onBack = navController::popBackStack,
                    onService = viewModel::selectMaintenanceService,
                    onDay = viewModel::selectMaintenanceDay,
                    onConfirm = viewModel::confirmMaintenanceBooking,
                )
            }
            composable(AppRoute.RESCUE.route) {
                RescueScreen(state, navController::popBackStack, viewModel::cancelRescue)
            }
            composable(AppRoute.SOFTWARE.route) {
                SoftwareScreen(state, navController::popBackStack, viewModel::toggleSubscription)
            }
            composable(AppRoute.DRIVING_LOG.route) {
                DrivingLogScreen(onBack = navController::popBackStack)
            }
            composable(AppRoute.PRIVACY.route) {
                PrivacyScreen(
                    state = state,
                    onBack = navController::popBackStack,
                    onLocation = viewModel::setLocationSharing,
                    onCamera = viewModel::setCabinCamera,
                    onClear = viewModel::clearDrivingCache,
                )
            }
        }
    }
}
