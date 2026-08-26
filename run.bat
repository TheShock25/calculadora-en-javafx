@echo off
setlocal
title NutriEnergia Pro - Calculadora Energetica Moderna

echo ======================================================================
echo   NutriEnergia Pro - Calculadora Energetica Moderna (JavaFX)
echo   Servicio Social - Facultad de Quimica
echo ======================================================================
echo.

:: Verificar instalacion de Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] No se encontro Java instalado o no esta configurado en el PATH.
    echo Por favor instala Java JDK 21 o superior (https://adoptium.net/ o https://oracle.com/java/).
    pause
    exit /b 1
)

echo [INFO] Iniciando aplicacion JavaFX...
echo.

if exist "dist\CalculadoraEnergeticaModernaa.jar" (
    java --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "dist\CalculadoraEnergeticaModernaa.jar;libs\*" calculadoraenergeticamodernaa.CalculadoraEnergeticaModernaa
) else (
    if not exist "build\classes" mkdir "build\classes"
    echo [INFO] Compilando fuentes...
    javac --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "libs\*" -d "build\classes" src\calculadoraenergeticamodernaa\*.java
    java --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "build\classes;resources;libs\*" calculadoraenergeticamodernaa.CalculadoraEnergeticaModernaa
)

if %errorlevel% neq 0 (
    echo.
    echo [AVISO] La aplicacion finalizo con codigo de salida %errorlevel%.
    pause
)
