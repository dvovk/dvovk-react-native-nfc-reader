#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <CoreNFC/CoreNFC.h>

@class TAGProcessing;

@protocol FredTestProtocol
- (void) scanFinished:(NSString *)result;
@end

@interface NfcReader : RCTEventEmitter <RCTBridgeModule, FredTestProtocol>
{
    
    TAGProcessing * myOb;
    NSTimer* myTimer;
    RCTResponseSenderBlock res_callback;
}

@property  id<FredTestProtocol> delegate;

-(void)checkScan:(NSTimer*) t;

@end
