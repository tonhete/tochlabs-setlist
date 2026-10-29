package com.tochlabs.setlist

import androidx.compose.ui.window.ComposeUIViewController

// Called from Swift as a factory, so it keeps the PascalCase name.
@Suppress("FunctionName", "ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App() }
