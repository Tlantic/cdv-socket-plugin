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
    CFStreamCreatePairWithSocketToHost(NULL, (__bridge CFStringRef)self->host, self->port, &readStream, &writeStream);
    
    self->inputStream = (__bridge_transfer NSInputStream *)readStream;
    [self->inputStream setDelegate:self];
    [self->inputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [self->inputStream open];
    
    self->outputStream = (__bridge NSOutputStream *)writeStream;
    [self->outputStream setDelegate:self];
    [self->outputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [self->outputStream open];
}

- (void)close {
    
    
    // closing-releasing input stream
    [self->inputStream close];
    [self->inputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [self->inputStream setDelegate:nil];
    self->inputStream = nil;
    
    // closing-releasing output  stream
    [self->outputStream close];
    [self->outputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
    [self->outputStream setDelegate:nil];
    self->outputStream = nil;
    
    
    // closing-releasing CF Read Streams
    CFReadStreamClose(self->readStream);
    if (readStream) CFRelease(self->readStream);
    
    // closing-releasing CF Write Stream
    CFWriteStreamClose(self->writeStream);
    if (writeStream) CFRelease(self->writeStream);
}


- (void)write :(NSString *)data {
	NSData *chunk = [[NSData alloc] initWithData:[data dataUsingEncoding:NSASCIIStringEncoding]];
	[self->outputStream write:[chunk bytes] maxLength:[chunk length]];
}


@end