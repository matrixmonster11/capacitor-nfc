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
        const payload: string = typeof record.payload === "string"
          ? record.payload
          : Array.isArray(record.payload)
            ? (new TextDecoder()).decode(new Uint8Array(record.payload as number[]))
            : (new TextDecoder()).decode(record.payload as Uint8Array);
        return {
          type: record.type,
          payload
        }
      }) ?? [],
    };

    await NFCPlug.writeNDEF(ndefMessage)
  },

  getUint8ArrayPayload(record?: NDEFRecord): Uint8Array {
    return new Uint8Array(record?.payload ?? []);
  },
  getStrPayload(record?: NDEFRecord): string {
    return new TextDecoder().decode(NFC.getUint8ArrayPayload(record));
  }
}
