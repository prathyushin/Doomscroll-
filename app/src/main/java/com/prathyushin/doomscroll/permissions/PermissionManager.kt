package com.prathyushin.doomscroll.permissions

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.prathyushin.doomscroll.model.PermissionStatusItem
import com.prathyushin.doomscroll.service.DoomAccessibilityService
import com.prathyushin.doomscroll.service.DoomNotificationListenerService

/**
 * Checks and opens system settings for optional permissions.
 * Runtime permissions (Camera, Microphone, Notifications) are requested just-in-time
 * only when the user initiates a feature that requires them.
 */
object PermissionManager {

    fun isAccessibilityEnabled(context: Context): Boolean {
        val expectedComponent = ComponentName(context, DoomAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expectedComponent, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val expectedComponent = ComponentName(context, DoomNotificationListenerService::class.java).flattenToString()
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return flat.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
    }

    fun hasCameraPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun hasMicrophonePermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun hasPostNotificationsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun buildStatusList(context: Context): List<PermissionStatusItem> = listOf(
        PermissionStatusItem(
            id = "accessibility",
            title = "App Session Monitoring (Accessibility)",
            purpose = "Detects when selected external apps cross your session or scroll limits.",
            granted = isAccessibilityEnabled(context)
        ),
        PermissionStatusItem(
            id = "notification_listener",
            title = "Notification Filtering Access",
            purpose = "Filters Instagram and Threads app notifications locally in memory.",
            granted = isNotificationListenerEnabled(context)
        ),
        PermissionStatusItem(
            id = "camera",
            title = "Camera (Just-in-time)",
            purpose = "Requested only when you capture a photo or video inside Instagram or Threads.",
            granted = hasCameraPermission(context)
        ),
        PermissionStatusItem(
            id = "microphone",
            title = "Microphone (Just-in-time)",
            purpose = "Requested only when you record audio or a voice note inside a WebView.",
            granted = hasMicrophonePermission(context)
        )
    )

    fun openAccessibilitySettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openNotificationListenerSettings(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openAppDetailsSettings(context: Context) {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
