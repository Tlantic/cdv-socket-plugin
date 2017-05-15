#import "Connection.h"

@implementation Connection: NSObject

- (id) initWithNetworkAddress : (NSString *) targetHost : (int) targetPort {
    self = [super init];
    if (self) {
        _host = targetHost;
        _port = targetPort;
        connected = NO;
        chunk = @"";
    }
    return self;
}

- (void) setDelegate : (id<ConnectionDelegate>) callbackRef {
    _hook = callbackRef;
}

- (BOOL) isConnected {
    return connected;
 }

- (void) open {
    // Init network communication settings
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    
    // Opening connection
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)_host, _port, &readStream, &writeStream);
    
    // Configuring input stream
    reader = CFBridgingRelease(readStream);
    [reader setDelegate:self];
    [reader scheduleInRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
    [reader open];
    
    // Configuring output stream
    writer = CFBridgingRelease(writeStream);
    [writer setDelegate:self];
    [writer scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [writer open];
}

- (void) close {
    // Closing output stream
    [writer close];
    [writer removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [writer setDelegate:nil];
    writer = nil;
    
    // Closing input stream
    [reader close];
    [reader removeFromRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
    [reader setDelegate:nil];
    reader = nil;
}

- (void) write : (NSString *) data {
    NSData *pChunk = [[NSData alloc] initWithData : [data dataUsingEncoding : NSASCIIStringEncoding]];
    [writer write : [pChunk bytes] maxLength : [pChunk length]];
}

- (void) stream : (NSStream *) theStream handleEvent : (NSStreamEvent) streamEvent {
    switch (streamEvent) {
        case NSStreamEventOpenCompleted:
            NSLog(@"Stream opened!");
            connected = YES;
            break;
            
        // Data receiving
        case NSStreamEventHasBytesAvailable:
            
            if (theStream == reader) {
                
                uint8_t buffer[1024];
                unsigned int len = 0;
                NSString* temp = nil;
 
                //
                while ([reader hasBytesAvailable]) {
                    len = [reader read:buffer   maxLength:sizeof(buffer)];
                    
                    // validating data read
                    if (len > 0) {
                        
                        temp = [[NSString alloc] initWithBytes:buffer   length:len  encoding:NSASCIIStringEncoding];
                        
                        // checking piece of data
                        if (nil != temp) {
                            NSLog(@"\n\nReceived buffer: %@", chunk);
                            chunk = [chunk stringByAppendingString:temp];
                        }
                    }
                }
                
                //
                if ([temp characterAtIndex:[temp length]-1] == '\n') {
                    NSLog(@"\n\nReceived data: %@", chunk);
                    [_hook sendMessage : _host : _port : chunk];
                    chunk = @"";
                }
                
            }
            break;
            
        case NSStreamEventErrorOccurred:
            NSLog(@"Connection has been interrupted!");
            connected = NO;
            break;
            
        case NSStreamEventEndEncountered:
            NSLog(@"Stream closed!");
            connected = NO;
            break;
            
        default:
            NSLog(@"Unknown event!");
    }
}

@end