/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

#include "org_jitsi_impl_neomedia_quicktime_QTCaptureDeviceInput.h"

#import <Foundation/NSException.h>
#import <QTKit/QTCaptureDevice.h>
#import <QTKit/QTCaptureDeviceInput.h>
#include <stdint.h>

JNIEXPORT jlong JNICALL
Java_org_jitsi_impl_neomedia_quicktime_QTCaptureDeviceInput_deviceInputWithDevice
    (JNIEnv *jniEnv, jclass clazz, jlong devicePtr)
{
    QTCaptureDevice *device;
    NSAutoreleasePool *autoreleasePool;
    id deviceInput;

    device = (QTCaptureDevice *) (intptr_t) devicePtr;
    autoreleasePool = [[NSAutoreleasePool alloc] init];

    @try
    {
        deviceInput = [QTCaptureDeviceInput deviceInputWithDevice:device];
    }
    @catch (NSException *ex)
    {
        deviceInput = nil;
    }
    if (deviceInput)
        [deviceInput retain];

    [autoreleasePool release];
    return (jlong) (intptr_t) deviceInput;
}
