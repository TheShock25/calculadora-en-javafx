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
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

public class CalculadoraSomatotipo extends Application {
    
    // Colores
    private static final Color PRIMARY_COLOR = Color.rgb(25, 118, 210);
    private static final Color SECONDARY_COLOR = Color.rgb(66, 165, 245);
    private static final Color SUCCESS_COLOR = Color.rgb(76, 175, 80);
    private static final Color ERROR_COLOR = Color.rgb(244, 67, 54);
    private static final Color WARNING_COLOR = Color.rgb(255, 152, 0);
    private static final Color BACKGROUND_COLOR = Color.rgb(250, 250, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = Color.rgb(33, 33, 33);
    private static final Color TEXT_SECONDARY = Color.rgb(117, 117, 117);
    
    // Componentes
    private TextField alturaField, pesoField;
    private TextField tricipitalField, subescapularField, supraespinalField, pantorrillaPliegueField;
    private TextField diametroHumeroField, diametroFemurField;
    private TextField perimetroBrazoField, perimetroPantorrillaField;
    
    private Label endoLabel, mesoLabel, ectoLabel, tipoLabel;
    private TextArea descripcionArea;
    
    private Canvas somatocartaCanvas;
    private Canvas tipoCorporalCanvas;
    private Canvas progressCanvas;
    
    private ResultadoSomatotipo resultadoActual;
    private boolean isTabletMode;
    
    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();
        
        primaryStage.setTitle("Calculadora de Somatotipo Heath-Carter");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #FAFAFA;");
        
        // Header
        VBox header = crearHeader();
        root.setTop(header);
        
        // TabPane central
        TabPane tabPane = crearTabs();
        root.setCenter(tabPane);
        
        // Escena
        double width = isTabletMode ? 
            Math.min(1100, ResponsiveManager.getScreenBounds().getWidth() - 50) : 1200;
        double height = isTabletMode ? 
            Math.min(900, ResponsiveManager.getScreenBounds().getHeight() - 50) : 800;
        
        Scene scene = new Scene(root, width, height);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox crearHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to right, #1976D2, #42A5F5);");
        
        double margin = ResponsiveManager.getMargin(20, 30);
        header.setPadding(new Insets(margin));
        
        Label titulo = new Label("Calculadora de Somatotipo Heath-Carter");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(24, 28)));
        titulo.setTextFill(Color.WHITE);
        
        Label subtitulo = new Label("Análisis Antropométrico Completo");
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(14, 16)));
        subtitulo.setTextFill(Color.rgb(255, 255, 255, 0.8));
        
        header.getChildren().addAll(titulo, subtitulo);
        
        return header;
    }
    
    private TabPane crearTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Tab 1: Datos
        Tab datosTab = new Tab("📊 Datos Antropométricos");
        datosTab.setContent(crearPanelDatos());
        
        // Tab 2: Resultados
        Tab resultadosTab = new Tab("📈 Resultados");
        resultadosTab.setContent(crearPanelResultados());
        
        // Tab 3: Visualización
        Tab visualTab = new Tab("🎨 Visualización");
        visualTab.setContent(crearPanelVisualizacion());
        
        tabPane.getTabs().addAll(datosTab, resultadosTab, visualTab);
        
        return tabPane;
    }
    
    private ScrollPane crearPanelDatos() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(20, 25));
        panel.setPadding(new Insets(ResponsiveManager.getMargin(20, 25)));
        
        // Inicializar campos
        alturaField = crearCampoEstilizado();
        pesoField = crearCampoEstilizado();
        tricipitalField = crearCampoEstilizado();
        subescapularField = crearCampoEstilizado();
        supraespinalField = crearCampoEstilizado();
        pantorrillaPliegueField = crearCampoEstilizado();
        diametroHumeroField = crearCampoEstilizado();
        diametroFemurField = crearCampoEstilizado();
        perimetroBrazoField = crearCampoEstilizado();
        perimetroPantorrillaField = crearCampoEstilizado();
        
        // Secciones
        VBox datosBasicos = crearSeccion("Datos Básicos", crearPanelDatosBasicos(), SUCCESS_COLOR);
        VBox pliegues = crearSeccion("Pliegues Cutáneos (mm)", crearPanelPliegues(), ERROR_COLOR);
        VBox diametros = crearSeccion("Diámetros Óseos (cm)", crearPanelDiametros(), WARNING_COLOR);
        VBox perimetros = crearSeccion("Perímetros (cm)", crearPanelPerimetros(), PRIMARY_COLOR);
        
        // Botón calcular
        Button calcularBtn = new Button("🧮 CALCULAR SOMATOTIPO");
        calcularBtn.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(16, 18)));
        calcularBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; " +
                            "-fx-background-radius: 5; -fx-padding: 15 40; -fx-cursor: hand;");
        calcularBtn.setOnAction(e -> calcularSomatotipo());
        
        VBox btnContainer = new VBox(calcularBtn);
        btnContainer.setAlignment(Pos.CENTER);
        
        panel.getChildren().addAll(datosBasicos, pliegues, diametros, perimetros, btnContainer);
        
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        
        return scroll;
    }
    
    private VBox crearSeccion(String titulo, Region contenido, Color accentColor) {
        VBox seccion = new VBox(ResponsiveManager.getSpacing(10, 15));
        seccion.setStyle("-fx-background-color: white; -fx-border-color: " + 
                        toRGBCode(accentColor) + "; -fx-border-width: 2; " +
                        "-fx-background-radius: 5; -fx-border-radius: 5;");
        
        double padding = ResponsiveManager.getMargin(15, 20);
        seccion.setPadding(new Insets(padding));
        
        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(16, 18)));
        tituloLabel.setTextFill(TEXT_PRIMARY);
        
        seccion.getChildren().addAll(tituloLabel, contenido);
        
        return seccion;
    }
    
    private GridPane crearPanelDatosBasicos() {
        GridPane grid = new GridPane();
        grid.setHgap(ResponsiveManager.getSpacing(15, 20));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        
        int cols = isTabletMode ? 1 : 2;
        
        grid.add(crearCampoConLabel("Altura (cm):", alturaField), 0, 0);
        if (!isTabletMode) grid.add(crearCampoConLabel("Peso (kg):", pesoField), 1, 0);
        else grid.add(crearCampoConLabel("Peso (kg):", pesoField), 0, 1);
        
        return grid;
    }
    
    private GridPane crearPanelPliegues() {
        GridPane grid = new GridPane();
        grid.setHgap(ResponsiveManager.getSpacing(15, 20));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        
        if (isTabletMode) {
            grid.add(crearCampoConLabel("Tricipital:", tricipitalField), 0, 0);
            grid.add(crearCampoConLabel("Subescapular:", subescapularField), 0, 1);
            grid.add(crearCampoConLabel("Supraespinal:", supraespinalField), 0, 2);
            grid.add(crearCampoConLabel("Pantorrilla:", pantorrillaPliegueField), 0, 3);
        } else {
            grid.add(crearCampoConLabel("Tricipital:", tricipitalField), 0, 0);
            grid.add(crearCampoConLabel("Subescapular:", subescapularField), 1, 0);
            grid.add(crearCampoConLabel("Supraespinal:", supraespinalField), 0, 1);
            grid.add(crearCampoConLabel("Pantorrilla:", pantorrillaPliegueField), 1, 1);
        }
        
        return grid;
    }
    
    private GridPane crearPanelDiametros() {
        GridPane grid = new GridPane();
        grid.setHgap(ResponsiveManager.getSpacing(15, 20));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        
        grid.add(crearCampoConLabel("Húmero:", diametroHumeroField), 0, 0);
        if (!isTabletMode) grid.add(crearCampoConLabel("Fémur:", diametroFemurField), 1, 0);
        else grid.add(crearCampoConLabel("Fémur:", diametroFemurField), 0, 1);
        
        return grid;
    }
    
    private GridPane crearPanelPerimetros() {
        GridPane grid = new GridPane();
        grid.setHgap(ResponsiveManager.getSpacing(15, 20));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        
        grid.add(crearCampoConLabel("Brazo flexionado:", perimetroBrazoField), 0, 0);
        if (!isTabletMode) grid.add(crearCampoConLabel("Pantorrilla:", perimetroPantorrillaField), 1, 0);
        else grid.add(crearCampoConLabel("Pantorrilla:", perimetroPantorrillaField), 0, 1);
        
        return grid;
    }
    
    private VBox crearCampoConLabel(String labelText, TextField campo) {
        VBox container = new VBox(5);
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(14, 16)));
        label.setTextFill(TEXT_SECONDARY);
        
        container.getChildren().addAll(label, campo);
        
        return container;
    }
    
    private ScrollPane crearPanelResultados() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(20, 25));
        panel.setPadding(new Insets(ResponsiveManager.getMargin(20, 25)));
        
        // Panel de valores
        VBox valoresPanel = crearPanelValores();
        
        // Panel de barras de progreso
        VBox progressPanel = crearPanelProgress();
        
        // Panel de descripción
        VBox descripcionPanel = crearPanelDescripcion();
        
        if (isTabletMode) {
            panel.getChildren().addAll(valoresPanel, progressPanel, descripcionPanel);
        } else {
            HBox topRow = new HBox(20);
            topRow.getChildren().addAll(valoresPanel, progressPanel);
            panel.getChildren().addAll(topRow, descripcionPanel);
        }
        
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        
        return scroll;
    }
    
    private VBox crearPanelValores() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(15, 20));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                      "-fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        double padding = ResponsiveManager.getMargin(20, 25);
        panel.setPadding(new Insets(padding));
        panel.setPrefWidth(ResponsiveManager.getWidth(300, 350));
        
        Label titulo = new Label("📊 Valores del Somatotipo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(16, 18)));
        titulo.setTextFill(TEXT_PRIMARY);
        
        endoLabel = crearLabelResultado("Endomorfia: -", ERROR_COLOR);
        mesoLabel = crearLabelResultado("Mesomorfia: -", SUCCESS_COLOR);
        ectoLabel = crearLabelResultado("Ectomorfia: -", PRIMARY_COLOR);
        tipoLabel = new Label("Tipo dominante: -");
        tipoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tipoLabel.setTextFill(TEXT_PRIMARY);
        tipoLabel.setWrapText(true);
        
        panel.getChildren().addAll(titulo, endoLabel, mesoLabel, ectoLabel, tipoLabel);
        
        return panel;
    }
    
    private VBox crearPanelProgress() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                      "-fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        double padding = ResponsiveManager.getMargin(20, 25);
        panel.setPadding(new Insets(padding));
        panel.setPrefWidth(ResponsiveManager.getWidth(350, 400));
        
        Label titulo = new Label("📊 Distribución Visual");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titulo.setTextFill(TEXT_PRIMARY);
        
        progressCanvas = new Canvas(350, 200);
        
        panel.getChildren().addAll(titulo, progressCanvas);
        
        return panel;
    }
    
    private VBox crearPanelDescripcion() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                      "-fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        double padding = ResponsiveManager.getMargin(15, 20);
        panel.setPadding(new Insets(padding));
        
        Label titulo = new Label("📋 Descripción y Recomendaciones");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setTextFill(TEXT_PRIMARY);
        
        descripcionArea = new TextArea();
        descripcionArea.setWrapText(true);
        descripcionArea.setEditable(false);
        descripcionArea.setPrefRowCount(10);
        descripcionArea.setStyle("-fx-font-family: Arial; -fx-font-size: 14px;");
        
        panel.getChildren().addAll(titulo, descripcionArea);
        VBox.setVgrow(descripcionArea, Priority.ALWAYS);
        
        return panel;
    }
    
    private ScrollPane crearPanelVisualizacion() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(20, 30));
        panel.setPadding(new Insets(ResponsiveManager.getMargin(20, 25)));
        panel.setAlignment(Pos.CENTER);
        
        // Somatocarta
        VBox somatocartaContainer = crearCanvasContainer("📈 Somatocarta", 450, 450);
        somatocartaCanvas = (Canvas) somatocartaContainer.getChildren().get(1);
        
        // Tipo corporal
        VBox tipoContainer = crearCanvasContainer("👤 Tipo Corporal", 400, 450);
        tipoCorporalCanvas = (Canvas) tipoContainer.getChildren().get(1);
        
        if (isTabletMode) {
            panel.getChildren().addAll(somatocartaContainer, tipoContainer);
        } else {
            HBox canvasRow = new HBox(30);
            canvasRow.setAlignment(Pos.CENTER);
            canvasRow.getChildren().addAll(somatocartaContainer, tipoContainer);
            panel.getChildren().add(canvasRow);
        }
        
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        
        return scroll;
    }
    
    private VBox crearCanvasContainer(String titulo, double width, double height) {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                          "-fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        container.setPadding(new Insets(15));
        container.setAlignment(Pos.CENTER);
        
        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tituloLabel.setTextFill(TEXT_PRIMARY);
        
        Canvas canvas = new Canvas(width, height);
        
        container.getChildren().addAll(tituloLabel, canvas);
        
        return container;
    }
    
    private TextField crearCampoEstilizado() {
        TextField field = new TextField();
        field.setPrefWidth(ResponsiveManager.getWidth(120, 150));
        field.setFont(Font.font("Arial", 14));
        field.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1; " +
                      "-fx-border-radius: 3; -fx-padding: 8 12;");
        return field;
    }
    
    private Label crearLabelResultado(String texto, Color color) {
        Label label = new Label(texto);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(16, 18)));
        label.setTextFill(color);
        return label;
    }
    
    private void calcularSomatotipo() {
        try {
            double altura = Double.parseDouble(alturaField.getText());
            double peso = Double.parseDouble(pesoField.getText());
            double tricipital = Double.parseDouble(tricipitalField.getText());
            double subescapular = Double.parseDouble(subescapularField.getText());
            double supraespinal = Double.parseDouble(supraespinalField.getText());
            double pantorrillaPliegue = Double.parseDouble(pantorrillaPliegueField.getText());
            double diametroHumero = Double.parseDouble(diametroHumeroField.getText());
            double diametroFemur = Double.parseDouble(diametroFemurField.getText());
            double perimetroBrazo = Double.parseDouble(perimetroBrazoField.getText());
            double perimetroPantorrilla = Double.parseDouble(perimetroPantorrillaField.getText());
            
            resultadoActual = Somatotipo.calcularSomatotipoCompleto(
                tricipital, subescapular, supraespinal, diametroHumero, diametroFemur,
                perimetroBrazo, perimetroPantorrilla, pantorrillaPliegue, altura, peso
            );
            
            actualizarResultados();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cálculo Completado");
            alert.setHeaderText("✅ ¡Cálculo completado!");
            alert.setContentText("Revisa las pestañas de Resultados y Visualización.");
            alert.showAndWait();
            
        } catch (NumberFormatException ex) {
            mostrarError("⚠️ Por favor, ingresa valores numéricos válidos en todos los campos.");
        } catch (Exception ex) {
            mostrarError("❌ Error en el cálculo: " + ex.getMessage());
        }
    }
    
    private void actualizarResultados() {
        if (resultadoActual == null) return;
        
        endoLabel.setText(String.format("🔴 Endomorfia: %.2f", resultadoActual.getEndomorfia()));
        mesoLabel.setText(String.format("🟢 Mesomorfia: %.2f", resultadoActual.getMesomorfia()));
        ectoLabel.setText(String.format("🔵 Ectomorfia: %.2f", resultadoActual.getEctomorfia()));
        tipoLabel.setText("🏆 Tipo dominante: " + resultadoActual.getTipoCorpoalDominante());
        
        descripcionArea.setText(resultadoActual.getDescripcion() + "\n\n" +
            obtenerRecomendacionesEjercicio(resultadoActual.getTipoCorpoalDominante()));
        
        dibujarBarrasProgreso();
        dibujarSomatocarta();
        dibujarTipoCorporal();
    }
    
    private void dibujarBarrasProgreso() {
        GraphicsContext gc = progressCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, progressCanvas.getWidth(), progressCanvas.getHeight());
        
        if (resultadoActual == null) return;
        
        double width = progressCanvas.getWidth() - 40;
        
        dibujarBarraProgreso(gc, "Endomorfia", resultadoActual.getEndomorfia(), 
                            ERROR_COLOR, 20, 50, width, 25);
        dibujarBarraProgreso(gc, "Mesomorfia", resultadoActual.getMesomorfia(), 
                            SUCCESS_COLOR, 20, 90, width, 25);
        dibujarBarraProgreso(gc, "Ectomorfia", resultadoActual.getEctomorfia(), 
                            PRIMARY_COLOR, 20, 130, width, 25);
    }
    
    private void dibujarBarraProgreso(GraphicsContext gc, String label, double value, 
                                     Color color, double x, double y, double width, double height) {
        // Fondo
        gc.setFill(Color.rgb(240, 240, 240));
        gc.fillRoundRect(x, y, width, height, 12, 12);
        
        // Barra
        double progressWidth = (width * (value / 10.0));
        gc.setFill(color);
        gc.fillRoundRect(x, y, progressWidth, height, 12, 12);
        
        // Label
        gc.setFill(TEXT_PRIMARY);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText(label, x, y - 5);
        
        // Valor
        gc.setFill(Color.WHITE);
        String valueText = String.format("%.1f", value);
        gc.fillText(valueText, x + progressWidth - 30, y + height - 8);
    }
    
    private void dibujarSomatocarta() {
        GraphicsContext gc = somatocartaCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, somatocartaCanvas.getWidth(), somatocartaCanvas.getHeight());
        
        double width = somatocartaCanvas.getWidth();
        double height = somatocartaCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double scale = Math.min(width - 100, height - 100) / 2 / 7;
        
        // Fondo
        gc.setFill(Color.rgb(250, 250, 250));
        gc.fillRect(0, 0, width, height);
        
        // Ejes
        gc.setStroke(Color.rgb(100, 100, 100));
        gc.setLineWidth(2);
        gc.strokeLine(centerX - 7 * scale, centerY, centerX + 7 * scale, centerY);
        gc.strokeLine(centerX, centerY - 7 * scale, centerX, centerY + 3 * scale);
        
        // Etiquetas
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setFill(ERROR_COLOR);
        gc.fillText("Endomorfia", centerX - 7 * scale - 40, centerY - 10);
        gc.setFill(PRIMARY_COLOR);
        gc.fillText("Ectomorfia", centerX + 6 * scale + 10, centerY - 10);
        gc.setFill(SUCCESS_COLOR);
        gc.fillText("Mesomorfia", centerX - 30, centerY - 7 * scale - 10);
        
        // Dibujar punto si hay resultado
        if (resultadoActual != null) {
            double endo = Math.max(1, Math.min(7, resultadoActual.getEndomorfia()));
            double meso = Math.max(1, Math.min(7, resultadoActual.getMesomorfia()));
            double ecto = Math.max(1, Math.min(7, resultadoActual.getEctomorfia()));
            
            int pointX = (int)(centerX + (ecto - endo) * scale);
            int pointY = (int)(centerY - (meso - 1) * scale);
            
            gc.setFill(Color.rgb(255, 193, 7));
            gc.fillOval(pointX - 5, pointY - 5, 10, 10);
            
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.fillText(String.format("%.1f-%.1f-%.1f", endo, meso, ecto), 
                       pointX + 12, pointY - 6);
        }
    }
    
    private void dibujarTipoCorporal() {
        GraphicsContext gc = tipoCorporalCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, tipoCorporalCanvas.getWidth(), tipoCorporalCanvas.getHeight());
        
        if (resultadoActual == null) return;
        
        String tipo = obtenerTipoMayor();
        
        double width = tipoCorporalCanvas.getWidth();
        double height = tipoCorporalCanvas.getHeight();
        double centerX = width / 2;
        
        // Título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(TEXT_PRIMARY);
        gc.fillText("TIPO: " + tipo, centerX - 80, 35);
        
        // Dibujar figura según tipo
        switch (tipo) {
            case "ECTOMORFO":
                dibujarEctomorfo(gc, centerX, height);
                break;
            case "MESOMORFO":
                dibujarMesomorfo(gc, centerX, height);
                break;
            case "ENDOMORFO":
                dibujarEndomorfo(gc, centerX, height);
                break;
        }
    }
    
    private void dibujarEctomorfo(GraphicsContext gc, double centerX, double height) {
        gc.setFill(PRIMARY_COLOR);
        // Cabeza
        gc.fillOval(centerX - 15, 60, 30, 35);
        // Torso estrecho
        gc.fillRoundRect(centerX - 18, 110, 36, 110, 15, 15);
        // Brazos delgados
        gc.fillRoundRect(centerX - 40, 120, 12, 75, 8, 8);
        gc.fillRoundRect(centerX + 28, 120, 12, 75, 8, 8);
        // Piernas delgadas
        gc.fillRoundRect(centerX - 12, 220, 10, 95, 8, 8);
        gc.fillRoundRect(centerX + 2, 220, 10, 95, 8, 8);
    }
    
    private void dibujarMesomorfo(GraphicsContext gc, double centerX, double height) {
        gc.setFill(SUCCESS_COLOR);
        // Cabeza
        gc.fillOval(centerX - 18, 60, 36, 35);
        // Torso en V (musculoso)
        gc.fillPolygon(
            new double[]{centerX - 32, centerX + 32, centerX + 22, centerX - 22},
            new double[]{110, 110, 210, 210},
            4
        );
        // Brazos musculosos
        gc.fillRoundRect(centerX - 50, 115, 18, 70, 10, 10);
        gc.fillRoundRect(centerX + 32, 115, 18, 70, 10, 10);
        // Piernas musculosas
        gc.fillRoundRect(centerX - 18, 210, 16, 100, 10, 10);
        gc.fillRoundRect(centerX + 2, 210, 16, 100, 10, 10);
    }
    
    private void dibujarEndomorfo(GraphicsContext gc, double centerX, double height) {
        gc.setFill(ERROR_COLOR);
        // Cabeza
        gc.fillOval(centerX - 20, 60, 40, 35);
        // Torso redondeado
        gc.fillOval(centerX - 38, 110, 76, 110);
        // Brazos gruesos
        gc.fillRoundRect(centerX - 58, 125, 20, 65, 12, 12);
        gc.fillRoundRect(centerX + 38, 125, 20, 65, 12, 12);
        // Piernas gruesas
        gc.fillRoundRect(centerX - 22, 210, 20, 100, 12, 12);
        gc.fillRoundRect(centerX + 2, 210, 20, 100, 12, 12);
    }
    
    private String obtenerTipoMayor() {
        if (resultadoActual == null) return "";
        
        double endo = resultadoActual.getEndomorfia();
        double meso = resultadoActual.getMesomorfia();
        double ecto = resultadoActual.getEctomorfia();
        
        if (endo >= meso && endo >= ecto) return "ENDOMORFO";
        if (meso >= endo && meso >= ecto) return "MESOMORFO";
        return "ECTOMORFO";
    }
    
    private String obtenerRecomendacionesEjercicio(String tipos) {
        StringBuilder sb = new StringBuilder();
        
        if (tipos.contains("ECTOMORFO")) {
            sb.append("🔵 ECTOMORFO\n");
            sb.append("🏋️ RECOMENDACIONES:\n");
            sb.append("• Entrenamiento con pesas pesadas\n");
            sb.append("• Ejercicios cortos e intensos (45-60 min)\n");
            sb.append("• Concentrarse en grupos musculares grandes\n");
            sb.append("• Cardio máximo 20 minutos, 3 veces por semana\n\n");
        }
        
        if (tipos.contains("MESOMORFO")) {
            sb.append("🟢 MESOMORFO\n");
            sb.append("🏋️ RECOMENDACIONES:\n");
            sb.append("• Entrenamiento combinado: pesas + cardio\n");
            sb.append("• Variedad en rutinas\n");
            sb.append("• Cardio moderado 3-4 veces por semana\n\n");
        }
        
        if (tipos.contains("ENDOMORFO")) {
            sb.append("🔴 ENDOMORFO\n");
            sb.append("🏋️ RECOMENDACIONES:\n");
            sb.append("• Ejercicios de cardio frecuentes\n");
            sb.append("• Entrenamiento de pesas con repeticiones altas\n");
            sb.append("• Cardio 5-6 veces por semana, 30-45 minutos\n\n");
        }
        
        return sb.toString();
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    // Clases internas para cálculos
    public static class ResultadoSomatotipo {
        private double endomorfia;
        private double mesomorfia;
        private double ectomorfia;
        private String tipoCorpoalDominante;
        private String descripcion;
        
        public ResultadoSomatotipo(double endomorfia, double mesomorfia, double ectomorfia) {
            this.endomorfia = endomorfia;
            this.mesomorfia = mesomorfia;
            this.ectomorfia = ectomorfia;
            this.tipoCorpoalDominante = determinarTipoDominante();
            this.descripcion = obtenerDescripcion();
        }
        
        private String determinarTipoDominante() {
            double max = Math.max(endomorfia, Math.max(mesomorfia, ectomorfia));
            StringBuilder tipos = new StringBuilder();
            
            if (Math.abs(endomorfia - max) <= 2) tipos.append("ENDOMORFO/");
            if (Math.abs(mesomorfia - max) <= 2) tipos.append("MESOMORFO/");
            if (Math.abs(ectomorfia - max) <= 2) tipos.append("ECTOMORFO/");
            
            if (tipos.length() > 0) tipos.setLength(tipos.length() - 1);
            
            return tipos.toString();
        }
        
        private String obtenerDescripcion() {
            StringBuilder desc = new StringBuilder();
            
            if (tipoCorpoalDominante.contains("ENDOMORFO")) {
                desc.append("🔴 ENDOMORFO - Cuerpo blando y redondo. Ganan músculo fácilmente pero también acumulan grasa.\n\n");
            }
            if (tipoCorpoalDominante.contains("MESOMORFO")) {
                desc.append("🟢 MESOMORFO - Atlético natural. Cuerpo musculado con hombros anchos y cintura estrecha.\n\n");
            }
            if (tipoCorpoalDominante.contains("ECTOMORFO")) {
                desc.append("🔵 ECTOMORFO - Altos, delgados con extremidades largas. No ganan grasa fácilmente.\n\n");
            }
            
            if (desc.length() == 0) {
                desc.append("🎯 Tipo corporal balanceado con características mixtas");
            }
            
            return desc.toString();
        }
        
        public double getEndomorfia() { return endomorfia; }
        public double getMesomorfia() { return mesomorfia; }
        public double getEctomorfia() { return ectomorfia; }
        public String getTipoCorpoalDominante() { return tipoCorpoalDominante; }
        public String getDescripcion() { return descripcion; }
    }
    
    public static class Somatotipo {
        
        public static double calcularEndomorfia(double pliegueTricipital, double pliegueSubescapular,
                double pliegueSupraespinal, double altura) {
            double X = (pliegueTricipital + pliegueSubescapular + pliegueSupraespinal) * (170.18 / altura);
            double endomorfia = -0.7182 + (0.1451 * X) - (0.00068 * Math.sqrt(X)) + (0.0000014 * Math.pow(X, 3));
            return Math.max(0.5, Math.min(7.0, endomorfia));
        }
        
        public static double calcularMesomorfia(double diametroHumero, double diametroFemur,
                double perimetroBrazo, double perimetroPantorrilla,
                double pliegueTricipital, double plieguePantorrilla,
                double altura) {
            
            double brazoCorregido = perimetroBrazo - (pliegueTricipital / 10.0);
            double pantorrillaCorregida = perimetroPantorrilla - (plieguePantorrilla / 10.0);
            
            double mesomorfia = (0.858 * diametroHumero) + (0.601 * diametroFemur) +
                    (0.188 * brazoCorregido) + (0.161 * pantorrillaCorregida) -
                    (0.131 * altura) + 4.5;
            
            return Math.max(0.5, Math.min(7.0, mesomorfia));
        }
        
        public static double calcularEctomorfia(double altura, double peso) {
            double indicePonderal = altura / Math.cbrt(peso);
            
            double ectomorfia;
            if (indicePonderal >= 40.75) {
                ectomorfia = (0.732 * indicePonderal) - 28.58;
            } else if (indicePonderal > 38.25 && indicePonderal < 40.75) {
                ectomorfia = (0.463 * indicePonderal) - 17.63;
            } else {
                ectomorfia = 0.5;
            }
            
            return Math.max(0.5, Math.min(7.0, ectomorfia));
        }
        
        public static ResultadoSomatotipo calcularSomatotipoCompleto(
                double pliegueTricipital, double pliegueSubescapular, double pliegueSupraespinal,
                double diametroHumero, double diametroFemur, double perimetroBrazo,
                double perimetroPantorrilla, double plieguePantorrilla, double altura, double peso) {
            
            double endomorfia = calcularEndomorfia(pliegueTricipital, pliegueSubescapular,
                    pliegueSupraespinal, altura);
            double mesomorfia = calcularMesomorfia(diametroHumero, diametroFemur, perimetroBrazo,
                    perimetroPantorrilla, pliegueTricipital,
                    plieguePantorrilla, altura);
            double ectomorfia = calcularEctomorfia(altura, peso);
            
            return new ResultadoSomatotipo(endomorfia, mesomorfia, ectomorfia);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}