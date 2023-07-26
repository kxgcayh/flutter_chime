import UIKit
import Flutter
import Foundation

public class AmazonRealtimePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let binaryMessenger: FlutterBinaryMessenger = registrar.messenger()
    let channel = FlutterMethodChannel(name: "dev.kxgcayh.amazon.realtime", binaryMessenger: binaryMessenger)
    let instance = AmazonRealtimePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)

    let iosViewFactory = IOSViewFactory(messenger: binaryMessenger)
    registrar.register(iosViewFactory, withId: "videoTile")
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
