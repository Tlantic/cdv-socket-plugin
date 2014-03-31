@interface Connection : NSObject <NSStreamDelegate> {
    @private
    
    NSString *host;
    int port;

    CFReadStreamRef reader;
    CFWriteStreamRef writer;
}

- (id)initWithNetworkAddress :(NSString*)targetHost :(int)targetPort;
- (Boolean)open;
- (void)close;
- (void)write :(NSString*)data;
@end