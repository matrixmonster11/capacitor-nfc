'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const NFCPlug = core.registerPlugin('NFC');
const NFC = {
    isSupported: NFCPlug.isSupported.bind(NFCPlug),
    startScan: NFCPlug.startScan.bind(NFCPlug),
    addListener: NFCPlug.addListener.bind(NFCPlug),
    removeAllListeners: NFCPlug.removeAllListeners.bind(NFCPlug),
    async writeNDEF(options) {
        var _a;
        const ndefMessage = {
            records: (_a = options === null || options === void 0 ? void 0 : options.records.map(record => {
                const payload = typeof record.payload === "string"
                    ? (new TextEncoder()).encode(record.payload)
                    : Array.isArray(record.payload)
                        ? new Uint8Array(record.payload)
                        : record.payload instanceof Uint8Array
                            ? record.payload
                            : null;
                if (!payload)
                    throw ("Unsupported payload type");
                return {
                    type: record.type,
                    payload
                };
            })) !== null && _a !== void 0 ? _a : [],
        };
        await NFCPlug.writeNDEF(ndefMessage);
    },
    getStrPayload(record) {
        return new TextDecoder().decode(record === null || record === void 0 ? void 0 : record.payload);
    }
};

exports.NFC = NFC;
//# sourceMappingURL=plugin.cjs.js.map
