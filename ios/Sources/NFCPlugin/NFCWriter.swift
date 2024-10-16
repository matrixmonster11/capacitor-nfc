import Foundation
import CoreNFC

@objc public class NFCWriter: NSObject, NFCNDEFReaderSessionDelegate {
    private var writerSession: NFCNDEFReaderSession?
    private var messageToWrite: NFCNDEFMessage?

    public var onWriteSuccess: (() -> Void)?
    public var onError: ((Error) -> Void)?

    @objc public func startWriting(message: NFCNDEFMessage) {
        print("NFCWriter startWriting called")
        self.messageToWrite = message

        guard NFCNDEFReaderSession.readingAvailable else {
            print("NFC writing not supported on this device")
            return
        }
        writerSession = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: false)
        writerSession?.alertMessage = "Hold your iPhone near the NFC tag to write."
        writerSession?.begin()
    }

    public func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
    }

    // NFCNDEFReaderSessionDelegate methods for writing
    public func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        print("NFC writer session error: \(error.localizedDescription)")
        onError?(error)
    }

    public func readerSessionDidBecomeActive(_ session: NFCNDEFReaderSession) {
        
    }

    public func readerSession(_ session: NFCNDEFReaderSession, didDetect tags: [NFCNDEFTag]) {
        if tags.count > 1 {
            let retryInterval = DispatchTimeInterval.milliseconds(500)
            session.alertMessage = "More than one tag detected. Please try again."
            DispatchQueue.global().asyncAfter(deadline: .now() + retryInterval) {
                session.restartPolling()
            }
            return
        }

        guard let tag = tags.first else { return }

        session.connect(to: tag) { (error) in
            if let error = error {
                session.alertMessage = "Unable to connect to tag."
                session.invalidate()
                self.onError?(error)
                return
            }

            tag.queryNDEFStatus { (ndefStatus, capacity, error) in
                if let error = error {
                    session.alertMessage = "Unable to query the NDEF status of tag."
                    session.invalidate()
                    self.onError?(error)
                    return
                }

                switch ndefStatus {
                case .notSupported:
                    session.alertMessage = "Tag is not NDEF compliant."
                    session.invalidate()
                case .readOnly:
                    session.alertMessage = "Tag is read-only."
                    session.invalidate()
                case .readWrite:
                    if let message = self.messageToWrite {
                        tag.writeNDEF(message) { (error) in
                            if let error = error {
                                session.alertMessage = "Failed to write NDEF message."
                                session.invalidate()
                                self.onError?(error)
                                return
                            }
                            session.alertMessage = "NDEF message written successfully."
                            session.invalidate()
                            self.onWriteSuccess?()
                        }
                    } else {
                        session.alertMessage = "No message to write."
                        session.invalidate()
                    }
                @unknown default:
                    session.alertMessage = "Unknown NDEF tag status."
                    session.invalidate()
                }
            }
        }
    }
}
