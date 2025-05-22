import { registerPlugin } from '@capacitor/core';

import type { NDEFRecord, NFCPlugin } from './definitions';

export const NFC = registerPlugin<NFCPlugin>('NFC');
export * from './definitions';

/**
 * Get the payload of an NDEF record as a Uint8Array.
 */
export const u8Payload = (record?: NDEFRecord): Uint8Array => {
  return new Uint8Array(record?.payload ?? []);
}

/**
 * Get the payload of an NDEF record as a string.
 */
export const strPayload = (record?: NDEFRecord): string => {
  return new TextDecoder().decode(u8Payload(record));
}