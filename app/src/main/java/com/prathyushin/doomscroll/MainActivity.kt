package com.prathyushin.doomscroll

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.prathyushin.doomscroll.browser.SocialBrowserHostView
import com.prathyushin.doomscroll.ui.DoomScrollApp

class MainActivity : ComponentActivity() {
    private var browserHost: SocialBrowserHostView? = null
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>
    private lateinit var mediaPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var pendingFileCallback: ValueCallback<Array<Uri>>? = null
    private var pendingWebPermission: PermissionRequest? = null

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch the system document/photo picker only after a website requests an upload.
        fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = pendingFileCallback
            pendingFileCallback = null
            val selected = if (result.resultCode == Activity.RESULT_OK) {
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            } else null
            callback?.onReceiveValue(selected)
        }

        // Camera/microphone permissions are requested only after a trusted platform page asks.
        mediaPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val request = pendingWebPermission
            pendingWebPermission = null
            if (request != null) {
                val allowed = request.resources.filter { resource ->
                    when (resource) {
                        PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                            grants[Manifest.permission.RECORD_AUDIO] == true ||
                                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                            grants[Manifest.permission.CAMERA] == true ||
                                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        else -> false
                    }
                }.toTypedArray()
                browserHost?.completeWebPermissionRequest(request, allowed)
            }
        }

        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        setContent {
            DoomScrollApp(onBrowserHostReady = { host ->
                browserHost = host
                host.setOnFileChooserRequested { callback, params ->
                    pendingFileCallback?.onReceiveValue(null)
                    pendingFileCallback = callback
                    try {
                        fileChooserLauncher.launch(params.createIntent())
                        true
                    } catch (_: android.content.ActivityNotFoundException) {
                        pendingFileCallback = null
                        false
                    } catch (_: SecurityException) {
                        pendingFileCallback = null
                        false
                    }
                }
                host.setOnWebPermissionRequested { request ->
                    pendingWebPermission?.let { previous ->
                        browserHost?.completeWebPermissionRequest(previous, emptyArray())
                    }
                    val requestedPermissions = request.resources.mapNotNull { resource ->
                        when (resource) {
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                            else -> null
                        }
                    }.distinct()
                    if (requestedPermissions.isEmpty()) {
                        host.completeWebPermissionRequest(request, emptyArray())
                    } else {
                        pendingWebPermission = request
                        val missing = requestedPermissions.filter {
                            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
                        }
                        if (missing.isEmpty()) {
                            pendingWebPermission = null
                            val alreadyGranted = request.resources.filter { resource ->
                                when (resource) {
                                    PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                                        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                    else -> false
                                }
                            }.toTypedArray()
                            host.completeWebPermissionRequest(request, alreadyGranted)
                        } else {
                            mediaPermissionLauncher.launch(missing.toTypedArray())
                        }
                    }
                }
            })
        }
    }

    override fun onPause() {
        browserHost?.pauseAndFlush()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        browserHost?.resumeViews()
    }

    override fun onDestroy() {
        pendingFileCallback?.onReceiveValue(null)
        pendingFileCallback = null
        pendingWebPermission?.deny()
        pendingWebPermission = null
        browserHost?.destroy()
        browserHost = null
        super.onDestroy()
    }
}
