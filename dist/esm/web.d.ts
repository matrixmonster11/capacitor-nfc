import { WebPlugin } from '@capacitor/core';
import type { NFCPlugin, TagResultListenerFunc } from './definitions';
export declare class NFCWeb extends WebPlugin implements NFCPlugin {
    wrapperListeners: never[];
    isSupported(): Promise<{
        supported: boolean;
    }>;
    startScan(): Promise<void>;
    writeNDEF(): Promise<void>;
    onRead(_func: TagResultListenerFunc): Promise<void>;
    onWrite(): Promise<void>;
    onError(_errorFn: (error: any) => void): Promise<void>;
}
