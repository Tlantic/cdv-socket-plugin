#import "Connection.h"

@implementation Connection: NSObject

- (id)init :(NSString*)targetHost :(NSInteger*)targetPort{
    self = [super init];
    if (self)
    {
        self->host = targetHost;
        self->port = targetPort;
    }
    return self;
}

@end