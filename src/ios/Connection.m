#import "Connection.h"


@implementation Connection: NSObject

- (id)initWithNetworkAddress:(NSString *)targetHost :(int)targetPort {
    self = [super init];
    if (self)
    {
        _host = targetHost;
        _port = targetPort;
    }
    return self;
}

- (void)setDelegate:(id<ConnectionDelegate>)callbackRef {
    _hook = callbackRef;
}

- (void)open {
    
    // init network communication settings
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    
    // opening connection
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)_host, _port, &readStream, &writeStream);
    
    // configuring input stream
    reader = objc_retainedObject(readStream);
    [reader setDelegate:self];
    [reader scheduleInRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
    [reader open];
    
    // configuring output stream
    writer = objc_retainedObject(writeStream);
    [writer setDelegate:self];
    [writer scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [writer open];
}

- (void)close {
    
    // closing output stream
    [writer close];
    [writer removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [writer setDelegate:nil];
    writer = nil;
    
    // closing input stream
    [reader close];
    [reader removeFromRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
    [reader setDelegate:nil];
    reader = nil;
}


- (void)write :(NSString *)data {
    NSData *chunk = [[NSData alloc] initWithData:[data dataUsingEncoding:NSASCIIStringEncoding]];
    [writer write:[chunk bytes] maxLength:[chunk length]];
}


- (void)stream:(NSStream *)theStream handleEvent:(NSStreamEvent)streamEvent {
	switch (streamEvent) {
            
		case NSStreamEventOpenCompleted:
			NSLog(@"Stream opened!");
			break;
            
            // DATA RECEIVING
		case NSStreamEventHasBytesAvailable:
            
            if (theStream == reader) {
                
                uint8_t buffer[1024];
                int len;
                
                while ([reader hasBytesAvailable]) {
                    len = [reader read:buffer maxLength:sizeof(buffer)];
                    
                    if (len > 0) {
                        
                        NSString *chunk = [[NSString alloc] initWithBytes:buffer length:len encoding:NSASCIIStringEncoding];
                        
                        if (nil != chunk) {
                            NSLog(@"Received data: %@", chunk);
                            [_hook sendMessage:_host :_port :chunk];
                        }
                    }
                }
            }
            break;
            
        case NSStreamEventErrorOccurred:
            NSLog(@"Can not connect to the host!");
            break;
            
        case NSStreamEventEndEncountered:
            NSLog(@"Stream closed!");
            break;
            
        default:
            NSLog(@"Unknown event!");
    }
}

@end