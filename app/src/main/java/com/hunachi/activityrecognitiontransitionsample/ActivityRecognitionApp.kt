package com.hunachi.activityrecognitiontransitionsample

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity

@Composable
fun ActivityRecognitionApp(
    viewModel: ActivityRecognitionViewModel,
    enableActivityTransition: () -> Unit,
    disableActivityTransitions: () -> Unit
) {
    val isActive by viewModel.activityTrackingEnabled
    val activeType = viewModel.currentActiveState
    val statusText = if (isActive) {
        toActivityString(activeType.value?.activeTypeId ?: -1)
    } else {
        "計測停止中"
    }
    val buttonText = if (isActive) "STOP" else "START"

    RequiresActivityRecignitionPermissionScreen {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = statusText, fontSize = 48.sp)
            Button(
                onClick = {
                    if (isActive) {
                        disableActivityTransitions()
                    } else {
                        enableActivityTransition()
                    }
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .height(48.dp)
                    .width(160.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text(text = buttonText, fontSize = 24.sp)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequiresActivityRecignitionPermissionScreen(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    if (permissionState.status.isGranted.not()) {
        Column {
            Text("「身体活動」の権限を許可してください")
            if (permissionState.status.shouldShowRationale) {
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("許可する")
                }
            } else {
                val settingsIntent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:com.hunachi.activityrecognitiontransitionsample")
                )
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Button(onClick = { context.startActivity(settingsIntent) }) {
                    Text("許可する")
                }
            }
        }
    } else {
        content()
    }
}

fun toActivityString(activity: Int): String {
    return when (activity) {
        DetectedActivity.STILL -> "STILL"
        DetectedActivity.WALKING -> "WALKING"
        else -> "UNKNOWN"
    }
}

fun toTransitionType(transitionType: Int): String {
    return when (transitionType) {
        ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
        ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
        else -> "UNKNOWN"
    }
}