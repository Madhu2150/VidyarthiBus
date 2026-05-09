package com.vidyarthibus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.vidyarthibus.navigation.VidyarthiBusNavGraph
import com.vidyarthibus.ui.theme.AppBackground
import com.vidyarthibus.ui.theme.VidyarthiBusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VidyarthiBusTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBackground),
                    color = AppBackground
                ) {
                    VidyarthiBusNavGraph(
                        context = applicationContext
                    )
                }
            }
        }
    }
}