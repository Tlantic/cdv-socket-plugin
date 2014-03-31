#import "CDVSocketPlugin.h"
#import "Connection.h"
#import <cordova/CDV.h>

@implementation CDVSocketPlugin



- (id)init {
    self = [super init];
    
    if (self) {
        self->pool = [[NSMutableDictionary alloc] init];
    }
    return self;
}



- (NSString*)buildKey :(NSString*)host :(int)port {
    NSString* tempHost = [host lowercaseString];
    NSString* tempPort = [NSString stringWithFormat:@"%d", port];
    
    return  [[tempHost stringByAppendingString:@":"] stringByAppendingString:tempPort];
}



- (void)connect:(CDVInvokedUrlCommand*)command
{
    
    // validating parameters
    if ([command.arguments count] < 2) {
        
        // triggering parameter error
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing arguments when calling 'connect' action."];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        
    } else {
        
        // running in background to avoid thread locks
        [self.commandDelegate runInBackground:^{
            
            CDVPluginResult* result= nil;
            Connection* socket = nil;
            NSString* key = nil;
            NSString* host = nil;
            int port = 0;
            
            // opening connection and adding into pool
            @try {
                
                // preparing parameters
                host = [command.arguments objectAtIndex:0];
                port = [[command.arguments objectAtIndex:1] integerValue];
                key = [self buildKey:host :port];
                
                // creating connection
                socket = [[Connection alloc] initWithNetworkAddress:host :port];
                [socket open];
                
                // adding to pool
                [pool setObject:socket forKey:key];
                
                //formatting success response
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:key];
                NSLog(@"Established connection with %@", key);
                
            }
            @catch (NSException *exception) {
                NSLog(@"Exception: %@", exception);
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unexpected exception when executon 'connect' action."];
            }
            
            //returning callback resolution
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            
        }];
        
    }
    
}

- (void)disconnect:(CDVInvokedUrlCommand*)command
{
    // validating parameters
    if ([command.arguments count] < 2) {
        // triggering parameter error
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing arguments when calling 'disconnect' action."];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        
    } else {
        
        // running in background to avoid thread locks
        [self.commandDelegate runInBackground:^{
            
            CDVPluginResult* result= nil;
            Connection* socket = nil;
            NSString* key = nil;
            NSString* host = nil;
            int port = 0;
            
            @try {
                // preparing parameters
                host = [command.arguments objectAtIndex:0];
                port = [[command.arguments objectAtIndex:1] integerValue];
                key = [self buildKey:host :port];
                
                // getting connection from pool
                socket = [pool objectForKey:key];
                
                // closing connection
                if (socket) {
                    [socket close];
                }
                
                //formatting success response
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:key];
                NSLog(@"Closing connection with %@", key);
            }
            @catch (NSException *exception) {
                NSLog(@"Exception: %@", exception);
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unexpected exception when executon 'disconnect' action."];
            }
            
            //returning callback resolution
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
        
    }
    
}


-(void)send: (CDVInvokedUrlCommand *) command {
    
    // validating parameters
    if ([command.arguments count] < 3) {
        // triggering parameter error
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Missing arguments when calling 'send' action."];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        
    } else {
        
        // running in background to avoid thread locks
        [self.commandDelegate runInBackground:^{
            
            CDVPluginResult* result= nil;
            Connection* socket = nil;
            NSString* data = nil;
            NSString* key = nil;
            NSString* host = nil;
            int port = 0;
            
            @try {
                // preparing parameters
                host = [command.arguments objectAtIndex:0];
                port = [[command.arguments objectAtIndex:1] integerValue];
                key = [self buildKey:host :port];
                
                // getting connection from pool
                socket = [pool objectForKey:key];
                
                // writting on output stream
                data = [command.arguments objectAtIndex:2];
                [socket write:data];
                
                //formatting success response
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:key];
                NSLog(@"Sending data to %@ - %@", key, data);
            }
            @catch (NSException *exception) {
                NSLog(@"Exception: %@", exception);
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unexpected exception when executon 'send' action."];
            }
            
            //returning callback resolution
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }];
        
        
    }
}
@end