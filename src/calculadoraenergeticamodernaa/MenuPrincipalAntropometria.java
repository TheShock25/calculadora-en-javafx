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
import javafx.scene.effect.DropShadow;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuPrincipalAntropometria extends Application {
    
    // Colores
    private static final Color PRIMARY_COLOR = Color.rgb(25, 118, 210);
    private static final Color SECONDARY_COLOR = Color.rgb(66, 165, 245);
    private static final Color ACCENT_COLOR = Color.rgb(255, 193, 7);
    private static final Color SUCCESS_COLOR = Color.rgb(76, 175, 80);
    private static final Color BACKGROUND_COLOR = Color.rgb(250, 250, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = Color.rgb(33, 33, 33);
    private static final Color TEXT_SECONDARY = Color.rgb(117, 117, 117);
    
    // Datos del usuario
    private String sexoSeleccionado = "Hombre";
    private double pesoUsuario = 70.0;
    private double edadUsuario = 25.0;
    private double alturaUsuario = 170.0;
    
    // Componentes
    private TextField pesoField, edadField, alturaField;
    private ComboBox<String> sexoCombo;
    private boolean isTabletMode;
    
    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();
        
        primaryStage.setTitle("Sistema Antropométrico Completo");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #FAFAFA;");
        
        // Header
        VBox header = crearHeader();
        root.setTop(header);
        
        // Centro
        ScrollPane centerScroll = crearPanelCentral();
        root.setCenter(centerScroll);
        
        // Footer
        HBox footer = crearFooter();
        root.setBottom(footer);
        
        // Escena
        double width = isTabletMode ? 
            Math.min(1100, ResponsiveManager.getScreenBounds().getWidth() - 50) : 1000;
        double height = isTabletMode ? 
            Math.min(800, ResponsiveManager.getScreenBounds().getHeight() - 50) : 700;
        
        Scene scene = new Scene(root, width, height);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        configurarEventos();
    }
    
    private VBox crearHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to right, #1976D2, #42A5F5);");
        
        double margin = ResponsiveManager.getMargin(30, 40);
        header.setPadding(new Insets(margin));
        header.setSpacing(10);
        
        Label titulo = new Label("Sistema Antropométrico Completo");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(28, 32)));
        titulo.setTextFill(Color.WHITE);
        
        Label subtitulo = new Label("Análisis de Composición Corporal y Somatotipo");
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(16, 18)));
        subtitulo.setTextFill(Color.rgb(255, 255, 255, 0.8));
        
        header.getChildren().addAll(titulo, subtitulo);
        
        return header;
    }
    
    private ScrollPane crearPanelCentral() {
        VBox centerPanel = new VBox(ResponsiveManager.getSpacing(30, 40));
        centerPanel.setAlignment(Pos.TOP_CENTER);
        
        double margin = ResponsiveManager.getMargin(30, 40);
        centerPanel.setPadding(new Insets(margin));
        
        // Panel de datos básicos
        VBox datosPanel = crearPanelDatosBasicos();
        
        // Panel de opciones (calculadoras)
        VBox opcionesPanel = crearPanelOpciones();
        
        centerPanel.getChildren().addAll(datosPanel, opcionesPanel);
        
        ScrollPane scroll = new ScrollPane(centerPanel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        
        return scroll;
    }
    
    private VBox crearPanelDatosBasicos() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(15, 20));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                      "-fx-border-width: 1; -fx-background-radius: 5; " +
                      "-fx-border-radius: 5;");
        
        double padding = ResponsiveManager.getMargin(20, 25);
        panel.setPadding(new Insets(padding));
        
        // Título
        Label tituloSeccion = new Label("Datos Básicos del Usuario");
        tituloSeccion.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(18, 20)));
        tituloSeccion.setTextFill(TEXT_PRIMARY);
        
        // Grid de campos
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(ResponsiveManager.getSpacing(15, 20));
        grid.setVgap(ResponsiveManager.getSpacing(10, 15));
        
        // Sexo
        Label sexoLabel = crearLabel("Sexo:");
        sexoCombo = new ComboBox<>();
        sexoCombo.getItems().addAll("Hombre", "Mujer");
        sexoCombo.setValue("Hombre");
        sexoCombo.setPrefWidth(ResponsiveManager.getWidth(120, 150));
        estilizarCombo(sexoCombo);
        
        // Peso
        Label pesoLabel = crearLabel("Peso (kg):");
        pesoField = crearCampoTexto("70.0");
        
        // Edad
        Label edadLabel = crearLabel("Edad (años):");
        edadField = crearCampoTexto("25");
        
        // Altura
        Label alturaLabel = crearLabel("Altura (cm):");
        alturaField = crearCampoTexto("170");
        
        if (isTabletMode) {
            // Layout vertical en tablet
            grid.add(sexoLabel, 0, 0);
            grid.add(sexoCombo, 1, 0);
            grid.add(pesoLabel, 0, 1);
            grid.add(pesoField, 1, 1);
            grid.add(edadLabel, 0, 2);
            grid.add(edadField, 1, 2);
            grid.add(alturaLabel, 0, 3);
            grid.add(alturaField, 1, 3);
        } else {
            // Layout en 2 columnas en desktop
            grid.add(sexoLabel, 0, 0);
            grid.add(sexoCombo, 1, 0);
            grid.add(edadLabel, 2, 0);
            grid.add(edadField, 3, 0);
            
            grid.add(pesoLabel, 0, 1);
            grid.add(pesoField, 1, 1);
            grid.add(alturaLabel, 2, 1);
            grid.add(alturaField, 3, 1);
        }
        
        panel.getChildren().addAll(tituloSeccion, grid);
        
        return panel;
    }
    
    private VBox crearPanelOpciones() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(20, 30));
        panel.setAlignment(Pos.CENTER);
        
        // Tarjetas de calculadoras
        if (isTabletMode) {
            // En tablet: apilar verticalmente
            VBox somatotipoCard = crearTarjetaCalculadora(
                "Calculadora de Somatotipo",
                "Análisis Heath-Carter completo",
                "• Determina tu tipo corporal (Endo/Meso/Ecto)\n" +
                "• Visualización gráfica en somatocarta\n" +
                "• Recomendaciones de ejercicio personalizadas\n" +
                "• Representación visual del tipo corporal",
                SUCCESS_COLOR,
                e -> abrirSomatotipo()
            );
            
            VBox imcCard = crearTarjetaCalculadora(
                "IMC y Composición Corporal",
                "Análisis antropométrico detallado",
                "• Cálculo de IMC y peso ideal\n" +
                "• Densidad y porcentaje de grasa corporal\n" +
                "• Áreas musculares y masa ósea\n" +
                "• Índices cintura-cadera y cintura-talla",
                Color.rgb(255, 152, 0),
                e -> abrirIMC()
            );
            
            panel.getChildren().addAll(somatotipoCard, imcCard);
        } else {
            // En desktop: lado a lado
            HBox cardsRow = new HBox(30);
            cardsRow.setAlignment(Pos.CENTER);
            
            VBox somatotipoCard = crearTarjetaCalculadora(
                "Calculadora de Somatotipo",
                "Análisis Heath-Carter completo",
                "• Determina tu tipo corporal (Endo/Meso/Ecto)\n" +
                "• Visualización gráfica en somatocarta\n" +
                "• Recomendaciones de ejercicio personalizadas\n" +
                "• Representación visual del tipo corporal",
                SUCCESS_COLOR,
                e -> abrirSomatotipo()
            );
            
            VBox imcCard = crearTarjetaCalculadora(
                "IMC y Composición Corporal",
                "Análisis antropométrico detallado",
                "• Cálculo de IMC y peso ideal\n" +
                "• Densidad y porcentaje de grasa corporal\n" +
                "• Áreas musculares y masa ósea\n" +
                "• Índices cintura-cadera y cintura-talla",
                Color.rgb(255, 152, 0),
                e -> abrirIMC()
            );
            
            cardsRow.getChildren().addAll(somatotipoCard, imcCard);
            panel.getChildren().add(cardsRow);
        }
        
        return panel;
    }
    
    private VBox crearTarjetaCalculadora(String titulo, String subtitulo, String descripcion,
                                        Color accentColor, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(ResponsiveManager.getSpacing(10, 15));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-border-radius: 10;");
        
        double padding = ResponsiveManager.getMargin(20, 25);
        card.setPadding(new Insets(padding));
        
        double width = ResponsiveManager.getWidth(400, 500);
        card.setPrefWidth(width);
        card.setMaxWidth(width);
        
        // Efecto sombra
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(10);
        card.setEffect(shadow);
        
        // Barra superior de color
        Region topBar = new Region();
        topBar.setPrefHeight(8);
        topBar.setStyle(String.format("-fx-background-color: %s; " +
                                     "-fx-background-radius: 10 10 0 0;", toRGBCode(accentColor)));
        
        // Título
        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(18, 20)));
        tituloLabel.setTextFill(TEXT_PRIMARY);
        tituloLabel.setWrapText(true);
        
        // Subtítulo
        Label subtituloLabel = new Label(subtitulo);
        subtituloLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(12, 14)));
        subtituloLabel.setTextFill(TEXT_SECONDARY);
        subtituloLabel.setWrapText(true);
        
        // Descripción
        Label descripcionLabel = new Label(descripcion);
        descripcionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(13, 15)));
        descripcionLabel.setTextFill(TEXT_SECONDARY);
        descripcionLabel.setWrapText(true);
        
        double descPadding = ResponsiveManager.getMargin(15, 20);
        VBox.setMargin(descripcionLabel, new Insets(descPadding, 0, descPadding, 0));
        
        // Botón
        Button button = new Button("Abrir Calculadora");
        button.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(14, 16)));
        button.setTextFill(Color.WHITE);
        
        String colorHex = toRGBCode(accentColor);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 5; " +
            "-fx-padding: 12 24; -fx-cursor: hand;", colorHex));
        
        button.setOnAction(action);
        
        // Efecto hover
        button.setOnMouseEntered(e -> button.setStyle(String.format(
            "-fx-background-color: derive(%s, -10%%); -fx-background-radius: 5; " +
            "-fx-padding: 12 24; -fx-cursor: hand;", colorHex)));
        
        button.setOnMouseExited(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 5; " +
            "-fx-padding: 12 24; -fx-cursor: hand;", colorHex)));
        
        card.getChildren().addAll(topBar, tituloLabel, subtituloLabel, 
                                  descripcionLabel, button);
        
        // Efecto hover en la tarjeta
        card.setOnMouseEntered(e -> {
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.rgb(0, 0, 0, 0.3));
            hoverShadow.setOffsetX(4);
            hoverShadow.setOffsetY(4);
            hoverShadow.setRadius(15);
            card.setEffect(hoverShadow);
        });
        
        card.setOnMouseExited(e -> card.setEffect(shadow));
        
        return card;
    }
    
    private HBox crearFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #F0F0F0;");
        
        double margin = ResponsiveManager.getMargin(15, 20);
        footer.setPadding(new Insets(margin));
        
        Label footerLabel = new Label("Sistema Antropométrico Integrado - Versión 1.0");
        footerLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(12, 14)));
        footerLabel.setTextFill(TEXT_SECONDARY);
        
        footer.getChildren().add(footerLabel);
        
        return footer;
    }
    
    private Label crearLabel(String texto) {
        Label label = new Label(texto);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(14, 16)));
        label.setTextFill(TEXT_PRIMARY);
        return label;
    }
    
    private TextField crearCampoTexto(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(ResponsiveManager.getWidth(80, 100));
        field.setFont(Font.font("Arial", 14));
        field.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1; " +
                      "-fx-border-radius: 3; -fx-padding: 5;");
        return field;
    }
    
    private void estilizarCombo(ComboBox<String> combo) {
        combo.setStyle("-fx-border-color: #BDBDBD; -fx-border-width: 1; " +
                      "-fx-border-radius: 3; -fx-background-radius: 3;");
    }
    
    private void configurarEventos() {
        // Listeners para actualizar datos
        pesoField.textProperty().addListener((obs, oldVal, newVal) -> actualizarDatos());
        edadField.textProperty().addListener((obs, oldVal, newVal) -> actualizarDatos());
        alturaField.textProperty().addListener((obs, oldVal, newVal) -> actualizarDatos());
        sexoCombo.setOnAction(e -> actualizarDatos());
    }
    
    private void actualizarDatos() {
        try {
            pesoUsuario = Double.parseDouble(pesoField.getText());
            edadUsuario = Double.parseDouble(edadField.getText());
            alturaUsuario = Double.parseDouble(alturaField.getText());
            sexoSeleccionado = sexoCombo.getValue();
        } catch (NumberFormatException ex) {
            // Ignorar errores durante la escritura
        }
    }
    
    private void abrirSomatotipo() {
        actualizarDatos();
        
        if (!validarDatos()) {
            return;
        }
        
        try {
            Stage stage = new Stage();
            CalculadoraSomatotipo somatotipo = new CalculadoraSomatotipo();
            somatotipo.start(stage);
            
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Calculadora de Somatotipo");
            info.setHeaderText("Se ha abierto la Calculadora de Somatotipo");
            info.setContentText(String.format(
                "Datos básicos:\n" +
                "• Sexo: %s\n" +
                "• Peso: %.1f kg\n" +
                "• Edad: %.0f años\n" +
                "• Altura: %.0f cm\n\n" +
                "Ingresa las medidas antropométricas específicas para obtener tu somatotipo.",
                sexoSeleccionado, pesoUsuario, edadUsuario, alturaUsuario));
            info.showAndWait();
        } catch (Exception ex) {
            mostrarError("Error al abrir Somatotipo: " + ex.getMessage());
        }
    }
    
    private void abrirIMC() {
        actualizarDatos();
        
        if (!validarDatos()) {
            return;
        }
        
        try {
            Stage stage = new Stage();
            VentanaIMC ventanaIMC = new VentanaIMC(sexoSeleccionado);
            ventanaIMC.establecerDatos(sexoSeleccionado, pesoUsuario, edadUsuario, alturaUsuario);
            ventanaIMC.start(stage);
            
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("IMC y Composición Corporal");
            info.setHeaderText("Se ha abierto la Calculadora de IMC");
            info.setContentText(String.format(
                "Los datos básicos han sido transferidos:\n" +
                "• Sexo: %s\n" +
                "• Peso: %.1f kg\n" +
                "• Edad: %.0f años\n" +
                "• Altura: %.0f cm\n\n" +
                "El IMC se calculará automáticamente.",
                sexoSeleccionado, pesoUsuario, edadUsuario, alturaUsuario));
            info.showAndWait();
        } catch (Exception ex) {
            mostrarError("Error al abrir IMC: " + ex.getMessage());
        }
    }
    
    private boolean validarDatos() {
        if (pesoUsuario <= 0 || pesoUsuario > 500) {
            mostrarError("Por favor, ingresa un peso válido (1-500 kg)");
            return false;
        }
        
        if (edadUsuario <= 0 || edadUsuario > 120) {
            mostrarError("Por favor, ingresa una edad válida (1-120 años)");
            return false;
        }
        
        if (alturaUsuario <= 0 || alturaUsuario > 300) {
            mostrarError("Por favor, ingresa una altura válida (1-300 cm)");
            return false;
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Datos Incompletos");
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
    
    public static void main(String[] args) {
            launch(args);
        }
    }