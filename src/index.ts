import { registerPlugin } from '@capacitor/core';

import type {
  NDEFRecord,
  NDEFWriteOptions,
  NFCPlugin,
  NFCPluginBasic
} from './definitions';

const NFCPlug = registerPlugin<NFCPluginBasic>('NFC');
export * from './definitions';

export const NFC: NFCPlugin = {
  isSupported: NFCPlug.isSupported.bind(NFCPlug),
  startScan: NFCPlug.startScan.bind(NFCPlug),
  addListener: NFCPlug.addListener.bind(NFCPlug),
  removeAllListeners: NFCPlug.removeAllListeners.bind(NFCPlug),

  async writeNDEF<T extends string | number[] | Uint8Array = string>(options?: NDEFWriteOptions<T>): Promise<void> {
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
  },

  getStrPayload(record?: NDEFRecord): string {
    return new TextDecoder().decode(record?.payload);
  }
}
