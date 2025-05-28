'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const NFCPlug = core.registerPlugin('NFC');
const NFC = {
    isSupported: NFCPlug.isSupported.bind(NFCPlug),
    startScan: NFCPlug.startScan.bind(NFCPlug),
    onRead: (func) => NFC.wrapperListeners.push(func),
    onWrite: () => NFCPlug.addListener(`nfcWriteSuccess`, () => Promise.resolve()),
    onError: (errorFn) => {
        NFCPlug.addListener(`nfcError`, errorFn);
    },
    removeAllListeners: (eventName) => {
        NFC.wrapperListeners = [];
        return NFCPlug.removeAllListeners(eventName);
    },
    wrapperListeners: [],
    async writeNDEF(options) {
        var _a;
        const ndefMessage = {
            records: (_a = options === null || options === void 0 ? void 0 : options.records.map((record) => {
                const payload = typeof record.payload === 'string'
                    ? Array.from(new TextEncoder().encode(record.payload))
                    : Array.isArray(record.payload)
                        ? record.payload
                        : record.payload instanceof Uint8Array
                            ? Array.from(record.payload)
                            : null;
                if (!payload)
                    throw 'Unsupported payload type';
                return {
                    type: record.type,
                    payload,
                };
            })) !== null && _a !== void 0 ? _a : [],
        };
        console.log('WRITING NDEF MESSAGE', ndefMessage);
        await NFCPlug.writeNDEF(ndefMessage);
    },
};
const decodeBase64 = (base64Payload) => {
    console.log("DECODING BASE64", base64Payload, atob(base64Payload)
        .split('')
        .map((char) => char.charCodeAt(0)));
    return atob(base64Payload)
        .split('')
        .map((char) => char.charCodeAt(0));
};
const mapPayloadTo = (type, data) => {
    return {
        messages: data.messages.map(message => ({
            records: message.records.map(record => ({
                type: record.type,
                payload: type === "b64"
                    ? record.payload
                    : type === "string"
                        ? decodeBase64(record.payload)
                        : type === "uint8Array"
                            ? new Uint8Array(decodeBase64(record.payload))
                            : type === "numberArray"
                                ? Array.from(decodeBase64(record.payload))
                                : record.payload
            }))
        }))
    };
};
NFCPlug.addListener(`nfcTag`, data => {
    console.log("GOT DATA", data);
    const wrappedData = {
        base64() {
            return mapPayloadTo("b64", data);
        },
        string() {
            return mapPayloadTo("string", data);
        },
        uint8Array() {
            return mapPayloadTo("uint8Array", data);
        },
        numberArray() {
            return mapPayloadTo("numberArray", data);
        }
    };
    for (const listener of NFC.wrapperListeners) {
        console.log("CALLING LISTENER WITH", wrappedData);
        listener(wrappedData);
    }
});

exports.NFC = NFC;
//# sourceMappingURL=plugin.cjs.js.map
