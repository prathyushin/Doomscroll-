package com.prathyushin.doomscroll.model

data class PermissionStatusItem(
    val id: String,
    val title: String,
    val purpose: String,
    val granted: Boolean,
    val isOptional: Boolean = true
)

data class LocalDataFootprint(
    val mutedStoryCount: Int,
    val focusScheduleCount: Int,
    val monitoredAppCount: Int,
    val todayMinutes: Int,
    val weekMinutes: Int,
    val storesDmContent: Boolean = false,
    val storesNotificationText: Boolean = false,
    val hasCloudAccount: Boolean = false
)
