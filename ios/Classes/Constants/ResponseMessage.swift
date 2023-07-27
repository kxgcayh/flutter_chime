import Foundation

class ResponseMessage {
    static let AUDIO_AUTH_GRANTED = "iOS: Audio usage authorized"
    static let AUDIO_AUTH_NOT_GRANTED = "iOS: Failed to authorize audio"
    static let VIDEO_AUTH_GRANTED = "iOS: Video usage authorized"
    static let VIDEO_AUTH_NOT_GRANTED = "iOS: Failed to authorize video"
    static let UNKNOWN_AUDIO_AUTHORIZATION_STATUS = "iOS: Failed to Authorization Audio"
    static let INCORRECT_JOIN_RESPONSE_PARAMS = "iOS: ERROR api response has incorrect/missing parameters"
    static let CREATE_MEETING_SUCCESS = "iOS: meetingSession created successfully"
    static let MEETING_STOPPED_SUCCESSFULLY = "iOS: meetingSession stopped successfully"
    static let MEETING_SESSION_IS_NULL = "iOS: ERROR Meeting session is null"
    static let MUTE_SUCCESSFUL = "iOS: Successfully muted user"
    static let MUTE_FAILED = "iOS: ERROR failed to mute user"
    static let UNMUTE_SUCCESSFUL = "iOS: Successfully unmuted user"
    static let UNMUTE_FAILED = "iOS: ERROR failed to unmute user"
    static let LOCAL_VIDEO_ON_SUCCESS = "iOS: Started local video"
    static let LOCAL_VIDEO_OFF_SUCCESS = "iOS: Stopped local video"
    static let AUDIO_DEVICE_UPDATED = "iOS: Audio device updated"
    static let AUDIO_DEVICE_UPDATE_FAILED = "iOS: Failed to update audio device"
    static let NULL_AUDIO_DEVICE = "iOS: ERROR received null as audio device"
    static let METHOD_NOT_IMPLEMENTED = "iOS: ERROR method not implemented"
}
