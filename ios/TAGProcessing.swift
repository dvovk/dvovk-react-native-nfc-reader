//
//  TAGProcessing.swift
//  DoubleConversion
//
//  Created by Dmitry Vovk on 12/2/19.
//

import Foundation
import CoreNFC

@available(iOS 13.0, *)
@objc public class TAGProcessing: NSObject, NFCTagReaderSessionDelegate {
var is_success = false
var success_string : String = "PROGRESS"
var passport : NFCPassportModel = NFCPassportModel()
var readerSession: NFCTagReaderSession?

var dataGroupsToRead : [DataGroupId] = [.COM, .DG1, .DG2]

var tagReader : TagReader?
var bacHandler : BACHandler?
var mrzKey : String = ""
    var deleg : FredTestProtocol?

    public override init() {
        super.init()
        
}
    
    @objc public func checkForResult() -> String
    {
        return success_string
    }

    
    @objc public func start(_ mrzK:String!, delegate:FredTestProtocol!)
{
    deleg = delegate;
    
    self.mrzKey = mrzK
    readerSession = NFCTagReaderSession(pollingOption: [.iso14443, .iso15693], delegate: self)
    readerSession?.begin()
    print("isReady: \(readerSession?.isReady)")
}


// MARK: - NFCTagReaderSessionDelegate
public func tagReaderSessionDidBecomeActive(_ session: NFCTagReaderSession) {
    // If necessary, you may perform additional operations on session start.
    // At this point RF polling is enabled.
    Log.debug( "tagReaderSessionDidBecomeActive" )
}

public func tagReaderSession(_ session: NFCTagReaderSession, didInvalidateWithError error: Error) {
    // If necessary, you may handle the error. Note session is no longer valid.
    // You must create a new session to restart RF polling.
    if(is_success)
    {
        success_string = "SUCCESS"
        Log.debug( "Read SUCCESS" )
    }
    else
    {
        success_string = "ERROR"
        Log.debug( "tagReaderSession:didInvalidateWithError - \(error)" )
    }
    
    deleg!.scanFinished(success_string)
    
    self.readerSession = nil
    
}

public func tagReaderSession(_ session: NFCTagReaderSession, didDetect tags: [NFCTag]) {
    Log.debug( "tagReaderSession:didDetect - \(tags[0])" )
    if tags.count > 1 {
        session.alertMessage = "More than 1 tags was found. Please present only 1 tag."
        return
    }
    
    let tag = tags.first!
    var passportTag: NFCISO7816Tag
    switch tags.first! {
    case let .iso7816(tag):
        passportTag = tag
    default:
        session.invalidate(errorMessage: "Tag not valid.")
        return
    }
    
    // Connect to tag
    session.connect(to: tag) { [unowned self] (error: Error?) in
        if error != nil {
            session.invalidate(errorMessage: "Connection error. Please try again.")
            return
        }
        
        self.readerSession?.alertMessage = "Authenticating with passport....."
        
        self.tagReader = TagReader(tag:passportTag)
        
        self.startReading( )
        
    }
}

func startReading() {
    self.handleBAC(completed: { [weak self] error in
        if error == nil {
            // At this point, BAC Has been done and the TagReader has been set up with the SecureMessaging
            // session keys
            
            self?.is_success = true
            self?.readerSession?.invalidate()
            
            /*self?.readerSession?.alertMessage = "Reading passport data....."
            
            self?.readNextDataGroup( ) { [weak self] error in
                if error != nil {
                    self?.readerSession?.invalidate(errorMessage: "Sorry, there was a problem reading the passport. Please try again" )
                } else {
                    self?.is_success = true
                    self?.readerSession?.invalidate()
                    //self?.scanCompletedHandler( self?.passport, nil )
                }
            }*/
        } else {
            self?.readerSession?.invalidate(errorMessage: "Sorry, there was a problem reading the passport. Please try again" )
            //self?.scanCompletedHandler(nil, error)
        }
    })
}

func handleBAC( completed: @escaping (TagError?)->()) {
    guard let tagReader = self.tagReader else {
        completed(TagError.NoConnectedTag)
        return
    }
    
    self.bacHandler = BACHandler( tagReader: tagReader )
    bacHandler?.performBACAndGetSessionKeys( mrzKey: self.mrzKey ) { error in
        self.bacHandler = nil
        completed(error)
    }
}

func readNextDataGroup( completedReadingGroups completed : @escaping (TagError?)->() ) {
    guard let tagReader = self.tagReader else { completed(TagError.NoConnectedTag ); return }
    if dataGroupsToRead.count == 0 {
        completed(nil)
        return
    }
    
    let dgId = dataGroupsToRead.removeFirst()
    Log.info( "Reading tag - \(dgId)" )
    
    tagReader.readDataGroup(dataGroup:dgId) { [unowned self] (response, err) in
        if let response = response {
            do {
                let dg = try DataGroupParser().parseDG(data: response)
                self.passport.dataGroupsRead[dgId] = dg
                self.readNextDataGroup(completedReadingGroups: completed)
            } catch let error as TagError {
                Log.error( "TagError reading tag - \(error)" )
                completed( error )
            } catch let error {
                Log.error( "Unexpected error reading tag - \(error)" )
                completed( TagError.UnexpectedError )
            }
            
        } else {
            completed( err )
        }
    }
}
}
