import { WebPlugin } from '@capacitor/core';

import type { NFCPlugin } from './definitions';

export class NFCWeb extends WebPlugin implements NFCPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
