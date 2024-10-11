capacitor-nfc
An NFC plugin for Capacitor that allows reading and writing NFC tags, including password-protected tags.

Install
bash
Copy code
npm install capacitor-nfc
npx cap sync
iOS Setup
To use NFC functionalities on iOS, you need to perform additional setup:

Enable the NFC capability:

Open your app in Xcode (npx cap open ios).
Select your project in the Project Navigator.
Go to the Signing & Capabilities tab.
Click on + Capability and add Near Field Communication Tag Reading.
Update Info.plist:

You need to add NFC usage descriptions to your Info.plist file.

Add the following keys and values to Info.plist:

xml
Copy code

<key>NFCReaderUsageDescription</key>
<string>This app requires access to NFC to read and write NFC tags.</string>
If your app uses background NFC tag reading (rare cases), you may need to add:

xml
Copy code

<key>com.apple.developer.nfc.readersession.formats</key>
<array>
  <string>NDEF</string>
</array>
Ensure that your app's Entitlements file includes the necessary permissions.

Add Privacy Description:

iOS requires a description explaining why the app needs NFC access.

Add the following to Info.plist:

xml
Copy code

<key>Privacy - NFC Scan Usage Description</key>
<string>This app uses NFC to interact with NFC tags for reading and writing data.</string>
API
<docgen-index>
startScan()
writeNdef(...)
addListener('nfcTag', ...)
addListener('nfcError', ...)
addListener('nfcWriteSuccess', ...)
removeAllListeners(...)
Interfaces
</docgen-index> <docgen-api> <!--Update the source file JSDoc comments and rerun docgen to update the docs below-->
startScan()
typescript
Copy code
startScan() => Promise<void>
Starts the NFC scanning session.

Returns: <code>Promise<void></code>

writeNdef(...)
typescript
Copy code
writeNdef(options: { records: NdefRecordInput[]; password?: string; }) => Promise<void>
Writes an NDEF message to an NFC tag.

Param Type Description
options <code>{ records: <a href="#ndefrecordinput">NdefRecordInput</a>[]; password?: string; }</code> The NDEF message records and optional password for the tag.
Returns: <code>Promise<void></code>

addListener('nfcTag', ...)
typescript
Copy code
addListener(eventName: 'nfcTag', listenerFunc: (data: NDEFMessages) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
Adds a listener for NFC tag detection events.

Param Type Description
eventName <code>'nfcTag'</code> The name of the event ('nfcTag').
listenerFunc <code>(data: <a href="#ndefmessages">NDEFMessages</a>) => void</code> The function to call when an NFC tag is detected.
Returns: <code>Promise<<a href="#pluginlistenerhandle">PluginListenerHandle</a>> & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

addListener('nfcError', ...)
typescript
Copy code
addListener(eventName: 'nfcError', listenerFunc: (error: NFCError) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
Adds a listener for NFC error events.

Param Type Description
eventName <code>'nfcError'</code> The name of the event ('nfcError').
listenerFunc <code>(error: <a href="#nfcerror">NFCError</a>) => void</code> The function to call when an NFC error occurs.
Returns: <code>Promise<<a href="#pluginlistenerhandle">PluginListenerHandle</a>> & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

addListener('nfcWriteSuccess', ...)
typescript
Copy code
addListener(eventName: 'nfcWriteSuccess', listenerFunc: () => void) => Promise<PluginListenerHandle> & PluginListenerHandle
Adds a listener for NFC write success events.

Param Type Description
eventName <code>'nfcWriteSuccess'</code> The name of the event ('nfcWriteSuccess').
listenerFunc <code>() => void</code> The function to call when an NFC write is successful
Returns: <code>Promise<<a href="#pluginlistenerhandle">PluginListenerHandle</a>> & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

removeAllListeners(...)
typescript
Copy code
removeAllListeners(eventName: 'nfcTag' | 'nfcError' | 'nfcWriteSuccess') => Promise<void>
Removes all listeners for the specified event.

Param Type Description
eventName <code>'nfcTag' | 'nfcError' | 'nfcWriteSuccess'</code> The name of the event.
Returns: <code>Promise<void></code>

Interfaces
PluginListenerHandle
Prop Type
remove <code>() => Promise<void></code>
NDEFMessages
Prop Type
messages <code>NDEFMessage[]</code>
NDEFMessage
Prop Type
records <code>NDEFRecord[]</code>
NDEFRecord
Prop Type Description
type <code>string</code> The type of the record.
payload <code>string</code> The payload of the record.
NdefRecordInput
Prop Type Description
type <code>string</code> The type of the record.
payload <code>string</code> The payload of the record.
NFCError
Prop Type Description
error <code>string</code> The error message.
</docgen-api>
Usage
Reading NFC Tags
typescript
Copy code
import { Plugins } from '@capacitor/core';
const { NFC } = Plugins;

// Start NFC scanning
NFC.startScan().catch(error => {
console.error('Failed to start NFC scan:', error);
});

// Listen for NFC tags
const tagListener = NFC.addListener('nfcTag', (data) => {
console.log('NFC Tag Data:', data);
});

// Listen for errors
const errorListener = NFC.addListener('nfcError', (error) => {
console.error('NFC Error:', error);
});

// Remove listeners when done
// tagListener.remove();
// errorListener.remove();
Writing to NFC Tags
typescript
Copy code
import { Plugins } from '@capacitor/core';
const { NFC } = Plugins;

// Listen for write success
const writeSuccessListener = NFC.addListener('nfcWriteSuccess', () => {
console.log('NFC Write Successful');
});

// Write NDEF message to tag
NFC.writeNdef({
records: [
{
type: 'T', // Text record
payload: 'Hello NFC',
},
],
password: 'your_password', // Optional password if required by the tag
}).catch(error => {
console.error('Failed to write NDEF message:', error);
});

// Remove listeners when done
// writeSuccessListener.remove();
Additional Information
Password Functionality
Password Protection:

If your NFC tags are password-protected, you can provide a password in the writeNdef method.
The password should be a string that corresponds to the tag's expected password.
Tag Compatibility:

Password functionality depends on the NFC tag's type and capabilities.
Not all tags support password protection or the same authentication methods.
Ensure that you are using compatible tags and consult the tag's documentation.
Error Handling
Common Errors:

Tag Not Supported: The tag is not NDEF compatible.
Read-Only Tag: The tag is read-only and cannot be written to.
Authentication Failed: Password authentication failed.
Connection Issues: Unable to connect to the tag.
Error Listener:

Use the nfcError event listener to handle errors gracefully in your application.
iOS Limitations
Background NFC Scanning:

iOS restricts NFC scanning to foreground sessions initiated by the user.
Background scanning requires specific entitlements and is limited.
Device Compatibility:

Ensure that the device supports NFC functionalities.
Use NFC.isAvailable() to check NFC availability (implement this method if needed).
Security Considerations
Password Handling:

Store and handle passwords securely within your application.
Avoid hardcoding passwords and consider using secure storage solutions.
Data Privacy:

Be mindful of the data you read from and write to NFC tags.
Ensure compliance with data protection regulations.
Contributing
Contributions are welcome! Please open an issue or submit a pull request.

License
MIT
