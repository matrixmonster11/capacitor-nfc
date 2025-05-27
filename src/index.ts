import { registerPlugin } from '@capacitor/core';

import type {
  NDEFMessagesTransformable,
  NDEFWriteOptions,
  NFCPlugin,
  NFCPluginBasic,
  PayloadType,
  TagResultListenerFunc,
  NFCError
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

    await NFCPlug.writeNDEF(ndefMessage)
  }
}

NFCPlug.addListener(`nfcTag`, data=> {
  const wrappedData: NDEFMessagesTransformable = {
    strings() {
      return {
        messages: data.messages.map(message => ({
          records: message.records.map(record => ({
            type: record.type,
            payload: new TextDecoder().decode(record.payload as Uint8Array)
          }))
        }))
      }
    },
    uint8Arrays() {
      return {
        messages: data.messages.map(message => ({
          records: message.records.map(record => ({
            type: record.type,
            payload: record.payload
          }))
        }))
      }
    },
    numberArrays() {
      return {
        messages: data.messages.map(message => ({
          records: message.records.map(record => ({
            type: record.type,
            payload: Array.from(record.payload)
          }))
        }))
      }
    }
  }

  for(const listener of NFC.wrapperListeners) {
    listener(wrappedData);
  }
})