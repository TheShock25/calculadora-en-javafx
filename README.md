# Calculadora Energetica Moderna y Sistema Antropometrico (NutriEnergia Pro)

[![Java](https://img.shields.io/badge/Java-23%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-FF6F00?style=flat-square&logo=java&logoColor=white)](https://openjfx.io/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-5.2.5-D22128?style=flat-square&logo=apache&logoColor=white)](https://poi.apache.org/)
[![NetBeans](https://img.shields.io/badge/NetBeans-IDE-1B6AC6?style=flat-square&logo=apache-netbeans-ide&logoColor=white)](https://netbeans.apache.org/)

Aplicacion de escritorio para Windows desarrollada en Java y JavaFX para el calculo del gasto energetico, evaluacion antropometrica (somatotipo y composicion corporal), planificacion dietetica basada en el Sistema Mexicano de Alimentos Equivalentes (SMAE 5a Edicion) y registro de recordatorio de 24 horas.

Todas las formulas, procesos clinicos, sincronizacion de datos y reglas de calculo fueron implementados y homologados estrictamente conforme a la ultima version de escritorio en Swing para Windows, preservando la logica cientifica de escritorio.

---

## Tabla de Contenidos
1. [Descripcion y Contexto](#descripcion-y-contexto)
2. [Modulos del Sistema](#modulos-del-sistema)
3. [Estructura del Proyecto](#estructura-del-proyecto)
4. [Tecnologias y Librerias](#tecnologias-y-librerias)
5. [Como Crear o Actualizar el Instalador (.exe)](#como-crear-o-actualizar-el-instalador-exe)
6. [Instrucciones de Ejecucion y Desarrollo](#instrucciones-de-ejecucion-y-desarrollo)
7. [Creditos](#creditos)

---

## Descripcion y Contexto

Este software fue desarrollado como parte del proyecto de Servicio Social para alumnos y docentes de la Facultad de Quimica. Su proposito es brindar una herramienta de calculo nutricional que agilice los procedimientos de diagnostico y prescripcion dietetica en un entorno de escritorio estructurado.

El sistema integra ecuaciones predictivas de energia, el catalogo de alimentos del SMAE (5a Edicion), preparaciones tradicionales mexicanas y formulas antropometricas estandarizadas.

---

## Modulos del Sistema

### 1. Calculadora de Gasto Energetico (`CalculadoraEnergeticaModernaa.java`)
- **Ecuaciones implementadas**:
  - Harris-Benedict (masculino y femenino).
  - Mifflin-St Jeor.
  - Valencia (estratificada por grupos de edad: 18-30, 30-59 y >=60 anos).
- **Calculo de componentes**:
  - Gasto Energetico Basal (GEB).
  - Efecto Termogenico de los Alimentos (ETA, 10%).
  - Gasto Energetico Total (GET) con factores de actividad fisica diferenciados por sexo.
- Graficacion del desglose energetico en Canvas interactivo.

### 2. Sistema de Equivalentes SMAE (`SistemaEquivalentes.java`)
- Distribucion teorica de macronutrientes (% y gramos de HC, Lipidos y Proteinas).
- Semaforo de adecuacion nutricional y comparacion respecto a las calorias objetivo.
- Manejo de grupos de alimentos: Verduras, Frutas, Cereales y tuberculos (con y sin grasa), Leguminosas, Alimentos de origen animal (clasificados por aporte de grasa), Leche (descremada, semi, entera, con azucar), Aceites y grasas (con y sin proteina), y Azucares (con y sin grasa).

### 3. Plan Alimenticio Personalizado (`PlanAlimenticio.java`)
- Prescripcion de alimentos por tiempos de comida (Desayuno, Comida, Cena).
- Control estricto de cuota diaria de porciones por grupo prescrito con deshabilitacion reactiva de casillas al consumir el cupo.
- Centrado proporcional y adaptativo de columnas de alimentos.
- Integracion de bases de datos:
  - `Platillos_mexicanos.csv`: Preparaciones tradicionales con aporte nutrimental.
  - `SMAE_5aed-2.0.xlsx`: Catalogo de alimentos equivalentes.
- Tablas de resumen final de nutrientes en gramos (g) y porcentaje (%).
- Exportacion del plan detallado a archivo de texto (.txt).

### 4. Recordatorio de 24 Horas (`Recordatorio.java`)
- Evaluacion del consumo habitual del paciente mediante registro de alimentos por grupos del SMAE y platillos tradicionales.
- Tablas de evaluacion cuantitativa de nutrientes actuales (g) y porcentaje de adecuacion (%).
- Exportacion de reportes clinicos a formato .txt.

### 5. Somatotipo Heath-Carter (`CalculadoraSomatotipo.java`)
- Determinacion de los componentes: Endomorfismo, Mesomorfismo y Ectomorfismo.
- Trazo automatico en la Somatocarta 2D en coordenadas cartesianas (X, Y).
- Clasificacion morfologica y recomendaciones deportivas.

### 6. Composicion Corporal e IMC (`VentanaIMC.java` y `VentanaInfoParametros.java`)
- Indice de Masa Corporal (IMC) y clasificacion diagnostica.
- Densidad y porcentaje de grasa corporal mediante medicion de pliegues cutaneos empleando las ecuaciones de Durnin & Womersley y Siri.
- Estimacion de masa magra, masa grasa, areas musculares, masa osea, indice cintura-cadera (ICC) e indice cintura-talla (ICT).

### 7. Gestor Centralizado de Ventanas (`WindowManager.java`)
- Control de instancias unicas (evita duplicidad de ventanas abiertas).
- Cierre maestro: al cerrar la ventana principal de la calculadora se cierran de forma automatica todas las ventanas secundarias abiertas.

---

## Estructura del Proyecto

```text
CalculadoraEnergeticaModernaa/
│
├── dist/                                     # Directorio de distribucion generado
│   ├── NutriEnergiaPro/                      # Aplicacion portable standalone (con JRE embebido)
│   │   ├── NutriEnergiaPro.exe               # Ejecutable nativo Windows
│   │   ├── app/                              # JARs de la aplicacion y librerias
│   │   └── runtime/                          # Entorno Java optimizado y DLLs de JavaFX
│   ├── NutriEnergiaPro_Instalador.exe        # Instalador autosuficiente de un solo archivo
│   └── CalculadoraEnergeticaModernaa.jar     # Archivo JAR compilado
│
├── libs/                                     # Dependencias y librerias externas
│   ├── natives/                              # Bibliotecas dinamicas (.dll) de JavaFX
│   ├── javafx.base.jar                       # Modulo base JavaFX
│   ├── javafx.controls.jar                   # Modulo de controles JavaFX
│   ├── javafx.fxml.jar                       # Modulo FXML
│   ├── javafx.graphics.jar                   # Modulo de renderizado JavaFX
│   ├── poi-5.2.5.jar                         # Apache POI para archivos XLSX
│   ├── poi-ooxml-5.2.5.jar                   # Manejo OOXML
│   ├── opencsv-5.9.jar                       # Procesador de CSV
│   └── ... (librerias auxiliares)
│
├── resources/                                # Archivos de datos y hojas de estilo
│   ├── data/
│   │   ├── Platillos_mexicanos.csv           # Base de datos de platillos mexicanos
│   │   └── SMAE_5aed-2.0.xlsx                # Catalogo oficial SMAE 5a Edicion
│   └── style/
│       └── styles.css                        # Hoja de estilos en verde y blanco
│
├── src/                                      # Codigo fuente JavaFX
│   └── calculadoraenergeticamodernaa/
│       ├── CalculadoraEnergeticaModernaa.java # Ventana principal y calculo energetico
│       ├── CalculadoraSomatotipo.java         # Modulo de somatotipo Heath-Carter
│       ├── MainLauncher.java                  # Launcher desacoplado de Application
│       ├── MenuPrincipalAntropometria.java    # Menu de evaluacion antropometrica
│       ├── PlanAlimenticio.java               # Modulo de planificacion dietetica
│       ├── Recordatorio.java                  # Modulo de recordatorio de 24 horas
│       ├── ResponsiveManager.java             # Administrador de resolucion
│       ├── SistemaEquivalentes.java           # Modulo de distribucion de equivalentes
│       ├── VentanaIMC.java                    # Modulo de composicion corporal e IMC
│       ├── VentanaInfoParametros.java         # Ventana informativa de parametros
│       └── WindowManager.java                 # Gestor centralizado de ventanas e instancias
│
├── build-exe.bat                             # Script automatizado para compilar y generar el .exe
├── installer.iss                             # Script de empaquetado para Inno Setup
├── run.bat                                   # Script de arranque rapido en desarrollo
├── manifest.mf                               # Manifiesto de empaquetado JAR
├── food-and-drink.ico                        # Icono de la aplicacion
└── README.md                                 # Documentacion del proyecto
```

---

## Tecnologias y Librerias

| Componente | Version | Descripcion |
| :--- | :---: | :--- |
| Java JDK | 21 / 23+ | Lenguaje y entorno de ejecucion principal. |
| JavaFX (OpenJFX) | 25 | Framework para la construccion de la interfaz de usuario. |
| Apache POI | 5.2.5 | Procesamiento del catalogo de alimentos del SMAE (.xlsx). |
| OpenCSV | 5.9 | Lectura de datos de platillos tradicionales mexicanos. |
| Inno Setup | 6+ | Generador del instalador ejecutable unico para Windows. |
| Apache NetBeans | 19+ / 21+ | Entorno de desarrollo recomendado. |

---

## Como Crear o Actualizar el Instalador (.exe)

Si realizas cambios en el codigo fuente (`src/`), en las hojas de calculo o en los estilos y deseas **generar o actualizar el instalador ejecutable**:

### Requisitos previos para generar el instalador:
1. Tener instalado **JDK 21 o JDK 23** (con las herramientas `jlink` y `jpackage`).
2. Tener instalado **Inno Setup 6** (descarga gratuita desde [jrsoftware.org](https://jrsoftware.org/isinfo.php)).

### Pasos para compilar y generar el instalador:

1. Abre una terminal (CMD o PowerShell) en la raiz del proyecto y ejecuta el script automatizado:
   ```cmd
   .\build-exe.bat
   ```

2. El script realizara automaticamente los siguientes pasos:
   - Limpiara los archivos temporales y compilaciones anteriores.
   - Creara un Java Runtime Environment (JRE) reducido y optimizado con `jlink`.
   - Incorporara las librerias nativas de renderizado grafico de JavaFX (`libs/natives/*.dll`).
   - Construira la aplicacion ejecutable standalone utilizando `jpackage`.
   - Compilara el instalador unico utilizando Inno Setup (`installer.iss`).
   - Copiara el archivo resultante directamente a tu **Escritorio**:
     ```text
     %USERPROFILE%\Desktop\NutriEnergiaPro_Instalador.exe
     ```

3. **Distribucion**: El archivo `NutriEnergiaPro_Instalador.exe` pesa aproximadamente ~63 MB y es completamente autosuficiente (incluye Java embebido). Se puede enviar a cualquier computadora con Windows y funcionara de inmediato sin que el usuario requiera instalar Java previamente.

*(Nota: En el repositorio de Git, los archivos binarios compilados de la carpeta `dist/` se omiten mediante `.gitignore` para mantener el repositorio ligero. El instalador se genera localmente ejecutando `build-exe.bat` o se puede publicar como un archivo adjunto en los **Releases** de GitHub).*

---

## Instrucciones de Ejecucion y Desarrollo

### Opcion 1: Ejecucion rapida con script (.bat)
Para probar la aplicacion de inmediato en desarrollo sin necesidad de abrir el IDE:
```cmd
.\run.bat
```

### Opcion 2: Ejecucion desde NetBeans IDE
1. Abrir **Apache NetBeans IDE**.
2. Ir a **File** -> **Open Project...** y seleccionar la carpeta `CalculadoraEnergeticaModernaa`.
3. Hacer clic en **Run Project** (o presionar la tecla **F6**).

### Opcion 3: Compilacion y ejecucion manual desde terminal

```powershell
# 1. Compilar clases
mkdir -p build/classes
javac --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "libs/*" -d build/classes src/calculadoraenergeticamodernaa/*.java

# 2. Ejecutar
java --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "build/classes;resources;libs/*" calculadoraenergeticamodernaa.CalculadoraEnergeticaModernaa
```

---

## Creditos

- **Autor**: Hugo Ayala Alatriste
- **Proyecto**: Calculadora Energetica Moderna & Sistema Antropometrico (NutriEnergia Pro)
- **Institucion**: Servicio Social - Facultad de Quimica
- **Repositorio**: [https://github.com/TheShock25/calculadora-en-javafx](https://github.com/TheShock25/calculadora-en-javafx)
