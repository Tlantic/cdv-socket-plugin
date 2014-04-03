#import <Cordova/CDV.h>
#import "Connection.h"

@interface CDVSocketPlugin : CDVPlugin <ConnectionDelegate> {
    NSMutableDictionary *pool;
}

-(void) connect: (CDVInvokedUrlCommand *) command;
-(void) disconnect: (CDVInvokedUrlCommand *) command;
-(void) disconnectAll: (CDVInvokedUrlCommand *) command;
-(void) send: (CDVInvokedUrlCommand *) command;

-(BOOL) disposeConnection :(NSString *)host :(int)port;

@end