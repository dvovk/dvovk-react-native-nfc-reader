# react-native-nfc-reader

## Info
This is test module. Please leave request on vovk.dimon@gmail.com with subject 'improve react-native-nfc-reader' and I'll modify library if many people need it.

## Getting started

`$ npm install react-native-nfc-reader --save`

### Mostly automatic installation

`$ react-native link react-native-nfc-reader`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-nfc-reader` and add `NfcReader.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libNfcReader.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.NfcReaderPackage;` to the imports at the top of the file
  - Add `new NfcReaderPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-nfc-reader'
  	project(':react-native-nfc-reader').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-nfc-reader/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-nfc-reader')
  	```


## Usage
```javascript
import NfcReader from 'react-native-nfc-reader';

// TODO: What to do with the module?
NfcReader;
```
