import { NativeModules,
  NativeEventEmitter, Platform } from 'react-native';

const { NfcReader } = NativeModules;

const NfcManagerEmitter = new NativeEventEmitter(NfcReader);

const NfcEvents = {
  DiscoverTag: 'NfcReadingStatus'
}

class NfcReaderManager {
  constructor() {
    NfcManagerEmitter.addListener(
      NfcEvents.DiscoverTag, this._onDiscoverTag
    );
  }

  setEventListener = (name, callback) => {
    NfcEvents.DiscoverTag = callback;
  }

  _onDiscoverTag = tag => {
      console.log("_onDiscoverTag");
      
    const callback = NfcEvents.DiscoverTag;
    if (callback) {
      callback(tag);
    }
  }

  start(num, dob, exp, check)
  {
      if(Platform.OS === 'android')
      {
        NfcReader.startNFCScan(
            num,
            dob,
            exp,
            (res)=>{
                console.log(res)
                if(res === 'NFCStatus.NOT_EXIST')
                {
                        
                }
                else if(res === 'NFCStatus.DISABLED')
                {

                }
                else if(res === 'scan_failed')
                {
                    
                }
                else
                {
                    //this.props.navigation.navigate('Success')
                }
            }
        )
      }
      else
      {
              NfcReader.startNFCScan(
            num,
            dob,
            exp,
            check,
            (res)=>{
                console.log('STATUS!!!!!! ' + res)
                if(res === 'NFCStatus.NOT_EXIST')
                {
                        
                }
                else if(res === 'NFCStatus.DISABLED')
                {

                }
                else if(res === 'scan_failed')
                {
                    
                }
                else
                {
                    //this.props.navigation.navigate('Success')
                }
            })
    
      }
  }

  getNFCStatus()
  {
      return new Promise((resolve, reject) => {
            
        NfcReader.getNFCStatus(
        (res)=>{
            console.log(res)
            resolve(res)
        })
            
        })
  }

  goToSettings()
  {
      return new Promise((resolve, reject) => {
            
        NfcReader.goToSettings(
        (res)=>{
            console.log(res)
            resolve(res)
        })
            
        })
  }

}

export default new NfcReaderManager();

export {
  NfcEvents
}


