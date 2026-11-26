package com.prathyushin.doomscroll.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.prathyushin.doomscroll.data.LocalPolicyStore
import com.prathyushin.doomscroll.policy.NotificationFilterEngine

/**
 * Opt-in local NotificationListenerService.
 * Evaluates Instagram and Threads notifications strictly in memory without storing or uploading text.
 */
class DoomNotificationListenerService : NotificationListenerService() {

    private lateinit var store: LocalPolicyStore
    private val engine = NotificationFilterEngine()

    override fun onCreate() {
        super.onCreate()
        store = LocalPolicyStore(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        val pkg = notification.packageName ?: return
        if (pkg != NotificationFilterEngine.INSTAGRAM_PACKAGE &&
            pkg != NotificationFilterEngine.THREADS_PACKAGE
        ) {
            return
        }

        val config = store.notificationFilterConfig()
        if (!config.masterEnabled) return

        val extras = notification.notification?.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        val decision = engine.evaluate(pkg, title, text, config)
        store.recordNotificationDecision(decision.shouldKeep)
        if (!decision.shouldKeep) {
            runCatching { cancelNotification(notification.key) }
        }
    }
}
