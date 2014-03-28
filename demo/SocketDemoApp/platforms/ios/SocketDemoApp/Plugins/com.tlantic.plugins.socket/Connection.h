@interface Connection : NSObject {
    NSString *host;
    NSInteger *port;
    CFReadStreamRef *reader;
    CFWriteStreamRef *writer;
}

- (id)init :(NSString*)targetHost :(NSInteger*)targetPort;
@end