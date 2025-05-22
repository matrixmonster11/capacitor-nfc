import { registerPlugin } from '@capacitor/core';

import type {
  NDEFRecord,
  NDEFWriteOptions,
  NDEFWriteStringOptions,
  NDEFWriteUint8ArrayOptions,
  NFCPlugin,
  NFCPluginBasic
} from './definitions';

const NFCPlug = registerPlugin<NFCPluginBasic>('NFC');
export * from './definitions';

export const NFC: NFCPlugin = {
  startScan: NFCPlug.startScan.bind(NFCPlug),
  writeNDEF: NFCPlug.writeNDEF.bind(NFCPlug),
  addListener: NFCPlug.addListener.bind(NFCPlug),
  removeAllListeners: NFCPlug.removeAllListeners.bind(NFCPlug),
  async writeNDEFStr(options: NDEFWriteStringOptions): Promise<void> {
    const ndefMessage: NDEFWriteUint8ArrayOptions = {
      records: options.records.map(record => ({
        type: record.type,
        payload: new TextEncoder().encode(record.payload ?? []),
      })),
    };

    console.log("WRITE MESSAGE str", ndefMessage);

    await NFC.writeNDEFU8Array(ndefMessage);
  },
  async writeNDEFU8Array(options: NDEFWriteUint8ArrayOptions): Promise<void> {
    const ndefMessage: NDEFWriteOptions = {
      records: options.records.map(record => ({
        type: record.type,
        payload: [...record.payload.values()],
      })),
    };

    console.log("WRITE MESSAGE u8[]", ndefMessage);

    await NFCPlug.writeNDEF(ndefMessage);
  },
  getUint8ArrayPayload(record?: NDEFRecord): Uint8Array {
    return new Uint8Array(record?.payload ?? []);
  },
  getStrPayload(record?: NDEFRecord): string {
    return new TextDecoder().decode(NFC.getUint8ArrayPayload(record));
  }
}
