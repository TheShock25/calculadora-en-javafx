package calculadoraenergeticamodernaa;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VentanaIMC extends Application {

    // Paleta de colores oficial: Verdes y Blancos
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color ACCENT_GREEN = Color.web("#43A047");        // Verde acción
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color LIGHT_MINT = Color.web("#E8F5E9");          // Fondo verde menta suave
    private static final Color BORDER_GREEN = Color.web("#C8E6C9");        // Borde verde suave
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Texto oscuro
    private static final Color TEXT_MUTED = Color.web("#5C7669");          // Texto secundario

    // Componentes generales
    private Label tituloLabel, modeloLabel;
    private Button calcularBtn, infoBtn;

    // Panel izquierdo - resultados principales
    private VBox panelIzquierdo;
    private Label imcValor, pesoValor, grasaCorporalValor, lbmValor;
    private Label iccValor, ictValor;

    // Inputs básicos
    private TextField brazoField, tricipitalField, edadField, alturaField;
    private TextField dmField, drField, dfField;
    private TextField pectoralField, axilarField, tricepField, subescapularField;
    private TextField abdominalField, suprailiacoField, musloField;
    private TextField cinturaField, caderaField;

    // Resultados áreas y composición
    private Label abLabel, agbLabel, ambLabel, ambCorrLabel;
    private Label masaOseaLabel, masaResidualLabel, densidadCorporalLabel;

    // Datos del paciente
    private String sexo = "Hombre";
    private double peso = 70.0;
    private Label pesoActualLabel;
    private boolean isTabletMode;

    // Control de datos pendientes
    private Double pendingPeso = null;
    private Integer pendingEdad = null;
    private Integer pendingAltura = null;
    private boolean hasPendingDatos = false;

    public VentanaIMC(String sexo) {
        this.sexo = sexo != null ? sexo : "Hombre";
    }

    public VentanaIMC() {
        this("Hombre");
    }

    public void establecerDatos(String sexo, double peso, double edad, double altura) {
        this.sexo = sexo != null ? sexo : "Hombre";
        this.peso = peso;

        if (edadField != null && alturaField != null && pesoActualLabel != null) {
            try {
                if (edad > 0) edadField.setText(String.valueOf((int) edad));
                if (altura > 0) alturaField.setText(String.valueOf((int) altura));
                if (peso > 0) pesoActualLabel.setText(String.format("%.2f kg", peso));
            } catch (Exception ignored) {}
            calcularIMC();
            hasPendingDatos = false;
        } else {
            pendingPeso = peso;
            pendingEdad = (int) Math.round(edad);
            pendingAltura = (int) Math.round(altura);
            hasPendingDatos = true;
        }
    }

    public void setSexo(String sexo) {
        this.sexo = sexo != null ? sexo : "Hombre";
        calcularIMC();
    }

    public void setPeso(double peso) {
        this.peso = peso;
        if (pesoActualLabel != null) {
            pesoActualLabel.setText(String.format("%.2f kg", peso));
        }
        calcularIMC();
    }

    public void actualizarEdadAltura(double edad, double altura) {
        if (edadField != null && edad > 0) {
            edadField.setText(String.valueOf((int) edad));
        }
        if (alturaField != null && altura > 0) {
            alturaField.setText(String.valueOf((int) altura));
            calcularIMC();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();

        primaryStage.setTitle("NutriEnergia Pro - IMC y Composicion Corporal");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // Header moderno
        VBox header = crearHeader();
        root.setTop(header);

        // Centro desplazable
        ScrollPane centerScroll = crearPanelCentral();
        root.setCenter(centerScroll);

        // Footer con botón de acción
        HBox footer = crearFooter();
        root.setBottom(footer);

        double width = isTabletMode ? 
            Math.min(1080, ResponsiveManager.getScreenBounds().getWidth() - 50) : 1020;
        double height = isTabletMode ? 
            Math.min(880, ResponsiveManager.getScreenBounds().getHeight() - 50) : 800;

        Scene scene = new Scene(root, width, height);
        cargarCSS(scene);

        primaryStage.setScene(scene);
        primaryStage.show();

        configurarEventos();

        if (hasPendingDatos) {
            try {
                if (pendingEdad != null && pendingEdad > 0) edadField.setText(String.valueOf(pendingEdad));
                if (pendingAltura != null && pendingAltura > 0) alturaField.setText(String.valueOf(pendingAltura));
                if (pendingPeso != null && pendingPeso > 0 && pesoActualLabel != null) {
                    pesoActualLabel.setText(String.format("%.2f kg", pendingPeso));
                }
            } catch (Exception ignored) {}
            calcularIMC();
            hasPendingDatos = false;
        }
    }

    private void cargarCSS(Scene scene) {
        String[] posiblesRutas = {
            "/style/styles.css",
            "style/styles.css",
            "resources/style/styles.css",
            "/styles.css",
            "styles.css"
        };

        for (String ruta : posiblesRutas) {
            try {
                java.net.URL cssUrl = getClass().getResource(ruta);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                    break;
                }
            } catch (Exception e) {
                // Continuar buscando
            }
        }
    }

    private VBox crearHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1B5E20, #2E7D32, #388E3C); " +
            "-fx-background-radius: 0 0 16 16;"
        );

        double margin = ResponsiveManager.getMargin(16, 20);
        header.setPadding(new Insets(margin, margin, margin + 4, margin));

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER);

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);

        Label badge = new Label("EVALUACION DE COMPOSICION CORPORAL");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.18); " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 3 10;"
        );

        tituloLabel = new Label("IMC y Densidad Corporal");
        tituloLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(22, 26)));
        tituloLabel.setTextFill(Color.WHITE);

        modeloLabel = new Label("Modelo de Fraccionamiento Antropometrico y Ecuaciones de Jackson & Pollock");
        modeloLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(12.5, 13.5)));
        modeloLabel.setTextFill(Color.web("#C8E6C9"));

        titleBox.getChildren().addAll(badge, tituloLabel, modeloLabel);

        // Botón de Información Clínica en el Header
        infoBtn = new Button("GUIA DE PARAMETROS");
        infoBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        infoBtn.setTextFill(DARK_FOREST);
        infoBtn.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 16; " +
            "-fx-padding: 6 14; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);"
        );
        infoBtn.setOnMouseEntered(e -> infoBtn.setStyle(
            "-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-padding: 6 14; -fx-cursor: hand;"
        ));
        infoBtn.setOnMouseExited(e -> infoBtn.setStyle(
            "-fx-background-color: #E8F5E9; -fx-background-radius: 16; -fx-padding: 6 14; -fx-cursor: hand;"
        ));
        infoBtn.setOnAction(e -> mostrarInfoParametros());

        HBox btnContainer = new HBox(infoBtn);
        btnContainer.setAlignment(Pos.CENTER_RIGHT);

        topRow.getChildren().addAll(titleBox);

        header.getChildren().addAll(topRow, btnContainer);
        header.setEffect(new DropShadow(10, 0, 3, Color.rgb(0, 0, 0, 0.15)));

        return header;
    }

    private ScrollPane crearPanelCentral() {
        VBox scrollContent = new VBox(20);
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.setPadding(new Insets(20));

        HBox mainLayout = new HBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setMaxWidth(960);

        // 1. Panel Lateral Izquierdo: Resumen Diagnóstico con Hover Tooltips
        panelIzquierdo = crearPanelIzquierdo();

        // 2. Panel Central: Secciones de entrada y resultados de áreas
        VBox centerForms = crearFormularios();

        if (isTabletMode) {
            scrollContent.getChildren().addAll(panelIzquierdo, centerForms);
        } else {
            mainLayout.getChildren().addAll(panelIzquierdo, centerForms);
            HBox.setHgrow(centerForms, Priority.ALWAYS);
            scrollContent.getChildren().add(mainLayout);
        }

        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearPanelIzquierdo() {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(260);
        panel.setMaxWidth(280);
        panel.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 14; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 18 16;"
        );
        panel.setEffect(new DropShadow(12, 0, 3, Color.rgb(0, 0, 0, 0.05)));

        Label titleLabel = new Label("DIAGNOSTICO GENERAL");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(PRIMARY_GREEN);
        titleLabel.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 6; -fx-padding: 3 10;");

        // 1. IMC
        VBox boxIMC = crearItemResumen("INDICE DE MASA CORPORAL (IMC)", 
            imcValor = crearLabelValor("- kg/m²"),
            "Indice de Masa Corporal (IMC)",
            "Medida antropometrica que relaciona el peso con la talla al cuadrado (peso / talla²). Clasificacion OMS: <18.5 Bajo peso, 18.5-24.9 Normal, 25-29.9 Sobrepeso, >=30 Obesidad.");

        // 2. Peso Ideal
        VBox boxPesoIdeal = crearItemResumen("PESO IDEAL ESTIMADO", 
            pesoValor = crearLabelValor("- kg"),
            "Peso Ideal Estimado (Formula de Lorentz)",
            "Estimacion del peso corporal optimo asociado con un menor riesgo de comorbilidades metabolicas segun sexo y estatura.");

        // 3. % Grasa Corporal
        VBox boxGrasa = crearItemResumen("PORCENTAJE DE GRASA (% GC)", 
            grasaCorporalValor = crearLabelValor("- %"),
            "Porcentaje de Grasa Corporal (% GC - Ecuacion de Siri)",
            "Proporcion de masa grasa respecto al peso corporal total calculada a partir de la densidad corporal (495/DC - 450).");

        // 4. Masa Magra (LBM)
        VBox boxLBM = crearItemResumen("MASA CORPORAL MAGRA (LBM)", 
            lbmValor = crearLabelValor("- kg"),
            "Masa Corporal Magra (Lean Body Mass)",
            "Representa el peso corporal total exento de tejido adiposo (masa muscular, tejido oseo, organos vitales y liquidos).");

        // 5. ICC
        VBox boxICC = crearItemResumen("INDICE CINTURA/CADERA (ICC)", 
            iccValor = crearLabelValor("-"),
            "Indice Cintura / Cadera (ICC)",
            "Indicador antropometrico de distribucion de grasa corporal y riesgo cardiovascular central (Riesgo: >=0.90 en hombres, >=0.85 en mujeres).");

        // 6. ICT
        VBox boxICT = crearItemResumen("INDICE CINTURA/TALLA (ICT)", 
            ictValor = crearLabelValor("-"),
            "Indice Cintura / Talla (ICT)",
            "Predictor temprano de riesgo cardiometabolico basado en la adiposidad visceral abdominal (Valor optimo: < 0.50).");

        panel.getChildren().addAll(
            titleLabel,
            boxIMC,
            boxPesoIdeal,
            boxGrasa,
            boxLBM,
            boxICC,
            boxICT
        );

        return panel;
    }

    private VBox crearItemResumen(String titulo, Label valorLabel, String hoverTitle, String hoverDesc) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #E0E8E3; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 10; " +
            "-fx-cursor: hand;"
        );

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        lblTitulo.setTextFill(TEXT_MUTED);

        box.getChildren().addAll(lblTitulo, valorLabel);

        // Instalar Hover Tooltip explicativo
        attachHoverInfo(box, hoverTitle, hoverDesc);

        return box;
    }

    private Label crearLabelValor(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14.5));
        label.setTextFill(TEXT_DARK);
        return label;
    }

    private VBox crearFormularios() {
        VBox panel = new VBox(14);

        // Sección 1: Datos Básicos
        VBox datosBasicos = crearSeccion("1. DATOS BASICOS DEL PACIENTE", crearPanelDatosBasicos());

        // Sección 2: Resultados Áreas Corporales
        VBox resultadosAreas = crearSeccion("2. AREAS CORPORALES DEL BRAZO", crearPanelResultadosAreas());

        // Sección 3: Masa Ósea y Residual
        VBox masaOsea = crearSeccion("3. MASA OSEA Y MASA RESIDUAL (ROCHA / WURCH)", crearPanelMasaOsea());

        // Sección 4: Pliegues Cutáneos
        VBox pliegues = crearSeccion("4. PLIEGUES CUTANEOS (7 SITIOS - JACKSON & POLLOCK)", crearPanelPliegues());

        // Sección 5: Circunferencias
        VBox circunferencias = crearSeccion("5. CIRCUNFERENCIAS CORPORALES", crearPanelCircunferencias());

        // Sección 6: Densidad Corporal
        VBox densidad = crearSeccion("6. DENSIDAD CORPORAL (DC)", crearPanelDensidad());

        panel.getChildren().addAll(
            datosBasicos, resultadosAreas, masaOsea, 
            pliegues, circunferencias, densidad
        );

        return panel;
    }

    private VBox crearSeccion(String titulo, Region contenido) {
        VBox seccion = new VBox(10);
        seccion.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 14 18;"
        );
        seccion.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        tituloLabel.setTextFill(PRIMARY_GREEN);

        seccion.getChildren().addAll(tituloLabel, contenido);
        return seccion;
    }

    private GridPane crearPanelDatosBasicos() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        brazoField = crearCampoTexto("30.0");
        tricipitalField = crearCampoTexto("12.0");
        edadField = crearCampoTexto("25");
        alturaField = crearCampoTexto("175.0");

        pesoActualLabel = new Label(String.format("%.2f kg", peso));
        pesoActualLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        pesoActualLabel.setTextFill(PRIMARY_GREEN);

        HBox boxBrazo = crearCampoConUnidad("Circunferencia Brazo:", brazoField, "cm",
            "Circunferencia de Brazo", "Perimetro tomado en el punto medio entre el acromion y el olecranon con el brazo relajado.");
        HBox boxTricipital = crearCampoConUnidad("Pliegue Tricipital:", tricipitalField, "mm",
            "Pliegue Tricipital", "Grosor del pliegue cutaneo vertical tomado en la cara posterior del brazo, sobre el musculo triceps.");
        HBox boxEdad = crearCampoConUnidad("Edad:", edadField, "anos",
            "Edad Cronologica", "Factor determinante para el calculo de densidad corporal y ecuaciones predictivas.");
        HBox boxAltura = crearCampoConUnidad("Altura:", alturaField, "cm",
            "Estatura del Paciente", "Medida tomada con estadimetro en posicion antropometrica erecta.");

        grid.add(boxBrazo, 0, 0);
        grid.add(boxTricipital, 1, 0);
        grid.add(boxEdad, 0, 1);
        grid.add(boxAltura, 1, 1);

        HBox pesoBox = new HBox(8);
        pesoBox.setAlignment(Pos.CENTER_LEFT);
        Label pesoTitle = new Label("Peso Actual Transferido:");
        pesoTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        pesoTitle.setTextFill(TEXT_DARK);
        pesoBox.getChildren().addAll(pesoTitle, pesoActualLabel);
        attachHoverInfo(pesoBox, "Peso Corporal", "Peso del paciente transferido desde el modulo principal.");

        grid.add(pesoBox, 0, 2, 2, 1);

        return grid;
    }

    private GridPane crearPanelResultadosAreas() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        abLabel = crearLabelResultado("AB (Area Brazo): Pendiente",
            "Area Total del Brazo (AB)", "Area de la seccion transversal del brazo calculada como (brazo² / 4π). Incluye tejido muscular, adiposo y oseo.");
        agbLabel = crearLabelResultado("AGB (Area Grasa): Pendiente",
            "Area Grasa del Brazo (AGB)", "Area de tejido adiposo en el brazo calculada a partir del perimetro braquial y el pliegue tricipital.");
        ambLabel = crearLabelResultado("AMB (Area Muscular): Pendiente",
            "Area Muscular del Brazo (AMB)", "Area del compartimento muscular (AB - AGB). Indicador clinico fundamental de la reserva proteica somatica.");
        ambCorrLabel = crearLabelResultado("AMB Corregido: Pendiente",
            "Area Muscular Corregida", "Ajuste del AMB restando el area osea (-10 cm² en hombres, -6.5 cm² en mujeres) para mayor precision diagnostica.");

        grid.add(abLabel, 0, 0);
        grid.add(agbLabel, 1, 0);
        grid.add(ambLabel, 0, 1);
        grid.add(ambCorrLabel, 1, 1);

        GridPane.setHgrow(abLabel, Priority.ALWAYS);
        GridPane.setHgrow(agbLabel, Priority.ALWAYS);
        GridPane.setHgrow(ambLabel, Priority.ALWAYS);
        GridPane.setHgrow(ambCorrLabel, Priority.ALWAYS);

        return grid;
    }

    private VBox crearPanelMasaOsea() {
        VBox panel = new VBox(10);

        HBox inputsPanel = new HBox(16);
        inputsPanel.setAlignment(Pos.CENTER_LEFT);

        dmField = crearCampoTexto("6.5");
        drField = crearCampoTexto("5.2");
        dfField = crearCampoTexto("9.5");

        inputsPanel.getChildren().addAll(
            crearCampoConUnidad("DM (Muneca):", dmField, "cm",
                "Diametro Biestiloideo Muneca (DM)", "Diametro oseo transversal entre los procesos estiloides del radio y cubito para estimar masa osea (Rocha)."),
            crearCampoConUnidad("DR (Rodilla):", drField, "cm",
                "Diametro Bicondileo Femur (DR)", "Medida de la distancia entre los epicondilos femorales medial y lateral en la rodilla."),
            crearCampoConUnidad("DF (Femur/Maleolar):", dfField, "cm",
                "Diametro Maleolar Tobillo (DF)", "Medida transversal entre los maleolos tibial y peroneo del tobillo.")
        );

        HBox resultPanel = new HBox(14);
        resultPanel.setAlignment(Pos.CENTER_LEFT);

        masaOseaLabel = crearLabelResultado("Masa Osea: Pendiente",
            "Masa Osea Estimada (Rocha)", "Estimacion del peso del tejido oseo en porcentaje a partir de los 3 diametros oseos ((DM + DR + DF) * 1.2 / 10).");
        masaResidualLabel = crearLabelResultado("Masa Residual: Pendiente",
            "Masa Residual (Wurch)", "Peso correspondiente a las visceras, organos internos y componentes no grasos ni oseos (24% en hombres, 21% en mujeres).");

        HBox.setHgrow(masaOseaLabel, Priority.ALWAYS);
        HBox.setHgrow(masaResidualLabel, Priority.ALWAYS);

        resultPanel.getChildren().addAll(masaOseaLabel, masaResidualLabel);
        panel.getChildren().addAll(inputsPanel, resultPanel);

        return panel;
    }

    private GridPane crearPanelPliegues() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        pectoralField = crearCampoTexto("10.0");
        axilarField = crearCampoTexto("12.0");
        tricepField = crearCampoTexto("12.0");
        subescapularField = crearCampoTexto("14.0");
        abdominalField = crearCampoTexto("18.0");
        suprailiacoField = crearCampoTexto("15.0");
        musloField = crearCampoTexto("16.0");

        grid.add(crearCampoConUnidad("Pectoral:", pectoralField, "mm",
            "Pliegue Pectoral", "Pliegue cutaneo tomado diagonalmente en la linea axilar anterior hacia el pezon."), 0, 0);
        grid.add(crearCampoConUnidad("Axilar Medio:", axilarField, "mm",
            "Pliegue Axilar Medio", "Pliegue vertical tomado en la linea axilar media a nivel de la apofisis xifoides."), 1, 0);
        grid.add(crearCampoConUnidad("Triceps:", tricepField, "mm",
            "Pliegue Tricipital", "Pliegue vertical en la linea media posterior del brazo, sobre el triceps."), 2, 0);

        grid.add(crearCampoConUnidad("Subescapular:", subescapularField, "mm",
            "Pliegue Subescapular", "Pliegue diagonal tomado 1 a 2 cm debajo del vertice inferior de la escapula."), 0, 1);
        grid.add(crearCampoConUnidad("Abdominal:", abdominalField, "mm",
            "Pliegue Abdominal", "Pliegue vertical tomado a 2 cm a la derecha de la cicatriz umbilical."), 1, 1);
        grid.add(crearCampoConUnidad("Suprailiaco:", suprailiacoField, "mm",
            "Pliegue Suprailiaco", "Pliegue diagonal tomado inmediatamente superior a la cresta iliaca anterior."), 2, 1);

        grid.add(crearCampoConUnidad("Muslo Anterior:", musloField, "mm",
            "Pliegue Muslo Anterior", "Pliegue vertical en la cara anterior del muslo, punto medio inguinal-rotuliano."), 0, 2);

        return grid;
    }

    private HBox crearPanelCircunferencias() {
        HBox panel = new HBox(20);
        panel.setAlignment(Pos.CENTER_LEFT);

        cinturaField = crearCampoTexto("80.0");
        caderaField = crearCampoTexto("98.0");

        panel.getChildren().addAll(
            crearCampoConUnidad("Cintura:", cinturaField, "cm",
                "Circunferencia de Cintura", "Medida tomada en el punto mas estrecho del abdomen entre la ultima costilla y la cresta iliaca para evaluar riesgo visceral."),
            crearCampoConUnidad("Cadera:", caderaField, "cm",
                "Circunferencia de Cadera", "Medida tomada en la maxima protuberancia posterior glutea con el paciente en bipedestacion.")
        );

        return panel;
    }

    private HBox crearPanelDensidad() {
        HBox panel = new HBox(14);
        panel.setAlignment(Pos.CENTER_LEFT);

        densidadCorporalLabel = crearLabelResultado("Densidad Corporal (DC): Pendiente de calculo",
            "Densidad Corporal (DC - Jackson & Pollock)", "Magnitud fisica calculada a partir de la sumatoria de 7 pliegues cutaneos y la edad, utilizada para derivar el porcentaje de grasa corporal.");
        HBox.setHgrow(densidadCorporalLabel, Priority.ALWAYS);

        panel.getChildren().add(densidadCorporalLabel);
        return panel;
    }

    private HBox crearCampoConUnidad(String labelText, TextField field, String unidad, String hoverTitle, String hoverDesc) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        label.setTextFill(TEXT_DARK);

        Label unitLabel = new Label(unidad);
        unitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        unitLabel.setTextFill(PRIMARY_GREEN);
        unitLabel.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 4 6; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6;"
        );

        box.getChildren().addAll(label, field, unitLabel);

        // Tooltip al pasar el mouse por la etiqueta o la caja
        attachHoverInfo(label, hoverTitle, hoverDesc);
        attachHoverInfo(box, hoverTitle, hoverDesc);

        return box;
    }

    private TextField crearCampoTexto(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(65);
        field.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        field.setAlignment(Pos.CENTER);
        field.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 4 6;"
        );

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #2E7D32; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 4 6; " +
                    "-fx-effect: dropshadow(gaussian, rgba(46, 125, 50, 0.25), 4, 0, 0, 1);"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: #FAFCFA; " +
                    "-fx-border-color: #C8E6C9; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 4 6;"
                );
            }
        });

        return field;
    }

    private Label crearLabelResultado(String texto, String hoverTitle, String hoverDesc) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        label.setTextFill(TEXT_DARK);
        label.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-border-color: #A5D6A7; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 12; " +
            "-fx-cursor: hand;"
        );
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);

        attachHoverInfo(label, hoverTitle, hoverDesc);

        return label;
    }

    private void attachHoverInfo(Node node, String title, String description) {
        Tooltip tooltip = new Tooltip(title + "\n\n" + description);
        tooltip.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        tooltip.setStyle(
            "-fx-background-color: #1C2D27; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 10 14; " +
            "-fx-font-size: 12px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 8, 0, 0, 3);"
        );
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(340);
        tooltip.setShowDelay(Duration.millis(120));
        tooltip.setShowDuration(Duration.seconds(18));
        Tooltip.install(node, tooltip);
    }

    private HBox crearFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E8E3; -fx-border-width: 1.5 0 0 0;");
        footer.setPadding(new Insets(12, 20, 14, 20));

        calcularBtn = new Button("CALCULAR AREAS Y COMPOSICION CORPORAL");
        calcularBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13.5));
        calcularBtn.setTextFill(Color.WHITE);
        calcularBtn.setMinWidth(320);
        calcularBtn.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-background-radius: 22; " +
            "-fx-padding: 12 28; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        );
        calcularBtn.setOnMouseEntered(e -> calcularBtn.setStyle(
            "-fx-background-color: #388E3C; " +
            "-fx-background-radius: 22; " +
            "-fx-padding: 12 28; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 8, 0, 0, 3);"
        ));
        calcularBtn.setOnMouseExited(e -> calcularBtn.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-background-radius: 22; " +
            "-fx-padding: 12 28; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        ));
        calcularBtn.setOnAction(e -> calcularAreasYDC());

        footer.getChildren().add(calcularBtn);
        return footer;
    }

    private void configurarEventos() {
        if (alturaField != null) {
            alturaField.textProperty().addListener((obs, oldVal, newVal) -> calcularIMC());
        }
    }

    public void calcularIMC() {
        try {
            String alturaTxt = safeGetText(alturaField);
            if (peso > 0 && !alturaTxt.trim().isEmpty()) {
                double alturaCm = Double.parseDouble(alturaTxt.trim());
                if (alturaCm > 0) {
                    double alturaM = alturaCm / 100.0;
                    double imc = peso / (alturaM * alturaM);

                    String estadoIMC;
                    if (imc < 18.5) estadoIMC = "(Bajo peso)";
                    else if (imc < 25.0) estadoIMC = "(Normal)";
                    else if (imc < 30.0) estadoIMC = "(Sobrepeso)";
                    else estadoIMC = "(Obesidad)";

                    if (imcValor != null) {
                        imcValor.setText(String.format("%.2f kg/m² %s", imc, estadoIMC));
                    }

                    double pesoIdeal;
                    if ("Hombre".equalsIgnoreCase(sexo)) {
                        pesoIdeal = alturaCm - 100 - ((alturaCm - 150) / 4.0);
                    } else {
                        pesoIdeal = alturaCm - 100 - ((alturaCm - 150) / 2.0);
                    }

                    if (pesoValor != null) {
                        pesoValor.setText(String.format("%.2f kg", pesoIdeal));
                    }
                }
            }
        } catch (NumberFormatException ignored) {}
    }

    public void calcularAreasYDC() {
        try {
            // 1. Áreas Corporales del Brazo
            double brazo = Double.parseDouble(safeGetText(brazoField).trim());
            double pliegue = Double.parseDouble(safeGetText(tricipitalField).trim());
            double edad = Double.parseDouble(safeGetText(edadField).trim());
            double pliegueCM = pliegue * 0.10;

            double ab = (Math.pow(brazo, 2)) / (4 * Math.PI);
            double agb = Math.pow((brazo - (Math.PI * pliegueCM)), 2) / (4 * Math.PI);
            double amb = ab - agb;
            double ambCorregido = "Hombre".equalsIgnoreCase(sexo) ? amb - 10 : amb - 6.5;

            abLabel.setText(String.format("AB (Area Brazo): %.2f cm²", ab));
            agbLabel.setText(String.format("AGB (Area Grasa): %.2f cm²", agb));
            ambLabel.setText(String.format("AMB (Area Muscular): %.2f cm²", amb));
            ambCorrLabel.setText(String.format("AMB Corregido: %.2f cm²", ambCorregido));

            // 2. Masa Ósea y Residual
            String dmTxt = safeGetText(dmField);
            String drTxt = safeGetText(drField);
            String dfTxt = safeGetText(dfField);
            if (!dmTxt.isEmpty() && !drTxt.isEmpty() && !dfTxt.isEmpty()) {
                double dm = Double.parseDouble(dmTxt.trim());
                double dr = Double.parseDouble(drTxt.trim());
                double df = Double.parseDouble(dfTxt.trim());

                double masaOsea = ((dm + dr + df) * 1.2) / 10.0;
                masaOseaLabel.setText(String.format("Masa Osea: %.2f %%", masaOsea));

                double masaResidual = "Hombre".equalsIgnoreCase(sexo) ? peso * 0.24 : peso * 0.21;
                masaResidualLabel.setText(String.format("Masa Residual: %.2f kg", masaResidual));
            }

            // 3. Densidad Corporal y % Grasa (Jackson & Pollock)
            if (todosPlieguesLlenos()) {
                double pPectoral = Double.parseDouble(safeGetText(pectoralField).trim());
                double pAxilar = Double.parseDouble(safeGetText(axilarField).trim());
                double pTricep = Double.parseDouble(safeGetText(tricepField).trim());
                double pSubesc = Double.parseDouble(safeGetText(subescapularField).trim());
                double pAbdom = Double.parseDouble(safeGetText(abdominalField).trim());
                double pSupra = Double.parseDouble(safeGetText(suprailiacoField).trim());
                double pMuslo = Double.parseDouble(safeGetText(musloField).trim());

                double sumaPliegues = pPectoral + pAxilar + pTricep + pSubesc + pAbdom + pSupra + pMuslo;
                double sumaCuadrado = Math.pow(pPectoral, 2) + Math.pow(pAxilar, 2) + Math.pow(pTricep, 2) +
                                      Math.pow(pSubesc, 2) + Math.pow(pAbdom, 2) + Math.pow(pSupra, 2) + Math.pow(pMuslo, 2);

                double DC;
                if ("Hombre".equalsIgnoreCase(sexo)) {
                    DC = 1.112 - 0.0004349 * sumaPliegues + 0.000000055 * sumaCuadrado - 0.0002882 * edad;
                } else {
                    DC = 1.112 - 0.0004697 * sumaPliegues + 0.000000056 * sumaCuadrado - 0.0001282 * edad;
                }

                densidadCorporalLabel.setText(String.format("Densidad Corporal (DC): %.4f g/ml", DC));

                if (DC > 0) {
                    double porcentajeGrasa = (495 / DC) - 450;
                    if (porcentajeGrasa < 3.0) porcentajeGrasa = 3.0;
                    grasaCorporalValor.setText(String.format("%.2f %%", porcentajeGrasa));

                    double masaGrasa = peso * porcentajeGrasa / 100.0;
                    double lbm = peso - masaGrasa;
                    lbmValor.setText(String.format("%.2f kg", lbm));
                }
            }

            // 4. ICC (Cintura / Cadera) e ICT (Cintura / Talla)
            if (!safeGetText(cinturaField).isEmpty() && !safeGetText(caderaField).isEmpty()) {
                double cintura = Double.parseDouble(safeGetText(cinturaField).trim());
                double cadera = Double.parseDouble(safeGetText(caderaField).trim());

                if (cadera > 0) {
                    double icc = cintura / cadera;
                    String riesgoICC = ("Hombre".equalsIgnoreCase(sexo) && icc >= 0.90) || (!"Hombre".equalsIgnoreCase(sexo) && icc >= 0.85) ? "(Riesgo)" : "(Normal)";
                    iccValor.setText(String.format("%.2f %s", icc, riesgoICC));
                }

                if (!safeGetText(alturaField).isEmpty()) {
                    double alturaCm = Double.parseDouble(safeGetText(alturaField).trim());
                    if (alturaCm > 0) {
                        double ict = cintura / alturaCm;
                        String riesgoICT = ict >= 0.50 ? "(Riesgo)" : "(Normal)";
                        ictValor.setText(String.format("%.2f %s", ict, riesgoICT));
                    }
                }
            }

            calcularIMC();

        } catch (NumberFormatException ex) {
            mostrarError("Por favor completa los campos con valores numericos validos.");
        }
    }

    private boolean todosPlieguesLlenos() {
        return !safeGetText(pectoralField).isEmpty() &&
               !safeGetText(axilarField).isEmpty() &&
               !safeGetText(tricepField).isEmpty() &&
               !safeGetText(subescapularField).isEmpty() &&
               !safeGetText(abdominalField).isEmpty() &&
               !safeGetText(suprailiacoField).isEmpty() &&
               !safeGetText(musloField).isEmpty();
    }

    private String safeGetText(TextField field) {
        if (field == null) return "";
        String t = field.getText();
        return t == null ? "" : t;
    }

    private void mostrarInfoParametros() {
        try {
            VentanaInfoParametros.abrir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atencion");
        alert.setHeaderText("Datos requeridos");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}