rem c:\cygwin\bin\mogrify -debug All +compress c:/ndk/tst/*.tif
rem funguje c:\cygwin\bin\mogrify -debug All +compress -path c:/ndk/tst //Hdigfscl02/CDT-01/CDM-002/*.tif 
rem funguje c:\cygwin\bin\mogrify -debug All +compress -path //Hastest/c$/NDK/tst //Hdigfscl02/CDT-01/CDM-002/*.tif
rem taky funguje c:\cygwin\bin\mogrify -debug All +compress -path //Hdigfscl02/CDT-01/CDM-003 //Hdigfscl02/CDT-01/CDM-002/*.jpg
rem funguje c:\cygwin\bin\convert -debug All //Hdigfscl02/CDT-01/CDM-002/1000350721_00_0006.tif +compress //Hdigfscl02/CDT-01/CDM-003/pokus.bmp
rem c:\cygwin\bin\convert -debug All //Hdigfscl02/CDT-01/CDM-002/1000350721_00_0006.tif -compress ip //Hdigfscl02/CDT-01/CDM-003/pokus.tif
set UTILS_HOME=c:\NDK\utils_temp
mkdir %UTILS_HOME%\%1
robocopy \\Hdigfscl02\CDT-01\CDM-001\%1\data\postprocessingData %UTILS_HOME%\%1
c:\cygwin\bin\mogrify -debug All +compress %UTILS_HOME%/%1/*.tif
robocopy %UTILS_HOME%\%1 \\Hdigfscl02\CDT-01\CDM-001\%1\data\postprocessingData
rmdir %UTILS_HOME%\%1 /Q /S