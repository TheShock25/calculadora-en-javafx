package calculadoraenergeticamodernaa;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuPrincipalAntropometria extends Application {

    // Paleta de colores oficial: Verdes y Blancos
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color ACCENT_GREEN = Color.web("#43A047");        // Verde acción
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Texto oscuro
    private static final Color TEXT_MUTED = Color.web("#5C7669");          // Texto secundario

    // Control de sincronización
    private boolean actualizandoDesdeCalculadora = false;

    // Datos del usuario
    private String sexoSeleccionado = "Hombre";
    private double pesoUsuario = 70.0;
    private double edadUsuario = 25.0;
    private double alturaUsuario = 175.0;

    // Componentes de entrada
    private TextField pesoField, edadField, alturaField;
    private ComboBox<String> sexoCombo;
    private boolean isTabletMode;

    private CalculadoraEnergeticaModernaa calculadora;

    public MenuPrincipalAntropometria() {
    }

    public MenuPrincipalAntropometria(CalculadoraEnergeticaModernaa calculadora) {
        this.calculadora = calculadora;
    }

    public void setCalculadora(CalculadoraEnergeticaModernaa calculadora) {
        this.calculadora = calculadora;
    }

    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();

        primaryStage.setTitle("NutriEnergia Pro - Menu Principal Antropometria");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // Header moderno
        VBox header = crearHeader();
        root.setTop(header);

        // Panel central centrado con ScrollPane
        ScrollPane centerScroll = crearPanelCentral();
        root.setCenter(centerScroll);

        // Footer limpio
        HBox footer = crearFooter();
        root.setBottom(footer);

        double width = isTabletMode ? 
            Math.min(1050, ResponsiveManager.getScreenBounds().getWidth() - 50) : 960;
        double height = isTabletMode ? 
            Math.min(850, ResponsiveManager.getScreenBounds().getHeight() - 50) : 740;

        Scene scene = new Scene(root, width, height);
        cargarCSS(scene);

        primaryStage.setScene(scene);
        primaryStage.show();

        configurarEventos();
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

        double margin = ResponsiveManager.getMargin(18, 22);
        header.setPadding(new Insets(margin, margin, margin + 4, margin));

        Label badge = new Label("MODULO ANTROPOMETRICO INTEGRADO");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.18); " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 3 10;"
        );

        Label titulo = new Label("Sistema Antropometrico Completo");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(22, 26)));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Evaluacion de Somatotipo, Composicion Corporal e IMC");
        subtitulo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(13, 14)));
        subtitulo.setTextFill(Color.web("#C8E6C9"));

        header.getChildren().addAll(badge, titulo, subtitulo);
        header.setEffect(new DropShadow(10, 0, 3, Color.rgb(0, 0, 0, 0.15)));

        return header;
    }

    private ScrollPane crearPanelCentral() {
        VBox centerPanel = new VBox(20);
        centerPanel.setAlignment(Pos.TOP_CENTER);
        centerPanel.setPadding(new Insets(20));

        // 1. Tarjeta de datos básicos del usuario (Centrada)
        VBox datosPanel = crearPanelDatosBasicos();

        // 2. Tarjetas de calculadoras disponibles (Centradas)
        VBox opcionesPanel = crearPanelOpciones();

        centerPanel.getChildren().addAll(datosPanel, opcionesPanel);

        ScrollPane scroll = new ScrollPane(centerPanel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearPanelDatosBasicos() {
        VBox formCard = new VBox(16);
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(860);
        formCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 14; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 20 26;"
        );
        formCard.setEffect(new DropShadow(12, 0, 3, Color.rgb(0, 0, 0, 0.05)));

        // Título de la sección
        HBox headerForm = new HBox();
        headerForm.setAlignment(Pos.CENTER);
        Label seccionTitulo = new Label("DATOS GENERALES DEL PACIENTE");
        seccionTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13.5));
        seccionTitulo.setTextFill(PRIMARY_GREEN);
        seccionTitulo.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 6; -fx-padding: 4 12;");
        headerForm.getChildren().add(seccionTitulo);

        // Fila de campos centrados
        HBox rowInputs = new HBox(20);
        rowInputs.setAlignment(Pos.CENTER);

        // Sexo
        VBox boxSexo = new VBox(5);
        boxSexo.setAlignment(Pos.CENTER_LEFT);
        Label sexoLabel = new Label("SEXO BIOLOGICO");
        sexoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        sexoLabel.setTextFill(TEXT_MUTED);

        sexoCombo = new ComboBox<>();
        sexoCombo.getItems().addAll("Hombre", "Mujer");
        sexoCombo.setValue("Hombre");
        sexoCombo.setPrefWidth(140);
        estilizarCombo(sexoCombo);
        boxSexo.getChildren().addAll(sexoLabel, sexoCombo);

        // Peso
        pesoField = crearCampoTexto("70.0");
        VBox boxPeso = crearBloqueCampo("PESO", pesoField, "kg");

        // Edad
        edadField = crearCampoTexto("25");
        VBox boxEdad = crearBloqueCampo("EDAD", edadField, "anos");

        // Altura
        alturaField = crearCampoTexto("175.0");
        VBox boxAltura = crearBloqueCampo("ALTURA", alturaField, "cm");

        rowInputs.getChildren().addAll(boxSexo, boxPeso, boxEdad, boxAltura);

        formCard.getChildren().addAll(headerForm, rowInputs);

        return formCard;
    }

    private VBox crearBloqueCampo(String etiqueta, TextField field, String unidad) {
        VBox block = new VBox(5);
        block.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(etiqueta);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(TEXT_MUTED);

        HBox inputWithUnit = new HBox(6);
        inputWithUnit.setAlignment(Pos.CENTER_LEFT);

        Label unitLabel = new Label(unidad);
        unitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        unitLabel.setTextFill(PRIMARY_GREEN);
        unitLabel.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 6 9; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6;"
        );

        inputWithUnit.getChildren().addAll(field, unitLabel);
        block.getChildren().addAll(label, inputWithUnit);

        return block;
    }

    private void estilizarCombo(ComboBox<String> combo) {
        combo.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 4 8; " +
            "-fx-font-size: 13px;"
        );
    }

    private TextField crearCampoTexto(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(80);
        field.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        field.setAlignment(Pos.CENTER);
        field.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 6 10;"
        );

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #2E7D32; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 6 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(46, 125, 50, 0.25), 6, 0, 0, 1);"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: #FAFCFA; " +
                    "-fx-border-color: #C8E6C9; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 6 10;"
                );
            }
        });

        return field;
    }

    private VBox crearPanelOpciones() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);

        HBox cardsRow = new HBox(24);
        cardsRow.setAlignment(Pos.CENTER);
        cardsRow.setMaxWidth(860);

        // Tarjeta 1: Somatotipo Heath-Carter
        VBox somatotipoCard = crearTarjetaModulo(
            "Calculadora de Somatotipo",
            "Analisis morfologico de Heath-Carter",
            new String[]{
                "Determina el tipo corporal dominante (Endo / Meso / Ecto)",
                "Graficacion interactiva en Somatocarta (X, Y)",
                "Recomendaciones deportivas y de acondicionamiento",
                "Representacion visual del arquetipo morfologico"
            },
            PRIMARY_GREEN,
            "ABRIR SOMATOTIPO",
            e -> abrirSomatotipo()
        );

        // Tarjeta 2: IMC y Composición Corporal
        VBox imcCard = crearTarjetaModulo(
            "IMC y Composicion Corporal",
            "Evaluacion antropometrica y densidad corporal",
            new String[]{
                "Indice de Masa Corporal (IMC) y clasificacion OMS",
                "Densidad corporal y porcentaje de grasa (Siri / Brozek)",
                "Areas muscular y grasa de brazo con masa osea",
                "Indices cintura-cadera y cintura-talla con riesgo"
            },
            ACCENT_GREEN,
            "ABRIR IMC Y COMPOSICION",
            e -> abrirIMC()
        );

        HBox.setHgrow(somatotipoCard, Priority.ALWAYS);
        HBox.setHgrow(imcCard, Priority.ALWAYS);

        cardsRow.getChildren().addAll(somatotipoCard, imcCard);
        panel.getChildren().add(cardsRow);

        return panel;
    }

    private VBox crearTarjetaModulo(String titulo, String subtitulo, String[] caracteristicas,
                                   Color accentColor, String btnText,
                                   javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 14; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 22 24;"
        );
        card.setEffect(new DropShadow(12, 0, 3, Color.rgb(0, 0, 0, 0.05)));

        // Barra decorativa superior
        Region topBar = new Region();
        topBar.setPrefHeight(5);
        String hexColor = toRGBCode(accentColor);
        topBar.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 4;", hexColor));

        // Título del módulo
        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16.5));
        tituloLabel.setTextFill(TEXT_DARK);

        // Subtítulo
        Label subtituloLabel = new Label(subtitulo);
        subtituloLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        subtituloLabel.setTextFill(TEXT_MUTED);

        // Lista de características
        VBox featureList = new VBox(8);
        featureList.setPadding(new Insets(6, 0, 8, 0));

        for (String feat : caracteristicas) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.CENTER_LEFT);

            Label bullet = new Label("•");
            bullet.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            bullet.setTextFill(accentColor);

            Label text = new Label(feat);
            text.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12.5));
            text.setTextFill(TEXT_DARK);
            text.setWrapText(true);

            item.getChildren().addAll(bullet, text);
            featureList.getChildren().add(item);
        }

        // Botón de acción
        Button button = new Button(btnText);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        button.setTextFill(Color.WHITE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 20; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);", hexColor));

        button.setOnMouseEntered(e -> button.setStyle(String.format(
            "-fx-background-color: derive(%s, -12%%); " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 20; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 6, 0, 0, 3);", hexColor)));

        button.setOnMouseExited(e -> button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 20; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 2);", hexColor)));

        button.setOnAction(action);

        card.getChildren().addAll(topBar, tituloLabel, subtituloLabel, featureList, button);

        // Hover suave en toda la tarjeta
        card.setOnMouseEntered(e -> {
            card.setEffect(new DropShadow(16, 0, 4, Color.rgb(0, 0, 0, 0.1)));
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: " + hexColor + "; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 14; " +
                "-fx-background-radius: 14; " +
                "-fx-padding: 22 24;"
            );
        });

        card.setOnMouseExited(e -> {
            card.setEffect(new DropShadow(12, 0, 3, Color.rgb(0, 0, 0, 0.05)));
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #E2E8E4; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 14; " +
                "-fx-background-radius: 14; " +
                "-fx-padding: 22 24;"
            );
        });

        return card;
    }

    private HBox crearFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E8E3; -fx-border-width: 1.5 0 0 0;");
        footer.setPadding(new Insets(12, 20, 14, 20));

        Label footerLabel = new Label("NutriEnergia Pro - Sistema Antropometrico Integrado");
        footerLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        footerLabel.setTextFill(TEXT_MUTED);

        footer.getChildren().add(footerLabel);

        return footer;
    }

    private void configurarEventos() {
        pesoField.textProperty().addListener((obs, oldVal, newVal) -> actualizarDatos());
        edadField.textProperty().addListener((obs, oldVal, newVal) -> actualizarDatos());
        alturaField.textProperty().addListener((obs, oldVal, newVal) -> actualizarDatos());
        sexoCombo.setOnAction(e -> actualizarDatos());
    }

    public void actualizarDatosDesdeCalculadora(String sexo, double peso, double edad, double altura) {
        actualizandoDesdeCalculadora = true;
        if (sexoCombo != null && sexo != null) sexoCombo.setValue(sexo);
        if (pesoField != null && peso > 0) pesoField.setText(String.format("%.1f", peso));
        if (edadField != null && edad > 0) edadField.setText(String.valueOf((int) edad));
        if (alturaField != null && altura > 0) alturaField.setText(String.format("%.1f", altura));
        actualizarDatos();
        actualizandoDesdeCalculadora = false;
    }

    private void actualizarDatos() {
        if (actualizandoDesdeCalculadora) return;

        try {
            if (pesoField != null && !pesoField.getText().trim().isEmpty()) {
                pesoUsuario = Double.parseDouble(pesoField.getText().trim());
            }
            if (edadField != null && !edadField.getText().trim().isEmpty()) {
                edadUsuario = Double.parseDouble(edadField.getText().trim());
            }
            if (alturaField != null && !alturaField.getText().trim().isEmpty()) {
                alturaUsuario = Double.parseDouble(alturaField.getText().trim());
            }
            if (sexoCombo != null) {
                sexoSeleccionado = sexoCombo.getValue();
            }

            if (calculadora != null) {
                calculadora.actualizarDatosDesdeMenu(sexoSeleccionado, pesoUsuario, edadUsuario, alturaUsuario);
            }
        } catch (NumberFormatException ex) {
            // Ignorar errores parciales mientras escribe el usuario
        }
    }

    private void abrirSomatotipo() {
        if (WindowManager.enfocarSiAbierta("CalculadoraSomatotipo")) {
            return;
        }

        actualizarDatos();

        if (!validarDatos()) {
            return;
        }

        try {
            Stage stage = new Stage();
            WindowManager.registrarVentana("CalculadoraSomatotipo", stage);
            CalculadoraSomatotipo somatotipo = new CalculadoraSomatotipo();
            somatotipo.actualizarDatosAntropometricos(pesoUsuario, alturaUsuario);
            somatotipo.start(stage);

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Calculadora de Somatotipo");
            info.setHeaderText("Calculadora de Somatotipo iniciada");
            info.setContentText(String.format(
                "Parametros transferidos:\n" +
                "• Sexo: %s\n" +
                "• Peso: %.1f kg\n" +
                "• Edad: %.0f anos\n" +
                "• Altura: %.0f cm\n\n" +
                "Ingresa los pliegues, diametros y perimetros para obtener el somatotipo completo.",
                sexoSeleccionado, pesoUsuario, edadUsuario, alturaUsuario));
            info.showAndWait();
        } catch (Exception ex) {
            mostrarError("Error al abrir Somatotipo: " + ex.getMessage());
        }
    }

    private void abrirIMC() {
        if (WindowManager.enfocarSiAbierta("VentanaIMC")) {
            return;
        }

        actualizarDatos();

        if (!validarDatos()) {
            return;
        }

        try {
            Stage stage = new Stage();
            WindowManager.registrarVentana("VentanaIMC", stage);
            VentanaIMC ventanaIMC = new VentanaIMC(sexoSeleccionado);
            ventanaIMC.establecerDatos(sexoSeleccionado, pesoUsuario, edadUsuario, alturaUsuario);
            ventanaIMC.start(stage);

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("IMC y Composicion Corporal");
            info.setHeaderText("Calculadora de IMC y Composicion iniciada");
            info.setContentText(String.format(
                "Parametros transferidos exitosamente:\n" +
                "• Sexo: %s\n" +
                "• Peso: %.1f kg\n" +
                "• Edad: %.0f anos\n" +
                "• Altura: %.0f cm\n\n" +
                "El IMC ha sido calculado. Puedes ingresar pliegues y circunferencias para el analisis morfologico.",
                sexoSeleccionado, pesoUsuario, edadUsuario, alturaUsuario));
            info.showAndWait();
        } catch (Exception ex) {
            mostrarError("Error al abrir IMC: " + ex.getMessage());
        }
    }

    private boolean validarDatos() {
        if (pesoUsuario <= 0 || pesoUsuario > 500) {
            mostrarError("Por favor, ingresa un peso valido (1 a 500 kg).");
            if (pesoField != null) pesoField.requestFocus();
            return false;
        }

        if (edadUsuario <= 0 || edadUsuario > 120) {
            mostrarError("Por favor, ingresa una edad valida (1 a 120 anos).");
            if (edadField != null) edadField.requestFocus();
            return false;
        }

        if (alturaUsuario <= 0 || alturaUsuario > 300) {
            mostrarError("Por favor, ingresa una altura valida (1 a 300 cm).");
            if (alturaField != null) alturaField.requestFocus();
            return false;
        }

        return true;
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atencion");
        alert.setHeaderText("Datos requeridos");
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