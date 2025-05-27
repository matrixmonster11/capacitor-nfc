import { registerPlugin } from '@capacitor/core';

import type {
  NDEFMessagesTransformable,
  NDEFWriteOptions,
  NFCPlugin,
  NFCPluginBasic,
  PayloadType,
  TagResultListenerFunc,
  NFCError,
  NDEFMessages
} from './definitions';

const NFCPlug = registerPlugin<NFCPluginBasic>('NFC');
export * from './definitions';

export const NFC: NFCPlugin = {
  isSupported: NFCPlug.isSupported.bind(NFCPlug),
  startScan: NFCPlug.startScan.bind(NFCPlug),
  onRead: (func: TagResultListenerFunc)=> NFC.wrapperListeners.push(func),
  onWrite: ()=> NFCPlug.addListener(`nfcWriteSuccess`, () => Promise.resolve()),
  onError: (errFunc: (error: NFCError) => void)=> NFCPlug.addListener(`nfcError`, errFunc),
  removeAllListeners: NFCPlug.removeAllListeners.bind(NFCPlug),
  wrapperListeners: [],

  async writeNDEF<T extends PayloadType = Uint8Array>(options?: NDEFWriteOptions<T>): Promise<void> {
    const ndefMessage: NDEFWriteOptions = {
      records: options?.records.map(record => {
        const payload: Uint8Array | null = typeof record.payload === "string"
          ? (new TextEncoder()).encode(record.payload)
          : Array.isArray(record.payload)
            ? new Uint8Array(record.payload)
            : record.payload instanceof Uint8Array
              ? record.payload
              : null;

        if(!payload) throw("Unsupported payload type")

        return {
          type: record.type,
          payload
        }
      }) ?? [],
    };

    console.log("WRITING NDEF MESSAGE", ndefMessage);
    await NFCPlug.writeNDEF(ndefMessage)
  }
}

type DecodeSpecifier = "b64" | "string" | "uint8Array" | "numberArray";
type decodedType<T extends DecodeSpecifier> = NDEFMessages<T extends "b64" ? string : T extends "string" ? string : T extends "uint8Array" ? Uint8Array : number[]>
const decodeBase64 = (base64Payload: string)=> {
  console.log("DECODING BASE64", base64Payload, atob(base64Payload)
    .split('')
    .map((char) => char.charCodeAt(0)));
  return atob(base64Payload)
    .split('')
    .map((char) => char.charCodeAt(0));
}
const mapPayloadTo = <T extends DecodeSpecifier>(type: T, data: NDEFMessages): decodedType<T> => {
  return {
    messages: data.messages.map(message => ({
      records: message.records.map(record => ({
        type: record.type,
        payload:
          type === "b64"
            ? record.payload
            :type === "string"
              ? decodeBase64(record.payload)
              : type === "uint8Array"
                ? new Uint8Array(decodeBase64(record.payload))
                : type === "numberArray"
                  ? Array.from(decodeBase64(record.payload))
                  : record.payload
      }))
    }))
  } as decodedType<T>
}

NFCPlug.addListener(`nfcTag`, data=> {
  console.log("GOT DATA", data);
  const wrappedData: NDEFMessagesTransformable = {
    strings() {
      return mapPayloadTo("string", data)
    },
    uint8Arrays() {
      return mapPayloadTo("uint8Array", data)
    },
    numberArrays() {
      return mapPayloadTo("numberArray", data)
    }
  }

  for(const listener of NFC.wrapperListeners) {
    console.log("CALLING LISTENER WITH", wrappedData)
    listener(wrappedData);
  }
})