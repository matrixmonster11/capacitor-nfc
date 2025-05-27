import { registerPlugin } from '@capacitor/core';
const NFCPlug = registerPlugin('NFC');
export * from './definitions';
export const NFC = {
    isSupported: NFCPlug.isSupported.bind(NFCPlug),
    startScan: NFCPlug.startScan.bind(NFCPlug),
    onRead: (func) => NFC.wrapperListeners.push(func),
    onWrite: () => NFCPlug.addListener(`nfcWriteSuccess`, () => Promise.resolve()),
    onError: (errFunc) => NFCPlug.addListener(`nfcError`, errFunc),
    removeAllListeners: NFCPlug.removeAllListeners.bind(NFCPlug),
    wrapperListeners: [],
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
    }
};
const decodeBase64 = (base64Payload) => atob(base64Payload).split('').map(char => char.charCodeAt(0));
const mapPayloadTo = (type, data) => {
    return {
        messages: data.messages.map(message => ({
            records: message.records.map(record => ({
                type: record.type,
                payload: type === "b64"
                    ? (new TextEncoder()).encode(btoa(String.fromCharCode(...record.payload)))
                    : type === "string"
                        ? decodeBase64((new TextDecoder()).decode(record.payload))
                        : type === "uint8Array"
                            ? new Uint8Array(decodeBase64((new TextDecoder()).decode(record.payload)))
                            : type === "numberArray"
                                ? Array.from(decodeBase64((new TextDecoder()).decode(record.payload)))
                                : record.payload
            }))
        }))
    };
};
NFCPlug.addListener(`nfcTag`, data => {
    const wrappedData = {
        strings() {
            return mapPayloadTo("string", data);
        },
        uint8Arrays() {
            return mapPayloadTo("uint8Array", data);
        },
        numberArrays() {
            return mapPayloadTo("numberArray", data);
        }
    };
    for (const listener of NFC.wrapperListeners) {
        listener(wrappedData);
    }
});
//# sourceMappingURL=index.js.map