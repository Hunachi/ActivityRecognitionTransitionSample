package com.hunachi.activityrecognitiontransitionsample

import android.Manifest
import android.app.PendingIntent
import android.os.Bundle
import com.google.android.gms.location.DetectedActivity
import android.content.Intent
import android.content.IntentFilter
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.ActivityRecognition
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.android.gms.location.ActivityTransitionResult
import com.hunachi.activityrecognitiontransitionsample.ui.theme.ActivityRecognitionTransitionSampleTheme

class MainActivity : ComponentActivity() {

    private val TRANSITIONS_RECEIVER_ACTION =
        BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION"

    private var transitionsPendingIntent: PendingIntent? = null
    private var transitionsReceiver: BroadcastReceiver? = null

    private val viewModel: ActivityRecognitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivityRecognitionTransitionSampleTheme {
                ActivityRecognitionApp(viewModel,
                    enableActivityTransition = { enableActivityTransitions() },
                    disableActivityTransitions = { disableActivityTransitions() })
            }
        }

        viewModel.changeActivityTransitionList(
            listOf(
                DetectedActivity.WALKING,
                DetectedActivity.STILL
            )
        )

        val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
        transitionsPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this@MainActivity, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getBroadcast(
                this@MainActivity,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        transitionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive(): $intent")
                if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.action)) return

                if (ActivityTransitionResult.hasResult(intent)) {
                    val result = ActivityTransitionResult.extractResult(intent) ?: return
                    for (event in result.transitionEvents) {
                        viewModel.changeActiveType(
                            ActiveState(
                                event.activityType,
                                event.transitionType
                            )
                        )
                        // 動作が変わったらトーストを出す
                        Toast.makeText(
                            context,
                            "${toActivityString(event.activityType)}(${toTransitionType(event.transitionType)})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        registerReceiver(transitionsReceiver, IntentFilter(TRANSITIONS_RECEIVER_ACTION))
    }

    override fun onPause() {
        if (viewModel.activityTrackingEnabled.value) {
            disableActivityTransitions()
        }
        super.onPause()
    }

    override fun onStop() {
        unregisterReceiver(transitionsReceiver)
        super.onStop()
    }

    private fun enableActivityTransitions() {
        Log.d(TAG, "enableActivityTransitions()")

        val request = ActivityTransitionRequest(viewModel.activityTransitionList.value)

        val task = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            null
        } else
            ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(request, transitionsPendingIntent!!)
        task?.addOnSuccessListener {
            viewModel.changeActivityTrackingEnabledState(true)
        }
        task?.addOnFailureListener { e ->
            Toast.makeText(
                this,
                "Transition Apiを使えませんでした",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun disableActivityTransitions() {
        Log.d(TAG, "disableActivityTransitions()")

        transitionsPendingIntent?.let {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) return
            ActivityRecognition.getClient(this).removeActivityTransitionUpdates(it)
        }?.addOnSuccessListener {
            viewModel.changeActivityTrackingEnabledState(false)
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Transitions could not be unregistered: $e")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}