import Foundation
import Capacitor
import CoreNFC

@objc(NFCPlugin)
public class NFCPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "NFCPlugin"
    public let jsName = "NFC"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "startScan", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = NFC()

    @objc func startScan(_ call: CAPPluginCall) {
        implementation.onNDEFMessageReceived = { messages in
            var ndefMessages = [[String: Any]]()
            for message in messages {
                var records = [[String: Any]]()
                for record in message.records {
                    let recordType = String(data: record.type, encoding: .utf8) ?? ""
                    let payload = String(data: record.payload, encoding: .utf8) ?? ""
                    records.append([
                        "type": recordType,
                        "payload": payload
                    ])
                }
                ndefMessages.append([
                    "records": records
                ])
            }
            self.notifyListeners("nfcTag", data: ["messages": ndefMessages])
        }

        implementation.onError = { error in
            if let nfcError = error as? NFCReaderError {
                if nfcError.code != .readerSessionInvalidationErrorUserCanceled {
                    self.notifyListeners("nfcError", data: ["error": nfcError.localizedDescription])
                }
            }
        }

        implementation.startScanning()
        call.resolve()
    }
}
