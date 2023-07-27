import Flutter
import UIKit

public class AmazonRealtimePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let binaryMessenger: FlutterBinaryMessenger = registrar.messenger()
    let channel = FlutterMethodChannel(name: "dev.kxgcayh.amazon.realtime.plugin", binaryMessenger: binaryMessenger)
    let instance = AmazonRealtimePlugin()

    registrar.addMethodCallDelegate(instance, channel: channel)
    channel.setMethodCallHandler(AmazonChannelCoordinator.shared.onMethodCall)

    let iosViewFactory = IOSViewFactory(messenger: binaryMessenger)
    registrar.register(iosViewFactory, withId: "videoTile")
  }
}