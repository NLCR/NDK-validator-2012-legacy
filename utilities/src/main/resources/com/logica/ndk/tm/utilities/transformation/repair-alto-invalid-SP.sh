#!/bin/sh
DIR=$(/bin/dirname $0)
IN=$1

#when in CDM, create backup of ALTO files
if [ -d $IN/../.workspace ] ; then
  BACKUP_DIR=$IN/../.workspace/backup-alto-fix
  if [ ! -d $BACKUP_DIR ] ; then /bin/mkdir $BACKUP_DIR ; fi
  if [ ! -d $BACKUP_DIR ] ; then 
    BACKUP_DIR=
  fi  
fi

/bin/find $IN -name "*.xml" |\
while read INFILE; do
  OUTFILE=$IN/$(/bin/basename $INFILE).tmp;  
  if [ ! -z $BACKUP_DIR ] ; then /bin/cp $INFILE $BACKUP_DIR; fi # create backup if backupdir exist
  /bin/xsltproc $DIR/remove-invalid-SP.xsl $INFILE > $OUTFILE && /bin/mv -f $OUTFILE $INFILE
  #if [ -f $OUTFILE ] ; then /bin/mv $OUTFILE /tmp/$(basename $OUTFILE); fi
done