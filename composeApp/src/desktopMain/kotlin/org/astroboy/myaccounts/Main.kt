package org.astroboy.myaccounts

import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import myaccounts.composeapp.generated.resources.Res
import myaccounts.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    val state = rememberWindowState(placement = WindowPlacement.Maximized)
    var showSettings by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = ::exitApplication,
        state = state,
        title = "MyAccounts",
        icon = painterResource(Res.drawable.compose_multiplatform),
    ) {
        // TODO: this looks crap
        MenuBar {
            Menu("File", mnemonic = 'F') {
                Item(
                    "Settings",
                    onClick = { showSettings = true },
                )
            }
        }

        if (showSettings) {
            DialogWindow(
                title = "Settings",
                onCloseRequest = { showSettings = false },
            ) {
                Surface {
                    // TODO: the theme here doesn't work
                }
            }
        }

        App()
    }
}
