import { registerPlugin } from '@capacitor/core';

import type {
  NDEFRecord,
  NDEFWriteOptions,
  NFCPlugin,
  NFCPluginBasic, PayloadType,
} from './definitions';

const NFCPlug = registerPlugin<NFCPluginBasic>('NFC');
export * from './definitions';

export const NFC: NFCPlugin = {
  isSupported: NFCPlug.isSupported.bind(NFCPlug),
  startScan: NFCPlug.startScan.bind(NFCPlug),
  addListener: NFCPlug.addListener.bind(NFCPlug),
  removeAllListeners: NFCPlug.removeAllListeners.bind(NFCPlug),

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
  },

  getStrPayload(record?: NDEFRecord): string {
    return new TextDecoder().decode(record?.payload);
  }
}
