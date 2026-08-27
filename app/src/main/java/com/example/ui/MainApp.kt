package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.CreateSolutionPostScreen
import com.example.ui.screens.CreateTicketScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ForumScreen
import com.example.ui.screens.KnowledgeBountyScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScheduleScreen
import com.example.ui.screens.SolutionDetailScreen
import com.example.ui.screens.SopLibraryScreen
import com.example.ui.screens.TicketDetailScreen
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.HighDensityBorder
import com.example.ui.theme.HighDensityNavy
import com.example.ui.theme.HighDensityTextSecondary
import com.example.ui.viewmodel.BengkelViewModel
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.WorkspacePremium

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun MainApp(
    viewModel: BengkelViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                // Login state change in viewModel triggers recomposition into NavHost
            }
        )
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route, "BERANDA", Icons.Filled.Home, Icons.Outlined.Home, "bottom_nav_beranda"),
        BottomNavItem(Screen.Forum.route, "FORUM", Icons.Filled.Forum, Icons.Outlined.Forum, "bottom_nav_forum"),
        BottomNavItem(Screen.KnowledgeBounty.route, "SOLUSI", Icons.Filled.WorkspacePremium, Icons.Outlined.WorkspacePremium, "bottom_nav_solusi"),
        BottomNavItem(Screen.SopLibrary.route, "SOP", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "bottom_nav_sop"),
        BottomNavItem(Screen.ProfileSchedule.route, "PROFIL", Icons.Filled.Person, Icons.Outlined.Person, "bottom_nav_profil")
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Forum.route,
        Screen.KnowledgeBounty.route,
        Screen.SopLibrary.route,
        Screen.ProfileSchedule.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = HighDensityBorder, thickness = 1.dp)
                    NavigationBar(
                        containerColor = Color.White,
                        contentColor = HighDensityNavy,
                        tonalElevation = 0.dp,
                        modifier = Modifier.testTag("main_bottom_nav_bar")
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = HighDensityNavy,
                                    selectedTextColor = HighDensityNavy,
                                    unselectedIconColor = HighDensityTextSecondary,
                                    unselectedTextColor = HighDensityTextSecondary,
                                    indicatorColor = HighDensityBlueLight
                                ),
                                modifier = Modifier.testTag(item.tag)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onCreateTicketClick = { navController.navigate(Screen.CreateTicket.route) },
                    onTicketClick = { ticketId -> navController.navigate(Screen.TicketDetail.createRoute(ticketId)) },
                    onNavigateToForum = { navController.navigate(Screen.Forum.route) },
                    onNavigateToProfile = { navController.navigate(Screen.ProfileSchedule.route) },
                    onNavigateToSolutions = { navController.navigate(Screen.KnowledgeBounty.route) },
                    onNavigateToSop = { navController.navigate(Screen.SopLibrary.route) }
                )
            }

            composable(Screen.Forum.route) {
                ForumScreen(
                    viewModel = viewModel,
                    onCreateTicketClick = { navController.navigate(Screen.CreateTicket.route) },
                    onTicketClick = { ticketId -> navController.navigate(Screen.TicketDetail.createRoute(ticketId)) },
                    onNavigateToSolutions = { navController.navigate(Screen.KnowledgeBounty.route) }
                )
            }

            composable(Screen.KnowledgeBounty.route) {
                KnowledgeBountyScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { solutionId ->
                        navController.navigate(Screen.SolutionDetail.createRoute(solutionId))
                    },
                    onNavigateToCreate = {
                        navController.navigate(Screen.CreateSolutionPost.route)
                    }
                )
            }

            composable(Screen.CreateSolutionPost.route) {
                CreateSolutionPostScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSuccess = { solutionId ->
                        navController.popBackStack()
                        navController.navigate(Screen.SolutionDetail.createRoute(solutionId))
                    }
                )
            }

            composable(
                route = Screen.SolutionDetail.route,
                arguments = listOf(navArgument("solutionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val solutionId = backStackEntry.arguments?.getLong("solutionId") ?: 1L
                viewModel.selectSolutionPost(solutionId)
                SolutionDetailScreen(
                    solutionId = solutionId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SopLibrary.route) {
                SopLibraryScreen(viewModel = viewModel)
            }

            composable(Screen.ProfileSchedule.route) {
                ProfileScheduleScreen(
                    viewModel = viewModel,
                    onLogoutClick = {
                        // Handled by viewModel.logout()
                    },
                    onNavigateToSolutions = { navController.navigate(Screen.KnowledgeBounty.route) },
                    onNavigateToForum = { navController.navigate(Screen.Forum.route) }
                )
            }

            composable(Screen.CreateTicket.route) {
                CreateTicketScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onTicketCreated = { ticketId ->
                        navController.popBackStack()
                        navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                    }
                )
            }

            composable(
                route = Screen.TicketDetail.route,
                arguments = listOf(navArgument("ticketId") { type = NavType.LongType })
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getLong("ticketId") ?: 1L
                viewModel.selectTicket(ticketId)
                TicketDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSop = { navController.navigate(Screen.SopLibrary.route) }
                )
            }
        }
    }
}

