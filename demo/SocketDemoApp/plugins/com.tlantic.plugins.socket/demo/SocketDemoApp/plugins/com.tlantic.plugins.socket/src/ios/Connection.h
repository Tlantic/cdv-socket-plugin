
@protocol ConnectionDelegate <NSObject>

-(void) sendMessage :(NSString *)host :(int)port :(NSString *)chunk;

@end



@interface Connection : NSObject <NSStreamDelegate> {
@private
    
    NSString *host;
    int port;
    
    NSInputStream *reader;
    NSOutputStream *writer;
}


@property (nonatomic, weak) id<ConnectionDelegate> hook;


- (id)initWithNetworkAddress :(NSString*)targetHost :(int)targetPort;
- (void)setDelegate:(id<ConnectionDelegate>)callbackRef;
- (void)open;
- (void)close;
- (void)write :(NSString*)data;

- (void)stream:(NSStream *)theStream handleEvent:(NSStreamEvent)streamEvent;
@end