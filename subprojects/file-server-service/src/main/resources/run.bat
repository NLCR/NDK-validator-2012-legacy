@echo OFF

SET SERVICE_HOME=%~dp0

set CP=%SERVICE_HOME%./file-server-service-1.0.28-SNAPSHOT.jar

"c:\Program Files (x86)\Java\jre7\bin\java.exe" -jar %CP% com.logica.ndk.tm.fileServer.service.Main