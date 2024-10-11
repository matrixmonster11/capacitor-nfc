# capacitor-nfc

NFC plugin

## Install

```bash
npm install capacitor-nfc
npx cap sync
```

## API

<docgen-index>

- [`startScan()`](#startscan)
- [`addListener('nfcTag', ...)`](#addlistenernfctag-)
- [`addListener('nfcError', ...)`](#addlistenernfcerror-)
- [`removeAllListeners(...)`](#removealllisteners)
- [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startScan()

```typescript
startScan() => Promise<void>
```

Starts the NFC scanning session.

---

### addListener('nfcTag', ...)

```typescript
addListener(eventName: 'nfcTag', listenerFunc: (data: NDEFMessages) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

Adds a listener for NFC tag detection events.

| Param              | Type                                                                     | Description                                       |
| ------------------ | ------------------------------------------------------------------------ | ------------------------------------------------- |
| **`eventName`**    | <code>'nfcTag'</code>                                                    | The name of the event ('nfcTag').                 |
| **`listenerFunc`** | <code>(data: <a href="#ndefmessages">NDEFMessages</a>) =&gt; void</code> | The function to call when an NFC tag is detected. |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

---

### addListener('nfcError', ...)

```typescript
addListener(eventName: 'nfcError', listenerFunc: (error: NFCError) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

Adds a listener for NFC error events.

| Param              | Type                                                              | Description                                    |
| ------------------ | ----------------------------------------------------------------- | ---------------------------------------------- |
| **`eventName`**    | <code>'nfcError'</code>                                           | The name of the event ('nfcError').            |
| **`listenerFunc`** | <code>(error: <a href="#nfcerror">NFCError</a>) =&gt; void</code> | The function to call when an NFC error occurs. |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

---

### removeAllListeners(...)

```typescript
removeAllListeners(eventName: 'nfcTag' | 'nfcError') => Promise<void>
```

Removes all listeners for the specified event.

| Param           | Type                                | Description            |
| --------------- | ----------------------------------- | ---------------------- |
| **`eventName`** | <code>'nfcTag' \| 'nfcError'</code> | The name of the event. |

---

### Interfaces

#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

#### NDEFMessages

| Prop           | Type                       |
| -------------- | -------------------------- |
| **`messages`** | <code>NDEFMessage[]</code> |

#### NDEFMessage

| Prop          | Type                      |
| ------------- | ------------------------- |
| **`records`** | <code>NDEFRecord[]</code> |

#### NDEFRecord

| Prop          | Type                | Description                |
| ------------- | ------------------- | -------------------------- |
| **`type`**    | <code>string</code> | The type of the record.    |
| **`payload`** | <code>string</code> | The payload of the record. |

#### NFCError

| Prop        | Type                | Description        |
| ----------- | ------------------- | ------------------ |
| **`error`** | <code>string</code> | The error message. |

</docgen-api>
