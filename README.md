# Calculadora Energetica Moderna y Sistema Antropometrico (NutriEnergia Pro)

[![Java](https://img.shields.io/badge/Java-23%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-FF6F00?style=flat-square&logo=java&logoColor=white)](https://openjfx.io/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-5.2.5-D22128?style=flat-square&logo=apache&logoColor=white)](https://poi.apache.org/)
[![NetBeans](https://img.shields.io/badge/NetBeans-IDE-1B6AC6?style=flat-square&logo=apache-netbeans-ide&logoColor=white)](https://netbeans.apache.org/)

Aplicacion de escritorio desarrollada en Java y JavaFX para la estimacion del gasto energetico, evaluacion antropometrica (somatotipo y composicion corporal), planificacion de dietas basadas en el Sistema Mexicano de Alimentos Equivalentes (SMAE) y registro de recordatorio de 24 horas.

---

## Tabla de Contenidos
1. [Descripcion y Contexto](#descripcion-y-contexto)
2. [Modulos de la Aplicacion](#modulos-de-la-aplicacion)
3. [Estructura del Repositorio](#estructura-del-repositorio)
4. [Tecnologias y Librerias](#tecnologias-y-librerias)
5. [Nota sobre el empaquetado (.exe)](#nota-sobre-el-empaquetado-exe)
6. [Requisitos del Sistema](#requisitos-del-sistema)
7. [Instrucciones de Ejecucion](#instrucciones-de-ejecucion)
   - [Opcion 1: Ejecucion desde NetBeans IDE](#opcion-1-ejecucion-desde-netbeans-ide)
   - [Opcion 2: Ejecucion desde Terminal / Linea de Comandos](#opcion-2-ejecucion-desde-terminal--linea-de-comandos)
   - [Opcion 3: Ejecucion rapida mediante script (.bat)](#opcion-3-ejecucion-rapida-mediante-script-bat)
8. [Creditos](#creditos)

---

## Descripcion y Contexto

Este software fue desarrollado como parte del proyecto de Servicio Social orientado a alumnos y docentes de la Facultad de Quimica. Su proposito es brindar una herramienta de calculo nutricional que agilice los procedimientos de diagnostico y prescripcion dietetica en un entorno grafico interactivo y estructurado.

El sistema integra ecuaciones predictivas de energia, el catalogo de alimentos del SMAE (5a Edicion), recetas mexicanas tradicionales y formulas antropometricas estandarizadas.

---

## Modulos de la Aplicacion

### 1. Calculadora de Gasto Energetico (`CalculadoraEnergeticaModernaa.java`)
- **Ecuaciones implementadas**:
  - Harris-Benedict (masculino y femenino).
  - Mifflin-St Jeor.
  - Valencia (estratificada por grupos de edad: 18-30, 30-59 y >=60 anos).
- **Calculo de componentes**:
  - GEB (Gasto Energetico Basal).
  - ETA (Efecto Termogenico de los Alimentos, 10%).
  - GET (Gasto Energetico Total) aplicando factores de actividad fisica diferenciados por sexo.
- Visualizacion grafica interactiva del desglose energetico sobre Canvas.

### 2. Sistema de Equivalentes SMAE (`SistemaEquivalentes.java`)
- Distribucion de macronutrientes: Porcentajes y gramos de Hidratos de Carbono, Lipidos y Proteinas.
- Comparacion de calorias calculadas respecto al valor calorico total (VCT) objetivo.
- Manejo de grupos de alimentos: Verduras, Frutas, Cereales (con/sin grasa), Leguminosas, Alimentos de Origen Animal (segun aporte de grasa), Leche, Aceites y grasas, y Azucares.

### 3. Generador de Planes Alimenticios (`PlanAlimenticio.java`)
- Estructuracion de tiempos de comida: Desayuno, Colacion 1, Comida, Colacion 2 y Cena.
- Integracion de fuentes de datos:
  - `Platillos_mexicanos.csv`: Preparaciones tradicionales con aporte nutrimental.
  - `SMAE_5aed-2.0.xlsx`: Catalogo general de equivalentes.
- Exportacion del plan alimenticio a formato Microsoft Excel (.xlsx) mediante Apache POI.

### 4. Recordatorio de 24 Horas (`Recordatorio.java`)
- Instrumento clinico para registrar el consumo habitual de alimentos del paciente durante el dia previo.
- Calculo del porcentaje de adecuacion nutrimental comparando lo consumido contra los requerimientos diarios.

### 5. Calculadora de Somatotipo Heath-Carter (`CalculadoraSomatotipo.java`)
- Determinacion de los componentes del somatotipo:
  - Endomorfismo (adiposidad relativa).
  - Mesomorfismo (desarrollo musculoesqueletico relativo).
  - Ectomorfismo (linealidad relativa).
- Trazo automatico en la Somatocarta 2D en coordenadas cartesianas (X, Y).
- Clasificacion morfologica y sugerencias de entrenamiento.

### 6. Composicion Corporal e IMC (`VentanaIMC.java` y `VentanaInfoParametros.java`)
- Indice de Masa Corporal (IMC) y clasificacion nutricional.
- Estimacion de densidad y porcentaje de grasa corporal mediante medicion de pliegues cutaneos (triceps, biceps, subescapular y suprailiaco) empleando las ecuaciones de Durnin & Womersley y Siri.
- Estimacion de masa magra, masa grasa, areas musculares, masa osea, indice cintura-cadera (ICC) e indice cintura-talla (ICT).

### 7. Gestion de Responsividad (`ResponsiveManager.java`)
- Ajuste dinamico de fuentes, margenes y distribucion de paneles para adaptarse tanto a pantallas de escritorio como a dispositivos tactiles o tabletas.

---

## Estructura del Repositorio

```text
CalculadoraEnergeticaModernaa/
│
├── dist/                                     # Archivos generados de distribucion
│   ├── CalculadoraEnergeticaModernaa.jar     # Archivo JAR ejecutable
│   ├── README.TXT                            # Informacion de distribucion de NetBeans
│   └── lib/                                  # Dependencias JAR requeridas por el build
│
├── libs/                                     # Librerias externas del proyecto
│   ├── commons-collections4-4.4.jar          # Librerias auxiliares Apache Commons
│   ├── commons-compress-1.21.jar
│   ├── commons-io-2.15.1.jar
│   ├── commons-lang3-3.14.0.jar
│   ├── curvesapi-1.07.jar
│   ├── javafx.base.jar                       # Modulo base de JavaFX
│   ├── javafx.controls.jar                   # Modulo de controles JavaFX
│   ├── javafx.fxml.jar                       # Modulo FXML
│   ├── javafx.graphics.jar                   # Modulo grafico y de renderizado JavaFX
│   ├── javafx.media.jar                      # Modulo multimedia
│   ├── javafx.swing.jar                      # Interoperabilidad con Swing
│   ├── javafx.web.jar                        # Motor web de JavaFX
│   ├── log4j-api-2.17.1.jar                  # Framework de logging
│   ├── log4j-core-2.17.1.jar
│   ├── opencsv-5.9.jar                       # Lectura y procesamiento de archivos CSV
│   ├── poi-5.2.5.jar                         # Apache POI para hojas de calculo
│   ├── poi-ooxml-5.2.5.jar                   # Manejo de formato XLSX en POI
│   ├── poi-ooxml-lite-5.2.5.jar
│   └── xmlbeans-5.1.1.jar                    # Esquemas XML para POI
│
├── nbproject/                                # Archivos de proyecto Apache NetBeans
│   ├── build-impl.xml                        # Script de construccion Ant
│   ├── genfiles.properties
│   ├── project.properties                    # Propiedades del proyecto y argumentos JVM
│   └── project.xml                           # Definicion de dependencias
│
├── resources/                                # Archivos de datos y estilos
│   ├── data/
│   │   ├── Platillos_mexicanos.csv           # Base de datos de platillos
│   │   └── SMAE_5aed-2.0.xlsx                # Catalogo SMAE 5a Edicion
│   └── style/
│       └── styles.css                        # Hoja de estilos de la interfaz JavaFX
│
├── src/                                      # Codigo fuente
│   └── calculadoraenergeticamodernaa/
│       ├── CalculadoraEnergeticaModernaa.java # Clase principal y modulo de gasto energetico
│       ├── CalculadoraSomatotipo.java         # Modulo de somatotipo Heath-Carter
│       ├── JavaFXEmbeddedLauncher.java        # Launcher para inicializacion de JavaFX
│       ├── MainLauncher.java                  # Launcher alternativo desacoplado
│       ├── MenuPrincipalAntropometria.java    # Menu de opciones antropometricas
│       ├── PlanAlimenticio.java               # Modulo de dietas y exportacion a Excel
│       ├── Recordatorio.java                  # Modulo de recordatorio de 24 horas
│       ├── ResponsiveManager.java             # Gestor de escala para diferentes pantallas
│       ├── SistemaEquivalentes.java           # Modulo de distribucion de equivalentes
│       ├── VentanaIMC.java                    # Modulo de composicion corporal e IMC
│       └── VentanaInfoParametros.java         # Ventana informativa de parametros
│
├── build.xml                                 # Script de construccion Apache Ant
├── manifest.mf                               # Manifiesto de empaquetado JAR
├── food-and-drink.ico                        # Icono de la aplicacion
├── run.bat                                   # Script de arranque rapido para Windows
└── README.md                                 # Documentacion principal
```

---

## Tecnologias y Librerias

| Componente | Version | Descripcion |
| :--- | :---: | :--- |
| Java JDK | 21 / 23+ | Lenguaje y entorno de ejecucion principal. |
| JavaFX (OpenJFX) | 25 | Framework para la construccion de la interfaz de usuario. |
| Apache POI | 5.2.5 | Procesamiento del archivo SMAE (.xlsx) y generacion de reportes. |
| OpenCSV | 5.9 | Lectura de datos estructurados de platillos mexicanos. |
| Apache Commons | 4.4 / 3.14 | Utilidades de I/O, compresion y manejo de colecciones. |
| Apache Log4j | 2.17.1 | Sistema de registro de eventos (logging). |
| Apache NetBeans | 19+ / 21+ | Entorno de desarrollo utilizado en el proyecto. |

---

## Nota sobre el empaquetado (.exe)

Durante el desarrollo no fue factible generar un ejecutable `.exe` nativo y autosuficiente debido a las siguientes restricciones de la arquitectura modular de Java:

1. **Modularizacion de JavaFX (JPMS)**: Desde Java 11, JavaFX no forma parte del JDK estandar y se distribuye como modulos desacoplados que deben cargarse explicitamente mediante `--module-path`.
2. **Dependencias no modulares en herramientas de empaquetado**: Herramientas como `jlink` o `jpackage` exigen que todas las librerias sean modulos JPMS nombrados. Librerias como Apache POI, Log4j y OpenCSV manejan dependencias automaticas que provocan conflictos en la creacion de imagenes nativas.
3. **Bibliotecas dinamicas de renderizado**: JavaFX depende de librerias nativas (.dll como `prism_d3d.dll` y `glass.dll`) asociadas a la plataforma, lo que genera fallos al empaquetar con herramientas legacy tipo Launch4j sin un runtime completo.

Por estos motivos, el proyecto se ejecuta de forma directa mediante la configuracion de modulos de JavaFX o desde NetBeans IDE.

---

## Requisitos del Sistema

- **Sistema Operativo**: Windows 10/11 (64 bits), macOS o Linux.
- **Java Development Kit (JDK)**: JDK 21 o JDK 23 instalado y referenciado en las variables de entorno (`PATH` / `JAVA_HOME`).
- **Librerias JavaFX**: Se encuentran incluidas en el directorio `libs/` del repositorio.

Para verificar la instalacion de Java en la terminal:
```bash
java -version
javac -version
```

---

## Instrucciones de Ejecucion

### Opcion 1: Ejecucion desde NetBeans IDE

1. Abrir **Apache NetBeans IDE**.
2. En el menu superior, seleccionar **File** -> **Open Project...**
3. Seleccionar la carpeta `CalculadoraEnergeticaModernaa`.
4. Ejecutar el proyecto haciendo clic en el boton verde **Run Project** (o mediante la tecla **F6**).

Si se requiere verificar los argumentos de ejecucion en NetBeans:
- Clic derecho en el proyecto -> **Properties** -> **Run** -> **VM Options**:
  ```text
  --module-path "libs" --add-modules javafx.controls,javafx.fxml
  ```

---

### Opcion 2: Ejecucion desde Terminal / Linea de Comandos

#### A. Ejecucion directa del JAR generado
Abrir una terminal (PowerShell o CMD) en la raiz del proyecto y ejecutar:

```powershell
# En Windows (PowerShell / CMD)
java --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "dist/CalculadoraEnergeticaModernaa.jar;libs/*" calculadoraenergeticamodernaa.CalculadoraEnergeticaModernaa
```

*(En sistemas Linux / macOS, sustituir los delimitadores `;` por `:` dentro del parametro `-cp`).*

#### B. Compilacion y ejecucion desde el codigo fuente
Si se realizaron modificaciones en `src/`:

```powershell
# 1. Crear directorio de clases
mkdir -p build/classes

# 2. Compilar fuentes
javac --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "libs/*" -d build/classes src/calculadoraenergeticamodernaa/*.java

# 3. Ejecutar aplicacion
java --module-path "libs" --add-modules javafx.controls,javafx.fxml -cp "build/classes;resources;libs/*" calculadoraenergeticamodernaa.CalculadoraEnergeticaModernaa
```

---

### Opcion 3: Ejecucion rapida mediante script (.bat)

Para conveniencia en Windows, se incluye el archivo `run.bat` en la raiz del proyecto:

1. Hacer doble clic sobre `run.bat`, o ejecutar en terminal:
   ```cmd
   .\run.bat
   ```

---

## Creditos

- **Autor**: Hugo Ayala Alaytriste y Nolasco Gonzalez Alexis Daniel
- **Proyecto**: Calculadora Energetica Moderna & Sistema Antropometrico
- **Institucion**: Servicio Social - Facultad de Quimica
- **Repositorio**: [https://github.com/TheShock25/calculadora-en-javafx](https://github.com/TheShock25/calculadora-en-javafx)
