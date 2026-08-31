package calculadoraenergeticamodernaa;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Recordatorio Alimenticio 24 Horas (R24H)
 * Versión de alto rendimiento:
 * - Carga asíncrona en segundo plano con caché de listas
 * - Desplazamiento vertical 100% fluido con redirección de eventos de scroll
 * - Encabezados de columnas con badges de alto contraste para grupos y subgrupos
 * - Tablas de resumen fijas: 'Nutrientes Actuales (g)' y 'Porcentaje de Nutrientes (%)'
 */
public class Recordatorio extends Application {

    // Paleta de colores oficial: Verdes y Blancos de alta legibilidad
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color LIGHT_MINT = Color.web("#E8F5E9");          // Fondo verde menta suave
    private static final Color BORDER_GREEN = Color.web("#C8E6C9");        // Borde verde suave
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Texto oscuro
    private static final Color TEXT_MUTED = Color.web("#4A6356");          // Texto secundario

    // Componentes principales
    private TableView<AlimentoComida> tablaDesayuno;
    private TableView<AlimentoComida> tablaComida;
    private TableView<AlimentoComida> tablaCena;
    private TableView<NutrienteItem> tablaNutrientesGramos;
    private TableView<NutrienteItem> tablaPorcentajes;
    private Label totalKcalLabel;
    private Label estadoCargaLabel;
    private ProgressBar barraProgreso;
    private ScrollPane scrollPrincipal;

    // Valores ideales u objetivos
    private double idealHc = 0;
    private double idealLipidos = 0;
    private double idealProteinas = 0;

    // Totales calculados en tiempo real
    private double totalHc = 0;
    private double totalLipidos = 0;
    private double totalProteinas = 0;

    // Mapas de datos y caché de listas
    private Map<String, List<String>> datosExcel = new HashMap<>();
    private Map<String, ObservableList<String>> cachedListasAlimentos = new HashMap<>();
    private Map<String, Map<String, Double>> nutrientesAlimentos = new HashMap<>();
    private Map<String, Map<String, Double>> nutrientesPlatillos = new HashMap<>();
    private ObservableList<String> listaPlatillos = FXCollections.observableArrayList();

    // 4 slots de platillos específicos por comida
    private Map<String, List<PlatilloSeleccionado>> platillosEspecificosSeleccionados = new HashMap<>();

    private DecimalFormat df = new DecimalFormat("#.##");
    private boolean isTabletMode;

    // 17 Grupos oficiales del SMAE
    private final List<String> todosLosGrupos = Arrays.asList(
        "Verduras",
        "Frutas",
        "Cereales y tubérculos - Sin Grasa",
        "Cereales y tubérculos - Con Grasa",
        "Leguminosas",
        "Alimentos de origen animal - MBAG",
        "Alimentos de origen animal - BAG",
        "Alimentos de origen animal - MAG",
        "Alimentos de origen animal - AAG",
        "Leche - Descremada",
        "Leche - Semi",
        "Leche - Entera",
        "Leche - Con Azucar",
        "Aceite y grasa - Sin proteina",
        "Aceite y grasa - Con proteina",
        "Azucar - Sin grasa",
        "Azucar - Con grasa"
    );

    // Mapeo a nombres de hojas Excel
    private static final Map<String, List<String>> nombreExcel = new HashMap<>();
    static {
        nombreExcel.put("Verduras", List.of("Verduras"));
        nombreExcel.put("Frutas", List.of("Frutas"));
        nombreExcel.put("Cereales y tubérculos - Sin Grasa", List.of("Cereales SG", "Cereales y tubérculos - Sin Grasa"));
        nombreExcel.put("Cereales y tubérculos - Con Grasa", List.of("Cereales CG", "Cereales y tubérculos - Con Grasa"));
        nombreExcel.put("Leguminosas", List.of("Leguminosas"));
        nombreExcel.put("Alimentos de origen animal - MBAG", List.of("AOA de muy bajo aporte de grasa", "AOA Muy Bajo", "AOA MRAG"));
        nombreExcel.put("Alimentos de origen animal - BAG", List.of("AOA de bajo aporte de grasa", "AOA Bajo"));
        nombreExcel.put("Alimentos de origen animal - MAG", List.of("AOA de Moderado aporte de grasa", "AOA Moderado"));
        nombreExcel.put("Alimentos de origen animal - AAG", List.of("AOA de Alto aporte de grasa", "AOA Alto"));
        nombreExcel.put("Leche - Descremada", List.of("Leche Descremada"));
        nombreExcel.put("Leche - Semi", List.of("Leche Semi"));
        nombreExcel.put("Leche - Entera", List.of("Leche Entera"));
        nombreExcel.put("Leche - Con Azucar", List.of("Leche Con Azucar"));
        nombreExcel.put("Aceite y grasa - Sin proteina", List.of("Grasas Sin Proteina"));
        nombreExcel.put("Aceite y grasa - Con proteina", List.of("Grasas Con Proteina"));
        nombreExcel.put("Azucar - Sin grasa", List.of("Azucares sin grasas", "Azucares"));
        nombreExcel.put("Azucar - Con grasa", List.of("Azucares con grasas", "Azucares Con Grasa"));
    }

    public Recordatorio() {
        this(0, 0, 0);
    }

    public Recordatorio(double hc, double lipidos, double proteinas) {
        this.idealHc = hc;
        this.idealLipidos = lipidos;
        this.idealProteinas = proteinas;
        this.isTabletMode = ResponsiveManager.isTabletMode();

        inicializarListas();
    }

    private void inicializarListas() {
        String[] comidas = { "DESAYUNO", "COMIDA", "CENA" };
        for (String c : comidas) {
            List<PlatilloSeleccionado> slots = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                slots.add(new PlatilloSeleccionado("", 1));
            }
            platillosEspecificosSeleccionados.put(c, slots);
        }

        for (String grupo : todosLosGrupos) {
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add("");
            cachedListasAlimentos.put(grupo, items);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("NutriEnergia Pro - Recordatorio Alimenticio 24 Horas");

        // Registrar ventana en el WindowManager para evitar duplicados y soportar cierre maestro
        WindowManager.registrarVentana("Recordatorio", primaryStage);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // Header institucional moderno
        VBox header = crearHeader();
        root.setTop(header);

        // Centro con scroll fluido
        scrollPrincipal = crearPanelCentral();
        root.setCenter(scrollPrincipal);

        // Footer con botones de acción
        HBox footer = crearFooter();
        root.setBottom(footer);

        double width = isTabletMode ? 
            Math.min(1280, ResponsiveManager.getScreenBounds().getWidth() - 30) : 1240;
        double height = isTabletMode ? 
            Math.min(860, ResponsiveManager.getScreenBounds().getHeight() - 40) : 800;

        Scene scene = new Scene(root, width, height);
        cargarCSS(scene);

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Iniciar carga asíncrona de datos en hilo de fondo
        iniciarCargaAsincrona();
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

        // Fila 1: Badge y Título
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER);

        Label badge = new Label("EVALUACION NUTRICIONAL R24H");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2); " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 3 8;"
        );

        Label titulo = new Label("Recordatorio Alimenticio de 24 Horas");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titulo.setTextFill(Color.WHITE);

        topRow.getChildren().addAll(badge, titulo);

        // Fila 2: Chips de Metas Dietéticas y Progreso
        HBox metricsRow = new HBox(14);
        metricsRow.setAlignment(Pos.CENTER);

        Label idealChip = crearChipMetrica("OBJETIVOS DIETETICOS",
            String.format("HC: %.1fg | Lip: %.1fg | Prot: %.1fg", idealHc, idealLipidos, idealProteinas));

        totalKcalLabel = crearChipMetrica("ENERGIA CONSUMIDA", "0.0 kcal");

        HBox cargaBox = new HBox(8);
        cargaBox.setAlignment(Pos.CENTER);

        estadoCargaLabel = new Label("Cargando SMAE y Platillos...");
        estadoCargaLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        estadoCargaLabel.setTextFill(Color.web("#C8E6C9"));

        barraProgreso = new ProgressBar();
        barraProgreso.setPrefWidth(90);
        barraProgreso.setPrefHeight(10);
        barraProgreso.setStyle("-fx-accent: #81C784;");

        cargaBox.getChildren().addAll(estadoCargaLabel, barraProgreso);

        metricsRow.getChildren().addAll(idealChip, totalKcalLabel, cargaBox);

        header.getChildren().addAll(topRow, metricsRow);
        header.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.12)));

        return header;
    }

    private Label crearChipMetrica(String titulo, String valor) {
        Label chip = new Label(titulo + ": " + valor);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        chip.setTextFill(Color.WHITE);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.22); " +
            "-fx-border-color: rgba(255, 255, 255, 0.3); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 3 10;"
        );
        return chip;
    }

    private ScrollPane crearPanelCentral() {
        VBox mainContainer = new VBox(16);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(14, 16, 24, 16));

        // 1. Platillos Tradicionales Mexicanos
        VBox platillosSeccion = crearSeccionPlatillos();

        // 2. Tablas de Comidas (17 Grupos SMAE con cabeceras de alto contraste)
        VBox comidasSeccion = crearSeccionComidas();

        // 3. Tablas Finales: 'Nutrientes Actuales (g)' y 'Porcentaje de Nutrientes (%)'
        VBox resumenNutrientesSeccion = crearSeccionResumenNutrientes();

        mainContainer.getChildren().addAll(platillosSeccion, comidasSeccion, resumenNutrientesSeccion);

        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearSeccionPlatillos() {
        VBox container = new VBox(6);
        container.setAlignment(Pos.TOP_LEFT);

        Label secTitle = new Label("1. PLATILLOS MEXICANOS TRADICIONALES");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        secTitle.setTextFill(PRIMARY_GREEN);

        HBox cardsBox = new HBox(12);
        cardsBox.setAlignment(Pos.CENTER);

        VBox cardDesayuno = crearTarjetaPlatillo("DESAYUNO", "#2E7D32");
        VBox cardComida = crearTarjetaPlatillo("COMIDA", "#1B5E20");
        VBox cardCena = crearTarjetaPlatillo("CENA", "#388E3C");

        HBox.setHgrow(cardDesayuno, Priority.ALWAYS);
        HBox.setHgrow(cardComida, Priority.ALWAYS);
        HBox.setHgrow(cardCena, Priority.ALWAYS);

        cardsBox.getChildren().addAll(cardDesayuno, cardComida, cardCena);
        container.getChildren().addAll(secTitle, cardsBox);
        return container;
    }

    private VBox crearTarjetaPlatillo(String comida, String hexColor) {
        VBox card = new VBox(6);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 10 12;"
        );
        card.setEffect(new DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: " + hexColor + "; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 5 8;"
        );

        Label labelComida = new Label("PLATILLOS EN " + comida);
        labelComida.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        labelComida.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVerTodos = new Button("Ver Todos");
        btnVerTodos.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9.5));
        btnVerTodos.setTextFill(DARK_FOREST);
        btnVerTodos.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 2 7; " +
            "-fx-cursor: hand;"
        );
        btnVerTodos.setOnAction(e -> mostrarPlatillosModal());

        header.getChildren().addAll(labelComida, spacer, btnVerTodos);

        VBox slotsContainer = new VBox(5);
        List<PlatilloSeleccionado> slots = platillosEspecificosSeleccionados.get(comida);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            HBox slotRow = new HBox(6);
            slotRow.setAlignment(Pos.CENTER_LEFT);

            Label slotNum = new Label((i + 1) + ".");
            slotNum.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
            slotNum.setTextFill(TEXT_MUTED);
            slotNum.setMinWidth(14);

            ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText("Elegir platillo...");
            combo.setItems(listaPlatillos);
            combo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(combo, Priority.ALWAYS);
            estilizarControl(combo);

            Spinner<Integer> spinner = new Spinner<>(1, 10, 1);
            spinner.setPrefWidth(55);
            spinner.setEditable(true);
            estilizarControl(spinner);

            // Listener combo
            combo.valueProperty().addListener((obs, oldV, newV) -> {
                String platillo = newV != null ? newV : "";
                int porc = spinner.getValue() != null ? spinner.getValue() : 1;
                slots.set(index, new PlatilloSeleccionado(platillo, porc));
                recalcularTotalesGenerales();
            });

            // Listener spinner
            spinner.valueProperty().addListener((obs, oldV, newV) -> {
                String platillo = combo.getValue() != null ? combo.getValue() : "";
                int porc = newV != null ? newV : 1;
                slots.set(index, new PlatilloSeleccionado(platillo, porc));
                recalcularTotalesGenerales();
            });

            slotRow.getChildren().addAll(slotNum, combo, spinner);
            slotsContainer.getChildren().add(slotRow);
        }

        card.getChildren().addAll(header, slotsContainer);
        return card;
    }

    private void estilizarControl(Control control) {
        control.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 11px;"
        );
    }

    private VBox crearSeccionComidas() {
        VBox container = new VBox(12);
        container.setAlignment(Pos.TOP_LEFT);

        Label secTitle = new Label("2. REGISTRO DE ALIMENTOS POR TIEMPO DE COMIDA (17 GRUPOS SMAE)");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        secTitle.setTextFill(PRIMARY_GREEN);

        tablaDesayuno = crearTablaComida();
        tablaComida = crearTablaComida();
        tablaCena = crearTablaComida();

        VBox desContainer = crearContenedorTabla("DESAYUNO", tablaDesayuno, "#2E7D32");
        VBox comContainer = crearContenedorTabla("COMIDA", tablaComida, "#1B5E20");
        VBox cenContainer = crearContenedorTabla("CENA", tablaCena, "#388E3C");

        container.getChildren().addAll(secTitle, desContainer, comContainer, cenContainer);
        return container;
    }

    private VBox crearContenedorTabla(String titulo, TableView<AlimentoComida> tabla, String hexColor) {
        VBox card = new VBox(6);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 8 10;"
        );
        card.setEffect(new DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: " + hexColor + "; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 5 10;"
        );

        Label labelTitulo = new Label(titulo);
        labelTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        labelTitulo.setTextFill(Color.WHITE);

        Label labelInfo = new Label("← Desplaza horizontalmente para ver todos los grupos →");
        labelInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        labelInfo.setTextFill(Color.web("#C8E6C9"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(labelTitulo, spacer, labelInfo);

        // Redirigir eventos de rueda del ratón vertical al ScrollPane principal para scroll ultra fluido
        tabla.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0 && Math.abs(event.getDeltaY()) > Math.abs(event.getDeltaX())) {
                if (scrollPrincipal != null) {
                    double delta = event.getDeltaY();
                    scrollPrincipal.setVvalue(scrollPrincipal.getVvalue() - delta / 350.0);
                    event.consume();
                }
            }
        });

        card.getChildren().addAll(header, tabla);
        return card;
    }

    private TableView<AlimentoComida> crearTablaComida() {
        TableView<AlimentoComida> tabla = new TableView<>();
        tabla.setEditable(true);
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Optimización clave para eliminar cualquier trabado al scrollear:
        tabla.setFixedCellSize(36.0);
        tabla.setPrefHeight(36.0 * 6 + 48.0);
        tabla.setMinHeight(36.0 * 6 + 48.0);
        tabla.setMaxHeight(36.0 * 6 + 48.0);

        for (String grupo : todosLosGrupos) {
            TableColumn<AlimentoComida, String> col = new TableColumn<>();
            col.setPrefWidth(225);
            col.setMinWidth(195);

            // Crear encabezado gráfico personalizado de alto contraste para el grupo y subgrupo
            VBox headerBox = new VBox(2);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setStyle("-fx-background-color: #1B5E20; -fx-padding: 4 6; -fx-background-radius: 4;");

            String[] partes = grupo.split(" - ");
            Label mainLabel = new Label(partes[0].trim());
            mainLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
            mainLabel.setTextFill(Color.WHITE);
            mainLabel.setAlignment(Pos.CENTER);

            headerBox.getChildren().add(mainLabel);

            if (partes.length > 1) {
                Label subLabel = new Label(partes[1].trim());
                subLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9.5));
                subLabel.setTextFill(Color.web("#E8F5E9"));
                subLabel.setStyle(
                    "-fx-background-color: #2E7D32; " +
                    "-fx-background-radius: 3; " +
                    "-fx-padding: 1 5;"
                );
                subLabel.setAlignment(Pos.CENTER);
                headerBox.getChildren().add(subLabel);
            }

            col.setGraphic(headerBox);
            col.setText(""); // Limpiar texto plano para usar exclusivamente el diseño gráfico nítido
            col.setStyle("-fx-alignment: CENTER;");
            col.setCellFactory(column -> new ComboBoxSpinnerTableCell(grupo));
            tabla.getColumns().add(col);
        }

        ObservableList<AlimentoComida> filas = FXCollections.observableArrayList();
        for (int i = 0; i < 6; i++) {
            filas.add(new AlimentoComida());
        }
        tabla.setItems(filas);

        return tabla;
    }

    // =========================================================================
    // SECCIÓN DE RESUMEN: 'NUTRIENTES ACTUALES (g)' y 'PORCENTAJE DE NUTRIENTES (%)'
    // =========================================================================

    private VBox crearSeccionResumenNutrientes() {
        VBox container = new VBox(8);
        container.setAlignment(Pos.TOP_LEFT);

        Label secTitle = new Label("3. RESUMEN DE NUTRIENTES CONSUMIDOS");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        secTitle.setTextFill(PRIMARY_GREEN);

        HBox tablesRow = new HBox(16);
        tablesRow.setAlignment(Pos.CENTER);

        // Tabla Izquierda: NUTRIENTES ACTUALES (g)
        VBox cardGramos = crearTablaNutrientesGramos();
        HBox.setHgrow(cardGramos, Priority.ALWAYS);

        // Tabla Derecha: PORCENTAJE DE NUTRIENTES (%)
        VBox cardPorcentajes = crearTablaPorcentajeNutrientes();
        HBox.setHgrow(cardPorcentajes, Priority.ALWAYS);

        tablesRow.getChildren().addAll(cardGramos, cardPorcentajes);
        container.getChildren().addAll(secTitle, tablesRow);

        return container;
    }

    private VBox crearTablaNutrientesGramos() {
        VBox container = new VBox(6);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #2E7D32; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 8 10;"
        );
        container.setEffect(new DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label titulo = new Label("NUTRIENTES ACTUALES (g)");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titulo.setTextFill(Color.WHITE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 12;"
        );

        tablaNutrientesGramos = new TableView<>();
        tablaNutrientesGramos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<NutrienteItem, String> nutCol = new TableColumn<>("Nutriente");
        nutCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nutCol.setPrefWidth(180);

        TableColumn<NutrienteItem, String> cantCol = new TableColumn<>("Cantidad (g)");
        cantCol.setCellValueFactory(new PropertyValueFactory<>("valor"));
        cantCol.setPrefWidth(120);

        tablaNutrientesGramos.getColumns().addAll(nutCol, cantCol);

        // Altura fija exacta para 3 filas sin filas vacías
        tablaNutrientesGramos.setFixedCellSize(28.0);
        tablaNutrientesGramos.prefHeightProperty().bind(tablaNutrientesGramos.fixedCellSizeProperty().multiply(3).add(32));
        tablaNutrientesGramos.minHeightProperty().bind(tablaNutrientesGramos.prefHeightProperty());
        tablaNutrientesGramos.maxHeightProperty().bind(tablaNutrientesGramos.prefHeightProperty());

        ObservableList<NutrienteItem> datos = FXCollections.observableArrayList(
            new NutrienteItem("Hidratos de Carbono", "0.0"),
            new NutrienteItem("Lipidos", "0.0"),
            new NutrienteItem("Proteinas", "0.0")
        );
        tablaNutrientesGramos.setItems(datos);

        container.getChildren().addAll(titulo, tablaNutrientesGramos);
        return container;
    }

    private VBox crearTablaPorcentajeNutrientes() {
        VBox container = new VBox(6);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #388E3C; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 8 10;"
        );
        container.setEffect(new DropShadow(6, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label titulo = new Label("PORCENTAJE DE NUTRIENTES (%)");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titulo.setTextFill(Color.WHITE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setStyle(
            "-fx-background-color: #388E3C; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 12;"
        );

        tablaPorcentajes = new TableView<>();
        tablaPorcentajes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<NutrienteItem, String> nutCol = new TableColumn<>("Nutriente");
        nutCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nutCol.setPrefWidth(180);

        TableColumn<NutrienteItem, String> porcCol = new TableColumn<>("Porcentaje (%)");
        porcCol.setCellValueFactory(new PropertyValueFactory<>("valor"));
        porcCol.setPrefWidth(120);

        tablaPorcentajes.getColumns().addAll(nutCol, porcCol);

        // Altura fija exacta para 3 filas sin filas vacías
        tablaPorcentajes.setFixedCellSize(28.0);
        tablaPorcentajes.prefHeightProperty().bind(tablaPorcentajes.fixedCellSizeProperty().multiply(3).add(32));
        tablaPorcentajes.minHeightProperty().bind(tablaPorcentajes.prefHeightProperty());
        tablaPorcentajes.maxHeightProperty().bind(tablaPorcentajes.prefHeightProperty());

        ObservableList<NutrienteItem> datos = FXCollections.observableArrayList(
            new NutrienteItem("Hidratos de Carbono", "0.0%"),
            new NutrienteItem("Lipidos", "0.0%"),
            new NutrienteItem("Proteinas", "0.0%")
        );
        tablaPorcentajes.setItems(datos);

        container.getChildren().addAll(titulo, tablaPorcentajes);
        return container;
    }

    private HBox crearFooter() {
        HBox footer = new HBox(14);
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E8E3; -fx-border-width: 1.5 0 0 0;");
        footer.setPadding(new Insets(8, 16, 10, 16));

        Button exportarBtn = new Button("EXPORTAR REPORTE A TXT");
        exportarBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        exportarBtn.setTextFill(Color.WHITE);
        exportarBtn.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-background-radius: 16; " +
            "-fx-padding: 7 20; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 3, 0, 0, 1);"
        );
        exportarBtn.setOnAction(e -> exportarATXT());

        Button limpiarBtn = new Button("LIMPIAR REGISTROS");
        limpiarBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        limpiarBtn.setTextFill(Color.WHITE);
        limpiarBtn.setStyle(
            "-fx-background-color: #C62828; " +
            "-fx-background-radius: 16; " +
            "-fx-padding: 7 16; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 3, 0, 0, 1);"
        );
        limpiarBtn.setOnAction(e -> reiniciarTablas());

        footer.getChildren().addAll(exportarBtn, limpiarBtn);
        return footer;
    }

    private void reiniciarTablas() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Limpieza");
        confirmacion.setHeaderText("¿Deseas reiniciar todas las comidas y platillos?");
        confirmacion.setContentText("Esta accion restablecera los datos del recordatorio a 0.");

        confirmacion.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                for (TableView<AlimentoComida> t : List.of(tablaDesayuno, tablaComida, tablaCena)) {
                    if (t != null) {
                        for (AlimentoComida row : t.getItems()) {
                            for (String g : todosLosGrupos) {
                                row.setAlimento(g, "");
                                row.setPorcion(g, 1);
                            }
                        }
                        t.refresh();
                    }
                }
                inicializarListas();
                recalcularTotalesGenerales();
            }
        });
    }

    // =========================================================================
    // LÓGICA DE RECALCULO DE NUTRIENTES
    // =========================================================================

    private void recalcularTotalesGenerales() {
        double sumHc = 0.0;
        double sumLip = 0.0;
        double sumProt = 0.0;

        // 1. Sumatoria de tablas de comidas
        List<TableView<AlimentoComida>> tablas = List.of(tablaDesayuno, tablaComida, tablaCena);
        for (TableView<AlimentoComida> t : tablas) {
            if (t == null) continue;
            for (AlimentoComida row : t.getItems()) {
                if (row == null) continue;
                for (String grupo : todosLosGrupos) {
                    String alimento = row.getAlimento(grupo);
                    if (alimento == null || alimento.trim().isEmpty()) continue;

                    int porciones = row.getPorcion(grupo);
                    if (porciones <= 0) continue;

                    Map<String, Double> nut = nutrientesAlimentos.get(alimento);
                    if (nut == null) nut = nutrientesPlatillos.get(alimento);

                    if (nut != null) {
                        sumHc += nut.getOrDefault("HC", 0.0) * porciones;
                        sumLip += nut.getOrDefault("Lípidos", 0.0) * porciones;
                        sumProt += nut.getOrDefault("Proteínas", 0.0) * porciones;
                    }
                }
            }
        }

        // 2. Sumatoria de platillos tradicionales
        for (List<PlatilloSeleccionado> slots : platillosEspecificosSeleccionados.values()) {
            if (slots == null) continue;
            for (PlatilloSeleccionado ps : slots) {
                if (ps == null || ps.nombre == null || ps.nombre.trim().isEmpty() || ps.porciones <= 0) continue;

                Map<String, Double> nut = nutrientesPlatillos.get(ps.nombre);
                if (nut == null) nut = nutrientesAlimentos.get(ps.nombre);

                if (nut != null) {
                    sumHc += nut.getOrDefault("HC", 0.0) * ps.porciones;
                    sumLip += nut.getOrDefault("Lípidos", 0.0) * ps.porciones;
                    sumProt += nut.getOrDefault("Proteínas", 0.0) * ps.porciones;
                }
            }
        }

        totalHc = sumHc;
        totalLipidos = sumLip;
        totalProteinas = sumProt;

        double totalKcal = (totalHc * 4.0) + (totalProteinas * 4.0) + (totalLipidos * 9.0);
        totalKcalLabel.setText("ENERGIA CONSUMIDA: " + df.format(totalKcal) + " kcal");

        // Actualizar Tabla 1: 'Nutrientes Actuales (g)'
        if (tablaNutrientesGramos != null && !tablaNutrientesGramos.getItems().isEmpty()) {
            ObservableList<NutrienteItem> items = tablaNutrientesGramos.getItems();
            if (items.size() >= 3) {
                items.get(0).setValor(df.format(totalHc));
                items.get(1).setValor(df.format(totalLipidos));
                items.get(2).setValor(df.format(totalProteinas));
                tablaNutrientesGramos.refresh();
            }
        }

        // Actualizar Tabla 2: 'Porcentaje de Nutrientes (%)'
        if (tablaPorcentajes != null && !tablaPorcentajes.getItems().isEmpty()) {
            ObservableList<NutrienteItem> pitems = tablaPorcentajes.getItems();
            double totalNutrientes = totalHc + totalLipidos + totalProteinas;

            double pctHc = totalNutrientes > 0 ? (totalHc / totalNutrientes) * 100.0 : 0.0;
            double pctLip = totalNutrientes > 0 ? (totalLipidos / totalNutrientes) * 100.0 : 0.0;
            double pctProt = totalNutrientes > 0 ? (totalProteinas / totalNutrientes) * 100.0 : 0.0;

            if (pitems.size() >= 3) {
                pitems.get(0).setValor(df.format(pctHc) + "%");
                pitems.get(1).setValor(df.format(pctLip) + "%");
                pitems.get(2).setValor(df.format(pctProt) + "%");
                tablaPorcentajes.refresh();
            }
        }
    }

    // =========================================================================
    // CARGA ASÍNCRONA DE DATOS (CON CACHÉ PARA EVITAR LAG EN CELDAS)
    // =========================================================================

    private void iniciarCargaAsincrona() {
        Task<Void> tareaCarga = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Cargando base SMAE...");
                cargarDatosExcel();
                updateMessage("Cargando platillos mexicanos...");
                cargarPlatillosCSV();
                return null;
            }
        };

        barraProgreso.progressProperty().bind(tareaCarga.progressProperty());
        estadoCargaLabel.textProperty().bind(tareaCarga.messageProperty());

        tareaCarga.setOnSucceeded(e -> {
            estadoCargaLabel.textProperty().unbind();
            estadoCargaLabel.setText("Base de datos lista");
            barraProgreso.progressProperty().unbind();
            barraProgreso.setVisible(false);

            // Refrescar tablas de comidas con las listas ya cacheadas
            if (tablaDesayuno != null) tablaDesayuno.refresh();
            if (tablaComida != null) tablaComida.refresh();
            if (tablaCena != null) tablaCena.refresh();
        });

        tareaCarga.setOnFailed(e -> {
            estadoCargaLabel.textProperty().unbind();
            estadoCargaLabel.setText("Aviso: datos locales cargados");
            barraProgreso.setVisible(false);
        });

        new Thread(tareaCarga).start();
    }

    private void cargarDatosExcel() {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("/data/SMAE_5aed-2.0.xlsx");
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("data/SMAE_5aed-2.0.xlsx");
            }
            if (is == null) {
                try {
                    is = new FileInputStream("resources/data/SMAE_5aed-2.0.xlsx");
                } catch (FileNotFoundException ignored) {}
            }

            if (is == null) return;

            try (Workbook workbook = new XSSFWorkbook(is)) {
                int inicio = 3;
                int fin = workbook.getNumberOfSheets() - 3;

                for (int i = inicio; i < fin; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    String nombreHoja = sheet.getSheetName().trim();

                    for (String grupo : todosLosGrupos) {
                        List<String> posibles = nombreExcel.getOrDefault(grupo, List.of(grupo));
                        if (posibles.stream().anyMatch(s -> s.equalsIgnoreCase(nombreHoja))) {
                            List<String> alimentos = new ArrayList<>();

                            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                                Row row = sheet.getRow(rowNum);
                                if (row != null) {
                                    org.apache.poi.ss.usermodel.Cell celdaAlimento = row.getCell(1);
                                    if (celdaAlimento != null && celdaAlimento.getCellType() == CellType.STRING) {
                                        String alimento = celdaAlimento.getStringCellValue().trim();
                                        if (!alimento.isEmpty()) {
                                            alimentos.add(alimento);

                                            org.apache.poi.ss.usermodel.Cell celdaHc = row.getCell(9);
                                            org.apache.poi.ss.usermodel.Cell celdaLipidos = row.getCell(10);
                                            org.apache.poi.ss.usermodel.Cell celdaProteinas = row.getCell(11);

                                            double hc = celdaHc != null && celdaHc.getCellType() == CellType.NUMERIC ? celdaHc.getNumericCellValue() : 0;
                                            double lipidos = celdaLipidos != null && celdaLipidos.getCellType() == CellType.NUMERIC ? celdaLipidos.getNumericCellValue() : 0;
                                            double proteinas = celdaProteinas != null && celdaProteinas.getCellType() == CellType.NUMERIC ? celdaProteinas.getNumericCellValue() : 0;

                                            Map<String, Double> nut = new HashMap<>();
                                            nut.put("HC", hc);
                                            nut.put("Lípidos", lipidos);
                                            nut.put("Proteínas", proteinas);

                                            nutrientesAlimentos.put(alimento, nut);
                                        }
                                    }
                                }
                            }
                            datosExcel.put(grupo, alimentos);

                            // Pre-cachear lista ObservableList una sola vez para rendimiento óptimo
                            ObservableList<String> observable = FXCollections.observableArrayList();
                            observable.add("");
                            observable.addAll(alimentos);
                            cachedListasAlimentos.put(grupo, observable);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void cargarPlatillosCSV() {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("/data/Platillos_mexicanos.csv");
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("data/Platillos_mexicanos.csv");
            }
            if (is == null) {
                try {
                    is = new FileInputStream("resources/data/Platillos_mexicanos.csv");
                } catch (FileNotFoundException ignored) {}
            }

            if (is == null) return;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String linea;
                List<String> platillosCargados = new ArrayList<>();

                while ((linea = br.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty()) continue;

                    String[] partes = linea.split(",", -1);
                    if (partes[0].toLowerCase().contains("platillo") || partes.length < 6) continue;

                    String nombrePlatillo = partes[0].trim();
                    if (nombrePlatillo.isEmpty()) continue;

                    try {
                        double prote = Double.parseDouble(partes[2].trim());
                        double lip = Double.parseDouble(partes[3].trim());
                        double hc = Double.parseDouble(partes[4].trim());

                        platillosCargados.add(nombrePlatillo);

                        Map<String, Double> nut = new HashMap<>();
                        nut.put("Proteínas", prote);
                        nut.put("Lípidos", lip);
                        nut.put("HC", hc);

                        nutrientesPlatillos.put(nombrePlatillo, nut);
                    } catch (NumberFormatException ignored) {}
                }

                Platform.runLater(() -> {
                    listaPlatillos.setAll(platillosCargados);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void mostrarPlatillosModal() {
        Stage modal = new Stage();
        modal.setTitle("Catalogo de Platillos Mexicanos");

        VBox root = new VBox(10);
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: white;");

        Label titulo = new Label("PLATILLOS MEXICANOS DISPONIBLES");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        titulo.setTextFill(PRIMARY_GREEN);

        ListView<String> lista = new ListView<>(listaPlatillos);
        lista.setPrefHeight(360);

        Button cerrar = new Button("Cerrar");
        cerrar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        cerrar.setTextFill(Color.WHITE);
        cerrar.setStyle("-fx-background-color: #2E7D32; -fx-background-radius: 5; -fx-padding: 5 14; -fx-cursor: hand;");
        cerrar.setOnAction(e -> modal.close());

        root.getChildren().addAll(titulo, lista, cerrar);

        Scene scene = new Scene(root, 460, 460);
        modal.setScene(scene);
        modal.show();
    }

    // =========================================================================
    // EXPORTACIÓN A TXT
    // =========================================================================

    private void exportarATXT() {
        if (!hayDatosParaExportar()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sin datos");
            alert.setHeaderText("No hay datos para exportar");
            alert.setContentText("Por favor ingresa al menos un alimento o platillo antes de exportar el reporte.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte de Recordatorio 24H");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt"));
        fileChooser.setInitialFileName("recordatorio_24h.txt");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("================================================================================\n");
                writer.write("           NUTRIENERGIA PRO - RECORDATORIO ALIMENTICIO DE 24 HORAS               \n");
                writer.write("================================================================================\n\n");

                writer.write("1. EVALUACION DE MACRONUTRIENTES:\n");
                writer.write("--------------------------------------------------------------------------------\n");
                writer.write(String.format("%-24s %-16s %-16s %-16s\n", "NUTRIENTE", "OBJETIVO (g)", "ACTUAL (g)", "DIFERENCIA"));
                writer.write(String.format("%-24s %-16s %-16s %-16s\n", "─".repeat(24), "─".repeat(16), "─".repeat(16), "─".repeat(16)));

                writer.write(String.format("%-24s %-16.1f %-16.1f %+.1f g\n", "Hidratos de Carbono", idealHc, totalHc, (totalHc - idealHc)));
                writer.write(String.format("%-24s %-16.1f %-16.1f %+.1f g\n", "Lipidos", idealLipidos, totalLipidos, (totalLipidos - idealLipidos)));
                writer.write(String.format("%-24s %-16.1f %-16.1f %+.1f g\n", "Proteinas", idealProteinas, totalProteinas, (totalProteinas - idealProteinas)));

                double totalKcal = (totalHc * 4) + (totalProteinas * 4) + (totalLipidos * 9);
                writer.write(String.format("\nEnergia Total Consumida: %.1f kcal\n\n", totalKcal));

                exportarDetalleComida(writer, "DESAYUNO", tablaDesayuno, platillosEspecificosSeleccionados.get("DESAYUNO"));
                exportarDetalleComida(writer, "COMIDA", tablaComida, platillosEspecificosSeleccionados.get("COMIDA"));
                exportarDetalleComida(writer, "CENA", tablaCena, platillosEspecificosSeleccionados.get("CENA"));

                writer.write("================================================================================\n");
                writer.write("Reporte generado el: " + new Date() + "\n");
                writer.write("================================================================================\n");

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Reporte Exportado");
                info.setHeaderText("Archivo generado con exito");
                info.setContentText("El reporte se ha guardado en:\n" + file.getAbsolutePath());
                info.showAndWait();

            } catch (IOException e) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Error");
                err.setHeaderText("No se pudo guardar el archivo");
                err.setContentText(e.getMessage());
                err.showAndWait();
            }
        }
    }

    private void exportarDetalleComida(FileWriter writer, String nombreComida, TableView<AlimentoComida> tabla, List<PlatilloSeleccionado> platillos) throws IOException {
        writer.write("--------------------------------------------------------------------------------\n");
        writer.write("REGISTRO DE: " + nombreComida + "\n");
        writer.write("--------------------------------------------------------------------------------\n");

        boolean tienePlatillos = false;
        if (platillos != null) {
            for (PlatilloSeleccionado p : platillos) {
                if (p != null && p.nombre != null && !p.nombre.isEmpty() && p.porciones > 0) {
                    if (!tienePlatillos) {
                        writer.write("• Platillos Tradicionales:\n");
                        tienePlatillos = true;
                    }
                    writer.write(String.format("   - %s (%d porcion/es)\n", p.nombre, p.porciones));
                }
            }
        }

        boolean tieneAlimentos = false;
        if (tabla != null) {
            for (AlimentoComida row : tabla.getItems()) {
                for (String grupo : todosLosGrupos) {
                    String alimento = row.getAlimento(grupo);
                    if (alimento != null && !alimento.isEmpty() && row.getPorcion(grupo) > 0) {
                        if (!tieneAlimentos) {
                            writer.write("• Alimentos por Grupo SMAE:\n");
                            tieneAlimentos = true;
                        }
                        writer.write(String.format("   - %s: %s (%d porc.)\n", grupo, alimento, row.getPorcion(grupo)));
                    }
                }
            }
        }

        if (!tienePlatillos && !tieneAlimentos) {
            writer.write("  (Sin alimentos registrados en este tiempo de comida)\n");
        }
        writer.write("\n");
    }

    private boolean hayDatosParaExportar() {
        for (TableView<AlimentoComida> t : List.of(tablaDesayuno, tablaComida, tablaCena)) {
            if (t != null) {
                for (AlimentoComida row : t.getItems()) {
                    for (String g : todosLosGrupos) {
                        if (row.getAlimento(g) != null && !row.getAlimento(g).isEmpty()) return true;
                    }
                }
            }
        }
        for (List<PlatilloSeleccionado> slots : platillosEspecificosSeleccionados.values()) {
            if (slots != null) {
                for (PlatilloSeleccionado ps : slots) {
                    if (ps != null && ps.nombre != null && !ps.nombre.isEmpty() && ps.porciones > 0) return true;
                }
            }
        }
        return false;
    }

    // =========================================================================
    // MODELOS DE DATOS Y CELDAS PERSONALIZADAS DE ALTO RENDIMIENTO
    // =========================================================================

    public static class PlatilloSeleccionado {
        String nombre;
        int porciones;

        public PlatilloSeleccionado(String nombre, int porciones) {
            this.nombre = nombre;
            this.porciones = porciones;
        }
    }

    public static class AlimentoComida {
        private Map<String, String> alimentos = new HashMap<>();
        private Map<String, Integer> porciones = new HashMap<>();

        public String getAlimento(String grupo) {
            return alimentos.get(grupo);
        }

        public void setAlimento(String grupo, String alimento) {
            alimentos.put(grupo, alimento);
        }

        public int getPorcion(String grupo) {
            return porciones.getOrDefault(grupo, 1);
        }

        public void setPorcion(String grupo, int porcion) {
            porciones.put(grupo, porcion);
        }
    }

    public static class NutrienteItem {
        private String nombre;
        private String valor;

        public NutrienteItem(String nombre, String valor) {
            this.nombre = nombre;
            this.valor = valor;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getValor() { return valor; }
        public void setValor(String valor) { this.valor = valor; }
    }

    private class ComboBoxSpinnerTableCell extends TableCell<AlimentoComida, String> {
        private final HBox container;
        private final ComboBox<String> comboBox;
        private final Spinner<Integer> spinner;
        private final String grupo;
        private boolean isUpdating = false;

        public ComboBoxSpinnerTableCell(String grupo) {
            this.grupo = grupo;

            comboBox = new ComboBox<>();
            comboBox.setPromptText("Elegir alimento...");
            comboBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(comboBox, Priority.ALWAYS);
            estilizarControl(comboBox);

            // Usar la lista pre-cacheada para que no cree objetos durante el scroll
            ObservableList<String> items = cachedListasAlimentos.get(grupo);
            if (items != null) {
                comboBox.setItems(items);
            }

            spinner = new Spinner<>(0, 10, 1);
            spinner.setPrefWidth(55);
            spinner.setEditable(true);
            estilizarControl(spinner);

            container = new HBox(4);
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().addAll(comboBox, spinner);

            comboBox.valueProperty().addListener((obs, oldV, newV) -> {
                if (!isUpdating && getIndex() >= 0) {
                    commitValues();
                }
            });

            spinner.valueProperty().addListener((obs, oldV, newV) -> {
                if (!isUpdating && getIndex() >= 0 && oldV != null && !oldV.equals(newV)) {
                    commitValues();
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
                setText(null);
                return;
            }

            isUpdating = true;
            try {
                AlimentoComida row = getTableView().getItems().get(getIndex());
                if (row == null) {
                    setGraphic(null);
                    return;
                }

                String alimActual = row.getAlimento(grupo);
                int porcActual = row.getPorcion(grupo);

                comboBox.setValue(alimActual != null ? alimActual : "");
                spinner.getValueFactory().setValue(porcActual > 0 ? porcActual : 1);

                setGraphic(container);
                setText(null);
            } finally {
                isUpdating = false;
            }
        }

        private void commitValues() {
            int idx = getIndex();
            if (idx < 0 || idx >= getTableView().getItems().size()) return;

            AlimentoComida row = getTableView().getItems().get(idx);
            if (row == null) return;

            String alimNuevo = comboBox.getValue() != null ? comboBox.getValue() : "";
            Integer porcNueva = spinner.getValue() != null ? spinner.getValue() : 1;

            row.setAlimento(grupo, alimNuevo);
            row.setPorcion(grupo, porcNueva);

            recalcularTotalesGenerales();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}