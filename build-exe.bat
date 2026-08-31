@echo off
title Generador de Ejecutable EXE - NutriEnergia Pro

echo ======================================================================
echo   Generador de Ejecutable e Instalador EXE - NutriEnergia Pro
echo   Servicio Social - Facultad de Quimica
echo ======================================================================
echo.

:: 1. Detectar JDK
set "JDK_BIN="
if exist "C:\Program Files\Java\jdk-23\bin\jpackage.exe" (
    set "JDK_BIN=C:\Program Files\Java\jdk-23\bin"
)
if not defined JDK_BIN (
    if exist "C:\Program Files\Java\jdk-21\bin\jpackage.exe" (
        set "JDK_BIN=C:\Program Files\Java\jdk-21\bin"
    )
)
if not defined JDK_BIN (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\jpackage.exe" set "JDK_BIN=%JAVA_HOME%\bin"
    )
)

if not defined JDK_BIN (
    echo [ERROR] No se encontro jpackage en el sistema.
    echo Asegurate de tener instalado Java JDK 21 o superior.
    pause
    exit /b 1
)

echo [1/6] JDK detectado en: %JDK_BIN%

:: 2. Limpiar temporales previos
if exist "build\temp-jre" rmdir /s /q "build\temp-jre"
if exist "build\package-input" rmdir /s /q "build\package-input"
if exist "dist\NutriEnergiaPro" rmdir /s /q "dist\NutriEnergiaPro"

if not exist "build\package-input\lib" mkdir "build\package-input\lib"

:: 3. Generar JRE reducido con jlink
echo [2/6] Generando entorno de ejecucion Java reducido...
"%JDK_BIN%\jlink.exe" --add-modules java.base,java.desktop,java.logging,java.sql,java.xml,jdk.unsupported,java.naming,java.management,java.net.http --output "build\temp-jre" --strip-debug --no-header-files --no-man-pages

if %errorlevel% neq 0 (
    echo [ERROR] Fallo la creacion del entorno con jlink.
    pause
    exit /b 1
)

:: 4. Copiar librerias nativas de JavaFX al JRE
echo [3/6] Integrando librerias nativas de renderizado JavaFX...
copy /y "libs\natives\*.dll" "build\temp-jre\bin\" >nul

:: 5. Preparar JARs de la aplicacion
echo [4/6] Preparando archivos y dependencias...
copy /y "dist\CalculadoraEnergeticaModernaa.jar" "build\package-input\" >nul
copy /y "libs\*.jar" "build\package-input\lib\" >nul

:: 6. Ejecutar jpackage
echo [5/6] Construyendo aplicacion standalone con jpackage...
"%JDK_BIN%\jpackage.exe" --type app-image --name "NutriEnergiaPro" --dest "dist" --input "build\package-input" --main-jar "CalculadoraEnergeticaModernaa.jar" --main-class "calculadoraenergeticamodernaa.MainLauncher" --runtime-image "build\temp-jre" --icon "food-and-drink.ico" --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED"

if %errorlevel% neq 0 (
    echo [ERROR] Fallo la creacion de la aplicacion con jpackage.
    pause
    exit /b 1
)

:: 7. Limpieza de temporales
rmdir /s /q "build\temp-jre" 2>nul
rmdir /s /q "build\package-input" 2>nul

:: 8. Compilar instalador unico .exe con Inno Setup si esta disponible
set "ISCC_PATH="
if exist "%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe" (
    set "ISCC_PATH=%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe"
) else if exist "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" (
    set "ISCC_PATH=C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
) else if exist "C:\Program Files\Inno Setup 6\ISCC.exe" (
    set "ISCC_PATH=C:\Program Files\Inno Setup 6\ISCC.exe"
)

if defined ISCC_PATH (
    echo [6/6] Creando archivo unico instalador NutriEnergiaPro_Instalador.exe...
    "%ISCC_PATH%" "installer.iss" >nul
    if exist "dist\NutriEnergiaPro_Instalador.exe" (
        copy /y "dist\NutriEnergiaPro_Instalador.exe" "%USERPROFILE%\Desktop\NutriEnergiaPro_Instalador.exe" >nul
    )
)

echo.
echo ======================================================================
echo   [EXITO] Archivo unico .exe generado en tu Escritorio:
echo   %USERPROFILE%\Desktop\NutriEnergiaPro_Instalador.exe
echo ======================================================================
echo.
echo Este archivo unico (.exe) lo puedes enviar por WhatsApp a cualquier
echo persona. Al descargarlo y abrirlo, se instala y ejecuta de inmediato.
echo No necesitan tener Java instalado.
echo.
pause
