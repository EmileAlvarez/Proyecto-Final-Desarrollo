@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0"
title Talento RH - Diagnostico

echo ===== DIAGNOSTICO TALENTO RH =====
echo.
echo Carpeta actual:
cd
echo.
echo --- JAVA ---
where java 2>&1
java -version 2>&1
echo.
echo --- JAR PRECOMPILADO ---
if exist target\reclutamiento-1.0.0.jar (
  echo [OK] target\reclutamiento-1.0.0.jar
) else (
  echo [FALTA] target\reclutamiento-1.0.0.jar
)
echo.
echo --- MAVEN WRAPPER ^(solo respaldo^) ---
if exist mvnw.cmd (echo [OK] mvnw.cmd) else (echo [FALTA] mvnw.cmd)
echo.
echo --- PUERTO 8080 ---
netstat -ano | findstr ":8080"
echo.
echo --- ARCHIVOS CLAVE ---
if exist pom.xml (echo [OK] pom.xml) else (echo [FALTA] pom.xml)
if exist src\main\resources\application-dev.properties (echo [OK] application-dev.properties) else (echo [FALTA] application-dev.properties)
echo.
pause
