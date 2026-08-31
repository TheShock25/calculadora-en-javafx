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
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Plan Alimenticio Personalizado (JavaFX)
 * Planificación dietética clínica por grupos prescritos del SMAE y Platillos Mexicanos.
 * Incluye:
 * - Centrado proporcional dinámico de columnas de grupos
 * - Regla estricta de cuota diaria de porciones por grupo alimenticio
 * - Dimensionamiento adaptativo a la pantalla
 * - Paleta institucional Verde y Blanca
 */
public class PlanAlimenticio extends Application {

    // Paleta oficial: Verdes y Blancos de alta legibilidad
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color LIGHT_MINT = Color.web("#E8F5E9");          // Fondo verde menta suave
    private static final Color BORDER_GREEN = Color.web("#C8E6C9");        // Borde verde suave
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Texto oscuro
    private static final Color TEXT_MUTED = Color.web("#4A6356");          // Texto secundario

    // Componentes principales
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
    private double kcalObjetivo = 0;

    // Totales calculados en tiempo real
    private double totalHc = 0;
    private double totalLipidos = 0;
    private double totalProteinas = 0;

    // Grupos prescritos y porciones objetivo
    private List<String> gruposEspecificos = new ArrayList<>();
    private List<Integer> porcionesObjetivo = new ArrayList<>();

    // Mapas de datos y caché de listas
    private Map<String, List<String>> datosExcel = new HashMap<>();
    private Map<String, ObservableList<String>> cachedListasAlimentos = new HashMap<>();
    private Map<String, Map<String, Double>> nutrientesAlimentos = new HashMap<>();
    private Map<String, Map<String, Double>> nutrientesPlatillos = new HashMap<>();
    private ObservableList<String> listaPlatillos = FXCollections.observableArrayList();

    // Matrices de estado para cada comida (6 filas x N grupos)
    private String[][] alimentosEnDesayuno;
    private int[][] porcionesEnDesayuno;
    private String[][] alimentosEnComida;
    private int[][] porcionesEnComida;
    private String[][] alimentosEnCena;
    private int[][] porcionesEnCena;

    // Controladores de slots registrados para actualización de cuota
    private Map<Integer, List<SlotControl>> controlesPorGrupo = new HashMap<>();
    private Map<Integer, Label> badgePorcionesPorGrupo = new HashMap<>();

    // 4 slots de platillos específicos por comida
    private Map<String, List<PlatilloSeleccionado>> platillosEspecificosSeleccionados = new HashMap<>();

    private DecimalFormat df = new DecimalFormat("#.##");

    public PlanAlimenticio() {
        this(0, 0, 0, new ArrayList<>(), new ArrayList<>());
    }

    public PlanAlimenticio(double hc, double lipidos, double proteinas, List<String> grupos, List<Integer> porciones) {
        this.idealHc = hc;
        this.idealLipidos = lipidos;
        this.idealProteinas = proteinas;
        this.kcalObjetivo = (hc * 4.0) + (proteinas * 4.0) + (lipidos * 9.0);
        this.gruposEspecificos = sanitizarGrupos(grupos);
        this.porcionesObjetivo = (porciones != null) ? new ArrayList<>(porciones) : new ArrayList<>();

        inicializarEstructuras();
    }

    public PlanAlimenticio(double hc, double lipidos, double proteinas, List<Map<String, Object>> seleccionEquivalentes, double kcalDiarias) {
        this.idealHc = hc;
        this.idealLipidos = lipidos;
        this.idealProteinas = proteinas;
        this.kcalObjetivo = kcalDiarias;
        this.gruposEspecificos = new ArrayList<>();
        this.porcionesObjetivo = new ArrayList<>();

        if (seleccionEquivalentes != null && !seleccionEquivalentes.isEmpty()) {
            for (Map<String, Object> item : seleccionEquivalentes) {
                String g = (String) item.get("grupo");
                String sub = (String) item.get("subgrupo");
                int porc = (item.get("porciones") instanceof Number) ? ((Number) item.get("porciones")).intValue() : 1;

                if (g != null) g = g.trim();
                if (sub != null) sub = sub.trim();

                String nombreCompleto = g;
                if (sub != null && !sub.isEmpty() && !sub.equalsIgnoreCase(g)) {
                    nombreCompleto = g + " - " + sub;
                }

                if (nombreCompleto != null && !nombreCompleto.isEmpty() && !this.gruposEspecificos.contains(nombreCompleto)) {
                    this.gruposEspecificos.add(nombreCompleto);
                    this.porcionesObjetivo.add(porc);
                }
            }
        }

        if (this.gruposEspecificos.isEmpty()) {
            this.gruposEspecificos = obtenerGruposPorDefecto();
        }

        inicializarEstructuras();
    }

    public PlanAlimenticio(List<Map<String, Object>> seleccionEquivalentes, double kcalDiarias) {
        this(0, 0, 0, seleccionEquivalentes, kcalDiarias);
    }

    private List<String> sanitizarGrupos(List<String> grupos) {
        if (grupos == null || grupos.isEmpty()) {
            return obtenerGruposPorDefecto();
        }
        List<String> resultado = new ArrayList<>();
        for (String g : grupos) {
            if (g != null && !g.trim().isEmpty()) {
                String limpio = g.trim();
                if (limpio.endsWith(" -")) {
                    limpio = limpio.substring(0, limpio.length() - 2).trim();
                }
                if (!resultado.contains(limpio)) {
                    resultado.add(limpio);
                }
            }
        }
        return resultado.isEmpty() ? obtenerGruposPorDefecto() : resultado;
    }

    private List<String> obtenerGruposPorDefecto() {
        return Arrays.asList(
            "Verduras", "Frutas", "Cereales y tubérculos - Sin Grasa", "Cereales y tubérculos - Con Grasa",
            "Leguminosas", "Alimentos de origen animal - MBAG", "Alimentos de origen animal - BAG",
            "Alimentos de origen animal - MAG", "Alimentos de origen animal - AAG", "Leche - Descremada",
            "Leche - Semi", "Leche - Entera", "Leche - Con Azucar", "Aceite y grasa - Sin proteina",
            "Aceite y grasa - Con proteina", "Azucar - Sin grasa", "Azucar - Con grasa"
        );
    }

    private void inicializarEstructuras() {
        int nGrupos = Math.max(1, gruposEspecificos.size());

        alimentosEnDesayuno = new String[6][nGrupos];
        porcionesEnDesayuno = new int[6][nGrupos];
        alimentosEnComida = new String[6][nGrupos];
        porcionesEnComida = new int[6][nGrupos];
        alimentosEnCena = new String[6][nGrupos];
        porcionesEnCena = new int[6][nGrupos];

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < nGrupos; c++) {
                alimentosEnDesayuno[r][c] = "";
                porcionesEnDesayuno[r][c] = 1;
                alimentosEnComida[r][c] = "";
                porcionesEnComida[r][c] = 1;
                alimentosEnCena[r][c] = "";
                porcionesEnCena[r][c] = 1;
            }
        }

        String[] comidas = { "DESAYUNO", "COMIDA", "CENA" };
        for (String c : comidas) {
            List<PlatilloSeleccionado> slots = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                slots.add(new PlatilloSeleccionado("", 1));
            }
            platillosEspecificosSeleccionados.put(c, slots);
        }

        for (int i = 0; i < gruposEspecificos.size(); i++) {
            String g = gruposEspecificos.get(i);
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add("");
            cachedListasAlimentos.put(g, items);
            controlesPorGrupo.put(i, new ArrayList<>());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("NutriEnergia Pro - Plan Alimenticio Personalizado");

        // Registrar en WindowManager para asegurar instancia única y soporte de cierre maestro
        WindowManager.registrarVentana("PlanAlimenticio", primaryStage);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // Header institucional
        VBox header = crearHeader();
        root.setTop(header);

        // Centro con scroll fluido
        scrollPrincipal = crearPanelCentral();
        root.setCenter(scrollPrincipal);

        // Footer con botones de acción
        HBox footer = crearFooter();
        root.setBottom(footer);

        // Ajustar dimensiones para que quepa perfectamente en pantalla
        double screenW = Screen.getPrimary().getVisualBounds().getWidth();
        double screenH = Screen.getPrimary().getVisualBounds().getHeight();

        double width = Math.min(1160, screenW - 40);
        double height = Math.min(700, screenH - 50);

        Scene scene = new Scene(root, width, height);
        cargarCSS(scene);

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(920);
        primaryStage.setMinHeight(560);
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
        header.setPadding(new Insets(8, 16, 8, 16));

        // Fila 1: Badge y Título
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER);

        Label badge = new Label("PRESCRIPCION NUTRICIONAL");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.2); " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 2 7;"
        );

        Label titulo = new Label("Plan Alimenticio Personalizado");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        titulo.setTextFill(Color.WHITE);

        topRow.getChildren().addAll(badge, titulo);

        // Fila 2: Chips de Metas Dietéticas y Progreso
        HBox metricsRow = new HBox(12);
        metricsRow.setAlignment(Pos.CENTER);

        Label idealChip = crearChipMetrica("OBJETIVOS DIETETICOS",
            String.format("HC: %.1fg | Lip: %.1fg | Prot: %.1fg", idealHc, idealLipidos, idealProteinas));

        if (kcalObjetivo > 0) {
            Label kcalChip = crearChipMetrica("META CALORICA", String.format("%.0f kcal/dia", kcalObjetivo));
            metricsRow.getChildren().add(kcalChip);
        }

        totalKcalLabel = crearChipMetrica("ENERGIA CONSUMIDA", "0.0 kcal");

        HBox cargaBox = new HBox(6);
        cargaBox.setAlignment(Pos.CENTER);

        estadoCargaLabel = new Label("Cargando SMAE y Platillos...");
        estadoCargaLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10.5));
        estadoCargaLabel.setTextFill(Color.web("#C8E6C9"));

        barraProgreso = new ProgressBar();
        barraProgreso.setPrefWidth(80);
        barraProgreso.setPrefHeight(9);
        barraProgreso.setStyle("-fx-accent: #81C784;");

        cargaBox.getChildren().addAll(estadoCargaLabel, barraProgreso);

        metricsRow.getChildren().addAll(idealChip, totalKcalLabel, cargaBox);

        header.getChildren().addAll(topRow, metricsRow);
        header.setEffect(new DropShadow(8, 0, 2, Color.rgb(0, 0, 0, 0.12)));

        return header;
    }

    private Label crearChipMetrica(String titulo, String valor) {
        Label chip = new Label(titulo + ": " + valor);
        chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        chip.setTextFill(Color.WHITE);
        chip.setAlignment(Pos.CENTER);
        chip.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.22); " +
            "-fx-border-color: rgba(255, 255, 255, 0.3); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 3 8;"
        );
        return chip;
    }

    private ScrollPane crearPanelCentral() {
        VBox mainContainer = new VBox(14);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(12, 14, 20, 14));

        // 1. Resumen de Grupos Prescritos (Chips)
        if (!gruposEspecificos.isEmpty()) {
            VBox prescripcionBox = crearSeccionPrescripcion();
            mainContainer.getChildren().add(prescripcionBox);
        }

        // 2. Platillos Tradicionales Mexicanos
        VBox platillosSeccion = crearSeccionPlatillos();

        // 3. Tablas de Comidas con Grupos Prescritos (Desayuno, Comida, Cena)
        VBox comidasSeccion = crearSeccionComidas();

        // 4. Tablas Finales: 'Nutrientes Actuales (g)' y 'Porcentaje de Nutrientes (%)'
        VBox resumenNutrientesSeccion = crearSeccionResumenNutrientes();

        mainContainer.getChildren().addAll(platillosSeccion, comidasSeccion, resumenNutrientesSeccion);

        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearSeccionPrescripcion() {
        VBox container = new VBox(5);
        container.setAlignment(Pos.TOP_LEFT);

        Label secTitle = new Label("GRUPOS ALIMENTARIOS PRESCRITOS");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        secTitle.setTextFill(PRIMARY_GREEN);

        FlowPane chipsPane = new FlowPane(6, 6);
        chipsPane.setPadding(new Insets(5, 8, 5, 8));
        chipsPane.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );

        for (int i = 0; i < gruposEspecificos.size(); i++) {
            String g = gruposEspecificos.get(i);
            int porc = (i < porcionesObjetivo.size()) ? porcionesObjetivo.get(i) : 1;

            Label chip = new Label(g + " (" + porc + " porc.)");
            chip.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9.5));
            chip.setTextFill(DARK_FOREST);
            chip.setStyle(
                "-fx-background-color: #E8F5E9; " +
                "-fx-border-color: #C8E6C9; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 2 7;"
            );
            chipsPane.getChildren().add(chip);
        }

        container.getChildren().addAll(secTitle, chipsPane);
        return container;
    }

    private VBox crearSeccionPlatillos() {
        VBox container = new VBox(5);
        container.setAlignment(Pos.TOP_LEFT);

        Label secTitle = new Label("1. PLATILLOS MEXICANOS TRADICIONALES");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        secTitle.setTextFill(PRIMARY_GREEN);

        HBox cardsBox = new HBox(10);
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
        VBox card = new VBox(5);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 8 10;"
        );
        card.setEffect(new DropShadow(5, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        HBox header = new HBox(6);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: " + hexColor + "; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 4 8;"
        );

        Label labelComida = new Label("PLATILLOS EN " + comida);
        labelComida.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        labelComida.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVerTodos = new Button("Ver Todos");
        btnVerTodos.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        btnVerTodos.setTextFill(DARK_FOREST);
        btnVerTodos.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 4; " +
            "-fx-padding: 2 6; " +
            "-fx-cursor: hand;"
        );
        btnVerTodos.setOnAction(e -> mostrarPlatillosModal());

        header.getChildren().addAll(labelComida, spacer, btnVerTodos);

        VBox slotsContainer = new VBox(4);
        List<PlatilloSeleccionado> slots = platillosEspecificosSeleccionados.get(comida);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            HBox slotRow = new HBox(5);
            slotRow.setAlignment(Pos.CENTER_LEFT);

            Label slotNum = new Label((i + 1) + ".");
            slotNum.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
            slotNum.setTextFill(TEXT_MUTED);
            slotNum.setMinWidth(12);

            ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText("Elegir platillo...");
            combo.setItems(listaPlatillos);
            combo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(combo, Priority.ALWAYS);
            estilizarControl(combo);

            Spinner<Integer> spinner = new Spinner<>(1, 10, 1);
            spinner.setPrefWidth(52);
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
            "-fx-border-radius: 4; " +
            "-fx-background-radius: 4; " +
            "-fx-font-size: 10.5px;"
        );
    }

    private VBox crearSeccionComidas() {
        VBox container = new VBox(10);
        container.setAlignment(Pos.TOP_LEFT);

        Label secTitle = new Label("2. DISTRIBUCION DE ALIMENTOS EN EL PLAN DIETETICO (" + gruposEspecificos.size() + " GRUPOS)");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        secTitle.setTextFill(PRIMARY_GREEN);

        VBox desContainer = crearContenedorComida("DESAYUNO", "#2E7D32", 0);
        VBox comContainer = crearContenedorComida("COMIDA", "#1B5E20", 1);
        VBox cenContainer = crearContenedorComida("CENA", "#388E3C", 2);

        container.getChildren().addAll(secTitle, desContainer, comContainer, cenContainer);
        return container;
    }

    private VBox crearContenedorComida(String titulo, String hexColor, int comidaIdx) {
        VBox card = new VBox(5);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 7 9;"
        );
        card.setEffect(new DropShadow(5, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
            "-fx-background-color: " + hexColor + "; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 5 8;"
        );

        Label labelTitulo = new Label(titulo);
        labelTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        labelTitulo.setTextFill(Color.WHITE);

        String infoText = gruposEspecificos.size() <= 4
            ? "✓ " + gruposEspecificos.size() + " grupo(s) prescrito(s)"
            : "← Desplaza horizontalmente para ver todos los grupos (" + gruposEspecificos.size() + ") →";

        Label labelInfo = new Label(infoText);
        labelInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 9.5));
        labelInfo.setTextFill(Color.web("#C8E6C9"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(labelTitulo, spacer, labelInfo);

        // Contenedor de columnas: CENTRADO si hay 4 grupos o menos, ALINEADO A LA IZQUIERDA con scroll si hay más
        HBox columnasBox = new HBox(12);
        columnasBox.setPadding(new Insets(6));

        boolean pocosGrupos = gruposEspecificos.size() <= 4;
        columnasBox.setAlignment(pocosGrupos ? Pos.CENTER : Pos.CENTER_LEFT);

        // Calcular ancho dinámico proporcional de tarjeta
        double cardWidth;
        if (gruposEspecificos.size() == 1) {
            cardWidth = 320;
        } else if (gruposEspecificos.size() == 2) {
            cardWidth = 280;
        } else if (gruposEspecificos.size() == 3) {
            cardWidth = 260;
        } else {
            cardWidth = 240;
        }

        for (int colIndex = 0; colIndex < gruposEspecificos.size(); colIndex++) {
            String grupo = gruposEspecificos.get(colIndex);
            VBox colCard = crearTarjetaColumnaGrupo(grupo, hexColor, comidaIdx, colIndex, cardWidth);
            columnasBox.getChildren().add(colCard);
        }

        ScrollPane scrollH = new ScrollPane(columnasBox);
        scrollH.setFitToHeight(true);
        scrollH.setFitToWidth(pocosGrupos);
        scrollH.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollH.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollH.setHbarPolicy(pocosGrupos ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Redirigir eventos de scroll vertical al scroll principal
        scrollH.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0 && Math.abs(event.getDeltaY()) > Math.abs(event.getDeltaX())) {
                if (scrollPrincipal != null) {
                    double delta = event.getDeltaY();
                    scrollPrincipal.setVvalue(scrollPrincipal.getVvalue() - delta / 350.0);
                    event.consume();
                }
            }
        });

        card.getChildren().addAll(header, scrollH);
        return card;
    }

    private VBox crearTarjetaColumnaGrupo(String grupo, String hexColor, int comidaIdx, int colIndex, double cardWidth) {
        VBox card = new VBox(5);
        card.setStyle(
            "-fx-background-color: #FAFCFA; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 6 8;"
        );
        card.setPrefWidth(cardWidth);
        card.setMinWidth(210);

        // Encabezado de grupo
        VBox headerBox = new VBox(2);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle(
            "-fx-background-color: " + hexColor + "; " +
            "-fx-padding: 4 6; " +
            "-fx-background-radius: 6;"
        );

        String[] partes = grupo.split(" - ");
        Label mainLabel = new Label(partes[0].trim());
        mainLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10.5));
        mainLabel.setTextFill(Color.WHITE);
        mainLabel.setAlignment(Pos.CENTER);

        headerBox.getChildren().add(mainLabel);

        if (partes.length > 1) {
            Label subLabel = new Label(partes[1].trim());
            subLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
            subLabel.setTextFill(Color.web("#E8F5E9"));
            subLabel.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.22); " +
                "-fx-background-radius: 3; " +
                "-fx-padding: 1 4;"
            );
            subLabel.setAlignment(Pos.CENTER);
            headerBox.getChildren().add(subLabel);
        }

        // Badge de Cuota de Porciones
        int quota = (colIndex < porcionesObjetivo.size()) ? porcionesObjetivo.get(colIndex) : 1;
        Label quotaBadge = new Label("Prescrito: " + quota + " porc.");
        quotaBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 8.5));
        quotaBadge.setTextFill(DARK_FOREST);
        quotaBadge.setStyle(
            "-fx-background-color: #E8F5E9; " +
            "-fx-background-radius: 3; " +
            "-fx-padding: 1 5;"
        );
        quotaBadge.setAlignment(Pos.CENTER);

        // Guardar badge para actualización reactiva
        if (comidaIdx == 0) {
            badgePorcionesPorGrupo.put(colIndex, quotaBadge);
        }

        headerBox.getChildren().add(quotaBadge);

        VBox rowsContainer = new VBox(4);
        ObservableList<String> items = cachedListasAlimentos.get(grupo);
        if (items == null) {
            items = FXCollections.observableArrayList("");
            cachedListasAlimentos.put(grupo, items);
        }

        // 6 filas por cada grupo
        for (int r = 0; r < 6; r++) {
            final int fila = r;
            HBox row = new HBox(4);
            row.setAlignment(Pos.CENTER_LEFT);

            ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText("Elegir alimento...");
            combo.setItems(items);
            combo.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(combo, Priority.ALWAYS);
            estilizarControl(combo);

            SpinnerValueFactory.IntegerSpinnerValueFactory svf = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, quota, 1);
            Spinner<Integer> spinner = new Spinner<>(svf);
            spinner.setPrefWidth(50);
            spinner.setEditable(true);
            estilizarControl(spinner);

            // Registrar slot para control de cuota
            SlotControl slotCtrl = new SlotControl(combo, spinner, svf, comidaIdx, fila, colIndex);
            List<SlotControl> listaSlots = controlesPorGrupo.computeIfAbsent(colIndex, k -> new ArrayList<>());
            listaSlots.add(slotCtrl);

            // Listener combo
            combo.valueProperty().addListener((obs, oldV, newV) -> {
                String nuevoAlim = newV != null ? newV : "";
                int nuevaPorc = spinner.getValue() != null ? spinner.getValue() : 1;
                actualizarNutrienteEnMatriz(comidaIdx, fila, colIndex, nuevoAlim, nuevaPorc);
            });

            // Listener spinner
            spinner.valueProperty().addListener((obs, oldV, newV) -> {
                String nuevoAlim = combo.getValue() != null ? combo.getValue() : "";
                int nuevaPorc = newV != null ? newV : 1;
                actualizarNutrienteEnMatriz(comidaIdx, fila, colIndex, nuevoAlim, nuevaPorc);
            });

            row.getChildren().addAll(combo, spinner);
            rowsContainer.getChildren().add(row);
        }

        card.getChildren().addAll(headerBox, rowsContainer);
        return card;
    }

    private void actualizarNutrienteEnMatriz(int comidaIdx, int fila, int colIndex, String nuevoAlim, int nuevaPorc) {
        String[][] matAlim;
        int[][] matPorc;

        switch (comidaIdx) {
            case 0:
                matAlim = alimentosEnDesayuno;
                matPorc = porcionesEnDesayuno;
                break;
            case 1:
                matAlim = alimentosEnComida;
                matPorc = porcionesEnComida;
                break;
            case 2:
                matAlim = alimentosEnCena;
                matPorc = porcionesEnCena;
                break;
            default:
                matAlim = alimentosEnDesayuno;
                matPorc = porcionesEnDesayuno;
        }

        if (fila < matAlim.length && colIndex < matAlim[0].length) {
            matAlim[fila][colIndex] = nuevoAlim;
            matPorc[fila][colIndex] = nuevaPorc;
        }

        // Aplicar la regla clínica de cuota diaria de porciones
        actualizarCuotasPorGrupo(colIndex);

        // Recalcular nutrientes globales
        recalcularTotalesGenerales();
    }

    /**
     * Aplica la regla clínica de porciones:
     * Si se asignan X porciones a un alimento en cualquier comida, se restan del cupo diario disponible.
     * Si el cupo restante llega a 0, todas las casillas vacías de ese grupo se deshabilitan.
     * Al liberar porciones, las casillas se vuelven a habilitar automáticamente.
     */
    private void actualizarCuotasPorGrupo(int colIndex) {
        int cuotaTotal = (colIndex < porcionesObjetivo.size()) ? porcionesObjetivo.get(colIndex) : 1;

        int usadas = 0;
        String[][][] matricesAlim = { alimentosEnDesayuno, alimentosEnComida, alimentosEnCena };
        int[][][] matricesPorc = { porcionesEnDesayuno, porcionesEnComida, porcionesEnCena };

        for (int m = 0; m < 3; m++) {
            for (int r = 0; r < 6; r++) {
                if (colIndex < matricesAlim[m][0].length) {
                    String alim = matricesAlim[m][r][colIndex];
                    int porc = matricesPorc[m][r][colIndex];
                    if (alim != null && !alim.trim().isEmpty() && porc > 0) {
                        usadas += porc;
                    }
                }
            }
        }

        int restantes = cuotaTotal - usadas;

        // Actualizar todos los controles del grupo
        List<SlotControl> slots = controlesPorGrupo.get(colIndex);
        if (slots != null) {
            for (SlotControl sc : slots) {
                int m = sc.comidaIdx;
                int r = sc.filaIdx;
                String alim = matricesAlim[m][r][colIndex];
                int porc = matricesPorc[m][r][colIndex];

                boolean tieneAlimento = (alim != null && !alim.trim().isEmpty());

                if (tieneAlimento) {
                    // El slot ocupado permite aumentar hasta su porción actual + restantes
                    int maxPermitido = Math.max(1, porc + restantes);
                    sc.svf.setMax(maxPermitido);
                    sc.combo.setDisable(false);
                    sc.spinner.setDisable(false);
                } else {
                    // Slot vacío: Si no quedan porciones, se deshabilita
                    if (restantes <= 0) {
                        sc.combo.setDisable(true);
                        sc.spinner.setDisable(true);
                        sc.combo.setPromptText("Límite (" + cuotaTotal + " porc.)");
                    } else {
                        sc.combo.setDisable(false);
                        sc.spinner.setDisable(false);
                        sc.combo.setPromptText("Elegir alimento...");
                        sc.svf.setMax(Math.max(1, restantes));
                        if (sc.spinner.getValue() > restantes) {
                            sc.svf.setValue(Math.max(1, restantes));
                        }
                    }
                }
            }
        }

        // Actualizar badge visual en el encabezado
        Label badge = badgePorcionesPorGrupo.get(colIndex);
        if (badge != null) {
            badge.setText("Usadas: " + usadas + "/" + cuotaTotal + " porc.");
            if (usadas == cuotaTotal) {
                badge.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 3; -fx-padding: 1 5;");
            } else if (usadas > cuotaTotal) {
                badge.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 3; -fx-padding: 1 5;");
            } else {
                badge.setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-font-weight: bold; -fx-background-radius: 3; -fx-padding: 1 5;");
            }
        }
    }

    // =========================================================================
    // SECCIÓN DE RESUMEN: 'NUTRIENTES ACTUALES (g)' y 'PORCENTAJE DE NUTRIENTES (%)'
    // =========================================================================

    private VBox crearSeccionResumenNutrientes() {
        VBox container = new VBox(6);
        container.setAlignment(Pos.TOP_LEFT);

        Label secTitle = new Label("3. RESUMEN DE NUTRIENTES CONSUMIDOS");
        secTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        secTitle.setTextFill(PRIMARY_GREEN);

        HBox tablesRow = new HBox(14);
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
        VBox container = new VBox(5);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #2E7D32; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 6 8;"
        );
        container.setEffect(new DropShadow(5, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label titulo = new Label("NUTRIENTES ACTUALES (g)");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        titulo.setTextFill(Color.WHITE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 6 10;"
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
        tablaNutrientesGramos.setFixedCellSize(26.0);
        tablaNutrientesGramos.prefHeightProperty().bind(tablaNutrientesGramos.fixedCellSizeProperty().multiply(3).add(30));
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
        VBox container = new VBox(5);
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #388E3C; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 6 8;"
        );
        container.setEffect(new DropShadow(5, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label titulo = new Label("PORCENTAJE DE NUTRIENTES (%)");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        titulo.setTextFill(Color.WHITE);
        titulo.setAlignment(Pos.CENTER);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setStyle(
            "-fx-background-color: #388E3C; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 6 10;"
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
        tablaPorcentajes.setFixedCellSize(26.0);
        tablaPorcentajes.prefHeightProperty().bind(tablaPorcentajes.fixedCellSizeProperty().multiply(3).add(30));
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

        Button exportarBtn = new Button("EXPORTAR PLAN A TXT");
        exportarBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        exportarBtn.setTextFill(Color.WHITE);
        exportarBtn.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 6 18; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 3, 0, 0, 1);"
        );
        exportarBtn.setOnAction(e -> exportarATXT());

        Button limpiarBtn = new Button("LIMPIAR PLAN");
        limpiarBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        limpiarBtn.setTextFill(Color.WHITE);
        limpiarBtn.setStyle(
            "-fx-background-color: #C62828; " +
            "-fx-background-radius: 14; " +
            "-fx-padding: 6 14; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 3, 0, 0, 1);"
        );
        limpiarBtn.setOnAction(e -> reiniciarPlan());

        footer.getChildren().addAll(exportarBtn, limpiarBtn);
        return footer;
    }

    private void reiniciarPlan() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Limpieza");
        confirmacion.setHeaderText("¿Deseas reiniciar todas las comidas y platillos del plan?");
        confirmacion.setContentText("Esta accion restablecera los datos del plan alimenticio a 0.");

        confirmacion.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                inicializarEstructuras();
                for (int i = 0; i < gruposEspecificos.size(); i++) {
                    actualizarCuotasPorGrupo(i);
                }
                recalcularTotalesGenerales();
                if (scrollPrincipal != null) {
                    scrollPrincipal.setContent(crearPanelCentral().getContent());
                }
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

        int nGrupos = gruposEspecificos.size();

        // 1. Sumatoria de matrices de alimentos en las 3 comidas
        String[][][] matricesAlim = { alimentosEnDesayuno, alimentosEnComida, alimentosEnCena };
        int[][][] matricesPorc = { porcionesEnDesayuno, porcionesEnComida, porcionesEnCena };

        for (int m = 0; m < 3; m++) {
            String[][] matA = matricesAlim[m];
            int[][] matP = matricesPorc[m];

            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < nGrupos; c++) {
                    if (r < matA.length && c < matA[0].length) {
                        String alim = matA[r][c];
                        int porc = matP[r][c];

                        if (alim != null && !alim.trim().isEmpty() && porc > 0) {
                            Map<String, Double> nut = nutrientesAlimentos.get(alim);
                            if (nut == null) nut = nutrientesPlatillos.get(alim);

                            if (nut != null) {
                                sumHc += nut.getOrDefault("HC", 0.0) * porc;
                                sumLip += nut.getOrDefault("Lípidos", 0.0) * porc;
                                sumProt += nut.getOrDefault("Proteínas", 0.0) * porc;
                            }
                        }
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
    // CARGA ASÍNCRONA DE DATOS (EXCEL Y CSV)
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

            // Actualizar todas las listas de ComboBoxes in-place
            for (String grupo : gruposEspecificos) {
                List<String> alimentos = datosExcel.get(grupo);
                if (alimentos != null && !alimentos.isEmpty()) {
                    ObservableList<String> obs = cachedListasAlimentos.get(grupo);
                    if (obs != null) {
                        List<String> items = new ArrayList<>();
                        items.add("");
                        items.addAll(alimentos);
                        obs.setAll(items);
                    }
                }
            }

            // Inicializar las cuotas de cada grupo
            for (int i = 0; i < gruposEspecificos.size(); i++) {
                actualizarCuotasPorGrupo(i);
            }
        });

        tareaCarga.setOnFailed(e -> {
            estadoCargaLabel.textProperty().unbind();
            estadoCargaLabel.setText("Aviso: datos locales cargados");
            barraProgreso.setVisible(false);
            if (tareaCarga.getException() != null) {
                tareaCarga.getException().printStackTrace();
            }
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
            if (is == null) {
                try {
                    is = new FileInputStream("data/SMAE_5aed-2.0.xlsx");
                } catch (FileNotFoundException ignored) {}
            }

            if (is == null) {
                System.err.println("ERROR: No se encontro SMAE_5aed-2.0.xlsx");
                return;
            }

            try (Workbook workbook = new XSSFWorkbook(is)) {
                int inicio = 3;
                int fin = workbook.getNumberOfSheets() - 3;

                for (int i = inicio; i < fin; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    String nombreHoja = sheet.getSheetName().trim();

                    for (String grupo : gruposEspecificos) {
                        if (coincideHojaConGrupo(nombreHoja, grupo)) {
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

    private boolean coincideHojaConGrupo(String nombreHoja, String grupo) {
        if (nombreHoja == null || grupo == null) return false;
        String hojaNorm = normalizarTexto(nombreHoja);
        String grupoNorm = normalizarTexto(grupo);

        if (hojaNorm.equalsIgnoreCase(grupoNorm)) return true;

        // Verduras
        if (grupoNorm.contains("verdura") && hojaNorm.contains("verdura")) return true;

        // Frutas
        if (grupoNorm.contains("fruta") && hojaNorm.contains("fruta")) return true;

        // Leguminosas
        if (grupoNorm.contains("leguminosa") && hojaNorm.contains("leguminosa")) return true;

        // Cereales
        if (grupoNorm.contains("cereal")) {
            if ((grupoNorm.contains("sin grasa") || grupoNorm.contains("sg")) && 
                (hojaNorm.contains("sg") || hojaNorm.contains("sin grasa"))) return true;
            if ((grupoNorm.contains("con grasa") || grupoNorm.contains("cg")) && 
                (hojaNorm.contains("cg") || hojaNorm.contains("con grasa"))) return true;
        }

        // AOA / Animal
        if (grupoNorm.contains("animal") || grupoNorm.contains("aoa")) {
            if ((grupoNorm.contains("muy bajo") || grupoNorm.contains("mbag") || grupoNorm.contains("mrag")) &&
                (hojaNorm.contains("muy bajo") || hojaNorm.contains("mbag") || hojaNorm.contains("mrag"))) return true;
            if ((grupoNorm.contains("bajo") && !grupoNorm.contains("muy bajo")) &&
                (hojaNorm.contains("bajo") && !hojaNorm.contains("muy bajo"))) return true;
            if (grupoNorm.contains("moderado") && hojaNorm.contains("moderado")) return true;
            if (grupoNorm.contains("alto") && hojaNorm.contains("alto")) return true;
        }

        // Leche
        if (grupoNorm.contains("leche")) {
            if (grupoNorm.contains("descremada") && !grupoNorm.contains("semi") && hojaNorm.contains("descremada")) return true;
            if ((grupoNorm.contains("semi") || grupoNorm.contains("semidescremada")) && hojaNorm.contains("semi")) return true;
            if (grupoNorm.contains("entera") && hojaNorm.contains("entera")) return true;
            if (grupoNorm.contains("azucar") && hojaNorm.contains("azucar")) return true;
        }

        // Aceites y Grasas
        if (grupoNorm.contains("grasa") || grupoNorm.contains("aceite")) {
            if (grupoNorm.contains("sin proteina") && hojaNorm.contains("sin proteina")) return true;
            if (grupoNorm.contains("con proteina") && hojaNorm.contains("con proteina")) return true;
        }

        // Azúcares
        if (grupoNorm.contains("azucar") || grupoNorm.contains("azucares")) {
            if (grupoNorm.contains("sin grasa") && (hojaNorm.equalsIgnoreCase("azucares") || hojaNorm.contains("sin grasa"))) return true;
            if (grupoNorm.contains("con grasa") && hojaNorm.contains("con grasa")) return true;
        }

        return false;
    }

    private String normalizarTexto(String str) {
        if (str == null) return "";
        String nfd = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("").trim().toLowerCase();
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
            if (is == null) {
                try {
                    is = new FileInputStream("data/Platillos_mexicanos.csv");
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
            alert.setContentText("Por favor ingresa al menos un alimento o platillo antes de exportar el plan alimenticio.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Plan Alimenticio Personalizado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt"));
        fileChooser.setInitialFileName("plan_alimenticio.txt");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("================================================================================\n");
                writer.write("           NUTRIENERGIA PRO - PLAN ALIMENTICIO PERSONALIZADO                    \n");
                writer.write("================================================================================\n\n");

                writer.write("1. EVALUACION DE MACRONUTRIENTES:\n");
                writer.write("--------------------------------------------------------------------------------\n");
                writer.write(String.format("%-24s %-16s %-16s %-16s\n", "NUTRIENTE", "OBJETIVO (g)", "ACTUAL (g)", "DIFERENCIA"));
                writer.write(String.format("%-24s %-16s %-16s %-16s\n", "─".repeat(24), "─".repeat(16), "─".repeat(16), "─".repeat(16)));

                writer.write(String.format("%-24s %-16.1f %-16.1f %+.1f g\n", "Hidratos de Carbono", idealHc, totalHc, (totalHc - idealHc)));
                writer.write(String.format("%-24s %-16.1f %-16.1f %+.1f g\n", "Lipidos", idealLipidos, totalLipidos, (totalLipidos - idealLipidos)));
                writer.write(String.format("%-24s %-16.1f %-16.1f %+.1f g\n", "Proteinas", idealProteinas, totalProteinas, (totalProteinas - idealProteinas)));

                double totalKcal = (totalHc * 4) + (totalProteinas * 4) + (totalLipidos * 9);
                writer.write(String.format("\nEnergia Total Consumida: %.1f kcal", totalKcal));
                if (kcalObjetivo > 0) {
                    writer.write(String.format(" (Meta: %.0f kcal)\n\n", kcalObjetivo));
                } else {
                    writer.write("\n\n");
                }

                exportarDetalleComida(writer, "DESAYUNO", alimentosEnDesayuno, porcionesEnDesayuno, platillosEspecificosSeleccionados.get("DESAYUNO"));
                exportarDetalleComida(writer, "COMIDA", alimentosEnComida, porcionesEnComida, platillosEspecificosSeleccionados.get("COMIDA"));
                exportarDetalleComida(writer, "CENA", alimentosEnCena, porcionesEnCena, platillosEspecificosSeleccionados.get("CENA"));

                writer.write("================================================================================\n");
                writer.write("Plan generado el: " + new Date() + "\n");
                writer.write("================================================================================\n");

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Plan Exportado");
                info.setHeaderText("Archivo generado con exito");
                info.setContentText("El plan alimenticio se ha guardado en:\n" + file.getAbsolutePath());
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

    private void exportarDetalleComida(FileWriter writer, String nombreComida, String[][] matA, int[][] matP, List<PlatilloSeleccionado> platillos) throws IOException {
        writer.write("--------------------------------------------------------------------------------\n");
        writer.write("DISTRIBUCION DE: " + nombreComida + "\n");
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
        int nGrupos = gruposEspecificos.size();
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < nGrupos; c++) {
                if (r < matA.length && c < matA[0].length) {
                    String alim = matA[r][c];
                    int porc = matP[r][c];
                    if (alim != null && !alim.isEmpty() && porc > 0) {
                        if (!tieneAlimentos) {
                            writer.write("• Alimentos por Grupo Prescrito:\n");
                            tieneAlimentos = true;
                        }
                        writer.write(String.format("   - %s: %s (%d porc.)\n", gruposEspecificos.get(c), alim, porc));
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
        int nGrupos = gruposEspecificos.size();
        String[][][] matricesAlim = { alimentosEnDesayuno, alimentosEnComida, alimentosEnCena };

        for (String[][] mat : matricesAlim) {
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < nGrupos; c++) {
                    if (r < mat.length && c < mat[0].length) {
                        if (mat[r][c] != null && !mat[r][c].trim().isEmpty()) return true;
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
    // CLASES AUXILIARES Y MODELOS
    // =========================================================================

    private static class SlotControl {
        ComboBox<String> combo;
        Spinner<Integer> spinner;
        SpinnerValueFactory.IntegerSpinnerValueFactory svf;
        int comidaIdx;
        int filaIdx;
        int colIdx;

        SlotControl(ComboBox<String> combo, Spinner<Integer> spinner,
                    SpinnerValueFactory.IntegerSpinnerValueFactory svf,
                    int comidaIdx, int filaIdx, int colIdx) {
            this.combo = combo;
            this.spinner = spinner;
            this.svf = svf;
            this.comidaIdx = comidaIdx;
            this.filaIdx = filaIdx;
            this.colIdx = colIdx;
        }
    }

    public static class PlatilloSeleccionado {
        String nombre;
        int porciones;

        public PlatilloSeleccionado(String nombre, int porciones) {
            this.nombre = nombre;
            this.porciones = porciones;
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

    public static void main(String[] args) {
        launch(args);
    }
}
