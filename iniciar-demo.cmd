@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0"
title Talento RH - Demo

echo ======================================================
echo              TALENTO RH - INICIO DEMO
echo ======================================================
echo.

where java >nul 2>&1
if errorlevel 1 (
  echo [ERROR] No se encontro Java en el PATH.
  echo.
  echo Instala JDK 21 y vuelve a intentarlo.
  echo Luego verifica con: java -version
  echo.
  pause
  exit /b 1
)

echo [1/3] Java detectado:
java -version 2>&1
echo.

for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":8080 .*LISTENING" 2^>nul') do set "PORT_PID=%%P"
if defined PORT_PID (
  echo [ERROR] El puerto 8080 ya esta siendo usado por el proceso PID %PORT_PID%.
  echo Cierra la otra aplicacion o ejecuta: taskkill /PID %PORT_PID% /F
  echo.
  pause
  exit /b 1
)

echo [2/3] Iniciando Spring Boot con perfil DEV ^(H2^)...
echo URL: http://localhost:8080
echo.
echo Cuando aparezca "Started ReclutamientoApplication", abre el navegador.
echo Usa Ctrl+C para detener la aplicacion.
echo.

set "APP_JAR=%~dp0target\reclutamiento-1.0.0.jar"
if exist "%APP_JAR%" (
  echo [3/3] Usando JAR precompilado incluido. No necesita descargar Maven.
  java -jar "%APP_JAR%" --spring.profiles.active=dev
) else (
  echo [3/3] No se encontro el JAR precompilado. Usando Maven Wrapper...
  call "%~dp0mvnw.cmd" spring-boot:run "-Dspring-boot.run.profiles=dev"
)
set "EXIT_CODE=%ERRORLEVEL%"

if not "%EXIT_CODE%"=="0" (
  echo.
  echo ======================================================
  echo [ERROR] Talento RH no pudo iniciar. Codigo: %EXIT_CODE%
  echo ======================================================
  echo Revisa arriba el primer mensaje que diga ERROR o FAILURE.
  echo.
  echo Comprobaciones utiles:
  echo   java -version
  echo   java -jar target\reclutamiento-1.0.0.jar --spring.profiles.active=dev
  echo.
  echo Si menciona Java 21, instala/configura JDK 21.
  echo Si menciona puerto 8080, cierra la aplicacion que lo usa.
  echo Si menciona que la base H2 esta en uso, cierra otra instancia de Talento RH.
  echo.
  pause
)

exit /b %EXIT_CODE%
