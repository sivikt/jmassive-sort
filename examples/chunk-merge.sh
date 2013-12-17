#!/bin/sh

if [ -z "$1" ]; then
   echo "Specify target jar file"
   exit 1
fi

if [ -z "$2" ]; then
   echo "Specify total chunks number"
   exit 1
fi

if [ -z "$3" ]; then
   echo "Specify output file"
   exit 1
fi

java -jar $1 chunk-merging $2 $3
case "$?" in
   0)
      echo SUCCESS
      ;;
   1)
      echo FAILED
      exit 1
      ;;
esac
