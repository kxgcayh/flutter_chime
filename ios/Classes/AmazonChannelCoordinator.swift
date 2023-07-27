import UIKit
import Flutter
import Foundation
import AmazonChimeSDK
import AVFoundation
import AmazonChimeSDKMedia

class AmazonChannelCoordinator: NSObject {
    static let shared = AmazonChannelCoordinator()

    public func onMethodCall(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        var callResult: AmazonChannelResponse = AmazonChannelResponse(result: false, arguments: ResponseMessage.METHOD_NOT_IMPLEMENTED)
        if let method = call.method as? String {
            switch method {
                case MethodCallFlutter.GET_PLATFORM_VERSION:
                    callResult = AmazonChannelResponse(result: true, arguments: "iOS \(UIDevice.current.systemVersion)")
                case MethodCallFlutter.MANAGE_AUDIO_PERMISSIONS:
                    callResult = self.manageAudioPermissions()
                default:
                    callResult = AmazonChannelResponse(result: false, arguments: ResponseMessage.METHOD_NOT_IMPLEMENTED)
            }
        }
        result(callResult.toFlutterCompatibleType())
    }
}
