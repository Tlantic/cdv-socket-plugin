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

- (Boolean)open {
    
    // opening connection
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)self->host, self->port, &reader, &writer);
    [NSThread sleepForTimeInterval:2]; //Delay
    
    // configuring to close native socket
    CFReadStreamSetProperty(reader, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    CFWriteStreamSetProperty(writer, kCFStreamPropertyShouldCloseNativeSocket, kCFBooleanTrue);
    
    // returning opening status
    return CFWriteStreamOpen(writer);
}

- (void)close {
    
    // closing-releasing CF Read Streams
    CFReadStreamClose(self->reader);
    CFRelease(self->reader);
    
    // closing-releasing CF Write Stream
    CFWriteStreamClose(self->writer);
    CFRelease(self->writer);
}


- (void)write :(NSString *)data {
    int bytes = CFWriteStreamWrite(writer, (const UInt8 *)[data UTF8String], [data lengthOfBytesUsingEncoding:NSUTF8StringEncoding]);
    NSLog(@"Bytes written on output stream: %d", bytes);
}


@end