
package calculadoraenergeticamodernaa;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class VentanaIMC extends Application {
    
    // Colores
    private static final Color PRIMARY_COLOR = Color.rgb(41, 128, 185);
    private static final Color BACKGROUND_COLOR = Color.rgb(245, 246, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = Color.rgb(44, 62, 80);
    private static final Color LIGHT_TEXT = Color.rgb(127, 140, 141);
    
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
    
    // Resultados áreas
    private Label abLabel, agbLabel, ambLabel, ambCorrLabel;
    private Label masaOseaLabel, masaResidualLabel, densidadCorporalLabel;
    
    // Datos
    private String sexo;
    private double peso;
    private Label pesoActualLabel;
    private boolean isTabletMode;
    // Campos para datos pendientes cuando la UI no está inicializada
    private Double pendingPeso = null;
    private Integer pendingEdad = null;
    private Integer pendingAltura = null;
    private boolean hasPendingDatos = false;
    
    public VentanaIMC(String sexo) {
        this.sexo = sexo;
        this.isTabletMode = ResponsiveManager.isTabletMode();
    }

    // No-arg constructor to allow Application.launch to instantiate this class
    public VentanaIMC() {
        this("Hombre");
    }
    
    public void establecerDatos(String sexo, double peso, double edad, double altura) {
        this.sexo = sexo;
        this.peso = peso;

        // Si los campos ya están creados, aplicarlos inmediatamente
        if (edadField != null && alturaField != null && pesoActualLabel != null) {
            try {
                edadField.setText(String.valueOf((int)edad));
                alturaField.setText(String.valueOf((int)altura));
                pesoActualLabel.setText(String.format("%.2f kg", peso));
            } catch (Exception ignored) {}
            calcularIMC();
            hasPendingDatos = false;
        } else {
            // Almacenar en variables pendientes para aplicar cuando la UI se cree
            pendingPeso = peso;
            pendingEdad = (int) Math.round(edad);
            pendingAltura = (int) Math.round(altura);
            hasPendingDatos = true;
        }
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("IMC y Densidad Corporal");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, " +
                     "#87CEEB, #FFB6C1);");
        
        // Header
        VBox header = crearHeader();
        root.setTop(header);
        
        // Centro con scroll
        ScrollPane centerScroll = crearPanelCentral();
        root.setCenter(centerScroll);
        
        // Footer con botón calcular
        HBox footer = crearFooter();
        root.setBottom(footer);
        
        // Escena
        double width = isTabletMode ? 
            Math.min(1000, ResponsiveManager.getScreenBounds().getWidth() - 50) : 950;
        double height = isTabletMode ? 
            Math.min(800, ResponsiveManager.getScreenBounds().getHeight() - 50) : 750;
        
        Scene scene = new Scene(root, width, height);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        configurarEventos();

        // Aplicar datos pendientes si existen
        if (hasPendingDatos) {
            try {
                if (pendingEdad != null) edadField.setText(String.valueOf(pendingEdad));
                if (pendingAltura != null) alturaField.setText(String.valueOf(pendingAltura));
                if (pendingPeso != null && pesoActualLabel != null) pesoActualLabel.setText(String.format("%.2f kg", pendingPeso));
            } catch (Exception ignored) {}
            calcularIMC();
            hasPendingDatos = false;
        }
    }
    
    private VBox crearHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to right, #2C3E50, #34495E);");
        
        double margin = ResponsiveManager.getMargin(15, 20);
        header.setPadding(new Insets(margin));
        
        tituloLabel = new Label("IMC Y DENSIDAD CORPORAL");
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(20, 24)));
        tituloLabel.setTextFill(PRIMARY_COLOR);
        
        modeloLabel = new Label("Modelo: Antropometría básica");
        modeloLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(12, 14)));
        modeloLabel.setTextFill(LIGHT_TEXT);
        
        // Botón info
        infoBtn = new Button("ℹ️ INFO PARÁMETROS");
        infoBtn.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(12, 14)));
        infoBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-cursor: hand;");
        infoBtn.setOnAction(e -> mostrarInfoParametros());
        
        header.getChildren().addAll(tituloLabel, modeloLabel, infoBtn);
        
        return header;
    }
    
    private ScrollPane crearPanelCentral() {
        HBox mainPanel = new HBox(ResponsiveManager.getSpacing(15, 20));
        mainPanel.setPadding(new Insets(ResponsiveManager.getMargin(15, 20)));
        
        // Panel izquierdo con resultados principales
        panelIzquierdo = crearPanelIzquierdo();
        
        // Panel central con formularios
        VBox centerPanel = crearFormularios();
        
        if (isTabletMode) {
            // En tablet, apilar verticalmente
            VBox tabletLayout = new VBox(15);
            tabletLayout.getChildren().addAll(panelIzquierdo, centerPanel);
            
            ScrollPane scroll = new ScrollPane(tabletLayout);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: transparent;");
            return scroll;
        } else {
            // En desktop, layout horizontal
            mainPanel.getChildren().addAll(panelIzquierdo, centerPanel);
            HBox.setHgrow(centerPanel, Priority.ALWAYS);
            
            ScrollPane scroll = new ScrollPane(mainPanel);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background-color: transparent;");
            return scroll;
        }
    }
    
    private VBox crearPanelIzquierdo() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(10, 15));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                      "-fx-border-width: 1; -fx-background-radius: 5; " +
                      "-fx-border-radius: 5;");
        
        double padding = ResponsiveManager.getMargin(15, 20);
        panel.setPadding(new Insets(padding));
        panel.setPrefWidth(ResponsiveManager.getWidth(200, 250));
        
        // IMC
        Label imcLabel = crearLabelBold("IMC:");
        imcValor = crearLabelValor("-");
        
        // Peso Ideal
        Label pesoIdealLabel = crearLabelBold("Peso Ideal:");
        pesoValor = crearLabelValor("- kg");
        
        // % Grasa Corporal
        Label grasaLabel = crearLabelBold("% Grasa Corporal:");
        grasaCorporalValor = crearLabelValor("- %");
        
        // LBM
        Label lbmLabel = crearLabelBold("Masa Corporal Magra:");
        lbmValor = crearLabelValor("- kg");
        
        // ICC
        Label iccLabel = crearLabelBold("ICC (Cintura/Cadera):");
        iccValor = crearLabelValor("-");
        
        // ICT
        Label ictLabel = crearLabelBold("ICT (Cintura/Talla):");
        ictValor = crearLabelValor("-");
        
        panel.getChildren().addAll(
            imcLabel, imcValor,
            new Separator(),
            pesoIdealLabel, pesoValor,
            new Separator(),
            grasaLabel, grasaCorporalValor,
            new Separator(),
            lbmLabel, lbmValor,
            new Separator(),
            iccLabel, iccValor,
            new Separator(),
            ictLabel, ictValor
        );
        
        return panel;
    }
    
    private VBox crearFormularios() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(15, 20));
        
        // Sección 1: Datos Básicos
        VBox datosBasicos = crearSeccion("DATOS BÁSICOS", crearPanelDatosBasicos());
        
        // Sección 2: Resultados Áreas
        VBox resultadosAreas = crearSeccion("ÁREAS CORPORALES", crearPanelResultadosAreas());
        
        // Sección 3: Masa Ósea
        VBox masaOsea = crearSeccion("MASA ÓSEA", crearPanelMasaOsea());
        
        // Sección 4: Pliegues Cutáneos
        VBox pliegues = crearSeccion("PLIEGUES CUTÁNEOS", crearPanelPliegues());
        
        // Sección 5: Circunferencias
        VBox circunferencias = crearSeccion("CIRCUNFERENCIAS CORPORALES", crearPanelCircunferencias());
        
        // Sección 6: Densidad Corporal
        VBox densidad = crearSeccion("COMPOSICIÓN CORPORAL", crearPanelDensidad());
        
        panel.getChildren().addAll(
            datosBasicos, resultadosAreas, masaOsea, 
            pliegues, circunferencias, densidad
        );
        
        return panel;
    }
    
    private VBox crearSeccion(String titulo, Region contenido) {
        VBox seccion = new VBox(ResponsiveManager.getSpacing(8, 12));
        seccion.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; " +
                        "-fx-border-width: 1; -fx-background-radius: 5; " +
                        "-fx-border-radius: 5;");
        
        double padding = ResponsiveManager.getMargin(10, 15);
        seccion.setPadding(new Insets(padding));
        
        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(14, 16)));
        tituloLabel.setTextFill(PRIMARY_COLOR);
        
        seccion.getChildren().addAll(tituloLabel, contenido);
        
        return seccion;
    }
    
    private GridPane crearPanelDatosBasicos() {
        GridPane grid = new GridPane();
        grid.setHgap(ResponsiveManager.getSpacing(10, 15));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        
        brazoField = crearCampoTexto("30");
        tricipitalField = crearCampoTexto("12");
        edadField = crearCampoTexto("25");
        alturaField = crearCampoTexto("165");
        
        pesoActualLabel = new Label("0.00 kg");
        pesoActualLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        pesoActualLabel.setTextFill(PRIMARY_COLOR);
        
        if (isTabletMode) {
            // Layout vertical
            grid.add(new Label("Brazo:"), 0, 0);
            grid.add(brazoField, 1, 0);
            grid.add(new Label("Pliegue:"), 0, 1);
            grid.add(tricipitalField, 1, 1);
            grid.add(new Label("Edad:"), 0, 2);
            grid.add(edadField, 1, 2);
            grid.add(new Label("Altura:"), 0, 3);
            grid.add(alturaField, 1, 3);
            grid.add(new Label("Peso:"), 0, 4);
            grid.add(pesoActualLabel, 1, 4);
        } else {
            // Layout en 2 columnas
            grid.add(new Label("Brazo:"), 0, 0);
            grid.add(brazoField, 1, 0);
            grid.add(new Label("Pliegue:"), 2, 0);
            grid.add(tricipitalField, 3, 0);
            
            grid.add(new Label("Edad:"), 0, 1);
            grid.add(edadField, 1, 1);
            grid.add(new Label("Altura:"), 2, 1);
            grid.add(alturaField, 3, 1);
            
            grid.add(new Label("Peso:"), 0, 2);
            grid.add(pesoActualLabel, 1, 2);
        }
        
        return grid;
    }
    
    private GridPane crearPanelResultadosAreas() {
        GridPane grid = new GridPane();
        grid.setHgap(ResponsiveManager.getSpacing(10, 15));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        
        abLabel = crearLabelResultado("AB: Pendiente");
        agbLabel = crearLabelResultado("AGB: Pendiente");
        ambLabel = crearLabelResultado("AMB: Pendiente");
        ambCorrLabel = crearLabelResultado("AMB corregido: Pendiente");
        
        int cols = isTabletMode ? 1 : 2;
        grid.add(abLabel, 0, 0);
        if (!isTabletMode) grid.add(agbLabel, 1, 0);
        else grid.add(agbLabel, 0, 1);
        
        grid.add(ambLabel, 0, isTabletMode ? 2 : 1);
        if (!isTabletMode) grid.add(ambCorrLabel, 1, 1);
        else grid.add(ambCorrLabel, 0, 3);
        
        return grid;
    }
    
    private VBox crearPanelMasaOsea() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(10, 15));
        
        // Inputs
        HBox inputsPanel = new HBox(ResponsiveManager.getSpacing(10, 15));
        inputsPanel.setAlignment(Pos.CENTER);
        
        dmField = crearCampoTexto("DM");
        drField = crearCampoTexto("DR");
        dfField = crearCampoTexto("DF");
        
        inputsPanel.getChildren().addAll(
            crearContenedorCampo("DM:", dmField),
            crearContenedorCampo("DR:", drField),
            crearContenedorCampo("DF:", dfField)
        );
        
        // Resultados
        HBox resultPanel = new HBox(ResponsiveManager.getSpacing(15, 20));
        resultPanel.setAlignment(Pos.CENTER);
        
        masaOseaLabel = crearLabelResultado("Masa Ósea: pendiente");
        masaResidualLabel = crearLabelResultado("Masa Residual: pendiente");
        
        resultPanel.getChildren().addAll(masaOseaLabel, masaResidualLabel);
        
        panel.getChildren().addAll(inputsPanel, resultPanel);
        
        return panel;
    }
    
    private GridPane crearPanelPliegues() {
        GridPane grid = new GridPane();
        grid.setHgap(ResponsiveManager.getSpacing(10, 15));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        grid.setAlignment(Pos.CENTER);
        
        pectoralField = crearCampoTexto("Pectoral");
        axilarField = crearCampoTexto("Axilar");
        tricepField = crearCampoTexto("Tríceps");
        subescapularField = crearCampoTexto("Subescapular");
        abdominalField = crearCampoTexto("Abdominal");
        suprailiacoField = crearCampoTexto("Suprailiaco");
        musloField = crearCampoTexto("Muslo");
        
        if (isTabletMode) {
            // Layout vertical en tablet
            grid.add(crearContenedorCampo("Pectoral:", pectoralField), 0, 0);
            grid.add(crearContenedorCampo("Axilar:", axilarField), 0, 1);
            grid.add(crearContenedorCampo("Tríceps:", tricepField), 0, 2);
            grid.add(crearContenedorCampo("Subescapular:", subescapularField), 0, 3);
            grid.add(crearContenedorCampo("Abdominal:", abdominalField), 0, 4);
            grid.add(crearContenedorCampo("Suprailiaco:", suprailiacoField), 0, 5);
            grid.add(crearContenedorCampo("Muslo:", musloField), 0, 6);
        } else {
            // Grid 3x3 en desktop
            grid.add(crearContenedorCampo("Pectoral:", pectoralField), 0, 0);
            grid.add(crearContenedorCampo("Axilar:", axilarField), 1, 0);
            grid.add(crearContenedorCampo("Tríceps:", tricepField), 2, 0);
            
            grid.add(crearContenedorCampo("Subescapular:", subescapularField), 0, 1);
            grid.add(crearContenedorCampo("Abdominal:", abdominalField), 1, 1);
            grid.add(crearContenedorCampo("Suprailiaco:", suprailiacoField), 2, 1);
            
            grid.add(crearContenedorCampo("Muslo:", musloField), 1, 2);
        }
        
        return grid;
    }
    
    private HBox crearPanelCircunferencias() {
        HBox panel = new HBox(ResponsiveManager.getSpacing(20, 30));
        panel.setAlignment(Pos.CENTER);
        
        cinturaField = crearCampoTexto("Cintura");
        caderaField = crearCampoTexto("Cadera");
        
        panel.getChildren().addAll(
            crearContenedorCampo("Cintura (cm):", cinturaField),
            crearContenedorCampo("Cadera (cm):", caderaField)
        );
        
        return panel;
    }
    
    private HBox crearPanelDensidad() {
        HBox panel = new HBox();
        panel.setAlignment(Pos.CENTER);
        
        densidadCorporalLabel = crearLabelResultado("Densidad Corporal: pendiente");
        panel.getChildren().add(densidadCorporalLabel);
        
        return panel;
    }
    
    private VBox crearContenedorCampo(String labelText, TextField campo) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(10, 12)));
        label.setTextFill(PRIMARY_COLOR);
        
        estilizarCampoAzul(campo);
        
        container.getChildren().addAll(label, campo);
        
        return container;
    }
    
    private HBox crearFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(ResponsiveManager.getMargin(10, 15)));
        
        calcularBtn = new Button("CALCULAR ÁREAS");
        calcularBtn.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(14, 16)));
        calcularBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: black; " +
                            "-fx-background-radius: 5; -fx-padding: 10 20; -fx-cursor: hand;");
        calcularBtn.setOnAction(e -> calcularAreasYDC());
        
        footer.getChildren().add(calcularBtn);
        
        return footer;
    }
    
    private Label crearLabelBold(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(12, 14)));
        label.setTextFill(TEXT_COLOR);
        return label;
    }
    
    private Label crearLabelValor(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(12, 14)));
        label.setTextFill(TEXT_COLOR);
        return label;
    }
    
    private Label crearLabelResultado(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(12, 14)));
        label.setTextFill(TEXT_COLOR);
        label.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 8; " +
                      "-fx-border-color: #2196F3; -fx-border-width: 2; " +
                      "-fx-border-radius: 3; -fx-background-radius: 3;");
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        
        return label;
    }
    
    private TextField crearCampoTexto(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setPrefWidth(ResponsiveManager.getWidth(70, 80));
        field.setFont(Font.font("Arial", 14));
        field.setAlignment(Pos.CENTER);
        field.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1; " +
                      "-fx-border-radius: 3; -fx-padding: 5;");
        return field;
    }
    
    private void estilizarCampoAzul(TextField field) {
        field.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2; " +
                      "-fx-border-radius: 3; -fx-background-color: #F0F8FF; " +
                      "-fx-padding: 5;");
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-border-color: #1976D2; -fx-border-width: 2; " +
                              "-fx-border-radius: 3; -fx-background-color: #E3F2FD; " +
                              "-fx-padding: 5;");
            } else {
                field.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2; " +
                              "-fx-border-radius: 3; -fx-background-color: #F0F8FF; " +
                              "-fx-padding: 5;");
            }
        });
    }
    
    private void configurarEventos() {
        // Listener para altura
        alturaField.textProperty().addListener((obs, oldVal, newVal) -> {
            calcularIMC();
        });
    }
    
    public void calcularIMC() {
        try {
            String alturaTxt = safeGetText(alturaField);
            if (peso > 0 && !alturaTxt.trim().isEmpty()) {
                double alturaCm = Double.parseDouble(alturaTxt.trim());
                double alturaM = alturaCm / 100.0;
                double imc = peso / (alturaM * alturaM);
                
                imcValor.setText(String.format("%.2f kg/m²", imc));
                
                double pesoIdeal;
                if ("Hombre".equalsIgnoreCase(sexo)) {
                    pesoIdeal = alturaCm - 100 - ((alturaCm - 150) / 4.0);
                } else {
                    pesoIdeal = alturaCm - 100 - ((alturaCm - 150) / 2.0);
                }
                pesoValor.setText(String.format("%.2f kg", pesoIdeal));
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
    
    public void calcularAreasYDC() {
        // Debug log - print field states to help diagnose NPEs seen at runtime
        try {
            System.err.println("[DEBUG] calcularAreasYDC() called. sexo=" + sexo + ", peso=" + peso + ", hasPendingDatos=" + hasPendingDatos);
            System.err.println("[DEBUG] fields: brazo='" + safeGetText(brazoField) + "', tricipital='" + safeGetText(tricipitalField) + "', edad='" + safeGetText(edadField) + "', altura='" + safeGetText(alturaField) + "'");
            System.err.println("[DEBUG] pliegues: pectoral='" + safeGetText(pectoralField) + "', axilar='" + safeGetText(axilarField) + "', tricep='" + safeGetText(tricepField) + "', subesc='" + safeGetText(subescapularField) + "', abdominal='" + safeGetText(abdominalField) + "', supra='" + safeGetText(suprailiacoField) + "', muslo='" + safeGetText(musloField) + "'");
        
            // Cálculos de áreas
            double brazo = Double.parseDouble(safeGetText(brazoField).trim());
            double pliegue = Double.parseDouble(safeGetText(tricipitalField).trim());
            double edad = Double.parseDouble(safeGetText(edadField).trim());
            double pliegueCM = pliegue * 0.10;
            
            double ab = (Math.pow(brazo, 2)) / (4 * Math.PI);
            double agb = Math.pow((brazo - (Math.PI * pliegueCM)), 2) / (4 * Math.PI);
            double amb = ab - agb;
            double ambCorregido = "Hombre".equalsIgnoreCase(sexo) ? amb - 10 : amb - 6.5;
            
            abLabel.setText(String.format("AB: %.2f cm²", ab));
            agbLabel.setText(String.format("AGB: %.2f cm²", agb));
            ambLabel.setText(String.format("AMB: %.2f cm²", amb));
            ambCorrLabel.setText(String.format("AMB corregido: %.2f cm²", ambCorregido));
            
            // Masa ósea y residual
                String dmTxt = safeGetText(dmField);
                String drTxt = safeGetText(drField);
                String dfTxt = safeGetText(dfField);
                if (!dmTxt.isEmpty() && !drTxt.isEmpty() && !dfTxt.isEmpty()) {
                    double dm = Double.parseDouble(dmTxt.trim());
                    double dr = Double.parseDouble(drTxt.trim());
                    double df = Double.parseDouble(dfTxt.trim());
                
                double masaOsea = ((dm + dr + df) * 1.2) / 10;
                masaOseaLabel.setText(String.format("Masa Ósea: %.2f %%", masaOsea));
                
                double masaResidual = "Hombre".equalsIgnoreCase(sexo) ? peso * 0.24 : peso * 0.21;
                masaResidualLabel.setText(String.format("Masa Residual: %.2f %%", masaResidual));
            }
            
            // Densidad corporal y % grasa
                if (todosPlieguesLlenos()) {
                    double sumaPliegues = Double.parseDouble(safeGetText(pectoralField).trim()) +
                        Double.parseDouble(safeGetText(axilarField).trim()) +
                        Double.parseDouble(safeGetText(tricepField).trim()) +
                        Double.parseDouble(safeGetText(subescapularField).trim()) +
                        Double.parseDouble(safeGetText(abdominalField).trim()) +
                        Double.parseDouble(safeGetText(suprailiacoField).trim()) +
                        Double.parseDouble(safeGetText(musloField).trim());
                
                    double sumaCuadrado = Math.pow(Double.parseDouble(safeGetText(pectoralField).trim()), 2) +
                        Math.pow(Double.parseDouble(safeGetText(axilarField).trim()), 2) +
                        Math.pow(Double.parseDouble(safeGetText(tricepField).trim()), 2) +
                        Math.pow(Double.parseDouble(safeGetText(subescapularField).trim()), 2) +
                        Math.pow(Double.parseDouble(safeGetText(abdominalField).trim()), 2) +
                        Math.pow(Double.parseDouble(safeGetText(suprailiacoField).trim()), 2) +
                        Math.pow(Double.parseDouble(safeGetText(musloField).trim()), 2);
                
                double DC;
                if ("Hombre".equalsIgnoreCase(sexo)) {
                    DC = 1.112 - 0.0004349 * sumaPliegues + 0.000000055 * sumaCuadrado - 0.0002882 * edad;
                } else {
                    DC = 1.112 - 0.0004697 * sumaPliegues + 0.000000056 * sumaCuadrado - 0.0001282 * edad;
                }
                
                densidadCorporalLabel.setText(String.format("Densidad Corporal: %.3f", DC));
                
                // % Grasa y LBM
                if (DC > 0) {
                    double porcentajeGrasa = (495 / DC) - 450;
                    grasaCorporalValor.setText(String.format("%.2f %%", porcentajeGrasa));
                    
                    double masaGrasa = peso * porcentajeGrasa / 100.0;
                    double lbm = peso - masaGrasa;
                    lbmValor.setText(String.format("%.2f kg", lbm));
                }
            }
            
            // ICC e ICT
            if (!safeGetText(cinturaField).isEmpty() && !safeGetText(caderaField).isEmpty()) {
                double cintura = Double.parseDouble(safeGetText(cinturaField).trim());
                double cadera = Double.parseDouble(safeGetText(caderaField).trim());

                if (cadera > 0) {
                    double icc = cintura / cadera;
                    iccValor.setText(String.format("%.2f", icc));
                }

                if (!safeGetText(alturaField).isEmpty()) {
                    double alturaCm = Double.parseDouble(safeGetText(alturaField).trim());
                    if (alturaCm > 0) {
                        double ict = cintura / alturaCm;
                        ictValor.setText(String.format("%.2f", ict));
                    }
                }
            }
            
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            mostrarError("Por favor complete todos los campos con valores numéricos válidos");
        }
        
        // Calcular IMC también
        calcularIMC();
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
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Información de Parámetros");
        info.setHeaderText("Parámetros Corporales - Valores de Referencia");
        //Division de informacion por honbre y mujer


        String contenido =

            "Masa Ósea (kg o %)*: Masa ósea estimada basada en medidas antropométricas.\n\n" +
            "Hombres: 12-15% del peso corporal"+
            "Mujeres: 10-12% del peso corporal"+
            "Bajo = riesgo de osteoporosis."+
            "\n\n" +

            "Masa Residual"+
            "(%): Porcentaje del peso corporal que corresponde a tejidos no grasos.\n\n" + 
            "Homnbres: ~24% del peso corporal"+ 
            "Mujeres: ~21% del peso corporal"+ 
            "Constante según sexo."+
            "\n\n" +

            "LBM (Masa Magra)"+
            "(kg): Peso corporal sin la masa grasa.\n\n" + 
             "Hombres: 75–85% del peso corporal"+
             "Mujeres: 65–75% del peso corporal"+ 
             "Baja = desnutrición proteico-calórica."+
            "\n\n" +

            "Circunferencia de Cintura"+
            "(cm): Indicador de grasa abdominal y riesgo cardiometabólico.\n\n" + 
             "Hombes: <90 cm = Normal\n 90–102 cm = Riesgo moderado\n >102 cm = Riesgo alto"+
             "Mujeres: <80 cm = Normal\n 80–88 cm = Riesgo moderado\n >88 cm = Riesgo alto"+
             "Predictor de riesgo cardiometabólico."+
            "\n\n" +

            "=== IMC (kg/m²) ===\n" +
            "• <18.5 = Bajo peso\n" +
            "• 18.5-24.9 = Normal\n" +
            "• 25-29.9 = Sobrepeso\n" +
            "• ≥30 = Obesidad\n\n" +
            
            "=== % Grasa Corporal ===\n" +
            "Hombres:\n" +
            "• Óptimo: 10-20%\n" +
            "• Moderado: 21-24%\n" +
            "• Alto: ≥25%\n\n" +
            "Mujeres:\n" +
            "• Óptimo: 18-28%\n" +
            "• Moderado: 29-32%\n" +
            "• Alto: ≥33%\n\n" +
            
            "=== ICC (Cintura/Cadera) ===\n" +
            "Hombres: <0.90 = Normal, ≥0.90 = Riesgo\n" +
            "Mujeres: <0.85 = Normal, ≥0.85 = Riesgo\n\n" +
            
            "=== ICT (Cintura/Talla) ===\n" +
            "• <0.5 = Normal\n" +
            "• ≥0.5 = Riesgo cardiometabólico\n\n" +

            "Masa Muscular (kg / AMB)"+
            "(cm²): Estimación de la masa muscular basada en el área muscular del brazo.\n\n" +
             "Hombres: AMB Deficit: <25 cm²\n 25–35 cm² = Normal\n >35 cm² = Alto (deportistas)"+
             "Mujeres: AMB Deficit: <20 cm²\n 20–30 cm² = Normal\n >30 cm² = Alto (deportistas)"+ 
             "Bajo = sarcopenia, riesgo funcional.\nEvalúa reservas proteicas."+
            "\n\n" +

            "Pliegue Tricipital (mm)"+
            "(mm): Medida del grosor del pliegue cutáneo en el tríceps.\n\n" + 
             "Hombres: Normal: 6–12 mm\n Moderado: 13–20 mm\n Alto: >20 mm"+ 
             "Mujeres: Normal: 12–20 mm\n Moderado: 21–30 mm\n Alto: >30 mm"+ 
             "Útil para reservas de grasa subcutánea."+

             "Z Pliegues (7 u 8 sitios)"+
            "(mm): Suma de varios pliegues cutáneos para estimar grasa corporal.\n\n" + 
             "Atletas: <50 mm\n Normal: 60–100 mm\n Alto: >100 mm"+ 
             "Atletas: <80 mm\n Normal: 90–150 mm\n Alto: >150 mm"+ 
             "Usado para % grasa con ecuaciones (Siri, Jackson & Pollock)."+
            
            "=== AMB (Área Muscular del Brazo) ===\n" +
            "Hombres: 25-35 cm² = Normal\n" +
            "Mujeres: 20-30 cm² = Normal";
        
        TextArea textArea = new TextArea(contenido);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(25);
        
        info.getDialogPane().setContent(textArea);
        info.getDialogPane().setPrefWidth(600);
        info.showAndWait();
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
                