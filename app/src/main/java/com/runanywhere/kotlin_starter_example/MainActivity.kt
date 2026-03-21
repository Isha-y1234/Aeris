package com.runanywhere.kotlin_starter_example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.runanywhere.kotlin_starter_example.services.ModelService
import com.runanywhere.kotlin_starter_example.ui.screens.*
import com.runanywhere.kotlin_starter_example.ui.theme.KotlinStarterTheme
import com.runanywhere.kotlin_starter_example.viewmodel.MainViewModel
import com.runanywhere.sdk.core.onnx.ONNX
import com.runanywhere.sdk.foundation.bridge.extensions.CppBridgeModelPaths
import com.runanywhere.sdk.llm.llamacpp.LlamaCPP
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.SDKEnvironment
import com.runanywhere.sdk.storage.AndroidPlatformContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AndroidPlatformContext.initialize(this)
//        RunAnywhere.initialize(SDKEnvironment.DEVELOPMENT)
        RunAnywhere.initialize("development")

        val basePath = java.io.File(filesDir, "runanywhere").absolutePath
        CppBridgeModelPaths.setBaseDirectory(basePath)

        try {
            LlamaCPP.register(priority = 100)
        } catch (e: Throwable) {
            Log.w("MainActivity", "LlamaCPP error: ${e.message}")
        }

        ONNX.register(priority = 100)

        ModelService.registerDefaultModels()

        setContent {
            KotlinStarterTheme {
                AerisApp()
            }
        }
    }
}

@Composable
fun AerisApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onLive = { navController.navigate("live") },
                onSettings = { navController.navigate("sensitivity") },
                onHaptics = { navController.navigate("haptics") }
            )
        }

        composable("live") {
            LiveDetectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("sensitivity") {
            SensitivityScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("haptics") {
            HapticsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}