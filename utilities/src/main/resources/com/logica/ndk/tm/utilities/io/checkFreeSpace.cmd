@echo off
set dir2Check=%1 
for /f "tokens=3" %%a in ('dir %dir2Check%') do (
    set bytesfree=%%a
)
echo %bytesfree%
