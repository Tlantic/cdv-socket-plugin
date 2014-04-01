#import "Connection.h"

@implementation Connection: NSObject

- (id)initWithNetworkAddress:(NSString *)targetHost :(int)targetPort {
    self = [super init];
    if (self)
    {
        self->host = targetHost;
        self->port = targetPort;
    }
    return self;
}

- (void)open {
    
    // init network communication settings
    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    
    // opening connection
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)self->host, self->port, &readStream, &writeStream);
    
    // configuring input stream
    reader = objc_unretainedObject(readStream);
    [reader setDelegate:self];
    [reader scheduleInRunLoop:[NSRunLoop mainRunLoop] forMode:NSDefaultRunLoopMode];
    [reader open];
    
    // configuring output stream
    writer = objc_unretainedObject(writeStream);
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
                        
                        NSString *output = [[NSString alloc] initWithBytes:buffer length:len encoding:NSASCIIStringEncoding];
                        
                        if (nil != output) {
                            NSLog(@"Received data: %@", output);
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