#!/bin/bash
adb shell am startservice -n net.callofdroidy.clipboardaccessor/.ServiceClipboardAccessor
if [ $1 = "get" ]; then
    adb shell am broadcast -a clipper.get
elif [ $1 = "set" ]; then
    adb shell am broadcast -a clipper.set -e text $2
else
    echo "invalid action"
fi
    
exit 0
