/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package dev.kxgcayh.amazon.realtime.managers

import dev.kxgcayh.amazon.realtime.AmazonChannelResponse
import dev.kxgcayh.amazon.realtime.constants.ResponseMessage
import dev.kxgcayh.amazon.realtime.observers.AudioVideoObserver
import dev.kxgcayh.amazon.realtime.observers.DataMessageObserver
import dev.kxgcayh.amazon.realtime.observers.RealtimeObserver
import dev.kxgcayh.amazon.realtime.observers.VideoTileObserver
import com.amazonaws.services.chime.sdk.meetings.utils.logger.ConsoleLogger
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoFacade
import com.amazonaws.services.chime.sdk.meetings.session.DefaultMeetingSession

import android.content.Context
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.DefaultEglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSession
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionConfiguration
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionCredentials
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.DefaultSurfaceTextureCaptureSourceFactory
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.CameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.DefaultCameraCaptureSource
import dev.kxgcayh.amazon.realtime.utils.CpuVideoProcessor
import dev.kxgcayh.amazon.realtime.utils.GpuVideoProcessor
import dev.kxgcayh.amazon.realtime.managers.ScreenShareManager

object MeetingSessionManager {
    val eglCoreFactory: EglCoreFactory = DefaultEglCoreFactory()
    private val meetingSessionlogger: ConsoleLogger = ConsoleLogger()

    lateinit var meetingSession: MeetingSession
    lateinit var credentials: MeetingSessionCredentials
    lateinit var configuration: MeetingSessionConfiguration
    lateinit var audioVideo: AudioVideoFacade
    lateinit var cameraCaptureSource: CameraCaptureSource
    lateinit var gpuVideoProcessor: GpuVideoProcessor
    lateinit var cpuVideoProcessor: CpuVideoProcessor

    lateinit var audioVideoObserver: AudioVideoObserver
    lateinit var dataMessageObserver: DataMessageObserver
    lateinit var realtimeObserver: RealtimeObserver
    lateinit var videoTileObserver: VideoTileObserver

    var meetingInitialized: Boolean = false
    var screenShareManager: ScreenShareManager? = null

    fun initialize(
        context: Context,
        meetingSession: MeetingSession,
        credentials: MeetingSessionCredentials,
        configuration: MeetingSessionConfiguration,
        audioVideo: AudioVideoFacade
    ) {
        this.meetingSession = meetingSession
        this.credentials = credentials
        this.configuration = configuration
        this.audioVideo = audioVideo

        val surfaceTextureCaptureSourceFactory = DefaultSurfaceTextureCaptureSourceFactory(
            meetingSessionlogger,
            eglCoreFactory
        )
        cameraCaptureSource = DefaultCameraCaptureSource(context, meetingSessionlogger, surfaceTextureCaptureSourceFactory).apply {
            eventAnalyticsController = meetingSession.eventAnalyticsController
        }
        cpuVideoProcessor = CpuVideoProcessor(meetingSessionlogger, eglCoreFactory)
        gpuVideoProcessor = GpuVideoProcessor(meetingSessionlogger, eglCoreFactory)
    }

    fun startMeeting(
        audioVideoObserver: AudioVideoObserver,
        dataMessageObserver: DataMessageObserver,
        realtimeObserver: RealtimeObserver,
        videoTileObserver: VideoTileObserver,
    ): AmazonChannelResponse {
        if (!meetingInitialized) {
            addObservers(
                audioVideoObserver,
                dataMessageObserver,
                realtimeObserver,
                videoTileObserver
            )
            audioVideo.start()
            audioVideo.startRemoteVideo()
            meetingInitialized = true
        }
        return AmazonChannelResponse(true, ResponseMessage.CREATE_MEETING_SUCCESS)
    }

    private fun addObservers(
        audioVideoObserver: AudioVideoObserver,
        dataMessageObserver: DataMessageObserver,
        realtimeObserver: RealtimeObserver,
        videoTileObserver: VideoTileObserver,
    ) {
        audioVideoObserver.let {
            audioVideo.addAudioVideoObserver(it)
            this.audioVideoObserver = audioVideoObserver
            meetingSessionlogger.debug("AudioVideoObserver", "AudioVideoObserver initialized")
        }
        dataMessageObserver.let {
            audioVideo.addRealtimeDataMessageObserver("capabilities", it)
            audioVideo.addRealtimeDataMessageObserver("chat", it)
            this.dataMessageObserver = dataMessageObserver
            meetingSessionlogger.debug("DataMessageObserver", "DataMessageObserver initialized")
        }
        realtimeObserver.let {
            audioVideo.addRealtimeObserver(it)
            this.realtimeObserver = realtimeObserver
            meetingSessionlogger.debug("RealtimeObserver", "RealtimeObserver initialized")
        }
        videoTileObserver.let {
            audioVideo.addVideoTileObserver(videoTileObserver)
            this.videoTileObserver = videoTileObserver
            meetingSessionlogger.debug("VideoTileObserver", "VideoTileObserver initialized")
        }
    }

    private fun removeObservers() {
        audioVideoObserver.let {
            audioVideo.removeAudioVideoObserver(it)
        }
        dataMessageObserver.let {
            audioVideo.removeRealtimeDataMessageObserverFromTopic("capabilities")
            audioVideo.removeRealtimeDataMessageObserverFromTopic("chat")
        }
        realtimeObserver.let {
            audioVideo.removeRealtimeObserver(it)
        }
        videoTileObserver.let {
            audioVideo.removeVideoTileObserver(it)
        }
    }

    fun stop(): AmazonChannelResponse {
        audioVideo.stopRemoteVideo()
        audioVideo.stop()
        removeObservers()
        meetingInitialized = false
        return AmazonChannelResponse(true, ResponseMessage.MEETING_STOPPED_SUCCESSFULLY)
    }

    private val NULL_MEETING_SESSION_RESPONSE: AmazonChannelResponse = AmazonChannelResponse(
        false, ResponseMessage.MEETING_SESSION_IS_NULL
    )
}