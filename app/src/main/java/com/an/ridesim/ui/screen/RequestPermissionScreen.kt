package com.an.ridesim.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.an.ridesim.R
import com.an.ridesim.ui.permission.PermissionHandler
import com.an.ridesim.ui.permission.PermissionManager

@Composable
fun RequestPermissionScreen(
    onPermissionChanged: (value: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val locationPermission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)

    // Create an instance of PermissionManager
    val permissionManager = remember { PermissionManager(context) }

    // State variables to trigger permission requests
    var requestPermission by remember { mutableStateOf(true) }

    // 🔁 Re-check permission when the app is resumed (e.g. after coming back from settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-check permission every time app resumes
                val granted = permissionManager.arePermissionsGranted(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                )
                onPermissionChanged(granted)
                requestPermission = !granted
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (requestPermission) {
        PermissionHandler(
            permissions = locationPermission,
            permissionManager = permissionManager,
            onPermissionsGranted = {
                                    requestPermission = false
                                    onPermissionChanged(true)
                                   },
            rationaleDialogContent = { permissions, retry ->
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(text = stringResource(R.string.request_permission_title)) },
                    text = { Text(text = stringResource(R.string.request_permission_desc)) },
                    confirmButton = {
                        TextButton(onClick = retry) {
                            Text(stringResource(R.string.btn_ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            requestPermission = false
                            onPermissionChanged(false)
                        }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                    }
                )
            },
            settingsDialogContent = { openSettings ->
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(text = stringResource(R.string.request_permission_title)) },
                    text = { Text(text = stringResource(R.string.request_permission_desc)) },
                    confirmButton = {
                        TextButton(onClick = openSettings) {
                            Text(stringResource(R.string.btn_settings))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            requestPermission = false
                            onPermissionChanged(false)
                        }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                    }
                )
            }
        )
    }
}