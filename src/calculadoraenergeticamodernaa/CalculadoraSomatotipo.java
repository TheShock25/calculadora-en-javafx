package calculadoraenergeticamodernaa;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class CalculadoraSomatotipo extends Application {

    // Paleta de colores oficial: Verdes y Blancos
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color ACCENT_GREEN = Color.web("#43A047");        // Verde acción
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color LIGHT_MINT = Color.web("#E8F5E9");          // Fondo verde menta suave
    private static final Color BORDER_GREEN = Color.web("#C8E6C9");        // Borde verde suave
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Texto oscuro
    private static final Color TEXT_MUTED = Color.web("#5C7669");          // Texto secundario

    // Colores funcionales para los 3 componentes del somatotipo
    private static final Color COLOR_ENDO = Color.web("#D32F2F");          // Rojo / Adiposidad
    private static final Color COLOR_MESO = Color.web("#2E7D32");          // Verde esmeralda / Musculatura
    private static final Color COLOR_ECTO = Color.web("#1976D2");          // Azul / Linealidad

    // Componentes de entrada
    private TextField alturaField, pesoField;
    private TextField tricipitalField, subescapularField, supraespinalField, pantorrillaPliegueField;
    private TextField diametroHumeroField, diametroFemurField;
    private TextField perimetroBrazoField, perimetroPantorrillaField;

    // Componentes de salida
    private Label endoValueLabel, mesoValueLabel, ectoValueLabel, tipoDominanteLabel;
    private TextArea descripcionArea;
    private Canvas progressCanvas;
    private Canvas somatocartaCanvas;
    private Canvas tipoCorporalCanvas;

    // Resultado
    private ResultadoSomatotipo resultadoActual;
    private boolean isTabletMode;

    public void actualizarDatosAntropometricos(double peso, double altura) {
        if (pesoField != null && peso > 0) {
            pesoField.setText(String.format("%.1f", peso));
        }
        if (alturaField != null && altura > 0) {
            alturaField.setText(String.format("%.1f", altura));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();

        primaryStage.setTitle("NutriEnergia Pro - Calculadora de Somatotipo Heath-Carter");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // Header moderno
        VBox header = crearHeader();
        root.setTop(header);

        // Pestañas principales
        TabPane tabPane = crearTabs();
        root.setCenter(tabPane);

        double width = isTabletMode ? 
            Math.min(1050, ResponsiveManager.getScreenBounds().getWidth() - 50) : 980;
        double height = isTabletMode ? 
            Math.min(850, ResponsiveManager.getScreenBounds().getHeight() - 50) : 780;

        Scene scene = new Scene(root, width, height);
        cargarCSS(scene);

        primaryStage.setScene(scene);
        primaryStage.show();
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

        Label badge = new Label("EVALUACION MORFOLOGICA");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.18); " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 3 10;"
        );

        Label titulo = new Label("Calculadora de Somatotipo Heath-Carter");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(22, 26)));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Determinacion de Endomorfia, Mesomorfia y Ectomorfia");
        subtitulo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(13, 14)));
        subtitulo.setTextFill(Color.web("#C8E6C9"));

        header.getChildren().addAll(badge, titulo, subtitulo);
        header.setEffect(new DropShadow(10, 0, 3, Color.rgb(0, 0, 0, 0.15)));

        return header;
    }

    private TabPane crearTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        // Tab 1: Datos Antropométricos
        Tab datosTab = new Tab("Datos Antropometricos");
        datosTab.setContent(crearPanelDatos());

        // Tab 2: Resultados
        Tab resultadosTab = new Tab("Resultados y Diagnostico");
        resultadosTab.setContent(crearPanelResultados());

        // Tab 3: Visualización Gráfica
        Tab visualTab = new Tab("Visualizacion y Somatocarta");
        visualTab.setContent(crearPanelVisualizacion());

        tabPane.getTabs().addAll(datosTab, resultadosTab, visualTab);

        return tabPane;
    }

    private ScrollPane crearPanelDatos() {
        VBox container = new VBox(16);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        // Contenedor centrado
        VBox formCard = new VBox(18);
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(820);
        formCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 14; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 22 28;"
        );
        formCard.setEffect(new DropShadow(12, 0, 3, Color.rgb(0, 0, 0, 0.05)));

        // Título del formulario
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER);
        Label headerLabel = new Label("MEDIDAS ANTROPOMETRICAS DEL PACIENTE");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        headerLabel.setTextFill(PRIMARY_GREEN);
        headerLabel.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 6; -fx-padding: 4 12;");
        headerBox.getChildren().add(headerLabel);

        // Inicializar campos
        alturaField = crearCampoEstilizado("175.0");
        pesoField = crearCampoEstilizado("70.0");
        tricipitalField = crearCampoEstilizado("12.0");
        subescapularField = crearCampoEstilizado("14.0");
        supraespinalField = crearCampoEstilizado("10.0");
        pantorrillaPliegueField = crearCampoEstilizado("8.0");
        diametroHumeroField = crearCampoEstilizado("6.8");
        diametroFemurField = crearCampoEstilizado("9.5");
        perimetroBrazoField = crearCampoEstilizado("32.0");
        perimetroPantorrillaField = crearCampoEstilizado("37.0");

        // 4 Secciones en tarjetas limpias
        VBox seccionBasicos = crearSubseccion("1. Datos Basicos", crearGridBasicos());
        VBox seccionPliegues = crearSubseccion("2. Pliegues Cutaneos (mm)", crearGridPliegues());
        VBox seccionDiametros = crearSubseccion("3. Diametros Oseos (cm)", crearGridDiametros());
        VBox seccionPerimetros = crearSubseccion("4. Perimetros Musculares (cm)", crearGridPerimetros());

        // Botón Calcular Centrado
        Button calcularBtn = new Button("CALCULAR SOMATOTIPO");
        calcularBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        calcularBtn.setTextFill(Color.WHITE);
        calcularBtn.setMinWidth(260);
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
        calcularBtn.setOnAction(e -> calcularSomatotipo());

        HBox btnBox = new HBox(calcularBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        formCard.getChildren().addAll(
            headerBox,
            seccionBasicos,
            seccionPliegues,
            seccionDiametros,
            seccionPerimetros,
            btnBox
        );

        container.getChildren().add(formCard);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearSubseccion(String titulo, Region grid) {
        VBox box = new VBox(8);
        box.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #E0E8E3; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 12 16;"
        );

        Label lbl = new Label(titulo);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lbl.setTextFill(PRIMARY_GREEN);

        box.getChildren().addAll(lbl, grid);
        return box;
    }

    private GridPane crearGridBasicos() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.add(crearCampoConUnidad("Altura:", alturaField, "cm"), 0, 0);
        grid.add(crearCampoConUnidad("Peso:", pesoField, "kg"), 1, 0);

        return grid;
    }

    private GridPane crearGridPliegues() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.add(crearCampoConUnidad("Tricipital:", tricipitalField, "mm"), 0, 0);
        grid.add(crearCampoConUnidad("Subescapular:", subescapularField, "mm"), 1, 0);
        grid.add(crearCampoConUnidad("Supraespinal:", supraespinalField, "mm"), 0, 1);
        grid.add(crearCampoConUnidad("Pantorrilla:", pantorrillaPliegueField, "mm"), 1, 1);

        return grid;
    }

    private GridPane crearGridDiametros() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.add(crearCampoConUnidad("Humero (biepicondilar):", diametroHumeroField, "cm"), 0, 0);
        grid.add(crearCampoConUnidad("Femur (bicondileo):", diametroFemurField, "cm"), 1, 0);

        return grid;
    }

    private GridPane crearGridPerimetros() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.add(crearCampoConUnidad("Brazo flexionado:", perimetroBrazoField, "cm"), 0, 0);
        grid.add(crearCampoConUnidad("Pantorrilla:", perimetroPantorrillaField, "cm"), 1, 0);

        return grid;
    }

    private HBox crearCampoConUnidad(String labelText, TextField field, String unidad) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(300);

        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12.5));
        label.setTextFill(TEXT_DARK);
        label.setMinWidth(150);

        Label unitLabel = new Label(unidad);
        unitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        unitLabel.setTextFill(PRIMARY_GREEN);
        unitLabel.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 5 8; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6;"
        );

        box.getChildren().addAll(label, field, unitLabel);
        return box;
    }

    private TextField crearCampoEstilizado(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.setPrefWidth(75);
        field.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        field.setAlignment(Pos.CENTER);
        field.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 5 8;"
        );

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #2E7D32; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 5 8; " +
                    "-fx-effect: dropshadow(gaussian, rgba(46, 125, 50, 0.25), 4, 0, 0, 1);"
                );
            } else {
                field.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-border-color: #C8E6C9; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 5 8;"
                );
            }
        });

        return field;
    }

    private ScrollPane crearPanelResultados() {
        VBox container = new VBox(16);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        VBox mainCard = new VBox(18);
        mainCard.setAlignment(Pos.CENTER);
        mainCard.setMaxWidth(860);

        // Fila Superior: 3 Tarjetas de Valores + Tarjeta Dominante
        HBox statRow = new HBox(14);
        statRow.setAlignment(Pos.CENTER);

        VBox cardEndo = crearStatCard("ENDOMORFIA", "Adiposidad relativa", "-", COLOR_ENDO);
        endoValueLabel = (Label) ((VBox) cardEndo.getChildren().get(2)).getChildren().get(0);

        VBox cardMeso = crearStatCard("MESOMORFIA", "Robustez musculoesqueletica", "-", COLOR_MESO);
        mesoValueLabel = (Label) ((VBox) cardMeso.getChildren().get(2)).getChildren().get(0);

        VBox cardEcto = crearStatCard("ECTOMORFIA", "Linealidad relativa", "-", COLOR_ECTO);
        ectoValueLabel = (Label) ((VBox) cardEcto.getChildren().get(2)).getChildren().get(0);

        HBox.setHgrow(cardEndo, Priority.ALWAYS);
        HBox.setHgrow(cardMeso, Priority.ALWAYS);
        HBox.setHgrow(cardEcto, Priority.ALWAYS);

        statRow.getChildren().addAll(cardEndo, cardMeso, cardEcto);

        // Tarjeta de Clasificación Dominante
        VBox dominanteBox = new VBox(6);
        dominanteBox.setAlignment(Pos.CENTER);
        dominanteBox.setStyle(
            "-fx-background-color: linear-gradient(to right, #E8F5E9, #FAFCFA); " +
            "-fx-border-color: #A5D6A7; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 12 18;"
        );
        dominanteBox.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label domTitle = new Label("CLASIFICACION SOMATOTIPICA DOMINANTE");
        domTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        domTitle.setTextFill(PRIMARY_GREEN);

        tipoDominanteLabel = new Label("Pendiente de calculo");
        tipoDominanteLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        tipoDominanteLabel.setTextFill(TEXT_DARK);

        dominanteBox.getChildren().addAll(domTitle, tipoDominanteLabel);

        // Panel de Barras de Distribución (Canvas)
        VBox chartCard = new VBox(10);
        chartCard.setAlignment(Pos.CENTER);
        chartCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 16 20;"
        );
        chartCard.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label chartTitle = new Label("DISTRIBUCION COMPARATIVA (ESCALA 0.5 - 7.0+)");
        chartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        chartTitle.setTextFill(PRIMARY_GREEN);

        progressCanvas = new Canvas(780, 130);
        chartCard.getChildren().addAll(chartTitle, progressCanvas);

        // Panel de Descripción y Recomendaciones
        VBox descCard = new VBox(10);
        descCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 16 20;"
        );
        descCard.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label descTitle = new Label("DESCRIPCION CLINICA Y RECOMENDACIONES DEPORTIVAS");
        descTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        descTitle.setTextFill(PRIMARY_GREEN);

        descripcionArea = new TextArea("Ingresa los datos del paciente y presiona 'CALCULAR SOMATOTIPO' para generar el analisis completo.");
        descripcionArea.setWrapText(true);
        descripcionArea.setEditable(false);
        descripcionArea.setPrefRowCount(9);
        descripcionArea.setFont(Font.font("Segoe UI", 13));
        descripcionArea.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #E0E8E3; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8;"
        );

        descCard.getChildren().addAll(descTitle, descripcionArea);

        mainCard.getChildren().addAll(statRow, dominanteBox, chartCard, descCard);
        container.getChildren().add(mainCard);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearStatCard(String titulo, String subtitulo, String valor, Color colorAccent) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E0E8E3; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 14 16;"
        );
        card.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblTitulo.setTextFill(colorAccent);
        lblTitulo.setStyle(String.format(
            "-fx-background-color: rgba(%d, %d, %d, 0.1); -fx-background-radius: 4; -fx-padding: 2 8;",
            (int)(colorAccent.getRed() * 255),
            (int)(colorAccent.getGreen() * 255),
            (int)(colorAccent.getBlue() * 255)
        ));

        Label lblSub = new Label(subtitulo);
        lblSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10.5));
        lblSub.setTextFill(TEXT_MUTED);

        VBox valBox = new VBox();
        valBox.setAlignment(Pos.CENTER);
        Label lblVal = new Label(valor);
        lblVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblVal.setTextFill(TEXT_DARK);
        valBox.getChildren().add(lblVal);

        card.getChildren().addAll(lblTitulo, lblSub, valBox);
        return card;
    }

    private ScrollPane crearPanelVisualizacion() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));

        // Contenedor de las dos gráficas lado a lado o apiladas
        HBox visualRow = new HBox(20);
        visualRow.setAlignment(Pos.CENTER);
        visualRow.setMaxWidth(920);

        // 1. Somatocarta
        VBox somatocartaBox = crearVisualCard("SOMATOCARTA DE HEATH-CARTER (COORDENADAS X, Y)");
        somatocartaCanvas = new Canvas(430, 430);
        somatocartaBox.getChildren().add(somatocartaCanvas);

        // 2. Tipo Corporal
        VBox tipoBox = crearVisualCard("REPRESENTACION MORFOLOGICA");
        tipoCorporalCanvas = new Canvas(390, 430);
        tipoBox.getChildren().add(tipoCorporalCanvas);

        HBox.setHgrow(somatocartaBox, Priority.ALWAYS);
        HBox.setHgrow(tipoBox, Priority.ALWAYS);

        visualRow.getChildren().addAll(somatocartaBox, tipoBox);
        container.getChildren().add(visualRow);

        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearVisualCard(String titulo) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 14;"
        );
        card.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.05)));

        Label titleLabel = new Label(titulo);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLabel.setTextFill(PRIMARY_GREEN);

        card.getChildren().add(titleLabel);
        return card;
    }

    private void calcularSomatotipo() {
        try {
            double altura = Double.parseDouble(alturaField.getText().trim());
            double peso = Double.parseDouble(pesoField.getText().trim());
            double tricipital = Double.parseDouble(tricipitalField.getText().trim());
            double subescapular = Double.parseDouble(subescapularField.getText().trim());
            double supraespinal = Double.parseDouble(supraespinalField.getText().trim());
            double pantorrillaPliegue = Double.parseDouble(pantorrillaPliegueField.getText().trim());
            double diametroHumero = Double.parseDouble(diametroHumeroField.getText().trim());
            double diametroFemur = Double.parseDouble(diametroFemurField.getText().trim());
            double perimetroBrazo = Double.parseDouble(perimetroBrazoField.getText().trim());
            double perimetroPantorrilla = Double.parseDouble(perimetroPantorrillaField.getText().trim());

            if (altura <= 0 || peso <= 0 || tricipital <= 0 || subescapular <= 0 || 
                supraespinal <= 0 || pantorrillaPliegue <= 0 || diametroHumero <= 0 || 
                diametroFemur <= 0 || perimetroBrazo <= 0 || perimetroPantorrilla <= 0) {
                mostrarError("Por favor ingresa valores positivos en todos los campos.");
                return;
            }

            resultadoActual = Somatotipo.calcularSomatotipoCompleto(
                tricipital, subescapular, supraespinal, diametroHumero, diametroFemur,
                perimetroBrazo, perimetroPantorrilla, pantorrillaPliegue, altura, peso
            );

            actualizarResultados();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Calculo Completado");
            alert.setHeaderText("Somatotipo calculado exitosamente");
            alert.setContentText(String.format(
                "Endomorfia: %.2f | Mesomorfia: %.2f | Ectomorfia: %.2f\nTipo dominante: %s\n\nConsulta las pestanas 'Resultados' y 'Visualizacion' para ver los detalles.",
                resultadoActual.getEndomorfia(),
                resultadoActual.getMesomorfia(),
                resultadoActual.getEctomorfia(),
                resultadoActual.getTipoCorpoalDominante()
            ));
            alert.showAndWait();

        } catch (NumberFormatException ex) {
            mostrarError("Por favor ingresa valores numericos validos en todos los campos antropometricos.");
        } catch (Exception ex) {
            mostrarError("Error en el calculo: " + ex.getMessage());
        }
    }

    private void actualizarResultados() {
        if (resultadoActual == null) return;

        endoValueLabel.setText(String.format("%.2f", resultadoActual.getEndomorfia()));
        mesoValueLabel.setText(String.format("%.2f", resultadoActual.getMesomorfia()));
        ectoValueLabel.setText(String.format("%.2f", resultadoActual.getEctomorfia()));

        tipoDominanteLabel.setText(resultadoActual.getTipoCorpoalDominante());

        descripcionArea.setText(
            resultadoActual.getDescripcion() + "\n\n" +
            obtenerRecomendacionesEjercicio(resultadoActual.getTipoCorpoalDominante())
        );

        dibujarBarrasProgreso();
        dibujarSomatocarta();
        dibujarTipoCorporal();
    }

    private void dibujarBarrasProgreso() {
        GraphicsContext gc = progressCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, progressCanvas.getWidth(), progressCanvas.getHeight());

        if (resultadoActual == null) return;

        double width = progressCanvas.getWidth() - 40;

        dibujarBarraProgreso(gc, "Endomorfia (Adiposidad)", resultadoActual.getEndomorfia(), COLOR_ENDO, 20, 20, width, 22);
        dibujarBarraProgreso(gc, "Mesomorfia (Musculatura)", resultadoActual.getMesomorfia(), COLOR_MESO, 20, 58, width, 22);
        dibujarBarraProgreso(gc, "Ectomorfia (Linealidad)", resultadoActual.getEctomorfia(), COLOR_ECTO, 20, 96, width, 22);
    }

    private void dibujarBarraProgreso(GraphicsContext gc, String label, double value, 
                                     Color color, double x, double y, double width, double height) {
        // Fondo
        gc.setFill(Color.web("#ECEFF1"));
        gc.fillRoundRect(x, y, width, height, 11, 11);

        // Barra con degradado suave
        double progressWidth = Math.min(width, (width * (value / 8.0)));
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, color),
            new Stop(1, color.deriveColor(0, 0.8, 1.2, 1.0))
        );
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, progressWidth, height, 11, 11);

        // Texto del componente
        gc.setFill(TEXT_DARK);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        gc.fillText(String.format("%s: %.2f", label, value), x + 10, y + 15);
    }

    private void dibujarSomatocarta() {
        GraphicsContext gc = somatocartaCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, somatocartaCanvas.getWidth(), somatocartaCanvas.getHeight());

        double width = somatocartaCanvas.getWidth();
        double height = somatocartaCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double scale = Math.min(width - 80, height - 80) / 2 / 7;

        // Fondo del gráfico
        gc.setFill(Color.web("#FAFCFA"));
        gc.fillRect(0, 0, width, height);

        // Grid sutil
        gc.setStroke(Color.web("#E0E8E3"));
        gc.setLineWidth(1);
        for (int i = -7; i <= 7; i++) {
            gc.strokeLine(centerX + i * scale, centerY - 7 * scale, centerX + i * scale, centerY + 7 * scale);
            gc.strokeLine(centerX - 7 * scale, centerY + i * scale, centerX + 7 * scale, centerY + i * scale);
        }

        // Ejes principales
        gc.setStroke(Color.web("#78909C"));
        gc.setLineWidth(1.5);
        gc.strokeLine(centerX - 7 * scale, centerY, centerX + 7 * scale, centerY);
        gc.strokeLine(centerX, centerY - 7 * scale, centerX, centerY + 3 * scale);

        // Etiquetas de polos
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        gc.setFill(COLOR_ENDO);
        gc.fillText("Endomorfia (-X)", centerX - 7 * scale - 10, centerY - 8);

        gc.setFill(COLOR_ECTO);
        gc.fillText("Ectomorfia (+X)", centerX + 4 * scale, centerY - 8);

        gc.setFill(COLOR_MESO);
        gc.fillText("Mesomorfia (+Y)", centerX - 45, centerY - 7 * scale - 8);

        // Dibujar punto del paciente
        if (resultadoActual != null) {
            double endo = Math.max(1, Math.min(7, resultadoActual.getEndomorfia()));
            double meso = Math.max(1, Math.min(7, resultadoActual.getMesomorfia()));
            double ecto = Math.max(1, Math.min(7, resultadoActual.getEctomorfia()));

            double coordX = ecto - endo;
            double coordY = 2 * meso - (endo + ecto);

            int pointX = (int)(centerX + (coordX) * scale);
            int pointY = (int)(centerY - (coordY) * scale);

            // Círculo exterior brillante
            gc.setFill(Color.rgb(46, 125, 50, 0.3));
            gc.fillOval(pointX - 10, pointY - 10, 20, 20);

            // Punto central
            gc.setFill(PRIMARY_GREEN);
            gc.fillOval(pointX - 5, pointY - 5, 10, 10);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokeOval(pointX - 5, pointY - 5, 10, 10);

            // Coordenadas
            gc.setFill(TEXT_DARK);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            gc.fillText(String.format("P (X:%.1f, Y:%.1f)", coordX, coordY), pointX + 12, pointY - 4);
        }
    }

    private void dibujarTipoCorporal() {
        GraphicsContext gc = tipoCorporalCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, tipoCorporalCanvas.getWidth(), tipoCorporalCanvas.getHeight());

        if (resultadoActual == null) {
            gc.setFill(TEXT_MUTED);
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
            gc.fillText("Realiza el calculo para visualizar el arquetipo", 45, tipoCorporalCanvas.getHeight() / 2);
            return;
        }

        String tipo = obtenerTipoMayor();

        double width = tipoCorporalCanvas.getWidth();
        double height = tipoCorporalCanvas.getHeight();
        double centerX = width / 2;

        // Título del tipo
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        gc.setFill(PRIMARY_GREEN);
        gc.fillText("ARQUETIPO: " + tipo, centerX - 70, 35);

        // Dibujar silueta según tipo dominante
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
        gc.setFill(COLOR_ECTO);
        // Cabeza
        gc.fillOval(centerX - 15, 60, 30, 35);
        // Torso estrecho
        gc.fillRoundRect(centerX - 18, 105, 36, 110, 14, 14);
        // Brazos delgados
        gc.fillRoundRect(centerX - 38, 115, 12, 75, 8, 8);
        gc.fillRoundRect(centerX + 26, 115, 12, 75, 8, 8);
        // Piernas delgadas
        gc.fillRoundRect(centerX - 14, 220, 10, 95, 8, 8);
        gc.fillRoundRect(centerX + 4, 220, 10, 95, 8, 8);

        // Badges descriptivos
        dibujarBadgeCaracteristica(gc, 1, "Estructura osea ligera y delgada", height);
        dibujarBadgeCaracteristica(gc, 2, "Metabolismo rapido, bajo tejido adiposo", height);
        dibujarBadgeCaracteristica(gc, 3, "Mayor dificultad para ganar masa muscular", height);
    }

    private void dibujarMesomorfo(GraphicsContext gc, double centerX, double height) {
        gc.setFill(COLOR_MESO);
        // Cabeza
        gc.fillOval(centerX - 18, 60, 36, 35);
        // Torso en V atlético
        gc.fillPolygon(
            new double[]{centerX - 32, centerX + 32, centerX + 20, centerX - 20},
            new double[]{105, 105, 210, 210},
            4
        );
        // Brazos musculosos
        gc.fillRoundRect(centerX - 48, 110, 18, 70, 10, 10);
        gc.fillRoundRect(centerX + 30, 110, 18, 70, 10, 10);
        // Piernas musculosas
        gc.fillRoundRect(centerX - 18, 215, 16, 100, 10, 10);
        gc.fillRoundRect(centerX + 2, 215, 16, 100, 10, 10);

        dibujarBadgeCaracteristica(gc, 1, "Complexion atletica y hombros anchos", height);
        dibujarBadgeCaracteristica(gc, 2, "Respuesta optima al estimulo hipertrofico", height);
        dibujarBadgeCaracteristica(gc, 3, "Eficiencia metabolica equilibrada", height);
    }

    private void dibujarEndomorfo(GraphicsContext gc, double centerX, double height) {
        gc.setFill(COLOR_ENDO);
        // Cabeza
        gc.fillOval(centerX - 20, 60, 40, 35);
        // Torso redondeado
        gc.fillOval(centerX - 36, 105, 72, 110);
        // Brazos robustos
        gc.fillRoundRect(centerX - 56, 120, 20, 65, 12, 12);
        gc.fillRoundRect(centerX + 36, 120, 20, 65, 12, 12);
        // Piernas robustas
        gc.fillRoundRect(centerX - 22, 215, 20, 100, 12, 12);
        gc.fillRoundRect(centerX + 2, 215, 20, 100, 12, 12);

        dibujarBadgeCaracteristica(gc, 1, "Estructura osea grande y robusta", height);
        dibujarBadgeCaracteristica(gc, 2, "Facilidad para ganar fuerza y masa muscular", height);
        dibujarBadgeCaracteristica(gc, 3, "Mayor propension a retener tejido adiposo", height);
    }

    private void dibujarBadgeCaracteristica(GraphicsContext gc, int index, String texto, double height) {
        double y = height - 100 + index * 26;
        double w = tipoCorporalCanvas.getWidth() - 30;

        gc.setFill(Color.web("#E8F5E9"));
        gc.fillRoundRect(15, y, w, 22, 8, 8);
        gc.setStroke(Color.web("#C8E6C9"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(15, y, w, 22, 8, 8);

        gc.setFill(TEXT_DARK);
        gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        gc.fillText(texto, 25, y + 15);
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
            sb.append("RECOMENDACIONES PARA ECTOMORFO:\n")
              .append("• Entrenamiento de hipertrofia con sobrecarga progresiva y pesos pesados.\n")
              .append("• Sesiones de fuerza de 45 a 60 minutos con descansos completos (2 a 3 min).\n")
              .append("• Enfoque en ejercicios multiarticulares (sentadillas, peso muerto, press banca).\n")
              .append("• Cardio moderado (maximo 20 a 30 minutos, 2-3 veces por semana) para preservar masa.\n")
              .append("• Superavit calorico controlado con alta densidad de carbohidratos complejos y proteinas.\n\n");
        }

        if (tipos.contains("MESOMORFO")) {
            sb.append("RECOMENDACIONES PARA MESOMORFO:\n")
              .append("• Entrenamiento periodizado combinando fuerza, hipertrofia y potencia.\n")
              .append("• Variedad en ejercicios y rangos de repeticiones para evitar estancamiento.\n")
              .append("• Cardio moderado 3 a 4 veces por semana para mantenimiento cardiovascular.\n")
              .append("• Dieta isocalorica o ciclado de carbohidratos segun la fase de composicion corporal.\n\n");
        }

        if (tipos.contains("ENDOMORFO")) {
            sb.append("RECOMENDACIONES PARA ENDOMORFO:\n")
              .append("• Combinacion de entrenamiento de fuerza en circuito y cardio HIIT/LISS frecuente.\n")
              .append("• Cardio de 35 a 50 minutos, 4 a 5 veces por semana para optimizar oxidacion de lipidos.\n")
              .append("• Control estricto de carbohidratos simples e incremento de fibra y proteinas magras.\n")
              .append("• Minimizar periodos de inactividad prolongada y priorizar el gasto energetico diario (NEAT).\n\n");
        }

        return sb.toString().trim();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atencion");
        alert.setHeaderText("Datos requeridos");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Clases del modelo
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
                desc.append("ENDOMORFO: Predominio de adiposidad relativa y formas redondeadas. Mayor capacidad de almacenamiento energetico y respuesta eficiente al desarrollo de fuerza.\n\n");
            }
            if (tipoCorpoalDominante.contains("MESOMORFO")) {
                desc.append("MESOMORFO: Predominio de desarrollo musculoesqueletico relativo. Hombros anchos, estructura osea densa y adaptabilidad rapida al entrenamiento de resistencia.\n\n");
            }
            if (tipoCorpoalDominante.contains("ECTOMORFO")) {
                desc.append("ECTOMORFO: Predominio de linealidad relativa y extremidades alargadas. Menor diametro oseo transversal, metabolismo acelerado y bajo porcentaje graso basal.\n\n");
            }

            if (desc.length() == 0) {
                desc.append("Tipo corporal balanceado con distribucion armonica de los tres componentes.");
            }

            return desc.toString().trim();
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