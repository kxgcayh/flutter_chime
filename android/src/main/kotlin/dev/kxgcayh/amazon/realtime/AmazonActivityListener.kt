package dev.kxgcayh.amazon.realtime

import android.util.Log
import android.app.Activity
import android.content.Intent
import android.content.Context
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener
import dev.kxgcayh.amazon.realtime.managers.PermissionHelper
import dev.kxgcayh.amazon.realtime.managers.MeetingSessionManager


class AmazonActivityListener(context: Context): ActivityResultListener, RequestPermissionsResultListener {
    private lateinit var context: Context

    init {
        this.context = context
    }

    companion object {
        private const val TAG = "AmazonActivityListener"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (PermissionHelper.instance.SCREEN_CAPTURE_REQUEST_CODE == requestCode) {
            if (resultCode != Activity.RESULT_OK || data == null) {
                PermissionHelper.instance.screenCaptureCallbackReceived()
            } else {
                data?.let {
                    MeetingSessionManager.startScreenShare(resultCode, it, context)
                }
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsList: Array<String>,
        grantResults: IntArray
    ): Boolean {
        var status: Boolean = false
        when (requestCode) {
            PermissionHelper.instance.AUDIO_PERMISSION_REQUEST_CODE -> {
                PermissionHelper.instance.audioCallbackReceived()
                status = true
            }
            PermissionHelper.instance.VIDEO_PERMISSION_REQUEST_CODE -> {
                PermissionHelper.instance.videoCallbackReceived()
                status = true
            }
            PermissionHelper.instance.SCREEN_CAPTURE_REQUEST_CODE -> {
                PermissionHelper.instance.screenCaptureCallbackReceived()
                status = true
            }
        }
        return status
    }
}
