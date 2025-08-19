import type { PluginListenerHandle } from '@capacitor/core';
export declare type PayloadType = string | number[] | Uint8Array;
export interface NFCPluginBasic {
    /**
     * Checks if NFC is supported on the device. Returns true on all iOS devices, and checks for support on Android.
     */
    isSupported(): Promise<{
        supported: boolean;
    }>;
    startScan(): Promise<void>;
    /**
     * Writes an NDEF message to an NFC tag.
     * @param options The NDEF message to write.
     */
    writeNDEF<T extends PayloadType = number[]>(options: NDEFWriteOptions<T>): Promise<void>;
    /**
     * Cancels writeNDEF on Android (exits "write mode").
     */
    cancelWriteAndroid(): Promise<void>;
    /**
     * Lock the tag
     */
    lockTag(): Promise<void>;
    /**
     * Set to auto lock mode
     */
    setReadAndLockMode(data: {
        enabled: boolean;
        data: string;
    }): Promise<any>;
    /**
     * Executed the saved plugin call message
     */
    startNfcOperation(): Promise<any>;
    /**
     * Adds a listener for NFC tag detection events.
     * @param eventName The name of the event ('nfcTag').
     * @param listenerFunc The function to call when an NFC tag is detected.
     */
    addListener(eventName: 'nfcTag', listenerFunc: (data: NDEFMessages) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
    /**
     * Adds a listener for NFC tag write events.
     * @param eventName The name of the event ('nfcWriteSuccess').
     * @param listenerFunc The function to call when an NFC tag is written.
     */
    addListener(eventName: 'nfcWriteSuccess', listenerFunc: () => void): Promise<PluginListenerHandle> & PluginListenerHandle;
    /**
     * Adds a listener for NFC error events.
     * @param eventName The name of the event ('nfcError').
     * @param listenerFunc The function to call when an NFC error occurs.
     */
    addListener(eventName: 'nfcError', listenerFunc: (error: NFCError) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
    /**
     * Removes all listeners for the specified event.
     * @param eventName The name of the event.
     */
    removeAllListeners(eventName: 'nfcTag' | 'nfcError'): Promise<void>;
}
export interface NDEFMessages<T extends PayloadType = string> {
    messages: NDEFMessage<T>[];
}
export interface NDEFMessage<T extends PayloadType = string> {
    records: NDEFRecord<T>[];
}
export interface NDEFRecord<T extends PayloadType = string> {
    /**
     * The type of the record.
     */
    type: string;
    /**
     * The payload of the record.
     */
    payload: T;
}
export interface NFCError {
    /**
     * The error message.
     */
    error: string;
}
export interface NDEFWriteOptions<T extends PayloadType = Uint8Array> {
    records: NDEFRecord<T>[];
}
export declare type NDEFMessagesTransformable = {
    base64: () => NDEFMessages;
    uint8Array: () => NDEFMessages<Uint8Array>;
    string: () => NDEFMessages;
    numberArray: () => NDEFMessages<number[]>;
};
export declare type TagResultListenerFunc = (data: NDEFMessagesTransformable) => void;
export interface NFCPlugin extends Omit<NFCPluginBasic, 'writeNDEF' | 'lockTag' | 'addListener'> {
    writeNDEF: <T extends PayloadType = Uint8Array>(record?: NDEFWriteOptions<T>) => Promise<void>;
    wrapperListeners: TagResultListenerFunc[];
    onRead: (listenerFunc: TagResultListenerFunc) => void;
    onWrite: (listenerFunc: () => void) => void;
    onError: (listenerFunc: (error: NFCError) => void) => void;
    lockTag: () => Promise<void>;
    setReadAndLockMode: (data: {
        enabled: boolean;
        data: string;
    }) => Promise<any>;
    startNfcOperation: () => Promise<any>;
}
