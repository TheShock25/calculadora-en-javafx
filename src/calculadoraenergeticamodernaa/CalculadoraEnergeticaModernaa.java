package calculadoraenergeticamodernaa;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;

public class CalculadoraEnergeticaModernaa extends Application {

    // Paleta de colores oficial: Verdes Modernos y Blancos
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color ACCENT_GREEN = Color.web("#43A047");        // Verde acción
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Carbón verdoso para alta legibilidad
    private static final Color TEXT_MUTED = Color.web("#5C7669");          // Texto secundario suave

    // Referencias a ventanas para sincronización de datos
    private VentanaIMC ventanaIMC;
    private CalculadoraSomatotipo ventanaSomatotipo;
    private MenuPrincipalAntropometria menuPrincipal;

    // Componentes de la interfaz
    private Label tituloLabel;
    private Label modeloLabel;
    private ComboBox<String> ecuacionCombo;
    private ComboBox<String> sexoCombo;
    private ComboBox<String> actividadCombo;
    private TextField pesoField, alturaField, edadField;
    private Label gebValueLabel, etaValueLabel, getValueLabel;
    private Label gebUnitLabel, etaUnitLabel, getUnitLabel;
    private VBox gebCard, etaCard, getCard;
    private Canvas graficaCanvas;
    private Button calcularBtn, equivalentesBtn, menuPrincipalBtn;

    // Variables de cálculo
    private double gastoBasal = 0.0;
    private double eta = 0.0;
    private double gastoTotal = 0.0;

    private boolean isTabletMode;

    // Arrays para niveles de actividad
    private final String[] actividadHombre = {
        "Sedentaria (1.20) - Poco o ningun ejercicio", 
        "Ligero (1.40) - Ejercicio ligero 1-3 dias/sem", 
        "Moderado (1.60) - Ejercicio moderado 3-5 dias/sem", 
        "Activo (1.75) - Ejercicio intenso 6-7 dias/sem", 
        "Muy Activo (1.95) - Trabajo fisico o atletas"
    };

    private final String[] actividadMujer = {
        "Sedentaria (1.20) - Poco o ningun ejercicio", 
        "Ligero (1.35) - Ejercicio ligero 1-3 dias/sem", 
        "Moderado (1.50) - Ejercicio moderado 3-5 dias/sem", 
        "Activo (1.65) - Ejercicio intenso 6-7 dias/sem", 
        "Muy Activo (1.80) - Trabajo fisico o atletas"
    };

    public void setVentanaIMC(VentanaIMC imc) {
        this.ventanaIMC = imc;
    }

    public void setVentanaSomatotipo(CalculadoraSomatotipo somatotipo) {
        this.ventanaSomatotipo = somatotipo;
    }

    public void setMenuPrincipal(MenuPrincipalAntropometria menu) {
        this.menuPrincipal = menu;
    }

    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();

        primaryStage.setTitle("NutriEnergia Pro - Calculadora de Gasto Energetico");

        // Layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // 1. Encabezado moderno
        VBox header = crearHeader();
        root.setTop(header);

        // 2. Panel central centrado
        ScrollPane centerScroll = crearPanelCentral();
        root.setCenter(centerScroll);

        // 3. Footer con botones de acción centrados
        HBox footer = crearFooter();
        root.setBottom(footer);

        // Dimensiones
        double width = isTabletMode ? 
            Math.min(960, ResponsiveManager.getScreenBounds().getWidth() - 50) : 860;
        double height = isTabletMode ? 
            Math.min(740, ResponsiveManager.getScreenBounds().getHeight() - 50) : 710;

        Scene scene = new Scene(root, width, height);

        cargarCSS(scene);

        primaryStage.setScene(scene);
        WindowManager.setMasterStage(primaryStage);
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
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1B5E20, #2E7D32, #388E3C); " +
            "-fx-background-radius: 0 0 16 16;"
        );

        double margin = ResponsiveManager.getMargin(18, 22);
        header.setPadding(new Insets(margin, margin, margin + 4, margin));

        // Badge superior decorativo
        Label badge = new Label("NUTRIENERGIA PRO");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.18); " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 3 10;"
        );

        // Título principal
        tituloLabel = new Label("Calculadora de Gasto Energetico");
        tituloLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(22, 26)));
        tituloLabel.setTextFill(Color.WHITE);

        // Subtítulo
        modeloLabel = new Label("Ecuacion de Harris-Benedict (Gasto Basal + ETA + Actividad)");
        modeloLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(13, 14)));
        modeloLabel.setTextFill(Color.web("#C8E6C9"));

        header.getChildren().addAll(badge, tituloLabel, modeloLabel);
        header.setSpacing(6);
        header.setEffect(new DropShadow(10, 0, 3, Color.rgb(0, 0, 0, 0.15)));

        return header;
    }

    private ScrollPane crearPanelCentral() {
        VBox centerPanel = new VBox();
        centerPanel.setAlignment(Pos.TOP_CENTER);
        centerPanel.setSpacing(16);

        double margin = ResponsiveManager.getMargin(16, 20);
        centerPanel.setPadding(new Insets(margin, margin, margin, margin));

        // 1. Tarjeta de Formulario (Centrada)
        VBox formPanel = crearFormulario();

        // 2. Tarjetas de Resultados (Centradas)
        HBox resultPanel = crearPanelResultados();

        // 3. Contenedor de la gráfica (Centrado)
        VBox graficaContainer = crearPanelGrafica();

        centerPanel.getChildren().addAll(formPanel, resultPanel, graficaContainer);

        ScrollPane scroll = new ScrollPane(centerPanel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearFormulario() {
        VBox form = new VBox();
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(780);
        form.setSpacing(16);
        form.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 14; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 22 28;"
        );
        form.setEffect(new DropShadow(12, 0, 3, Color.rgb(0, 0, 0, 0.05)));

        // Encabezado del Formulario (Centrado)
        HBox headerForm = new HBox(8);
        headerForm.setAlignment(Pos.CENTER);
        
        Label seccionTitulo = new Label("PARAMETROS DEL PACIENTE");
        seccionTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        seccionTitulo.setTextFill(PRIMARY_GREEN);
        seccionTitulo.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 6; -fx-padding: 4 12;");
        
        headerForm.getChildren().add(seccionTitulo);

        // Fila 1: Ecuación y Sexo (Centrados)
        HBox row1 = new HBox(24);
        row1.setAlignment(Pos.CENTER);

        VBox boxEcuacion = crearBloqueControl("ECUACION PREDICTIVA", ecuacionCombo = new ComboBox<>());
        ecuacionCombo.getItems().addAll("Harris-Benedict", "Mifflin-St Jeor", "Valencia");
        ecuacionCombo.setValue("Harris-Benedict");
        ecuacionCombo.setPrefWidth(240);
        estilizarCombo(ecuacionCombo);

        VBox boxSexo = crearBloqueControl("SEXO BIOLOGICO", sexoCombo = new ComboBox<>());
        sexoCombo.getItems().addAll("Hombre", "Mujer");
        sexoCombo.setValue("Hombre");
        sexoCombo.setPrefWidth(180);
        estilizarCombo(sexoCombo);

        row1.getChildren().addAll(boxEcuacion, boxSexo);

        // Fila 2: Peso, Altura y Edad (Centrados en bloques con unidades)
        HBox row2 = new HBox(20);
        row2.setAlignment(Pos.CENTER);

        pesoField = crearCampoTexto("70.0");
        VBox boxPeso = crearBloqueCampo("PESO", pesoField, "kg");

        alturaField = crearCampoTexto("175.0");
        VBox boxAltura = crearBloqueCampo("ALTURA", alturaField, "cm");

        edadField = crearCampoTexto("25");
        VBox boxEdad = crearBloqueCampo("EDAD", edadField, "anos");

        row2.getChildren().addAll(boxPeso, boxAltura, boxEdad);

        // Fila 3: Nivel de actividad física (Centrado)
        actividadCombo = new ComboBox<>();
        actividadCombo.getItems().addAll(actividadHombre);
        actividadCombo.setValue(actividadHombre[0]);
        actividadCombo.setPrefWidth(444);
        estilizarCombo(actividadCombo);

        VBox boxActividad = crearBloqueControl("NIVEL DE ACTIVIDAD FISICA (FACTOR)", actividadCombo);
        boxActividad.setAlignment(Pos.CENTER);

        form.getChildren().addAll(headerForm, row1, row2, boxActividad);

        return form;
    }

    private VBox crearBloqueControl(String etiqueta, Control control) {
        VBox block = new VBox(5);
        block.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(etiqueta);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(TEXT_MUTED);

        block.getChildren().addAll(label, control);
        return block;
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
        unitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        unitLabel.setTextFill(PRIMARY_GREEN);
        unitLabel.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 7 10; " +
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
        field.setPrefWidth(90);
        field.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        field.setAlignment(Pos.CENTER);
        field.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 7 10;"
        );

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #2E7D32; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 7 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(46, 125, 50, 0.25), 6, 0, 0, 1);"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: #FAFCFA; " +
                    "-fx-border-color: #C8E6C9; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 7 10;"
                );
            }
        });

        return field;
    }

    private HBox crearPanelResultados() {
        HBox resultPanel = new HBox(16);
        resultPanel.setAlignment(Pos.CENTER);
        resultPanel.setMaxWidth(780);

        // Tarjeta GEB
        gebCard = crearTarjetaResultado("GASTO BASAL (GEB)", "Pendiente", "kcal/dia", "#2E7D32", "#E8F5E9");
        gebValueLabel = (Label) ((VBox) gebCard.getChildren().get(1)).getChildren().get(0);
        gebUnitLabel = (Label) ((VBox) gebCard.getChildren().get(1)).getChildren().get(1);

        // Tarjeta ETA
        etaCard = crearTarjetaResultado("ETA (ETA 10%)", "Pendiente", "kcal/dia", "#388E3C", "#F1F8E9");
        etaValueLabel = (Label) ((VBox) etaCard.getChildren().get(1)).getChildren().get(0);
        etaUnitLabel = (Label) ((VBox) etaCard.getChildren().get(1)).getChildren().get(1);

        // Tarjeta GET (Destacada)
        getCard = crearTarjetaResultado("GASTO TOTAL (GET)", "Pendiente", "kcal/dia", "#1B5E20", "#E0F2E9");
        getValueLabel = (Label) ((VBox) getCard.getChildren().get(1)).getChildren().get(0);
        getUnitLabel = (Label) ((VBox) getCard.getChildren().get(1)).getChildren().get(1);

        HBox.setHgrow(gebCard, Priority.ALWAYS);
        HBox.setHgrow(etaCard, Priority.ALWAYS);
        HBox.setHgrow(getCard, Priority.ALWAYS);

        resultPanel.getChildren().addAll(gebCard, etaCard, getCard);

        return resultPanel;
    }

    private VBox crearTarjetaResultado(String titulo, String valorInicial, String unidad, String colorAccent, String bgHex) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            String.format(
                "-fx-background-color: linear-gradient(to bottom, #FFFFFF, %s); " +
                "-fx-border-color: #A5D6A7; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 14 18;", bgHex)
        );
        card.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.05)));

        Label labelTitulo = new Label(titulo);
        labelTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        labelTitulo.setTextFill(Color.web(colorAccent));
        labelTitulo.setStyle("-fx-background-color: rgba(46, 125, 50, 0.1); -fx-background-radius: 4; -fx-padding: 2 8;");

        VBox valueContainer = new VBox(2);
        valueContainer.setAlignment(Pos.CENTER);

        Label labelValor = new Label(valorInicial);
        labelValor.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        labelValor.setTextFill(TEXT_DARK);

        Label labelUnidad = new Label(unidad);
        labelUnidad.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        labelUnidad.setTextFill(TEXT_MUTED);

        valueContainer.getChildren().addAll(labelValor, labelUnidad);
        card.getChildren().addAll(labelTitulo, valueContainer);

        return card;
    }

    private VBox crearPanelGrafica() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        container.setMaxWidth(780);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 14; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 16 20;"
        );
        container.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.05)));

        HBox chartHeader = new HBox(8);
        chartHeader.setAlignment(Pos.CENTER);

        Label graficaTitulo = new Label("DISTRIBUCION VISUAL DEL GASTO ENERGETICO (GET)");
        graficaTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        graficaTitulo.setTextFill(PRIMARY_GREEN);

        chartHeader.getChildren().add(graficaTitulo);

        graficaCanvas = new Canvas(730, 90);
        
        container.getChildren().addAll(chartHeader, graficaCanvas);

        return container;
    }

    private HBox crearFooter() {
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E8E3; -fx-border-width: 1.5 0 0 0;");
        footer.setPadding(new Insets(14, 20, 16, 20));

        calcularBtn = crearBotonPildora("CALCULAR GASTO ENERGETICO", PRIMARY_GREEN, "#388E3C", 220);
        equivalentesBtn = crearBotonPildora("SISTEMA DE EQUIVALENTES", SECONDARY_GREEN, "#4CAF50", 220);
        menuPrincipalBtn = crearBotonPildora("MENU PRINCIPAL", DARK_FOREST, "#2E7D32", 180);

        footer.getChildren().addAll(calcularBtn, equivalentesBtn, menuPrincipalBtn);

        return footer;
    }

    private Button crearBotonPildora(String texto, Color baseColor, String hoverHex, double minWidth) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        btn.setTextFill(Color.WHITE);
        btn.setMinWidth(minWidth);

        String baseHex = String.format("#%02X%02X%02X",
            (int)(baseColor.getRed() * 255),
            (int)(baseColor.getGreen() * 255),
            (int)(baseColor.getBlue() * 255));

        btn.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 22; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);", baseHex));

        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 22; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 8, 0, 0, 3);", hoverHex)));

        btn.setOnMouseExited(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 22; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);", baseHex)));

        return btn;
    }

    private void configurarEventos() {
        // Cambio de ecuación
        ecuacionCombo.setOnAction(e -> {
            String seleccion = ecuacionCombo.getValue();
            modeloLabel.setText("Ecuacion seleccionada: " + seleccion);
        });

        // Cambio de sexo
        sexoCombo.setOnAction(e -> {
            actualizarNivelesActividad();
            actualizarVentanasSecundarias();
        });

        // Botón calcular
        calcularBtn.setOnAction(e -> realizarCalculo());

        // Botón equivalentes
        equivalentesBtn.setOnAction(e -> {
            if (WindowManager.enfocarSiAbierta("SistemaEquivalentes")) {
                return;
            }
            try {
                Stage stage = new Stage();
                WindowManager.registrarVentana("SistemaEquivalentes", stage);
                new SistemaEquivalentes(gastoTotal > 0 ? gastoTotal : 2000).start(stage);
            } catch (Exception ex) {
                mostrarError("Error al abrir Sistema de Equivalentes: " + ex.getMessage());
            }
        });

        // Botón menú principal
        menuPrincipalBtn.setOnAction(e -> {
            if (WindowManager.enfocarSiAbierta("MenuPrincipalAntropometria")) {
                return;
            }
            try {
                Stage stage = new Stage();
                WindowManager.registrarVentana("MenuPrincipalAntropometria", stage);
                MenuPrincipalAntropometria nuevoMenu = new MenuPrincipalAntropometria();
                nuevoMenu.start(stage);
                this.setMenuPrincipal(nuevoMenu);
            } catch (Exception ex) {
                mostrarError("Error al abrir Menu Principal: " + ex.getMessage());
            }
        });

        // Sincronización en tiempo real al escribir en los campos de texto
        pesoField.textProperty().addListener((obs, oldVal, newVal) -> actualizarVentanasSecundarias());
        alturaField.textProperty().addListener((obs, oldVal, newVal) -> actualizarVentanasSecundarias());
        edadField.textProperty().addListener((obs, oldVal, newVal) -> actualizarVentanasSecundarias());
    }

    private void actualizarNivelesActividad() {
        String sexo = sexoCombo.getValue();
        String[] niveles = "Hombre".equals(sexo) ? actividadHombre : actividadMujer;

        int selectedIndex = actividadCombo.getSelectionModel().getSelectedIndex();
        actividadCombo.getItems().setAll(niveles);

        if (selectedIndex >= 0 && selectedIndex < niveles.length) {
            actividadCombo.setValue(niveles[selectedIndex]);
        } else {
            actividadCombo.setValue(niveles[0]);
        }
    }

    private void realizarCalculo() {
        try {
            double peso = Double.parseDouble(pesoField.getText().trim());
            double altura = Double.parseDouble(alturaField.getText().trim());
            int edad = Integer.parseInt(edadField.getText().trim());

            if (peso <= 0 || altura <= 0 || edad <= 0) {
                mostrarError("Por favor ingresa valores positivos para peso, altura y edad.");
                return;
            }

            String sexo = sexoCombo.getValue();
            String ecuacion = ecuacionCombo.getValue();

            calcularGEB(ecuacion, sexo, peso, altura, edad);
            eta = gastoBasal * 0.10;
            double factor = obtenerFactorActividad();
            gastoTotal = (gastoBasal + eta) * factor;

            actualizarResultados(factor);
            dibujarGrafica();

        } catch (NumberFormatException ex) {
            mostrarError("Por favor ingresa valores numericos validos en todos los campos.");
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

    private void actualizarResultados(double factor) {
        gebValueLabel.setText(String.format("%.0f", gastoBasal));
        gebUnitLabel.setText("kcal/dia");

        etaValueLabel.setText(String.format("%.0f", eta));
        etaUnitLabel.setText("kcal/dia (10%)");

        getValueLabel.setText(String.format("%.0f", gastoTotal));
        getUnitLabel.setText(String.format("kcal/dia (Factor x%.2f)", factor));
    }

    private void dibujarGrafica() {
        GraphicsContext gc = graficaCanvas.getGraphicsContext2D();

        // Limpiar canvas
        gc.clearRect(0, 0, graficaCanvas.getWidth(), graficaCanvas.getHeight());

        if (gastoTotal <= 0) return;

        double canvasWidth = graficaCanvas.getWidth();
        double barWidth = canvasWidth - 40;
        double barHeight = 26;
        double x = 20;
        double y = 20;

        // Fondo de la barra en gris perla con esquinas redondeadas
        gc.setFill(Color.web("#ECEFF1"));
        gc.fillRoundRect(x, y, barWidth, barHeight, 13, 13);

        // Barra con degradado verde esmeralda
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#1B5E20")),
            new Stop(0.6, Color.web("#2E7D32")),
            new Stop(1, Color.web("#4CAF50"))
        );
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, barWidth, barHeight, 13, 13);

        // Borde verde sutil
        gc.setStroke(Color.web("#81C784"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, barWidth, barHeight, 13, 13);

        // Texto informativo del GET dentro de la barra
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        String label = String.format("Gasto Energetico Total: %.0f kcal/dia", gastoTotal);
        gc.fillText(label, x + 18, y + 18);

        // Escalas inferiores centradas
        gc.setFill(TEXT_MUTED);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.fillText("0 kcal", x, y + barHeight + 20);
        String labelMax = String.format("%.0f kcal (Meta Requerida)", gastoTotal);
        gc.fillText(labelMax, x + barWidth - 140, y + barHeight + 20);
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atencion");
        alert.setHeaderText("Datos de entrada requeridos");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void actualizarDatosDesdeMenu(String sexo, double peso, double edad, double altura) {
        if (sexoCombo != null) sexoCombo.setValue(sexo);
        if (pesoField != null) pesoField.setText(String.valueOf(peso));
        if (edadField != null) edadField.setText(String.valueOf((int) edad));
        if (alturaField != null) alturaField.setText(String.valueOf((int) altura));
        realizarCalculo();
    }

    private void actualizarVentanasSecundarias() {
        try {
            String sexoActual = sexoCombo != null ? sexoCombo.getValue() : "Hombre";
            double pesoActual = 0;
            double edadActual = 0;
            double alturaActual = 0;

            if (pesoField != null && !pesoField.getText().trim().isEmpty()) {
                pesoActual = Double.parseDouble(pesoField.getText().trim());
            }
            if (edadField != null && !edadField.getText().trim().isEmpty()) {
                edadActual = Double.parseDouble(edadField.getText().trim());
            }
            if (alturaField != null && !alturaField.getText().trim().isEmpty()) {
                alturaActual = Double.parseDouble(alturaField.getText().trim());
            }

            if (ventanaIMC != null) {
                ventanaIMC.establecerDatos(sexoActual, pesoActual, edadActual, alturaActual);
            }
            if (ventanaSomatotipo != null) {
                ventanaSomatotipo.actualizarDatosAntropometricos(pesoActual, alturaActual);
            }
            if (menuPrincipal != null) {
                menuPrincipal.actualizarDatosDesdeCalculadora(sexoActual, pesoActual, edadActual, alturaActual);
            }
        } catch (NumberFormatException ex) {
            // Ignorar errores parciales mientras el usuario escribe
        }
    }

    public static void launchDirectly() {
        launch(CalculadoraEnergeticaModernaa.class);
    }

    public static void main(String[] args) {
        launch(args);
    }
}