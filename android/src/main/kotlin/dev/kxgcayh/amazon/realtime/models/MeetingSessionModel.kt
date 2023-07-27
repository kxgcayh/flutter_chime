/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.kxgcayh.amazon.realtime.models

import androidx.lifecycle.ViewModel
import com.amazonaws.services.chime.sdk.meetings.audiovideo.AudioVideoFacade
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.backgroundfilter.backgroundblur.BackgroundBlurVideoFrameProcessor
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.backgroundfilter.backgroundreplacement.BackgroundReplacementVideoFrameProcessor
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.CameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.DefaultEglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.EglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSession
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionConfiguration
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionCredentials

import dev.kxgcayh.amazon.realtime.utils.CpuVideoProcessor
import dev.kxgcayh.amazon.realtime.utils.GpuVideoProcessor
import dev.kxgcayh.amazon.realtime.managers.ScreenShareManager

object MeetingSessionModel {
    lateinit var meetingSession: MeetingSession
    lateinit var credentials: MeetingSessionCredentials
    lateinit var configuration: MeetingSessionConfiguration
    lateinit var audioVideo: AudioVideoFacade

    fun initialize(
        meetingSession: MeetingSession,
        credentials: MeetingSessionCredentials,
        configuration: MeetingSessionConfiguration,
        audioVideo: AudioVideoFacade
    ) {
        this.meetingSession = meetingSession
        this.credentials = credentials
        this.configuration = configuration
        this.audioVideo = audioVideo
    }

    // Graphics/capture related objects
    val eglCoreFactory: EglCoreFactory = DefaultEglCoreFactory()
    lateinit var cameraCaptureSource: CameraCaptureSource
    lateinit var gpuVideoProcessor: GpuVideoProcessor
    lateinit var cpuVideoProcessor: CpuVideoProcessor
    lateinit var backgroundBlurVideoFrameProcessor: BackgroundBlurVideoFrameProcessor
    lateinit var backgroundReplacementVideoFrameProcessor: BackgroundReplacementVideoFrameProcessor
    // Source for screen capture and share, will be set only if created in call
    var screenShareManager: ScreenShareManager? = null
    // For use with replica promotions, null if not a replica meeting
    var primaryExternalMeetingId: String? = null
}
