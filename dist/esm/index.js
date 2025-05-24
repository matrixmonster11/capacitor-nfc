import { registerPlugin } from '@capacitor/core';
const NFCPlug = registerPlugin('NFC');
export * from './definitions';
export const NFC = {
    isSupported: NFCPlug.isSupported.bind(NFCPlug),
    startScan: NFCPlug.startScan.bind(NFCPlug),
    addListener: NFCPlug.addListener.bind(NFCPlug),
    removeAllListeners: NFCPlug.removeAllListeners.bind(NFCPlug),
    async writeNDEF(options) {
        var _a;
        const ndefMessage = {
            records: (_a = options === null || options === void 0 ? void 0 : options.records.map(record => {
                const payload = typeof record.payload === "string"
                    ? record.payload
                    : Array.isArray(record.payload)
                        ? (new TextDecoder()).decode(new Uint8Array(record.payload))
                        : (new TextDecoder()).decode(record.payload);
                return {
                    type: record.type,
                    payload
                };
            })) !== null && _a !== void 0 ? _a : [],
        };
        await NFCPlug.writeNDEF(ndefMessage);
    },
    getUint8ArrayPayload(record) {
        var _a;
        return new Uint8Array((_a = record === null || record === void 0 ? void 0 : record.payload) !== null && _a !== void 0 ? _a : []);
    },
    getStrPayload(record) {
        return new TextDecoder().decode(NFC.getUint8ArrayPayload(record));
    }
};
//# sourceMappingURL=index.js.map