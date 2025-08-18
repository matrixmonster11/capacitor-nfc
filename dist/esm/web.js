import { WebPlugin } from '@capacitor/core';
export class NFCWeb extends WebPlugin {
    constructor() {
        super(...arguments);
        this.wrapperListeners = [];
    }
    isSupported() {
        return Promise.resolve({ supported: false });
    }
    startScan() {
        return Promise.reject(new Error('NFC is not supported on web'));
    }
    cancelWriteAndroid() {
        return Promise.reject(new Error('NFC is not supported on web'));
    }
    writeNDEF() {
        return Promise.reject(new Error('NFC is not supported on web'));
    }
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    onRead(_func) {
        return Promise.reject(new Error('NFC is not supported on web'));
    }
    onWrite() {
        return Promise.reject(new Error('NFC is not supported on web'));
    }
    lockTag() {
        return Promise.reject(new Error('NFC is not supported on web'));
    }
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    onError(_errorFn) {
        return Promise.reject(new Error('NFC is not supported on web'));
    }
}
//# sourceMappingURL=web.js.map