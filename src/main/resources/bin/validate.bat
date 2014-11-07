@echo OFF

SET INDEXER_HOME=%~dp0../

set CP=%INDEXER_HOME%/config;%INDEXER_HOME%/lib/*;%INDEXER_HOME%/validation-1.0.19-SNAPSHOT.jar

java -cp %CP% com.logica.ndk.tm.validation.DPValidator %1 %2