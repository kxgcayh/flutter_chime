package dev.kxgcayh.amazon.realtime.managers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.MethodChannel
import androidx.appcompat.app.AppCompatActivity
import dev.kxgcayh.amazon.realtime.AmazonChannelResponse
import dev.kxgcayh.amazon.realtime.constants.ResponseMessage
import android.media.projection.MediaProjectionManager

class PermissionManager(val activity: Activity): AppCompatActivity() {
    private var context: Context = activity.applicationContext

    private var audioResult: MethodChannel.Result? = null
    private var videoResult: MethodChannel.Result? = null
    private var screenCaptureResult: MethodChannel.Result? = null

    val AUDIO_PERMISSION_REQUEST_CODE = 1
    val AUDIO_PERMISSIONS = arrayOf(
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.RECORD_AUDIO,
    )

    val VIDEO_PERMISSION_REQUEST_CODE = 2
    val VIDEO_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    val SCREEN_CAPTURE_REQUEST_CODE = 3
    val SCREEN_CAPTURE_PERMISSIONS = arrayOf(
        Manifest.permission.FOREGROUND_SERVICE
    )

    fun manageAudioPermissions(result: MethodChannel.Result) {
        audioResult = result
        if (hasPermissionsAlready(AUDIO_PERMISSIONS)) {
            audioCallbackReceived()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                AUDIO_PERMISSIONS,
                AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun manageVideoPermissions(result: MethodChannel.Result) {
        videoResult = result
        if (hasPermissionsAlready(VIDEO_PERMISSIONS)) {
            videoCallbackReceived()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                VIDEO_PERMISSIONS,
                VIDEO_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun manageScreenCapturePermissions(result: MethodChannel.Result) {
        screenCaptureResult = result
        if (hasPermissionsAlready(SCREEN_CAPTURE_PERMISSIONS)) {
            screenCaptureCallbackReceived()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                SCREEN_CAPTURE_PERMISSIONS,
                SCREEN_CAPTURE_REQUEST_CODE
            )
        }
    }

    fun audioCallbackReceived() {
        val callResult: AmazonChannelResponse
        if (hasPermissionsAlready(AUDIO_PERMISSIONS)) {
            callResult = AmazonChannelResponse(true, "Android: Audio Auth Granted")
            audioResult?.success(callResult.toFlutterCompatibleType())
        } else {
            callResult = AmazonChannelResponse(false, "Android: Audio Auth Not Granted")
            audioResult?.error("Failed", "Permission Error", callResult.toFlutterCompatibleType())
        }
        audioResult = null
    }

    fun videoCallbackReceived() {
        val callResult: AmazonChannelResponse
        if (hasPermissionsAlready(VIDEO_PERMISSIONS)) {
            callResult = AmazonChannelResponse(true, ResponseMessage.VIDEO_AUTH_GRANTED)
            videoResult?.success(callResult.toFlutterCompatibleType())
        } else {
            callResult = AmazonChannelResponse(false, ResponseMessage.VIDEO_AUTH_NOT_GRANTED)
            videoResult?.error("Failed", "Permission Error", callResult.toFlutterCompatibleType())
        }
        videoResult = null
    }

    fun screenCaptureCallbackReceived() {
        val callResult: AmazonChannelResponse
        if (hasPermissionsAlready(SCREEN_CAPTURE_PERMISSIONS)) {
            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            activity.startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                SCREEN_CAPTURE_REQUEST_CODE
            )
            callResult = AmazonChannelResponse(true, ResponseMessage.SCREEN_CAPTURE_AUTH_GRANTED)
            screenCaptureResult?.success(callResult.toFlutterCompatibleType())
        } else {
            callResult = AmazonChannelResponse(false, ResponseMessage.SCREEN_CAPTURE_AUTH_NOT_GRANTED)
            screenCaptureResult?.error("Failed", "Permission Error", callResult.toFlutterCompatibleType())
        }
        screenCaptureResult = null
    }

    private fun hasPermissionsAlready(PERMISSIONS: Array<String>): Boolean {
        return PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}


object PermissionHelper {
    lateinit var instance: PermissionManager

    fun setPermissionManager(activity: Activity) {
        this.instance = PermissionManager(activity)
    }
}