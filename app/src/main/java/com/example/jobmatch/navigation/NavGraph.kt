package com.example.jobmatch.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jobmatch.model.UserRole
import com.example.jobmatch.screens.*
import com.example.jobmatch.viewmodel.AuthViewModel

// Definisikan semua route screen
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object HomeJobseeker : Screen("home_jobseeker")
    object HomeCompany : Screen("home_company")
    object AdminDashboard : Screen("admin_dashboard")
    object JobSeekerProfile : Screen("jobseeker_profile")
    object CompanyProfile : Screen("company_profile")
    object AddEditLowongan : Screen("add_edit_lowongan/{lowonganId}") {
        fun passId(id: String = "new") = "add_edit_lowongan/$id"
    }
    object DetailLowongan : Screen("detail_lowongan/{lowonganId}") {
        fun passId(id: String) = "detail_lowongan/$id"
    }
    object FormLamaran : Screen("form_lamaran/{lowonganId}") {
        fun passId(id: String) = "form_lamaran/$id"
    }
    object Tracking : Screen("tracking")
    object CompanyLamaranList : Screen("company_lamaran_list")
    object CompanyLamaranDetail : Screen("company_lamaran_detail/{lamaranId}") {
        fun passId(id: String) = "company_lamaran_detail/$id"
    }
    object ListLowongan : Screen("list_lowongan")
    object CompanyLowonganList : Screen("company_lowongan_list")
}

@Composable
fun JobMatchNavGraph(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    val startDestination = when (currentUser?.role) {
        UserRole.JOBSEEKER -> Screen.HomeJobseeker.route
        UserRole.COMPANY -> Screen.HomeCompany.route
        UserRole.ADMIN -> Screen.AdminDashboard.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ==================== AUTENTIKASI ====================
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }

        // ==================== PENCAIR KERJA (JOBSEEKER) ====================
        composable(Screen.HomeJobseeker.route) {
            HomeJobseekerScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
        composable(Screen.JobSeekerProfile.route) {
            JobSeekerProfileScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
        composable(Screen.ListLowongan.route) {
            ListLowonganScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
        composable(
            route = Screen.DetailLowongan.route,
            arguments = listOf(navArgument("lowonganId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lowonganId = backStackEntry.arguments?.getString("lowonganId") ?: ""
            DetailLowonganScreen(
                navController = navController,
                lowonganId = lowonganId,
                windowSizeClass = windowSizeClass
            )
        }
        composable(
            route = Screen.FormLamaran.route,
            arguments = listOf(navArgument("lowonganId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lowonganId = backStackEntry.arguments?.getString("lowonganId") ?: ""
            FormLamaranScreen(
                navController = navController,
                lowonganId = lowonganId,
                windowSizeClass = windowSizeClass
            )
        }
        composable(Screen.Tracking.route) {
            TrackingScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }

        // ==================== PERUSAHAAN (COMPANY) ====================
        composable(Screen.HomeCompany.route) {
            HomeCompanyScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
        composable(Screen.CompanyProfile.route) {
            CompanyProfileScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
        composable(
            route = Screen.AddEditLowongan.route,
            arguments = listOf(navArgument("lowonganId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lowonganId = backStackEntry.arguments?.getString("lowonganId") ?: "new"
            AddEditLowonganScreen(
                navController = navController,
                lowonganId = lowonganId,
                windowSizeClass = windowSizeClass
            )
        }
        composable(Screen.CompanyLamaranList.route) {
            CompanyLamaranListScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
        composable(
            route = Screen.CompanyLamaranDetail.route,
            arguments = listOf(navArgument("lamaranId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lamaranId = backStackEntry.arguments?.getString("lamaranId") ?: ""
            CompanyLamaranDetailScreen(
                navController = navController,
                lamaranId = lamaranId,
                windowSizeClass = windowSizeClass
            )
        }
        composable(Screen.CompanyLowonganList.route) {
            CompanyLowonganListScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }

        // ==================== ADMIN ====================
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                windowSizeClass = windowSizeClass
            )
        }
    }
}

// Extension function untuk mengecek apakah layar lebar (tablet/desktop)
fun WindowSizeClass.isWideScreen(): Boolean {
    return this.widthSizeClass == WindowWidthSizeClass.Expanded ||
            this.widthSizeClass == WindowWidthSizeClass.Medium
}
