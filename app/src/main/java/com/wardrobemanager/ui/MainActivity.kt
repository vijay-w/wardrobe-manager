package com.wardrobemanager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.wardrobemanager.ui.navigation.BottomNavigationBar
import com.wardrobemanager.ui.navigation.WardrobeNavigation
import com.wardrobemanager.ui.theme.WardrobeManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WardrobeManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WardrobeManagerApp()
                }
            }
        }
    }
}

@Composable
fun WardrobeManagerApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        WardrobeNavigation(
            navController = navController
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WardrobeManagerAppPreview() {
    WardrobeManagerTheme {
        WardrobeManagerApp()
    }
}