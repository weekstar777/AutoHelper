@rem Gradle startup script for Windows
@rem See https://gradle.org for more information

@if "%DEBUG%"=="" @echo off
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_BASE_NAME=%~n0
set CLASSPATH=%DIRNAME%\gradle\wrapper\gradle-wrapper.jar

java -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
