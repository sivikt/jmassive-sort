#!/bin/sh

if [ -z "$1" ]; then
   echo "Specify target jar file"
   exit 1
fi

java -jar $1 chunk-merging
case "$?" in
   0)
      echo SUCCESS
      ;;
   1)
      echo FAILED
      exit 1
      ;;
esac
