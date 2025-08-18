import { WebPlugin } from '@capacitor/core';

import type { NFCPlugin, TagResultListenerFunc } from './definitions';

export class NFCWeb extends WebPlugin implements NFCPlugin {
  wrapperListeners = [];
  
  isSupported(): Promise<{ supported: boolean }> {
    return Promise.resolve({ supported: false });
  }

  startScan(): Promise<void> {
    return Promise.reject(new Error('NFC is not supported on web'));
  }

  cancelWriteAndroid(): Promise<void> {
    return Promise.reject(new Error('NFC is not supported on web'));
  }

  writeNDEF(): Promise<void> {
    return Promise.reject(new Error('NFC is not supported on web'));
  }
  
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  onRead(_func: TagResultListenerFunc): Promise<void> {
    return Promise.reject(new Error('NFC is not supported on web'));
  }

  onWrite(): Promise<void> {
    return Promise.reject(new Error('NFC is not supported on web'));
  }

  lockTag(): Promise<void> {
    return Promise.reject(new Error('NFC is not supported on web'));
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  onError(_errorFn: (error: any) => void): Promise<void> {
    return Promise.reject(new Error('NFC is not supported on web'));
  }
}