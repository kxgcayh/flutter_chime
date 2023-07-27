import AVFoundation

class PermissionManager: NSObject {
    static let shared = PermissionManager()

    func manageAudioPermissions() -> AmazonChannelResponse {
        let audioPermission = AVAudioSession.sharedInstance().recordPermission
        switch audioPermission {
            case .undetermined:
                if self.requestAudioPermission() {
                    return AmazonChannelResponse(result: true, arguments: ResponseMessage.AUDIO_AUTH_GRANTED)
                }
                return AmazonChannelResponse(result: false, arguments: ResponseMessage.AUDIO_AUTH_NOT_GRANTED)
            case .granted:
                return AmazonChannelResponse(result: true, arguments: ResponseMessage.AUDIO_AUTH_GRANTED)
            case .denied:
                return AmazonChannelResponse(result: false, arguments: ResponseMessage.AUDIO_AUTH_NOT_GRANTED)
            default:
                return AmazonChannelResponse(result: false, arguments: ResponseMessage.UNKNOWN_AUDIO_AUTHORIZATION_STATUS)
        }
    }

    private func requestAudioPermission() -> Bool {
        var result = false

        let group = DispatchGroup()
        group.enter()
        DispatchQueue.global(qos: .default).async {
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                result = granted
                group.leave()
            }
        }
        group.wait()
        return result
    }
}