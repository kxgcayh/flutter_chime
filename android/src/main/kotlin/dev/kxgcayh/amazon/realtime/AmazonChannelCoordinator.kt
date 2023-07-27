package dev.kxgcayh.amazon.realtime

import com.google.gson.Gson
import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import dev.kxgcayh.amazon.realtime.constants.ResponseMessage
import dev.kxgcayh.amazon.realtime.constants.MethodCallFlutter
import dev.kxgcayh.amazon.realtime.managers.MeetingSessionManager
import dev.kxgcayh.amazon.realtime.observers.AudioVideoObserver
import dev.kxgcayh.amazon.realtime.observers.DataMessageObserver
import dev.kxgcayh.amazon.realtime.observers.RealtimeObserver
import dev.kxgcayh.amazon.realtime.observers.VideoTileObserver
import com.amazonaws.services.chime.sdk.meetings.device.MediaDevice
import com.amazonaws.services.chime.sdk.meetings.session.Attendee
import com.amazonaws.services.chime.sdk.meetings.session.Meeting
import com.amazonaws.services.chime.sdk.meetings.session.MediaPlacement
import com.amazonaws.services.chime.sdk.meetings.utils.logger.ConsoleLogger
import com.amazonaws.services.chime.sdk.meetings.session.DefaultMeetingSession
import com.amazonaws.services.chime.sdk.meetings.session.CreateMeetingResponse
import com.amazonaws.services.chime.sdk.meetings.session.CreateAttendeeResponse
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionConfiguration
import dev.kxgcayh.amazon.realtime.managers.PermissionHelper

class AmazonChannelCoordinator(channel: MethodChannel, context: Context): MethodCallHandler, AppCompatActivity() {
    private val gson = Gson()
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    var isReplied: Boolean = false

    init {
        this.channel = channel
        this.context = context
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        isReplied = false
        var callResult: AmazonChannelResponse = AmazonChannelResponse(false, ResponseMessage.METHOD_NOT_IMPLEMENTED)
        when (call.method) {
					MethodCallFlutter.GET_PLATFORM_VERSION -> {
							callResult = AmazonChannelResponse(true, "Android ${android.os.Build.VERSION.RELEASE}")
					}
					MethodCallFlutter.MANAGE_AUDIO_PERMISSIONS -> {
							PermissionHelper.instance.manageAudioPermissions(result)
							isReplied = true
					}
					MethodCallFlutter.MANAGE_VIDEO_PERMISSIONS -> {
							PermissionHelper.instance.manageVideoPermissions(result)
							isReplied = true
					}
					MethodCallFlutter.REQUEST_SCREEN_CAPTURE -> {
							PermissionHelper.instance.manageScreenCapturePermissions(result)
							isReplied = true
					}
					MethodCallFlutter.JOIN -> {
							callResult = join(call)
					}
					MethodCallFlutter.LIST_AUDIO_DEVICES -> {
							callResult = listAudioDevices()
					}
					MethodCallFlutter.INITIAL_AUDIO_SELECTION -> {
							callResult = initialAudioSelection()
					}
					MethodCallFlutter.UPDATE_AUDIO_DEVICE -> {
							callResult = updateAudioDevice(call)
					}
					MethodCallFlutter.MUTE -> {
							callResult = mute()
					}
					MethodCallFlutter.UNMUTE -> {
							callResult = unmute()
					}
					MethodCallFlutter.STOP -> {
							callResult = MeetingSessionManager.stop()
					}
					MethodCallFlutter.START_LOCAL_VIDEO -> {
							callResult = startLocalVideo()
					}
					MethodCallFlutter.STOP_LOCAL_VIDEO -> {
							callResult = stopLocalVideo()
					}
					else -> callResult = AmazonChannelResponse(false, ResponseMessage.METHOD_NOT_IMPLEMENTED)
        }

        if (callResult.result) {
          result.success(callResult.toFlutterCompatibleType())
        } else {
					if (!isReplied) {
						result.error(
								"Failed",
								"MethodChannelHandler failed",
								callResult.toFlutterCompatibleType()
						)
					}
        }
    }

    fun callFlutterMethod(method: String, args: Any?) {
      channel.invokeMethod(method, args)
    }

    fun join(call: MethodCall): AmazonChannelResponse {
			val arguments = call.arguments as? Map<String, String>
			if (arguments == null) {
					return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			}

			val meetingId: String = arguments["MeetingId"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val externalMeetingId: String = arguments["ExternalMeetingId"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val mediaRegion: String = arguments["MediaRegion"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val audioHostUrl: String = arguments["AudioHostUrl"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val audioFallbackUrl: String = arguments["AudioFallbackUrl"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val signalingUrl: String = arguments["SignalingUrl"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val turnControlUrl: String = arguments["TurnControlUrl"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val externalUserId: String = arguments["ExternalUserId"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val attendeeId: String = arguments["AttendeeId"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)
			val joinToken: String = arguments["JoinToken"] ?: return AmazonChannelResponse(false, ResponseMessage.INCORRECT_JOIN_RESPONSE_PARAMS)

			val createMeetingResponse: CreateMeetingResponse = CreateMeetingResponse(
				Meeting(
						externalMeetingId,
						MediaPlacement(audioFallbackUrl, audioHostUrl, signalingUrl, turnControlUrl),
						mediaRegion,
						meetingId
				)
			)

			val createAttendeeResponse = CreateAttendeeResponse(
				Attendee(attendeeId, externalUserId, joinToken)
			)

			val meetingSessionConfiguration: MeetingSessionConfiguration = MeetingSessionConfiguration(
				createMeetingResponse, createAttendeeResponse, ::urlRewriter
			)

			val meetingSession = DefaultMeetingSession(
				meetingSessionConfiguration, ConsoleLogger(), context, MeetingSessionManager.eglCoreFactory
			)

			MeetingSessionManager.initialize(
				context,
				meetingSession,
				meetingSession.configuration.credentials,
				meetingSession.configuration,
				meetingSession.audioVideo
			)


			return MeetingSessionManager.startMeeting(
				AudioVideoObserver(this),
				DataMessageObserver(this),
				RealtimeObserver(this),
				VideoTileObserver(this),
			)
    }

    fun initialAudioSelection(): AmazonChannelResponse {
			val device = MeetingSessionManager.audioVideo.getActiveAudioDevice()
					?: return NULL_MEETING_SESSION_RESPONSE
			return AmazonChannelResponse(true, gson.toJson(mediaDeviceToMap(device)))
    }

    fun listAudioDevices(): AmazonChannelResponse {
			val audioDevices = MeetingSessionManager.audioVideo.listAudioDevices()
			val audioDeviceMapJson = audioDevices.map { device: MediaDevice ->
					mediaDeviceToMap(device)
			}
			return AmazonChannelResponse(true, gson.toJson(audioDeviceMapJson))
    }

    fun updateAudioDevice(call: MethodCall): AmazonChannelResponse {
			val arguments = call.arguments as? Map<String, String> ?: return AmazonChannelResponse(false, ResponseMessage.NULL_AUDIO_DEVICE)
			val device: String = arguments["device"] ?: return AmazonChannelResponse(false, ResponseMessage.NULL_AUDIO_DEVICE)
			val audioDevices = MeetingSessionManager.audioVideo.listAudioDevices()

			for (dev in audioDevices) {
				if (device == dev.label) {
					MeetingSessionManager.audioVideo.chooseAudioDevice(dev)
							?: return AmazonChannelResponse(false, ResponseMessage.AUDIO_DEVICE_UPDATE_FAILED)
					return AmazonChannelResponse(true, ResponseMessage.AUDIO_DEVICE_UPDATED)
				}
			}
			return AmazonChannelResponse(false, ResponseMessage.AUDIO_DEVICE_UPDATE_FAILED)
    }

    fun mute(): AmazonChannelResponse {
			val muted = MeetingSessionManager.audioVideo.realtimeLocalMute()
			return if (muted) AmazonChannelResponse(
				true,
				ResponseMessage.MUTE_SUCCESSFUL
			) else AmazonChannelResponse(false, ResponseMessage.MUTE_FAILED)
    }

    fun unmute(): AmazonChannelResponse {
			val unmuted = MeetingSessionManager.audioVideo.realtimeLocalUnmute()
			return if (unmuted) AmazonChannelResponse(
				true,
				ResponseMessage.UNMUTE_SUCCESSFUL
			) else AmazonChannelResponse(false, ResponseMessage.UNMUTE_FAILED)
    }

    fun startLocalVideo(): AmazonChannelResponse {
			MeetingSessionManager.audioVideo.startLocalVideo()
			return AmazonChannelResponse(true, ResponseMessage.LOCAL_VIDEO_ON_SUCCESS)
    }

    fun stopLocalVideo(): AmazonChannelResponse {
			MeetingSessionManager.audioVideo.stopLocalVideo()
			return AmazonChannelResponse(true, ResponseMessage.LOCAL_VIDEO_OFF_SUCCESS)
    }

    private fun mediaDeviceToMap(mediaDevice: MediaDevice) : Map<String, String?> {
			return mapOf(
					"label" to mediaDevice.label,
					"type" to mediaDevice.type.toString(),
					"id" to mediaDevice.id
			)
    }

    private fun urlRewriter(url: String): String {
			return url
    }

    private val NULL_MEETING_SESSION_RESPONSE: AmazonChannelResponse = AmazonChannelResponse(false, ResponseMessage.MEETING_SESSION_IS_NULL)
}