package com.example.learningdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learningdashboard.data.local.SessionStore
import com.example.learningdashboard.ui.dashboard.DashboardScreen
import com.example.learningdashboard.ui.details.CourseDetailsScreen
import com.example.learningdashboard.ui.login.LoginScreen
import com.example.learningdashboard.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // system picks dark/light status-bar icons to match the theme
        setContent {
            AppTheme {
                Surface(color = MaterialTheme.colorScheme.background) { AppNav() }
            }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val session = remember { SessionStore(context.applicationContext) }
    // Signed-in users skip the login screen on relaunch (works offline too).
    val startDestination = remember { if (session.isLoggedIn) "dashboard" else "login" }

    NavHost(
        navController = nav,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(tween(220)) { it / 8 } + fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(120)) },
        popEnterTransition = { fadeIn(tween(200)) },
        popExitTransition = { slideOutHorizontally(tween(200)) { it / 8 } + fadeOut(tween(200)) }
    ) {
        composable("login") {
            LoginScreen(onLoggedIn = {
                session.isLoggedIn = true
                nav.navigate("dashboard") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("dashboard") { DashboardScreen(onCourseClick = { nav.navigate("details/$it") }) }
        composable(
            "details/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.IntType })
        ) { CourseDetailsScreen(onBack = { nav.popBackStack() }) }
    }
}