@interface Connection : NSObject <NSStreamDelegate> {
    @private
    
    NSString *host;
    int port;

    NSInputStream *reader;
    NSOutputStream *writer;
}

- (id)initWithNetworkAddress :(NSString*)targetHost :(int)targetPort;
- (void)open;
- (void)close;
- (void)write :(NSString*)data;
- (void)stream:(NSStream *)theStream handleEvent:(NSStreamEvent)streamEvent;
@end