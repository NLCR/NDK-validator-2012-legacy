INDEXER_HOME=$(dirname $0)/..
echo $INDEXER_HOME

CP=$INDEXER_HOME/config:$INDEXER_HOME/lib/*:validation-1.5.1-SNAPSHOT.jar
echo $CP
java -cp $CP com.logica.ndk.tm.validation.DPValidator $1 $2