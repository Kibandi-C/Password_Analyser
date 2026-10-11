@echo off
setlocal

if not exist "%~dp0dist\password-analyzer.jar" goto missing_files
if not exist "%~dp0dist\password-analyzer-gui.jar" goto missing_files

where javaw.exe >nul 2>&1
if errorlevel 1 goto missing_java

start "" javaw.exe -cp "%~dp0dist\password-analyzer.jar;%~dp0dist\password-analyzer-gui.jar" com.sit300.password.app.Main
exit /b

:missing_files
echo The application JAR files are missing from the dist folder.
echo Follow GUI-GUIDE.md to build them, then try again.
pause
exit /b 1

:missing_java
echo Java was not found. Add your Java 17 or later JDK bin folder to PATH.
echo Then close this window and try again.
pause
exit /b 1
