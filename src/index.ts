import { registerPlugin } from '@capacitor/core';

import type { NFCPlugin } from './definitions';

const NFC = registerPlugin<NFCPlugin>('NFC', {
  web: () => import('./web').then(m => new m.NFCWeb()),
});

export * from './definitions';
export { NFC };
