@echo off
echo ========================================================
echo Starting Enterprise Resource Planning (ERP) System...
echo Access Web UI at: http://localhost:8080
echo H2 Central Database Console: http://localhost:8080/h2-console
echo ========================================================
call "%~dp0mvnw.cmd" spring-boot:run
pause
