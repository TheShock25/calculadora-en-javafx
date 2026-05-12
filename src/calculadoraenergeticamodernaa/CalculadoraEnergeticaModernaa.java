package calculadoraenergeticamodernaa;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class CalculadoraEnergeticaModernaa extends Application {
    
    // Colores del tema
    private static final Color PRIMARY_COLOR = Color.rgb(41, 128, 185);
    private static final Color SECONDARY_COLOR = Color.rgb(52, 152, 219);
    private static final Color BACKGROUND_COLOR = Color.rgb(245, 246, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = Color.rgb(44, 62, 80);
    private static final Color LIGHT_TEXT = Color.rgb(127, 140, 141);
    
    // Componentes
    private Label tituloLabel;
    private Label modeloLabel;
    private ComboBox<String> ecuacionCombo;
    private ComboBox<String> sexoCombo;
    private ComboBox<String> actividadCombo;
    private TextField pesoField, alturaField, edadField;
    private Label gebResultLabel, etaResultLabel, getResultLabel;
    private Canvas graficaCanvas;
    private Button calcularBtn, equivalentesBtn, menuPrincipalBtn;
    private VBox panelActividad;
    
    // Variables de cálculo
    private double gastoBasal = 0.0;
    private double eta = 0.0;
    private double gastoTotal = 0.0;
    
    private boolean isTabletMode;
    
    // Arrays para niveles de actividad
    private final String[] actividadHombre = {
        "Sedentaria (1.2)", "Ligero (1.4)", "Moderado (1.6)", 
        "Activo (1.75)", "Muy Activo (1.95)"
    };
    
    private final String[] actividadMujer = {
        "Sedentaria (1.2)", "Ligero (1.35)", "Moderado (1.5)", 
        "Activo (1.65)", "Muy Activo (1.8)"
    };
    
    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();
        
        // Configurar ventana
        primaryStage.setTitle("NutriEnergía Pro - Calculadora de Gasto Energético");
        
        // Crear layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F6FA;");
        
        // Header
        VBox header = crearHeader();
        root.setTop(header);
        
        // Centro
        ScrollPane centerScroll = crearPanelCentral();
        root.setCenter(centerScroll);
        
        // Footer con botones
        HBox footer = crearFooter();
        root.setBottom(footer);
        
        // Crear escena
        double width = isTabletMode ? 
            Math.min(950, ResponsiveManager.getScreenBounds().getWidth() - 50) : 800;
        double height = isTabletMode ? 
            Math.min(700, ResponsiveManager.getScreenBounds().getHeight() - 50) : 650;
        
        Scene scene = new Scene(root, width, height);
        
        // Cargar CSS con sistema robusto
        cargarCSS(scene);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        configurarEventos();
    }
    
    private void cargarCSS(Scene scene) {
    // DEBUG: Listar todos los recursos disponibles
    System.out.println("=== DEBUG: Buscando recursos en el JAR ===");
    
    // Intentar diferentes ubicaciones posibles para el CSS
    String[] posiblesRutas = {
        "/style/styles.css",      // Dentro del JAR (ESTA DEBERÍA FUNCIONAR)
        "/styles.css",            // En root del JAR  
        "style/styles.css",       // Otra variante
        "styles.css",             // En classpath directo
        "resources/style/styles.css", // Ruta alternativa
        "resources/styles.css"        // Otra alternativa
    };
    
    boolean cssCargado = false;
    
    for (String ruta : posiblesRutas) {
        try {
            // Quitar el slash inicial para algunas rutas
            String rutaLimpia = ruta.startsWith("/") ? ruta.substring(1) : ruta;
            java.net.URL cssUrl = getClass().getResource(ruta);
            
            System.out.println("Buscando CSS en: '" + ruta + "' -> URL: " + cssUrl);
            
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("✅ CSS cargado correctamente desde: " + cssUrl);
                cssCargado = true;
                
                // DEBUG adicional: ver el contenido
                try {
                    java.io.InputStream is = cssUrl.openStream();
                    System.out.println("✅ CSS encontrado, tamaño: " + is.available() + " bytes");
                    is.close();
                } catch (Exception e) {
                    System.err.println("❌ Error al leer CSS: " + e.getMessage());
                }
                break;
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cargar CSS desde '" + ruta + "': " + e.getMessage());
        }
    }
    
    if (!cssCargado) {
        System.err.println("⚠️ No se pudo cargar el archivo CSS desde el JAR.");
        System.err.println("=== VERIFICANDO ESTRUCTURA DEL JAR ===");
        
        // Listar recursos disponibles
        try {
            // Verificar estructura de recursos
            java.net.URL rootUrl = getClass().getResource("/");
            System.out.println("Root URL: " + rootUrl);
            
            // Verificar si existe style/
            java.net.URL styleUrl = getClass().getResource("/style/");
            System.out.println("Style folder URL: " + styleUrl);
            
            // Verificar si existe data/
            java.net.URL dataUrl = getClass().getResource("/data/");
            System.out.println("Data folder URL: " + dataUrl);
            
            // Listar archivos en style/
            if (styleUrl != null) {
                try {
                    java.nio.file.Path stylePath = java.nio.file.Paths.get(styleUrl.toURI());
                    java.nio.file.Files.list(stylePath).forEach(file -> 
                        System.out.println("  - " + file.getFileName()));
                } catch (Exception e) {
                    System.err.println("No se pudo listar archivos en style/: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error al verificar estructura: " + e.getMessage());
        }
        
        System.err.println("=== RUTAS BUSCADAS ===");
        for (String ruta : posiblesRutas) {
            System.err.println("  - " + ruta);
        }
        
        // Solución de emergencia: cargar CSS desde sistema de archivos
        String[] rutasExternas = {
            "resources/style/style.css",
            "../resources/style/style.css", 
            "../../resources/style/style.css"
        };
        
        for (String ruta : rutasExternas) {
            try {
                java.io.File cssFile = new java.io.File(ruta);
                if (cssFile.exists()) {
                    scene.getStylesheets().add("file:" + cssFile.getAbsolutePath());
                    System.out.println("✅ CSS cargado desde sistema de archivos: " + cssFile.getAbsolutePath());
                    cssCargado = true;
                    break;
                }
            } catch (Exception e) {
                System.err.println("❌ Error al cargar CSS externo '" + ruta + "': " + e.getMessage());
            }
        }
    }
    
    if (!cssCargado) {
        System.err.println("❌❌❌ NO SE PUDO CARGAR NINGÚN ARCHIVO CSS ❌❌❌");
    }
}
    
    private VBox crearHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #2C3E50;");
        
        double margin = ResponsiveManager.getMargin(20, 30);
        header.setPadding(new Insets(margin));
        
        // Título
        tituloLabel = new Label("CALCULADORA DE GASTO ENERGÉTICO");
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(24, 28)));
        tituloLabel.setTextFill(PRIMARY_COLOR);
        
        // Subtítulo
        modeloLabel = new Label("Ecuación de Harris-Benedict");
        modeloLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(14, 16)));
        modeloLabel.setTextFill(LIGHT_TEXT);
        
        header.getChildren().addAll(tituloLabel, modeloLabel);
        header.setSpacing(ResponsiveManager.getSpacing(5, 8));
        
        return header;
    }
    
    private ScrollPane crearPanelCentral() {
        VBox centerPanel = new VBox();
        centerPanel.setAlignment(Pos.TOP_CENTER);
        centerPanel.setSpacing(ResponsiveManager.getSpacing(15, 20));
        
        double margin = ResponsiveManager.getMargin(15, 20);
        centerPanel.setPadding(new Insets(margin));
        
        // Panel de formulario
        VBox formPanel = crearFormulario();
        
        // Panel de resultados
        HBox resultPanel = crearPanelResultados();
        
        // Gráfica
        graficaCanvas = new Canvas(700, 140);
        VBox graficaContainer = new VBox(graficaCanvas);
        graficaContainer.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1; " +
                                 "-fx-background-color: white; -fx-padding: 10;");
        
        centerPanel.getChildren().addAll(formPanel, resultPanel, graficaContainer);
        
        ScrollPane scroll = new ScrollPane(centerPanel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        
        return scroll;
    }
    
    private VBox crearFormulario() {
        VBox form = new VBox();
        form.setSpacing(ResponsiveManager.getSpacing(10, 15));
        form.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                     "-fx-border-width: 1; -fx-padding: 20;");
        
        // Combos de ecuación y sexo
        HBox row1 = new HBox(ResponsiveManager.getSpacing(15, 20));
        row1.setAlignment(Pos.CENTER_LEFT);
        
        Label ecuacionLabel = new Label("Ecuación:");
        ecuacionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        ecuacionCombo = new ComboBox<>();
        ecuacionCombo.getItems().addAll("Harris-Benedict", "Mifflin-St Jeor", "Valencia");
        ecuacionCombo.setValue("Harris-Benedict");
        ecuacionCombo.setPrefWidth(ResponsiveManager.getWidth(200, 250));
        
        Label sexoLabel = new Label("Sexo:");
        sexoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        sexoCombo = new ComboBox<>();
        sexoCombo.getItems().addAll("Hombre", "Mujer");
        sexoCombo.setValue("Hombre");
        sexoCombo.setPrefWidth(ResponsiveManager.getWidth(200, 250));
        
        row1.getChildren().addAll(ecuacionLabel, ecuacionCombo, sexoLabel, sexoCombo);
        
        // Campos de datos
        HBox row2 = new HBox(ResponsiveManager.getSpacing(15, 20));
        row2.setAlignment(Pos.CENTER_LEFT);
        
        pesoField = crearCampoTexto("70");
        alturaField = crearCampoTexto("175");
        edadField = crearCampoTexto("25");
        
        row2.getChildren().addAll(
            new Label("Peso (kg):"), pesoField,
            new Label("Altura (cm):"), alturaField,
            new Label("Edad:"), edadField
        );
        
        // Panel de actividad
        panelActividad = new VBox(5);
        HBox actividadRow = new HBox(10);
        actividadRow.setAlignment(Pos.CENTER_LEFT);
        
        Label actividadLabel = new Label("Nivel de actividad:");
        actividadLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        actividadCombo = new ComboBox<>();
        actividadCombo.getItems().addAll(actividadHombre);
        actividadCombo.setValue(actividadHombre[0]);
        actividadCombo.setPrefWidth(ResponsiveManager.getWidth(250, 300));
        
        actividadRow.getChildren().addAll(actividadLabel, actividadCombo);
        panelActividad.getChildren().add(actividadRow);
        
        form.getChildren().addAll(row1, row2, panelActividad);
        
        return form;
    }
    
    private TextField crearCampoTexto(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(ResponsiveManager.getWidth(80, 100));
        field.setFont(Font.font("Arial", 14));
        field.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1; " +
                      "-fx-padding: 5;");
        return field;
    }
    
    private HBox crearPanelResultados() {
        HBox resultPanel = new HBox(ResponsiveManager.getSpacing(10, 15));
        resultPanel.setAlignment(Pos.CENTER);
        resultPanel.setPadding(new Insets(20));
        
        gebResultLabel = crearLabelResultado("GEB: Pendiente");
        etaResultLabel = crearLabelResultado("ETA: Pendiente");
        getResultLabel = crearLabelResultado("GET: Pendiente");
        
        resultPanel.getChildren().addAll(gebResultLabel, etaResultLabel, getResultLabel);
        
        return resultPanel;
    }
    
    private Label crearLabelResultado(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(16, 18)));
        label.setTextFill(TEXT_COLOR);
        label.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 10; " +
                      "-fx-border-color: #64B5F6; -fx-border-width: 2;");
        label.setAlignment(Pos.CENTER);
        label.setPrefWidth(ResponsiveManager.getWidth(180, 220));
        
        return label;
    }
    
    private HBox crearFooter() {
        HBox footer = new HBox(ResponsiveManager.getSpacing(10, 15));
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(ResponsiveManager.getMargin(15, 20)));
        
        calcularBtn = crearBoton("CALCULAR GASTO ENERGÉTICO", PRIMARY_COLOR);
        equivalentesBtn = crearBoton("SISTEMA DE EQUIVALENTES", Color.rgb(63, 81, 181));
        menuPrincipalBtn = crearBoton("MENÚ PRINCIPAL", Color.rgb(76, 175, 80));
        
        footer.getChildren().addAll(calcularBtn, equivalentesBtn, menuPrincipalBtn);
        
        return footer;
    }
    
    private Button crearBoton(String texto, Color color) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(14, 16)));
        btn.setTextFill(Color.WHITE);
        
        String colorHex = String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
        
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 5; " +
            "-fx-padding: 10 20; -fx-cursor: hand;", colorHex));
        
        // Efecto hover
        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
            "-fx-background-color: derive(%s, -10%%); -fx-background-radius: 5; " +
            "-fx-padding: 10 20; -fx-cursor: hand;", colorHex)));
        
        btn.setOnMouseExited(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 5; " +
            "-fx-padding: 10 20; -fx-cursor: hand;", colorHex)));
        
        return btn;
    }
    
    private void configurarEventos() {
        // Cambio de ecuación
        ecuacionCombo.setOnAction(e -> {
            String seleccion = ecuacionCombo.getValue();
            modeloLabel.setText("Modelo: " + seleccion);
            
        });
        
        // Cambio de sexo
        sexoCombo.setOnAction(e -> actualizarNivelesActividad());
        
        // Botón calcular
        calcularBtn.setOnAction(e -> realizarCalculo());
        
        // Botón equivalentes
        equivalentesBtn.setOnAction(e -> {
            try {
                Stage stage = new Stage();
                new SistemaEquivalentes(gastoTotal > 0 ? gastoTotal : 2000).start(stage);
            } catch (Exception ex) {
                mostrarError("Error al abrir Sistema de Equivalentes: " + ex.getMessage());
            }
        });
        
        // Botón menú principal
        menuPrincipalBtn.setOnAction(e -> {
            try {
                Stage stage = new Stage();
                new MenuPrincipalAntropometria().start(stage);
            } catch (Exception ex) {
                mostrarError("Error al abrir Menú Principal: " + ex.getMessage());
            }
        });
    }
    
    private void actualizarNivelesActividad() {
        String sexo = sexoCombo.getValue();
        String[] niveles = "Hombre".equals(sexo) ? actividadHombre : actividadMujer;
        
        int selectedIndex = actividadCombo.getSelectionModel().getSelectedIndex();
        actividadCombo.getItems().setAll(niveles);
        
        if (selectedIndex >= 0 && selectedIndex < niveles.length) {
            actividadCombo.setValue(niveles[selectedIndex]);
        }
    }
    
    private void realizarCalculo() {
        try {
            double peso = Double.parseDouble(pesoField.getText().trim());
            double altura = Double.parseDouble(alturaField.getText().trim());
            int edad = Integer.parseInt(edadField.getText().trim());
            
            if (peso <= 0 || altura <= 0 || edad <= 0) {
                mostrarError("Por favor ingresa valores positivos");
                return;
            }
            
            String sexo = sexoCombo.getValue();
            String ecuacion = ecuacionCombo.getValue();
            
            calcularGEB(ecuacion, sexo, peso, altura, edad);
            eta = gastoBasal * 0.10;
            double factor = obtenerFactorActividad();
            gastoTotal = (gastoBasal + eta) * factor;
            
            actualizarResultados();
            dibujarGrafica();
            
        } catch (NumberFormatException ex) {
            mostrarError("Por favor ingresa valores numéricos válidos");
        }
    }
    
    private void calcularGEB(String ecuacion, String sexo, double peso, double altura, int edad) {
        switch (ecuacion) {
            case "Harris-Benedict":
                if ("Hombre".equals(sexo)) {
                    gastoBasal = 66.4730 + (13.7516 * peso) + (5.0033 * altura) - (6.7559 * edad);
                } else {
                    gastoBasal = 655.0955 + (9.5634 * peso) + (1.8496 * altura) - (4.6756 * edad);
                }
                break;
                
            case "Mifflin-St Jeor":
                if ("Hombre".equals(sexo)) {
                    gastoBasal = (10 * peso) + (6.25 * altura) - (5 * edad) + 5;
                } else {
                    gastoBasal = (10 * peso) + (6.25 * altura) - (5 * edad) - 161;
                }
                break;
                
            case "Valencia":
                calcularGEBValencia(sexo, peso, Math.max(18, edad));
                break;
        }
    }
    
    private void calcularGEBValencia(String sexo, double peso, int edad) {
        if ("Hombre".equals(sexo)) {
            if (edad <= 30) {
                gastoBasal = 13.37 * peso + 747;
            } else if (edad < 60) {
                gastoBasal = 13.08 * peso + 693;
            } else {
                gastoBasal = 14.21 * peso + 429;
            }
        } else {
            if (edad <= 30) {
                gastoBasal = 11.02 * peso + 679;
            } else if (edad < 60) {
                gastoBasal = 10.92 * peso + 677;
            } else {
                gastoBasal = 10.98 * peso + 520;
            }
        }
    }
    
    private double obtenerFactorActividad() {
        String nivel = actividadCombo.getValue();
        if (nivel == null) return 1.0;
        
        int inicio = nivel.indexOf('(');
        int fin = nivel.indexOf(')');
        
        if (inicio != -1 && fin != -1 && fin > inicio) {
            try {
                String factorStr = nivel.substring(inicio + 1, fin);
                return Double.parseDouble(factorStr);
            } catch (NumberFormatException e) {
                return 1.0;
            }
        }
        
        return 1.0;
    }
    
    private void actualizarResultados() {
        gebResultLabel.setText(String.format("GEB: %.0f kcal/día", gastoBasal));
        etaResultLabel.setText(String.format("ETA: %.0f kcal/día", eta));
        getResultLabel.setText(String.format("GET: %.0f kcal/día", gastoTotal));
    }
    
    private void dibujarGrafica() {
        GraphicsContext gc = graficaCanvas.getGraphicsContext2D();
        
        // Limpiar canvas
        gc.clearRect(0, 0, graficaCanvas.getWidth(), graficaCanvas.getHeight());
        
        if (gastoTotal <= 0) return;
        
        double width = graficaCanvas.getWidth() - 40;
        double height = 30;
        double x = 20;
        double y = 55;
        
        // Fondo
        gc.setFill(Color.rgb(236, 240, 241));
        gc.fillRoundRect(x, y, width, height, 15, 15);
        
        // Barra GET
        double getWidth = Math.min(gastoTotal / gastoTotal * width, width);
        gc.setFill(Color.rgb(100, 200, 100));
        gc.fillRoundRect(x, y, getWidth, height, 15, 15);
        
        // Texto
        gc.setFill(TEXT_COLOR);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        String label = String.format("GET: %.0f kcal", gastoTotal);
        gc.fillText(label, x + width - 100, y + 20);
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    public static void launchDirectly() {
        // Lanzar en el hilo actual
        launch(CalculadoraEnergeticaModernaa.class);
    }
    
    // Mantener el main original como backup
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            // Silencio en caso de error
        }
    }
}