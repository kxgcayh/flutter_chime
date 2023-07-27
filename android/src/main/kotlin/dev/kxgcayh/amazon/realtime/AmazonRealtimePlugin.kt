package dev.kxgcayh.amazon.realtime

import android.util.Log
import android.app.Activity
import android.content.Intent
import android.content.Context
import io.flutter.plugin.common.MethodChannel
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import dev.kxgcayh.amazon.realtime.managers.PermissionHelper
import dev.kxgcayh.amazon.realtime.managers.MeetingSessionManager
import dev.kxgcayh.amazon.realtime.utils.AndroidViewFactory
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding

/** AmazonRealtimePlugin */
class AmazonRealtimePlugin: FlutterPlugin, ActivityAware, FlutterActivity() {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var context: Context
  private lateinit var activity: Activity
  private lateinit var channel: MethodChannel
  private lateinit var methodCallHandler: AmazonChannelCoordinator
  private lateinit var activityBinding: ActivityPluginBinding
  private lateinit var listener: AmazonActivityListener

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "dev.kxgcayh.amazon.realtime.plugin")
    context = flutterPluginBinding.applicationContext
    methodCallHandler = AmazonChannelCoordinator(channel, context)
    channel.setMethodCallHandler(methodCallHandler)
    flutterPluginBinding.platformViewRegistry.registerViewFactory("videoTile", AndroidViewFactory())
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    setActivityBinding(binding)
  }

  override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    removeActivityBinding()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    setActivityBinding(binding)
  }

  override fun onDetachedFromActivity() {
    removeActivityBinding()
  }

  override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
    super.configureFlutterEngine(flutterEngine)
  }

  private fun setActivityBinding(binding: ActivityPluginBinding) {
    activityBinding = binding
    activity = activityBinding.activity
    listener = AmazonActivityListener(activity.applicationContext)
    activityBinding.addRequestPermissionsResultListener(listener);
    activityBinding.addActivityResultListener(listener);
    PermissionHelper.setPermissionManager(activity)
  }

  private fun removeActivityBinding() {
    activityBinding.removeRequestPermissionsResultListener(listener);
    activityBinding.removeActivityResultListener(listener);
  }

  companion object {
    private const val TAG = "AmazonRealtimePlugin"
  }
}