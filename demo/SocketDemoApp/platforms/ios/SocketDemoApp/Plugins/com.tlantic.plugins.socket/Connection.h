@interface Connection : NSObject <NSStreamDelegate> {
    @private
    
    NSString *host;
    int port;

    CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    
    NSInputStream* inputStream;
    NSOutputStream* outputStream;
}

- (id)initWithNetworkAddress :(NSString*)targetHost :(int)targetPort;
- (void)open;
- (void)close;
- (void)write :(NSString*)data;
@end