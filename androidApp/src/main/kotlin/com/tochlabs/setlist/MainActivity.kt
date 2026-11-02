package com.tochlabs.setlist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
        // Only for a fresh start: after a rotation the file was already handed over.
        if (savedInstanceState == null) receiveSetlistFile(this, intent)
    }

    /** The app is already open (single task) and another app opens a setlist file with it. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveSetlistFile(this, intent)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
