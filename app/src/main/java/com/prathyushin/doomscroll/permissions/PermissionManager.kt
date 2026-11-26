package com.prathyushin.doomscroll.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.prathyushin.doomscroll.model.PermissionStatusItem

/**
 * Checks and opens system settings for optional just-in-time runtime permissions.
 * System-wide Accessibility and Notification Listener services are intentionally
 * omitted from direct-install APK builds so Google Play Protect Enhanced Fraud
 * Protection never blocks installation.
 */
object PermissionManager {

    fun isAccessibilityEnabled(context: Context): Boolean = false

    fun isNotificationListenerEnabled(context: Context): Boolean = false

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
            id = "camera",
            title = "Camera (Just-in-time)",
            purpose = "Requested only when you capture a photo or video inside Instagram or Threads.",
            granted = hasCameraPermission(context)
        ),
        PermissionStatusItem(
            id = "microphone",
            title = "Microphone (Just-in-time)",
            purpose = "Requested only when you record audio or a voice note inside Instagram or Threads.",
            granted = hasMicrophonePermission(context)
        ),
        PermissionStatusItem(
            id = "notifications",
            title = "Notifications (Optional)",
            purpose = "Optional local focus and session status alerts.",
            granted = hasPostNotificationsPermission(context)
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
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
