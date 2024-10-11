import Foundation
import CoreNFC

@objc public class NFC: NSObject, NFCNDEFReaderSessionDelegate {
    private var nfcSession: NFCNDEFReaderSession?
    public var onNDEFMessageReceived: (([NFCNDEFMessage]) -> Void)?
    public var onError: ((Error) -> Void)?

    @objc public func startScanning() {
        guard NFCNDEFReaderSession.readingAvailable else {
            print("NFC scanning not supported on this device")
            return
        }
        nfcSession = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: true)
        nfcSession?.alertMessage = "Hold your iPhone near the NFC tag."
        nfcSession?.begin()
    }

    // NFCNDEFReaderSessionDelegate methods
    public func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        onError?(error)
    }

    public func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        onNDEFMessageReceived?(messages)
    }
}
