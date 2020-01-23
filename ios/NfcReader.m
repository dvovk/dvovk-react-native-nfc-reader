#import "NfcReader.h"
#import <react_native_nfc_reader/react_native_nfc_reader-Swift.h>

@implementation NfcReader

- (void) scanFinished:(NSString *)result
{
    if([result isEqualToString:@"SUCCESS"])
    {
        [self sendEventWithName:@"NfcReadingStatus" body:@{@"status": @"TAG_READING_SUCCESS"}];
        
    }
    else if([result isEqualToString:@"ERROR"])
    {
        [self sendEventWithName:@"NfcReadingStatus" body:@{@"status": @"TAG_READING_FAILED"}];
        
    }
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(startNFCScan:(NSString *)num dobArg:(NSString *)dob expArg:(NSString *)exp checkArg:(NSString *)check callback:(RCTResponseSenderBlock)callback)
{
    //callback(@[@"SUCCESS"]);
    res_callback = callback;
    
    if (@available(iOS 13.0, *)) {
        myOb = [[TAGProcessing alloc] init];
        //NSInteger result = [myOb addX:5 andY:5];
        [myOb start:check delegate:self];
        
        //myTimer = [NSTimer scheduledTimerWithTimeInterval: 1.0 target: self selector: @selector(checkScan:) userInfo: nil repeats: YES];
    } else {
        // Fallback on earlier versions
    }
    //TAGProcessing *pp = [[TAGProcessing alloc] init:@""];
}

-(void)checkScan:(NSTimer*) t
{
    NSLog(@"dfgdgfg");
    /*NSString *res = [myOb checkForResult];
    
    NSLog(res);
    
    if([res isEqualToString:@"SUCCESS"])
    {
        res_callback(@[@"SUCCESS"]);
    }
    else if([res isEqualToString:@"ERROR"])
    {
        res_callback(@[@"ERROR"]);
    }*/
}

RCT_EXPORT_METHOD(getNFCStatus:(RCTResponseSenderBlock)callback)
{
    if (@available(iOS 11.0, *)) {
        if([NFCNDEFReaderSession readingAvailable])
            callback(@[@"NFCStatus.READY"]);
        else
            callback(@[@"NFCStatus.NOT_EXIST"]);
    } else {
        callback(@[@"NFCStatus.NOT_EXIST"]);
    }
}


- (NSArray<NSString *> *)supportedEvents
{
    return @[
             @"NfcReadingStatus"
             ];
}

@end
