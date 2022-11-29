package com.hunachi.activityrecognitiontransitionsample

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionViewModel : ViewModel() {

    var activityTrackingEnabled = mutableStateOf(false)
        private set

    var currentActiveState = mutableStateOf<ActiveState?>(null)
        private set

    var activityTransitionList = mutableStateOf<List<ActivityTransition>>(emptyList())
        private set

    fun changeActivityTrackingEnabledState(state: Boolean) {
        activityTrackingEnabled.value = state
    }

    fun changeActiveType(activeState: ActiveState) {
        currentActiveState.value = activeState
    }

    fun changeActivityTransitionList(activityIds: List<Int>) {
        val enterList = activityIds.map {
            ActivityTransition.Builder()
                .setActivityType(it)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        }
        val exitList = activityIds.map {
            ActivityTransition.Builder()
                .setActivityType(it)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        }
        activityTransitionList.value = enterList + exitList
    }
}

data class ActiveState(
    val activeTypeId: Int,
    val transitionTypeId: Int
)
