package calculadoraenergeticamodernaa;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sistema de Equivalentes Nutricionales (SMAE)
 * Distribución fluida que aprovecha el 100% del espacio disponible sin aplastar tablas.
 */
public class SistemaEquivalentes extends Application {

    // Paleta de colores oficial: Verdes y Blancos
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color ACCENT_GREEN = Color.web("#43A047");        // Verde acción
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color LIGHT_MINT = Color.web("#E8F5E9");          // Fondo verde menta suave
    private static final Color BORDER_GREEN = Color.web("#C8E6C9");        // Borde verde suave
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Texto oscuro
    private static final Color TEXT_MUTED = Color.web("#5C7669");          // Texto secundario

    // Componentes principales
    private TableView<Equivalente> tablaEquivalentes;
    private TableView<Nutriente> tablaDistribucion;
    private TableView<Nutriente> tablaNutrientes;
    private Label totalKcalLabel, sumaKcalLabel;
    private TextField kcalDiaField;
    private TextField porcentajeHCField, porcentajeProteinasField, porcentajeLipidosField;

    private DecimalFormat df = new DecimalFormat("#.#");
    private double kcalObjetivo = 2000.0;
    private boolean isTabletMode;

    // Porcentajes de macronutrientes por defecto
    private double porcentajeHC = 60.0;
    private double porcentajeLipidos = 15.0;
    private double porcentajeProteinas = 25.0;

    // Datos base de equivalentes según el SMAE
    private Object[][] datosBase = {
        { "Verduras", " ", 25, 2, 0, 4, "1 taza cruda o 1/2 taza cocida (ej. espinacas, calabacita, jitomate)" },
        { "Frutas", " ", 60, 0, 0, 15, "1 pieza mediana o 1 taza de fruta picada (ej. manzana, papaya, platano 1/2 pza)" },
        { "Cereales y tuberculos", "Sin Grasa", 70, 2, 0, 15, "1 tortilla, 1 rebanada de pan integral, 1/2 taza arroz o avena" },
        { "Cereales y tuberculos", "Con Grasa", 115, 2, 5, 15, "1 galleta dulce, 1/2 pza pan dulce, 1 barra de cereal" },
        { "Leguminosas", " ", 120, 8, 1, 20, "1/2 taza cocida (frijol, lenteja, garbanzo, haba)" },
        { "Alimentos de origen animal", "Muy Bajo Aporte Grasa", 40, 7, 1, 0, "30-40g pechuga de pollo/pavo, atun en agua, claras de huevo" },
        { "Alimentos de origen animal", "Bajo Aporte Grasa", 55, 7, 3, 0, "30-40g filete de res, lomo de cerdo, queso panela, jamon pavo" },
        { "Alimentos de origen animal", "Moderado Aporte Grasa", 75, 7, 5, 0, "1 huevo entero, 30g queso fresco/oaxaca, carne cerdo molida" },
        { "Alimentos de origen animal", "Alto Aporte Grasa", 100, 7, 8, 0, "30g queso amarillo/manchego, chuleta frita, longaniza" },
        { "Leche", "Descremada", 95, 9, 2, 12, "1 taza (240 ml) leche descremada o yogurt light sin azucar" },
        { "Leche", "Semidescremada", 110, 9, 4, 12, "1 taza (240 ml) leche semidescremada" },
        { "Leche", "Entera", 150, 9, 8, 12, "1 taza (240 ml) leche entera pasteurizada" },
        { "Leche", "Con Azucar", 200, 8, 5, 30, "1 taza leche con chocolate o yogurt con azucar" },
        { "Aceites y grasas", "Sin Proteina", 45, 0, 5, 0, "1 cdita aceite vegetal, 1/3 pza aguacate, 2 cdas mayonesa light" },
        { "Aceites y grasas", "Con Proteina", 70, 3, 5, 3, "3 cdas cacahuates, 14 pzas almendras, 3 pzas nueces" },
        { "Azucares", "Sin Grasa", 40, 0, 0, 10, "2 cditas azucar mascabado, 1 cda miel abeja, 1 pza gelatina" },
        { "Azucares", "Con Grasa", 85, 0, 5, 10, "1 barrita chocolate con leche (15g), 1 bola helado" }
    };

    private ObservableList<Equivalente> equivalentesData;

    public SistemaEquivalentes() {
        this(2000.0);
    }

    public SistemaEquivalentes(double kcalCalculadas) {
        this.kcalObjetivo = kcalCalculadas > 0 ? kcalCalculadas : 2000.0;
        this.isTabletMode = ResponsiveManager.isTabletMode();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("NutriEnergia Pro - Sistema de Equivalentes Nutricionales (SMAE)");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // Header moderno
        VBox header = crearHeader();
        root.setTop(header);

        // Centro con tablas que se expanden para ocupar todo el espacio vertical y horizontal
        Node centerPanel = crearPanelCentral();
        root.setCenter(centerPanel);

        // Footer con botones de acción visibles
        HBox footer = crearFooter();
        root.setBottom(footer);

        // Dimensiones automáticas según la pantalla
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1160, Math.max(900, screenBounds.getWidth() * 0.92));
        double height = Math.min(760, Math.max(620, screenBounds.getHeight() * 0.90));

        Scene scene = new Scene(root, width, height);
        cargarCSS(scene);

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(560);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Inicializar cálculos
        actualizarTablaDistribucion();
        calcularTotales();
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
            } catch (Exception ignored) {}
        }
    }

    private VBox crearHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1B5E20, #2E7D32, #388E3C); " +
            "-fx-background-radius: 0 0 12 12;"
        );
        header.setPadding(new Insets(10, 18, 10, 18));

        // Fila 1: Título y subtítulo
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER);

        Label badge = new Label("SMAE 5a EDICION");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9.5));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2); " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 2 7;"
        );

        Label titulo = new Label("Sistema de Equivalentes Nutricionales");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17.5));
        titulo.setTextFill(Color.WHITE);

        titleRow.getChildren().addAll(badge, titulo);

        // Fila 2: Métricas de Kcal + Controles de Distribución
        HBox controlsRow = new HBox(14);
        controlsRow.setAlignment(Pos.CENTER);

        totalKcalLabel = crearBadgeMetrica("OBJETIVO", String.format("%.0f kcal", kcalObjetivo));
        sumaKcalLabel = crearBadgeMetrica("SUMA ACTUAL", "0 kcal");

        // Panel de entrada compacto
        HBox inputsRow = new HBox(8);
        inputsRow.setAlignment(Pos.CENTER);
        inputsRow.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.14); " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 4 10;"
        );

        // Kcal input
        Label kcalLbl = new Label("Kcal/dia:");
        kcalLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        kcalLbl.setTextFill(Color.WHITE);
        kcalDiaField = new TextField(String.format("%.0f", kcalObjetivo));
        kcalDiaField.setPrefWidth(65);
        kcalDiaField.setAlignment(Pos.CENTER);
        estilizarInputHeader(kcalDiaField);

        // HC %
        Label hcLbl = new Label("HC%:");
        hcLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        hcLbl.setTextFill(Color.WHITE);
        porcentajeHCField = new TextField(String.valueOf((int) porcentajeHC));
        porcentajeHCField.setPrefWidth(40);
        porcentajeHCField.setAlignment(Pos.CENTER);
        estilizarInputHeader(porcentajeHCField);

        // Prot %
        Label protLbl = new Label("Prot%:");
        protLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        protLbl.setTextFill(Color.WHITE);
        porcentajeProteinasField = new TextField(String.valueOf((int) porcentajeProteinas));
        porcentajeProteinasField.setPrefWidth(40);
        porcentajeProteinasField.setAlignment(Pos.CENTER);
        estilizarInputHeader(porcentajeProteinasField);

        // Lip %
        Label lipLbl = new Label("Lip%:");
        lipLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lipLbl.setTextFill(Color.WHITE);
        porcentajeLipidosField = new TextField(String.valueOf((int) porcentajeLipidos));
        porcentajeLipidosField.setPrefWidth(40);
        porcentajeLipidosField.setAlignment(Pos.CENTER);
        estilizarInputHeader(porcentajeLipidosField);

        // Botón Actualizar
        Button actualizarBtn = new Button("ACTUALIZAR");
        actualizarBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        actualizarBtn.setTextFill(DARK_FOREST);
        actualizarBtn.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 4 12; " +
            "-fx-cursor: hand;"
        );
        actualizarBtn.setOnAction(e -> {
            if (validarYActualizarPorcentajes()) {
                actualizarTablaDistribucion();
                calcularTotales();
            }
        });

        inputsRow.getChildren().addAll(
            kcalLbl, kcalDiaField,
            hcLbl, porcentajeHCField,
            protLbl, porcentajeProteinasField,
            lipLbl, porcentajeLipidosField,
            actualizarBtn
        );

        controlsRow.getChildren().addAll(totalKcalLabel, sumaKcalLabel, inputsRow);

        header.getChildren().addAll(titleRow, controlsRow);
        header.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.12)));

        return header;
    }

    private void estilizarInputHeader(TextField field) {
        field.setStyle(
            "-fx-background-color: #FFFFFF; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 3 4; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold;"
        );
    }

    private Label crearBadgeMetrica(String titulo, String valor) {
        Label label = new Label(titulo + ": " + valor);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        label.setTextFill(Color.WHITE);
        label.setAlignment(Pos.CENTER);
        label.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.22); " +
            "-fx-border-color: rgba(255, 255, 255, 0.3); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 4 10;"
        );
        return label;
    }

    private boolean validarYActualizarPorcentajes() {
        try {
            double hc = Double.parseDouble(porcentajeHCField.getText().trim());
            double prot = Double.parseDouble(porcentajeProteinasField.getText().trim());
            double lip = Double.parseDouble(porcentajeLipidosField.getText().trim());

            double suma = hc + prot + lip;

            if (Math.abs(suma - 100.0) > 0.1) {
                mostrarAlerta("Distribucion Porcentual", "La suma de porcentajes debe ser exactamente 100%.\nSuma actual: " + df.format(suma) + "%");
                porcentajeHCField.setText(String.valueOf((int) porcentajeHC));
                porcentajeProteinasField.setText(String.valueOf((int) porcentajeProteinas));
                porcentajeLipidosField.setText(String.valueOf((int) porcentajeLipidos));
                return false;
            }

            if (hc < 0 || prot < 0 || lip < 0) {
                mostrarAlerta("Valores Invalidos", "Los porcentajes no pueden ser negativos.");
                return false;
            }

            porcentajeHC = hc;
            porcentajeProteinas = prot;
            porcentajeLipidos = lip;
            return true;

        } catch (NumberFormatException e) {
            mostrarAlerta("Formato Invalido", "Por favor ingresa valores numericos validos.");
            return false;
        }
    }

    private Node crearPanelCentral() {
        HBox tablesLayout = new HBox(14);
        tablesLayout.setAlignment(Pos.CENTER);
        tablesLayout.setPadding(new Insets(14));

        // 1. Tabla Principal de Equivalentes (Izquierda / Centro) - Expande al 100% de alto y ancho disponible
        VBox tablaEquivalentesPanel = crearTablaEquivalentes();
        HBox.setHgrow(tablaEquivalentesPanel, Priority.ALWAYS);

        // 2. Panel Derecho: Tablas de Distribución y Nutrientes Calculados - Expanden verticalmente
        VBox rightPanel = crearPanelDerecho();

        tablesLayout.getChildren().addAll(tablaEquivalentesPanel, rightPanel);
        return tablesLayout;
    }

    private VBox crearTablaEquivalentes() {
        VBox container = new VBox(8);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 12 14;"
        );
        container.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("DISTRIBUCION POR EQUIVALENTES (SMAE)");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
        titulo.setTextFill(PRIMARY_GREEN);

        Label subinfo = new Label("- Ajusta las porciones con las flechas o escribiendo el numero");
        subinfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        subinfo.setTextFill(TEXT_MUTED);

        headerBox.getChildren().addAll(titulo, subinfo);

        tablaEquivalentes = new TableView<>();
        tablaEquivalentes.setEditable(true);
        tablaEquivalentes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columnas
        TableColumn<Equivalente, String> grupoCol = new TableColumn<>("Grupo de Alimento");
        grupoCol.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        grupoCol.setPrefWidth(160);

        TableColumn<Equivalente, String> subgrupoCol = new TableColumn<>("Subgrupo");
        subgrupoCol.setCellValueFactory(new PropertyValueFactory<>("subgrupo"));
        subgrupoCol.setPrefWidth(120);

        TableColumn<Equivalente, Integer> porcionesCol = new TableColumn<>("Porciones");
        porcionesCol.setCellValueFactory(new PropertyValueFactory<>("porciones"));
        porcionesCol.setCellFactory(column -> new TableCell<Equivalente, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(0, 99, 0);

            {
                spinner.setEditable(true);
                spinner.setPrefWidth(70);
                spinner.getEditor().setAlignment(Pos.CENTER);
                spinner.getEditor().setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && getTableRow() != null && getTableRow().getItem() != null) {
                        Equivalente eq = getTableRow().getItem();
                        if (eq.getPorciones() != newVal) {
                            eq.setPorciones(newVal);
                            calcularTotales();
                            tablaEquivalentes.refresh();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    spinner.getValueFactory().setValue(item);
                    setGraphic(spinner);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        porcionesCol.setPrefWidth(85);

        TableColumn<Equivalente, Integer> energiaCol = new TableColumn<>("Energia (kcal)");
        energiaCol.setCellValueFactory(new PropertyValueFactory<>("energiaTotal"));
        energiaCol.setPrefWidth(85);

        TableColumn<Equivalente, Integer> proteinasCol = new TableColumn<>("Proteinas (g)");
        proteinasCol.setCellValueFactory(new PropertyValueFactory<>("proteinasTotal"));
        proteinasCol.setPrefWidth(85);

        TableColumn<Equivalente, Integer> lipidosCol = new TableColumn<>("Lipidos (g)");
        lipidosCol.setCellValueFactory(new PropertyValueFactory<>("lipidosTotal"));
        lipidosCol.setPrefWidth(80);

        TableColumn<Equivalente, Integer> hcCol = new TableColumn<>("HC (g)");
        hcCol.setCellValueFactory(new PropertyValueFactory<>("hcTotal"));
        hcCol.setPrefWidth(80);

        // Resaltar con verde menta cuando porciones > 0
        tablaEquivalentes.setRowFactory(tv -> new TableRow<Equivalente>() {
            @Override
            protected void updateItem(Equivalente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getPorciones() > 0) {
                    setStyle("-fx-background-color: #E8F5E9; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        tablaEquivalentes.getColumns().addAll(
            grupoCol, subgrupoCol, porcionesCol, energiaCol,
            proteinasCol, lipidosCol, hcCol
        );

        // Cargar datos
        equivalentesData = FXCollections.observableArrayList();
        for (Object[] d : datosBase) {
            equivalentesData.add(new Equivalente(
                (String) d[0],
                (String) d[1],
                0,
                (Integer) d[2],
                (Integer) d[3],
                (Integer) d[4],
                (Integer) d[5],
                (String) d[6]
            ));
        }
        tablaEquivalentes.setItems(equivalentesData);

        // Permitir que la tabla crezca al 100% del alto disponible
        VBox.setVgrow(tablaEquivalentes, Priority.ALWAYS);
        tablaEquivalentes.setMaxHeight(Double.MAX_VALUE);

        container.getChildren().addAll(headerBox, tablaEquivalentes);
        return container;
    }

    private VBox crearPanelDerecho() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(350);
        panel.setMaxWidth(370);

        // 1. Tabla de Distribución Teórica
        VBox distribucionPanel = crearTablaDistribucion();

        // 2. Tabla de Nutrientes Calculados con Semáforo de Adecuación
        VBox nutrientesPanel = crearTablaNutrientes();

        panel.getChildren().addAll(distribucionPanel, nutrientesPanel);
        return panel;
    }

    private VBox crearTablaDistribucion() {
        VBox container = new VBox(6);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 10 12;"
        );
        container.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label titulo = new Label("DISTRIBUCION TEORICA DE MACRONUTRIENTES");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        titulo.setTextFill(PRIMARY_GREEN);

        tablaDistribucion = new TableView<>();
        tablaDistribucion.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Nutriente, String> macroCol = new TableColumn<>("Macro");
        macroCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        macroCol.setPrefWidth(70);

        TableColumn<Nutriente, String> porcentajeCol = new TableColumn<>("%");
        porcentajeCol.setCellValueFactory(new PropertyValueFactory<>("porcentaje"));
        porcentajeCol.setPrefWidth(55);

        TableColumn<Nutriente, String> kcalCol = new TableColumn<>("Kcal");
        kcalCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        kcalCol.setPrefWidth(65);

        TableColumn<Nutriente, String> gramosCol = new TableColumn<>("Gramos (g)");
        gramosCol.setCellValueFactory(new PropertyValueFactory<>("gramos"));
        gramosCol.setPrefWidth(70);

        tablaDistribucion.getColumns().addAll(macroCol, porcentajeCol, kcalCol, gramosCol);

        // Altura exacta para 4 filas sin relleno vacío
        tablaDistribucion.setFixedCellSize(28.0);
        tablaDistribucion.prefHeightProperty().bind(tablaDistribucion.fixedCellSizeProperty().multiply(4).add(32));
        tablaDistribucion.minHeightProperty().bind(tablaDistribucion.prefHeightProperty());
        tablaDistribucion.maxHeightProperty().bind(tablaDistribucion.prefHeightProperty());

        container.getChildren().addAll(titulo, tablaDistribucion);
        return container;
    }

    private VBox crearTablaNutrientes() {
        VBox container = new VBox(6);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 10 12;"
        );
        container.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label titulo = new Label("NUTRIENTES CALCULADOS Y ADECUACION");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        titulo.setTextFill(PRIMARY_GREEN);

        tablaNutrientes = new TableView<>();
        tablaNutrientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Nutriente, String> nutrienteCol = new TableColumn<>("Nutriente");
        nutrienteCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nutrienteCol.setPrefWidth(65);

        TableColumn<Nutriente, String> totalKcalCol = new TableColumn<>("Kcal");
        totalKcalCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        totalKcalCol.setPrefWidth(60);

        TableColumn<Nutriente, String> totalGramosCol = new TableColumn<>("Gramos (g)");
        totalGramosCol.setCellValueFactory(new PropertyValueFactory<>("gramos"));
        totalGramosCol.setPrefWidth(65);

        TableColumn<Nutriente, String> adecuacionCol = new TableColumn<>("% Adec.");
        adecuacionCol.setCellValueFactory(new PropertyValueFactory<>("adecuacion"));
        adecuacionCol.setPrefWidth(70);

        // Semáforo visual para % de Adecuación
        adecuacionCol.setCellFactory(column -> new TableCell<Nutriente, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    try {
                        double val = Double.parseDouble(item.replace("%", "").trim());
                        if (val >= 95.0 && val <= 105.0) {
                            setStyle(
                                "-fx-background-color: #E8F5E9; " +
                                "-fx-text-fill: #2E7D32; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 4;"
                            );
                        } else if ((val >= 90.0 && val < 95.0) || (val > 105.0 && val <= 110.0)) {
                            setStyle(
                                "-fx-background-color: #FFF9C4; " +
                                "-fx-text-fill: #F57F17; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 4;"
                            );
                        } else {
                            setStyle(
                                "-fx-background-color: #FFEBEE; " +
                                "-fx-text-fill: #C62828; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 4;"
                            );
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });

        tablaNutrientes.getColumns().addAll(nutrienteCol, totalKcalCol, totalGramosCol, adecuacionCol);

        // Altura exacta para 4 filas sin relleno vacío
        tablaNutrientes.setFixedCellSize(28.0);
        tablaNutrientes.prefHeightProperty().bind(tablaNutrientes.fixedCellSizeProperty().multiply(4).add(32));
        tablaNutrientes.minHeightProperty().bind(tablaNutrientes.prefHeightProperty());
        tablaNutrientes.maxHeightProperty().bind(tablaNutrientes.prefHeightProperty());

        container.getChildren().addAll(titulo, tablaNutrientes);
        return container;
    }

    private void actualizarTablaDistribucion() {
        try {
            double kcalTotal = Double.parseDouble(kcalDiaField.getText().trim());

            double kcalHC = kcalTotal * (porcentajeHC / 100.0);
            double gramosHC = kcalHC / 4.0;

            double kcalProteinas = kcalTotal * (porcentajeProteinas / 100.0);
            double gramosProteinas = kcalProteinas / 4.0;

            double kcalLipidos = kcalTotal * (porcentajeLipidos / 100.0);
            double gramosLipidos = kcalLipidos / 9.0;

            double gramosTotal = gramosHC + gramosProteinas + gramosLipidos;

            ObservableList<Nutriente> datos = FXCollections.observableArrayList(
                new Nutriente("HC", df.format(porcentajeHC) + "%", df.format(kcalHC), df.format(gramosHC), ""),
                new Nutriente("Lipidos", df.format(porcentajeLipidos) + "%", df.format(kcalLipidos), df.format(gramosLipidos), ""),
                new Nutriente("Proteinas", df.format(porcentajeProteinas) + "%", df.format(kcalProteinas), df.format(gramosProteinas), ""),
                new Nutriente("Total", "100%", df.format(kcalTotal), df.format(gramosTotal), "")
            );

            tablaDistribucion.setItems(datos);
            totalKcalLabel.setText("OBJETIVO: " + df.format(kcalTotal) + " kcal");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calcularTotales() {
        double totalKcal = 0;
        double totalProteinas = 0;
        double totalLipidos = 0;
        double totalHC = 0;

        for (Equivalente eq : equivalentesData) {
            totalKcal += eq.getEnergiaTotal();
            totalProteinas += eq.getProteinasTotal();
            totalLipidos += eq.getLipidosTotal();
            totalHC += eq.getHcTotal();
        }

        try {
            double kcalRef = Double.parseDouble(kcalDiaField.getText().trim());

            double kcalHC = totalHC * 4;
            double kcalProteinas = totalProteinas * 4;
            double kcalLipidos = totalLipidos * 9;

            double kcalEsperadaHC = kcalRef * (porcentajeHC / 100.0);
            double kcalEsperadaProteinas = kcalRef * (porcentajeProteinas / 100.0);
            double kcalEsperadaLipidos = kcalRef * (porcentajeLipidos / 100.0);

            double adecHC = kcalEsperadaHC > 0 ? (kcalHC / kcalEsperadaHC) * 100 : 0;
            double adecProt = kcalEsperadaProteinas > 0 ? (kcalProteinas / kcalEsperadaProteinas) * 100 : 0;
            double adecLip = kcalEsperadaLipidos > 0 ? (kcalLipidos / kcalEsperadaLipidos) * 100 : 0;
            double adecTotal = kcalRef > 0 ? (totalKcal / kcalRef) * 100 : 0;

            ObservableList<Nutriente> datosNutrientes = FXCollections.observableArrayList(
                new Nutriente("HC", "", df.format(kcalHC), df.format(totalHC), df.format(adecHC) + "%"),
                new Nutriente("Lipidos", "", df.format(kcalLipidos), df.format(totalLipidos), df.format(adecLip) + "%"),
                new Nutriente("Proteinas", "", df.format(kcalProteinas), df.format(totalProteinas), df.format(adecProt) + "%"),
                new Nutriente("Total", "", df.format(totalKcal), df.format(totalProteinas + totalLipidos + totalHC), df.format(adecTotal) + "%")
            );

            tablaNutrientes.setItems(datosNutrientes);

            // Actualizar badge superior de Suma Kcal y proximidad
            sumaKcalLabel.setText("SUMA: " + df.format(totalKcal) + " kcal");

            double diferencia = Math.abs(totalKcal - kcalRef);
            if (diferencia <= 50) {
                sumaKcalLabel.setStyle(
                    "-fx-background-color: rgba(46, 125, 50, 0.7); " +
                    "-fx-border-color: #81C784; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 4 10;"
                );
            } else if (diferencia <= 150) {
                sumaKcalLabel.setStyle(
                    "-fx-background-color: rgba(245, 127, 23, 0.7); " +
                    "-fx-border-color: #FFF59D; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 4 10;"
                );
            } else {
                sumaKcalLabel.setStyle(
                    "-fx-background-color: rgba(198, 40, 40, 0.7); " +
                    "-fx-border-color: #EF9A9A; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-padding: 4 10;"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox crearFooter() {
        HBox footer = new HBox(14);
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E8E3; -fx-border-width: 1.5 0 0 0;");
        footer.setPadding(new Insets(10, 16, 12, 16));

        Button r24hBtn = crearBotonAccion("RECORDATORIO 24H", DARK_FOREST);
        r24hBtn.setOnAction(e -> abrirRecordatorio());

        Button planBtn = crearBotonAccion("PLAN ALIMENTICIO", PRIMARY_GREEN);
        planBtn.setOnAction(e -> abrirPlanAlimenticio());

        Button resetBtn = crearBotonAccion("REINICIAR PORCIONES", Color.web("#C62828"));
        resetBtn.setOnAction(e -> reiniciarTabla());

        Button exportBtn = crearBotonAccion("EXPORTAR DIETA", ACCENT_GREEN);
        exportBtn.setOnAction(e -> exportarDatos());

        footer.getChildren().addAll(r24hBtn, planBtn, resetBtn, exportBtn);
        return footer;
    }

    private Button crearBotonAccion(String texto, Color color) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        String hex = toRGBCode(color);

        btn.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 18; " +
            "-fx-padding: 8 18; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 3, 0, 0, 1);", hex));

        btn.setOnMouseEntered(e -> btn.setStyle(String.format(
            "-fx-background-color: derive(%s, -12%%); " +
            "-fx-background-radius: 18; " +
            "-fx-padding: 8 18; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);", hex)));

        btn.setOnMouseExited(e -> btn.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 18; " +
            "-fx-padding: 8 18; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 3, 0, 0, 1);", hex)));

        return btn;
    }

    private void reiniciarTabla() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Reinicio");
        confirmacion.setHeaderText("¿Deseas reiniciar todas las porciones a 0?");
        confirmacion.setContentText("Esta accion restablecera los valores calculados de la tabla.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (Equivalente eq : equivalentesData) {
                    eq.setPorciones(0);
                }
                tablaEquivalentes.refresh();
                calcularTotales();
            }
        });
    }

    private void exportarDatos() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("      SISTEMA MEXICANO DE ALIMENTOS EQUIVALENTES    \n");
        sb.append("====================================================\n\n");

        sb.append("1. OBJETIVO DIETETICO:\n");
        sb.append(String.format("• Kcal Diarias Objetivo: %s kcal\n", kcalDiaField.getText()));
        sb.append(String.format("• Hidratos de Carbono:   %.1f%%\n", porcentajeHC));
        sb.append(String.format("• Proteinas:             %.1f%%\n", porcentajeProteinas));
        sb.append(String.format("• Lipidos:               %.1f%%\n\n", porcentajeLipidos));

        sb.append("2. EQUIVALENTES SELECCIONADOS:\n");
        boolean haySeleccion = false;
        for (Equivalente eq : equivalentesData) {
            if (eq.getPorciones() > 0) {
                haySeleccion = true;
                sb.append(String.format("• %-26s | %-16s | %2d porc. | %4d kcal | P:%3dg | L:%3dg | HC:%3dg\n",
                    eq.getGrupo(), eq.getSubgrupo(), eq.getPorciones(),
                    eq.getEnergiaTotal(), eq.getProteinasTotal(), eq.getLipidosTotal(), eq.getHcTotal()));
            }
        }
        if (!haySeleccion) {
            sb.append("  (No hay grupos con porciones asignadas mayores a 0)\n");
        }

        sb.append("\n3. NUTRIENTES CALCULADOS:\n");
        for (Nutriente n : tablaNutrientes.getItems()) {
            sb.append(String.format("• %-10s: %6s kcal | %5s g | Adec: %s\n",
                n.getNombre(), n.getKcal(), n.getGramos(), n.getAdecuacion()));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportacion de Plan de Equivalentes");
        alert.setHeaderText("Resumen Dietetico Generado");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(Font.font("Consolas", 12));
        textArea.setPrefRowCount(18);
        textArea.setPrefWidth(620);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void abrirRecordatorio() {
        if (WindowManager.enfocarSiAbierta("Recordatorio")) {
            return;
        }

        try {
            double totalHC = 0, totalLipidos = 0, totalProteinas = 0;
            for (Equivalente eq : equivalentesData) {
                totalProteinas += eq.getProteinasTotal();
                totalLipidos += eq.getLipidosTotal();
                totalHC += eq.getHcTotal();
            }

            Stage stage = new Stage();
            WindowManager.registrarVentana("Recordatorio", stage);
            Recordatorio recordatorio = new Recordatorio(totalHC, totalLipidos, totalProteinas);
            recordatorio.start(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Error al abrir Recordatorio 24H", ex.getMessage());
        }
    }

    private void abrirPlanAlimenticio() {
        if (WindowManager.enfocarSiAbierta("PlanAlimenticio")) {
            return;
        }

        try {
            List<Map<String, Object>> seleccion = new ArrayList<>();
            double kcalDiarias = kcalObjetivo;

            try {
                kcalDiarias = Double.parseDouble(kcalDiaField.getText().trim());
            } catch (Exception ignored) {}

            double sumHC = 0, sumLipidos = 0, sumProteinas = 0;
            for (Equivalente eq : equivalentesData) {
                if (eq.getPorciones() > 0) {
                    sumProteinas += eq.getProteinasTotal();
                    sumLipidos += eq.getLipidosTotal();
                    sumHC += eq.getHcTotal();

                    Map<String, Object> m = new HashMap<>();
                    m.put("grupo", eq.getGrupo());
                    m.put("subgrupo", eq.getSubgrupo());
                    m.put("porciones", eq.getPorciones());
                    seleccion.add(m);
                }
            }

            if (seleccion.isEmpty()) {
                mostrarAlerta("Sin equivalentes seleccionados",
                    "Por favor asigna porciones a los grupos de alimentos antes de abrir el Plan Alimenticio.");
                return;
            }

            Stage stage = new Stage();
            WindowManager.registrarVentana("PlanAlimenticio", stage);
            PlanAlimenticio plan = new PlanAlimenticio(sumHC, sumLipidos, sumProteinas, seleccion, kcalDiarias);
            plan.start(stage);

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Error al abrir Plan Alimenticio", ex.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atencion");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    // Modelos de datos para TableView
    public static class Equivalente {
        private String grupo;
        private String subgrupo;
        private int porciones;
        private int energiaBase;
        private int proteinasBase;
        private int lipidosBase;
        private int hcBase;
        private String descripcion;

        public Equivalente(String grupo, String subgrupo, int porciones,
                           int energiaBase, int proteinasBase, int lipidosBase, int hcBase, String descripcion) {
            this.grupo = grupo;
            this.subgrupo = subgrupo;
            this.porciones = porciones;
            this.energiaBase = energiaBase;
            this.proteinasBase = proteinasBase;
            this.lipidosBase = lipidosBase;
            this.hcBase = hcBase;
            this.descripcion = descripcion;
        }

        public String getGrupo() { return grupo; }
        public String getSubgrupo() { return subgrupo; }
        public int getPorciones() { return porciones; }
        public void setPorciones(int porciones) { this.porciones = porciones; }
        public int getEnergiaTotal() { return energiaBase * porciones; }
        public int getProteinasTotal() { return proteinasBase * porciones; }
        public int getLipidosTotal() { return lipidosBase * porciones; }
        public int getHcTotal() { return hcBase * porciones; }
        public String getDescripcion() { return descripcion; }
    }

    public static class Nutriente {
        private String nombre;
        private String porcentaje;
        private String kcal;
        private String gramos;
        private String adecuacion;

        public Nutriente(String nombre, String porcentaje, String kcal, String gramos, String adecuacion) {
            this.nombre = nombre;
            this.porcentaje = porcentaje;
            this.kcal = kcal;
            this.gramos = gramos;
            this.adecuacion = adecuacion;
        }

        public String getNombre() { return nombre; }
        public String getPorcentaje() { return porcentaje; }
        public String getKcal() { return kcal; }
        public String getGramos() { return gramos; }
        public String getAdecuacion() { return adecuacion; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}