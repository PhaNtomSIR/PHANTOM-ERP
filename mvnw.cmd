@echo off
set "JAVA_HOME=%~dp0tools\jdk\jdk-21.0.12.1+1"
set "M2_HOME=%~dp0tools\maven\apache-maven-3.9.9"
set "PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%"

"%M2_HOME%\bin\mvn.cmd" %*
