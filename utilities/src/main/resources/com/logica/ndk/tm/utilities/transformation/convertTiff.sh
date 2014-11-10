#!/bin/bash
TIFF_FILE=$1
TARGET_DIR=$2
 
if [ ! -f "$TIFF_FILE" ] ; then
  echo Missing input file $TIFF_FILE >&2
  exit 4
fi
 
if [ ! -d "$TARGET_DIR" ] ; then
  echo Missing target dir $TARGET_DIR >&2
  exit 5
fi
 
TMP_FILE=$(/bin/mktemp /tmp/convertTiff-XXXXXXXXX.tif)
TIFF_BASE=$(/bin/basename $TIFF_FILE)
 
/bin/tiffcp -r 16 -c jpeg:70 $TIFF_FILE $TMP_FILE
if [ $? -ne 0 ] ; then
  echo Failed to convert $TIFF_FILE to $TMP_FILE >&2
  exit 1
fi
 
/bin/cp $TMP_FILE $TARGET_DIR/$TIFF_BASE
if [ $? -ne 0 ] ; then
  echo Failed to copy $TMP_FILE to $TARGET_DIR/$TIFF_BASE >&2
  exit 2
fi
 
/bin/rm -f $TMP_FILE
if [ $? -ne 0 ] ; then
  echo Failed to remove $TMP_FILE >&2
  exit 3
fi
