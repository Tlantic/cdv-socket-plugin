#import <Cordova/CDV.h>

@interface CDVSocketPlugin : CDVPlugin {
    NSMutableDictionary *pool;
}

-(void) connect: (CDVInvokedUrlCommand *) command;
-(void) disconnect: (CDVInvokedUrlCommand *) command;

@end