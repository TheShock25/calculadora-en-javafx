package calculadoraenergeticamodernaa;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Recordatorio extends Application {

    // Colores
    private static final Color PRIMARY_COLOR = Color.rgb(52, 152, 219);
    private static final Color SUCCESS_COLOR = Color.rgb(39, 174, 96);
    private static final Color WARNING_COLOR = Color.rgb(241, 196, 15);
    private static final Color DANGER_COLOR = Color.rgb(231, 76, 60);
    private static final Color BACKGROUND_COLOR = Color.rgb(245, 246, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = Color.rgb(44, 62, 80);

    // Componentes
    private TableView<AlimentoComida> tablaDesayuno;
    private TableView<AlimentoComida> tablaComida;
    private TableView<AlimentoComida> tablaCena;
    private TableView<NutrienteInfo> tablaNutrientes;
    private TableView<NutrienteInfo> tablaPorcentajes;

    // Datos
    private double idealHc, idealLipidos, idealProteinas;
    private double totalHc = 0, totalLipidos = 0, totalProteinas = 0;

    private Map<String, List<String>> datosExcel = new HashMap<>();
    private Map<String, Map<String, Double>> nutrientesAlimentos = new HashMap<>();
    private Map<String, Map<String, Double>> nutrientesPlatillos = new HashMap<>();
    private ObservableList<String> listaPlatillos = FXCollections.observableArrayList();

    private boolean isTabletMode;
    private DecimalFormat df = new DecimalFormat("#.##");

    // Agregar después de: private boolean isTabletMode;
    private Map<String, List<PlatilloSeleccionado>> platillosEspecificosSeleccionados = new HashMap<>();

    // Agregar como clase interna estática (antes de AlimentoComida)
    private static class PlatilloSeleccionado {
        String nombre;
        int porciones;

        PlatilloSeleccionado(String nombre, int porciones) {
            this.nombre = nombre;
            this.porciones = porciones;
        }
    }

    // Lista de todos los grupos
    private List<String> todosLosGrupos = Arrays.asList(
            "Verduras", "Frutas", "Cereales y tubérculos - Sin Grasa", "Cereales y tubérculos - Con Grasa",
            "Leguminosas", "Alimentos de origen animal - MBAG", "Alimentos de origen animal - BAG",
            "Alimentos de origen animal - MAG", "Alimentos de origen animal - AAG", "Leche - Descremada",
            "Leche - Semi", "Leche - Entera", "Leche - Con Azucar", "Aceite y grasa - Sin proteina",
            "Aceite y grasa - Con proteina", "Azucar - Sin grasa", "Azucar - Con grasa");

    private Map<String, List<String>> nombreExcel = new HashMap<>();

    public Recordatorio() {
        // default targets to 0 (can be set by caller if using constructor with params)
        this(0, 0, 0);
    }

    public Recordatorio(double hc, double lipidos, double proteinas) {
        this.idealHc = hc;
        this.idealLipidos = lipidos;
        this.idealProteinas = proteinas;
        this.isTabletMode = ResponsiveManager.isTabletMode();

        inicializarMapeoExcel();
    }

    private void inicializarMapeoExcel() {
        nombreExcel.put("Verduras", List.of("Verduras"));
        nombreExcel.put("Frutas", List.of("Frutas"));
        nombreExcel.put("Cereales y tubérculos - Sin Grasa",
                List.of("Cereales SG", "Cereales y tubérculos - Sin Grasa"));
        nombreExcel.put("Cereales y tubérculos - Con Grasa",
                List.of("Cereales CG", "Cereales y tubérculos - Con Grasa"));
        nombreExcel.put("Leguminosas", List.of("Leguminosas"));
        nombreExcel.put("Alimentos de origen animal - MBAG",
                List.of("AOA de muy bajo aporte de grasa", "AOA Muy Bajo"));
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

    private VBox crearHeader() {
        VBox header = new VBox(ResponsiveManager.getSpacing(8, 10));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #2C3E50;");

        double margin = ResponsiveManager.getMargin(10, 15);
        header.setPadding(new Insets(margin));

        Label titulo = new Label("RECORDATORIO ALIMENTICIO - TODOS LOS GRUPOS");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, ResponsiveManager.getFontSize(18, 20)));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Registro completo de todos los grupos alimenticios disponibles");
        subtitulo.setFont(Font.font("Arial", FontWeight.NORMAL, ResponsiveManager.getFontSize(11, 12)));
        subtitulo.setTextFill(Color.rgb(189, 195, 199));

        Button exportarBtn = new Button("Exportar a TXT");
        exportarBtn.setStyle(
                "-fx-background-color: #27AE60; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 12;");
        exportarBtn.setOnAction(e -> exportarATXT());

        header.getChildren().addAll(titulo, subtitulo, exportarBtn);

        return header;
    }

    private ScrollPane crearPanelCentral() {
        VBox centerPanel = new VBox(ResponsiveManager.getSpacing(10, 15));
        centerPanel.setPadding(new Insets(ResponsiveManager.getMargin(10, 15)));

        // Panel de platillos específicos
        HBox platillosPanel = crearPanelPlatillosEspecificos();

        // Panel de tablas de comidas
        VBox comidasPanel = crearPanelComidas();

        // Panel de nutrientes
        VBox nutrientesPanel = crearPanelNutrientes();

        centerPanel.getChildren().addAll(platillosPanel, comidasPanel, nutrientesPanel);

        ScrollPane scroll = new ScrollPane(centerPanel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        return scroll;
    }

    private HBox crearPanelPlatillosEspecificos() {
        HBox panel = new HBox(ResponsiveManager.getSpacing(8, 10));
        panel.setPadding(new Insets(ResponsiveManager.getMargin(0, 0), ResponsiveManager.getMargin(0, 0), 8,
                ResponsiveManager.getMargin(0, 0)));

        // Crear tarjetas para platillos de cada comida
        VBox desayunoCard = crearTarjetaPlatillos("DESAYUNO", PRIMARY_COLOR);
        VBox comidaCard = crearTarjetaPlatillos("COMIDA", Color.rgb(230, 126, 34));
        VBox cenaCard = crearTarjetaPlatillos("CENA", Color.rgb(155, 89, 182));

        panel.getChildren().addAll(desayunoCard, comidaCard, cenaCard);

        return panel;
    }

    private VBox crearTarjetaPlatillos(String titulo, Color color) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + toRGBCode(color) +
                "; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5;");

        double width = ResponsiveManager.getWidth(200, 240);
        double height = ResponsiveManager.getHeight(120, 140);
        card.setPrefSize(width, height);
        card.setMaxWidth(width);

        // Header
        Label headerLabel = new Label("PLATILLOS - " + titulo);
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD,
                ResponsiveManager.getFontSize(10, 11)));
        headerLabel.setTextFill(Color.WHITE);
        headerLabel.setStyle("-fx-background-color: " + toRGBCode(color) + "; -fx-padding: 6 10;");
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(Pos.CENTER);

        // Inicializar lista de platillos para esta comida
        platillosEspecificosSeleccionados.putIfAbsent(titulo, new ArrayList<>());

        // Contenedor de platillos con scroll si es necesario
        VBox platillosContainer = new VBox(6);
        platillosContainer.setPadding(new Insets(8));

        // Crear 2 filas de ComboBox + Spinner
        for (int i = 0; i < 4; i++) {
            final int index = i;

            // Etiqueta del slot
            Label slotLabel = new Label("Platillo " + (i + 1) + ":");
            slotLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
            slotLabel.setTextFill(TEXT_COLOR);

            HBox row = new HBox(6);
            row.setAlignment(Pos.CENTER_LEFT);

            // ComboBox más grande
            ComboBox<String> combo = new ComboBox<>();
            combo.setPromptText("Seleccionar...");
            combo.setPrefWidth(width - 100);
            combo.setEditable(false);

            // IMPORTANTE: Establecer los items ANTES de agregar listeners
            combo.setItems(listaPlatillos);

            // Spinner con mejor visibilidad
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
            Spinner<Integer> spinner = new Spinner<>(valueFactory);
            spinner.setPrefWidth(65);
            spinner.setMaxWidth(65);
            spinner.setEditable(true);
            spinner.setStyle("-fx-font-size: 11px;");

            // CRÍTICO: Usar valueProperty().addListener() en vez de setOnAction()
            combo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    Integer porciones = spinner.getValue();
                    if (porciones != null && porciones > 0) {
                        actualizarPlatilloEspecifico(titulo, index, newVal, porciones);
                        System.out.println("ComboBox cambió: " + newVal + " x" + porciones);
                    }
                } else if (newVal == null || newVal.isEmpty()) {
                    // Si se limpió la selección, remover el platillo
                    actualizarPlatilloEspecifico(titulo, index, "", 0);
                }
            });

            spinner.valueProperty().addListener((obs, oldV, newV) -> {
                String platilloSeleccionado = combo.getValue();
                if (platilloSeleccionado != null && !platilloSeleccionado.isEmpty() &&
                        newV != null && newV > 0) {
                    actualizarPlatilloEspecifico(titulo, index, platilloSeleccionado, newV);
                    System.out.println("Spinner cambió: " + platilloSeleccionado + " x" + newV);
                }
            });

            // Listener cuando el spinner pierde el foco (para edición manual)
            spinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    String platilloSeleccionado = combo.getValue();
                    Integer porciones = spinner.getValue();
                    if (platilloSeleccionado != null && !platilloSeleccionado.isEmpty() &&
                            porciones != null && porciones > 0) {
                        actualizarPlatilloEspecifico(titulo, index, platilloSeleccionado, porciones);
                    }
                }
            });

            row.getChildren().addAll(combo, spinner);

            VBox slotContainer = new VBox(3);
            slotContainer.getChildren().addAll(slotLabel, row);
            platillosContainer.getChildren().add(slotContainer);
        }

        // Botón "Ver todos" más pequeño
        Button verTodos = new Button("Ver todos");
        verTodos.setStyle("-fx-font-size: 9px; -fx-padding: 3 8;");
        verTodos.setOnAction(e -> mostrarPlatillosEspecificosModal(listaPlatillos));
        HBox btnBox = new HBox(verTodos);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(2, 0, 0, 0));
        platillosContainer.getChildren().add(btnBox);

        card.getChildren().addAll(headerLabel, platillosContainer);

        return card;
    }

    private void actualizarPlatilloEspecifico(String comida, int index, String platillo, int porciones) {
        List<PlatilloSeleccionado> lista = platillosEspecificosSeleccionados.get(comida);

        // Asegurar que la lista tenga suficiente tamaño
        while (lista.size() <= index) {
            lista.add(new PlatilloSeleccionado("", 0));
        }

        // Obtener el platillo anterior para restar sus nutrientes
        PlatilloSeleccionado anterior = lista.get(index);

        // Restar nutrientes del platillo anterior
        if (anterior != null && !anterior.nombre.isEmpty()) {
            Map<String, Double> nutAnterior = nutrientesPlatillos.get(anterior.nombre);
            if (nutAnterior != null) {
                totalHc -= nutAnterior.getOrDefault("HC", 0.0) * anterior.porciones;
                totalLipidos -= nutAnterior.getOrDefault("Lípidos", 0.0) * anterior.porciones;
                totalProteinas -= nutAnterior.getOrDefault("Proteínas", 0.0) * anterior.porciones;
            }
        }

        // Actualizar con el nuevo platillo
        if (platillo != null && !platillo.isEmpty()) {
            lista.set(index, new PlatilloSeleccionado(platillo, porciones));

            // Sumar nutrientes del nuevo platillo
            Map<String, Double> nutNuevo = nutrientesPlatillos.get(platillo);
            if (nutNuevo != null) {
                totalHc += nutNuevo.getOrDefault("HC", 0.0) * porciones;
                totalLipidos += nutNuevo.getOrDefault("Lípidos", 0.0) * porciones;
                totalProteinas += nutNuevo.getOrDefault("Proteínas", 0.0) * porciones;
            }
        } else {
            lista.set(index, new PlatilloSeleccionado("", 0));
        }

        // Actualizar las tablas de nutrientes
        actualizarTablasNutrientes();

        System.out.println("Platillo " + comida + "[" + index + "]: " + platillo + " x" + porciones);
    }

    private void actualizarTablasNutrientes() {
        // Actualizar tabla de nutrientes (gramos)
        if (tablaNutrientes != null && !tablaNutrientes.getItems().isEmpty()) {
            ObservableList<NutrienteInfo> items = tablaNutrientes.getItems();
            if (items.size() >= 3) {
                items.get(0).setCantidad(df.format(totalHc));
                items.get(1).setCantidad(df.format(totalLipidos));
                items.get(2).setCantidad(df.format(totalProteinas));
                tablaNutrientes.refresh();
            }
        }

        // Actualizar tabla de porcentajes
        if (tablaPorcentajes != null && !tablaPorcentajes.getItems().isEmpty()) {
            ObservableList<NutrienteInfo> pitems = tablaPorcentajes.getItems();
            double totalNutrientes = totalHc + totalLipidos + totalProteinas;

            double pctHc = 0.0;
            double pctLip = 0.0;
            double pctProt = 0.0;

            if (totalNutrientes > 0) {
                pctHc = (totalHc / totalNutrientes) * 100.0;
                pctLip = (totalLipidos / totalNutrientes) * 100.0;
                pctProt = (totalProteinas / totalNutrientes) * 100.0;
            }
            if (pitems.size() >= 3) {
                pitems.get(0).setCantidad(df.format(pctHc) + "%");
                pitems.get(1).setCantidad(df.format(pctLip) + "%");
                pitems.get(2).setCantidad(df.format(pctProt) + "%");
                tablaPorcentajes.refresh();
            }
        }
    }

    private VBox crearContenedorTabla(String titulo, TableView<AlimentoComida> tabla, Color color) {
        VBox container = new VBox(5);
        container.setStyle("-fx-background-color: white; -fx-border-color: " + toRGBCode(color)
                + "; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5;");
        container.setPadding(new Insets(10));

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + toRGBCode(color) + "; -fx-padding: 10 15;");

        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        tituloLabel.setTextFill(Color.WHITE);

        Label infoLabel = new Label("← Desplaza → para ver todos los grupos");
        infoLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
        infoLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(tituloLabel, spacer, infoLabel);

        // Configurar tabla
        double tableHeight = isTabletMode ? 180 : 150;
        tabla.setPrefHeight(tableHeight);

        container.getChildren().addAll(header, tabla);

        return container;
    }

    private TableView<AlimentoComida> crearTablaComida(String nombreComida, Color color) {
        TableView<AlimentoComida> tabla = new TableView<>();
        tabla.setEditable(true);
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Crear columnas para todos los grupos: alimento + porciones
        for (String grupo : todosLosGrupos) {
            TableColumn<AlimentoComida, String> col = new TableColumn<>(grupo);
            col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                    Optional.ofNullable(cellData.getValue().getAlimento(grupo)).orElse("")));

            // IMPORTANTE: Ancho adecuado para que se vean ComboBox + Spinner
            col.setPrefWidth(ResponsiveManager.getWidth(180, 200));
            col.setMinWidth(170);
            col.setMaxWidth(250);

            col.setCellFactory(column -> new ComboBoxSpinnerTableCell(grupo));
            tabla.getColumns().add(col);
        }

        // Datos iniciales (3 filas)
        ObservableList<AlimentoComida> datos = FXCollections.observableArrayList();
        for (int i = 0; i < 6; i++)
            datos.add(new AlimentoComida());
        tabla.setItems(datos);

        return tabla;
    }

    private VBox crearPanelComidas() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(10, 15));
        panel.setStyle("-fx-background-color: #F5F6FA;");
        panel.setPadding(new Insets(ResponsiveManager.getMargin(8, 10)));

        // Crear las tablas de comidas
        tablaDesayuno = crearTablaComida("DESAYUNO", PRIMARY_COLOR);
        tablaComida = crearTablaComida("COMIDA", Color.rgb(230, 126, 34));
        tablaCena = crearTablaComida("CENA", Color.rgb(155, 89, 182));

        // Crear contenedores para las tablas
        VBox desayunoContainer = crearContenedorTabla("DESAYUNO", tablaDesayuno, PRIMARY_COLOR);
        VBox comidaContainer = crearContenedorTabla("COMIDA", tablaComida, Color.rgb(230, 126, 34));
        VBox cenaContainer = crearContenedorTabla("CENA", tablaCena, Color.rgb(155, 89, 182));

        panel.getChildren().addAll(desayunoContainer, comidaContainer, cenaContainer);

        return panel;
    }

    private VBox crearPanelNutrientes() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(10, 15));
        panel.setStyle("-fx-background-color: #F5F6FA;");
        panel.setPadding(new Insets(ResponsiveManager.getMargin(8, 10)));

        HBox nutrientesRow = new HBox(ResponsiveManager.getSpacing(10, 15));

        // Panel de nutrientes actuales
        VBox nutrientesPanel = crearPanelNutrientesGramos();

        // Panel de porcentajes
        VBox porcentajesPanel = crearPanelPorcentajeNutrientes();

        if (isTabletMode) {
            VBox verticalLayout = new VBox(10);
            verticalLayout.getChildren().addAll(nutrientesPanel, porcentajesPanel);
            panel.getChildren().add(verticalLayout);
        } else {
            nutrientesRow.getChildren().addAll(nutrientesPanel, porcentajesPanel);
            panel.getChildren().add(nutrientesRow);
        }

        return panel;
    }

    private VBox crearPanelNutrientesGramos() {
        VBox panel = new VBox(5);
        panel.setStyle("-fx-background-color: white; -fx-border-color: " + toRGBCode(PRIMARY_COLOR)
                + "; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5;");
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(ResponsiveManager.getWidth(280, 350));

        Label titulo = new Label("NUTRIENTES ACTUALES (g)");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titulo.setTextFill(Color.WHITE);
        titulo.setStyle("-fx-background-color: " + toRGBCode(PRIMARY_COLOR) + "; -fx-padding: 15 20;");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);

        tablaNutrientes = new TableView<>();
        tablaNutrientes.setPrefHeight(120);

        TableColumn<NutrienteInfo, String> nutrienteCol = new TableColumn<>("Nutriente");
        nutrienteCol.setCellValueFactory(new PropertyValueFactory<>("nutriente"));
        nutrienteCol.setPrefWidth(120);

        TableColumn<NutrienteInfo, String> cantidadCol = new TableColumn<>("Cantidad (g)");
        cantidadCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        cantidadCol.setPrefWidth(120);

        tablaNutrientes.getColumns().addAll(nutrienteCol, cantidadCol);

        ObservableList<NutrienteInfo> datosNutrientes = FXCollections.observableArrayList(
                new NutrienteInfo("HC", "0.0"),
                new NutrienteInfo("Lípidos", "0.0"),
                new NutrienteInfo("Proteínas", "0.0"));
        tablaNutrientes.setItems(datosNutrientes);

        panel.getChildren().addAll(titulo, tablaNutrientes);

        return panel;
    }

    private VBox crearPanelPorcentajeNutrientes() {
        VBox panel = new VBox(5);
        panel.setStyle("-fx-background-color: white; -fx-border-color: " + toRGBCode(SUCCESS_COLOR)
                + "; -fx-border-width: 2; -fx-background-radius: 5; -fx-border-radius: 5;");
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(ResponsiveManager.getWidth(280, 350));

        Label titulo = new Label("PORCENTAJE DE NUTRIENTES (%)");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titulo.setTextFill(Color.WHITE);
        titulo.setStyle("-fx-background-color: " + toRGBCode(SUCCESS_COLOR) + "; -fx-padding: 15 20;");
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.setAlignment(Pos.CENTER);

        tablaPorcentajes = new TableView<>();
        tablaPorcentajes.setPrefHeight(120);

        TableColumn<NutrienteInfo, String> nutrienteCol = new TableColumn<>("Nutriente");
        nutrienteCol.setCellValueFactory(new PropertyValueFactory<>("nutriente"));
        nutrienteCol.setPrefWidth(120);

        TableColumn<NutrienteInfo, String> porcentajeCol = new TableColumn<>("Porcentaje (%)");
        porcentajeCol.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        porcentajeCol.setPrefWidth(120);

        tablaPorcentajes.getColumns().addAll(nutrienteCol, porcentajeCol);

        ObservableList<NutrienteInfo> datosPorcentajes = FXCollections.observableArrayList(
                new NutrienteInfo("HC", "0.0%"),
                new NutrienteInfo("Lípidos", "0.0%"),
                new NutrienteInfo("Proteínas", "0.0%"));
        tablaPorcentajes.setItems(datosPorcentajes);

        panel.getChildren().addAll(titulo, tablaPorcentajes);

        return panel;
    }

    private void cargarDatosExcel() {
    InputStream is = null;
    try {
        // Intentar diferentes ubicaciones posibles
        is = getClass().getResourceAsStream("/data/SMAE_5aed-2.0.xlsx");
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream("data/SMAE_5aed-2.0.xlsx");
        }
        if (is == null) {
            // Último intento: desde sistema de archivos
            try {
                is = new java.io.FileInputStream("resources/data/SMAE_5aed-2.0.xlsx");
            } catch (java.io.FileNotFoundException e) {
                // Continuar al manejo de error
            }
        }
        
        if (is == null) {
            System.err.println("ERROR: No se pudo encontrar el archivo SMAE_5aed-2.0.xlsx");
            System.err.println("Buscado en:");
            System.err.println("1. /data/SMAE_5aed-2.0.xlsx (classpath)");
            System.err.println("2. data/SMAE_5aed-2.0.xlsx (classloader)");
            System.err.println("3. resources/data/SMAE_5aed-2.0.xlsx (sistema archivos)");
            return;
        }
        
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

                                        double hc = celdaHc != null && celdaHc.getCellType() == CellType.NUMERIC
                                                ? celdaHc.getNumericCellValue()
                                                : 0;
                                        double lipidos = celdaLipidos != null
                                                && celdaLipidos.getCellType() == CellType.NUMERIC
                                                        ? celdaLipidos.getNumericCellValue()
                                                        : 0;
                                        double proteinas = celdaProteinas != null
                                                && celdaProteinas.getCellType() == CellType.NUMERIC
                                                        ? celdaProteinas.getNumericCellValue()
                                                        : 0;

                                        Map<String, Double> nutrientes = new HashMap<>();
                                        nutrientes.put("HC", hc);
                                        nutrientes.put("Lípidos", lipidos);
                                        nutrientes.put("Proteínas", proteinas);

                                        nutrientesAlimentos.put(alimento, nutrientes);
                                    }
                                }
                            }
                        }
                        datosExcel.put(grupo, alimentos);
                    }
                }
            }
            System.out.println("✅ Excel cargado correctamente en Recordatorio: " + datosExcel.size() + " grupos");
        }
    } catch (Exception e) {
        System.err.println("❌ Error al cargar archivo Excel en Recordatorio: " + e.getMessage());
        e.printStackTrace();
    } finally {
        if (is != null) {
            try { is.close(); } catch (Exception e) {}
        }
    }
}

    private void cargarPlatillosCSV() {
    InputStream is = null;
    try {
        // Intentar diferentes ubicaciones posibles
        is = getClass().getResourceAsStream("/data/Platillos_mexicanos.csv");
        if (is == null) {
            is = getClass().getClassLoader().getResourceAsStream("data/Platillos_mexicanos.csv");
        }
        if (is == null) {
            try {
                is = new java.io.FileInputStream("resources/data/Platillos_mexicanos.csv");
            } catch (java.io.FileNotFoundException e) {
                // Continuar al manejo de error
            }
        }
        
        if (is == null) {
            System.err.println("ERROR: No se pudo encontrar el archivo Platillos_mexicanos.csv");
            System.err.println("Buscado en:");
            System.err.println("1. /data/Platillos_mexicanos.csv (classpath)");
            System.err.println("2. data/Platillos_mexicanos.csv (classloader)");
            System.err.println("3. resources/data/Platillos_mexicanos.csv (sistema archivos)");
            return;
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty())
                    continue;

                String[] partes = linea.split(",", -1);

                if (partes[0].toLowerCase().contains("platillo"))
                    continue;
                if (partes.length < 6)
                    continue;

                String nombrePlatillo = partes[0].trim();

                boolean esSubgrupo = true;
                for (int i = 2; i <= 4; i++) {
                    if (i < partes.length) {
                        try {
                            Double.parseDouble(partes[i].trim());
                            esSubgrupo = false;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (esSubgrupo)
                    continue;

                double prote = 0, lip = 0, hc = 0;
                try {
                    prote = Double.parseDouble(partes[2].trim());
                } catch (Exception ignored) {
                }
                try {
                    lip = Double.parseDouble(partes[3].trim());
                } catch (Exception ignored) {
                }
                try {
                    hc = Double.parseDouble(partes[4].trim());
                } catch (Exception ignored) {
                }

                listaPlatillos.add(nombrePlatillo);

                Map<String, Double> nutrientes = new HashMap<>();
                nutrientes.put("Proteínas", prote);
                nutrientes.put("Lípidos", lip);
                nutrientes.put("HC", hc);

                nutrientesPlatillos.put(nombrePlatillo, nutrientes);
            }
            System.out.println("✅ CSV cargado correctamente en Recordatorio: " + listaPlatillos.size() + " platillos encontrados.");
        }
    } catch (Exception e) {
        System.err.println("❌ Error al cargar archivo CSV en Recordatorio: " + e.getMessage());
        e.printStackTrace();
    } finally {
        if (is != null) {
            try { is.close(); } catch (Exception e) {}
        }
    }
}

    private void exportarATXT() {
        // Verificar si hay datos
        if (!hayDatosParaExportar()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sin datos");
            alert.setHeaderText("No hay datos para exportar");
            alert.setContentText("Por favor, agregue al menos un alimento o platillo específico.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar recordatorio alimenticio");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
        fileChooser.setInitialFileName("recordatorio_alimenticio.txt");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("=".repeat(80) + "\n");
                writer.write("RECORDATORIO ALIMENTICIO - TODOS LOS GRUPOS\n");
                writer.write("=".repeat(80) + "\n\n");

                writer.write("NUTRIENTES\n");
                writer.write("-".repeat(80) + "\n");
                writer.write(String.format("%-20s %-15s %-15s\n",
                        "NUTRIENTE", "OBJETIVO (g)", "ACTUAL (g)"));
                writer.write(String.format("%-20s %-15s %-15s\n",
                        "─".repeat(20), "─".repeat(15), "─".repeat(15)));
                writer.write(String.format("%-20s %-15.1f %-15.1f\n",
                        "Hidratos de Carbono", idealHc, totalHc));
                writer.write(String.format("%-20s %-15.1f %-15.1f\n",
                        "Lípidos", idealLipidos, totalLipidos));
                writer.write(String.format("%-20s %-15.1f %-15.1f\n",
                        "Proteínas", idealProteinas, totalProteinas));

                writer.write("\n" + "=".repeat(80) + "\n\n");

                // Exportar DESAYUNO
                exportarComidaTXT(writer, "DESAYUNO", tablaDesayuno);

                // Exportar COMIDA
                exportarComidaTXT(writer, "COMIDA", tablaComida);

                // Exportar CENA
                exportarComidaTXT(writer, "CENA", tablaCena);

                // Resumen final
                writer.write("=".repeat(80) + "\n");
                writer.write("RESUMEN DEL PLAN\n");
                writer.write("=".repeat(80) + "\n");

                double diffHc = totalHc - idealHc;
                double diffLip = totalLipidos - idealLipidos;
                double diffProt = totalProteinas - idealProteinas;

                writer.write(String.format("%-20s: %6.1f g (Objetivo: %6.1f g) %s\n",
                        "Hidratos de Carbono", totalHc, idealHc,
                        diffHc >= 0 ? String.format("(+%.1f g)", diffHc) : String.format("(%.1f g)", diffHc)));
                writer.write(String.format("%-20s: %6.1f g (Objetivo: %6.1f g) %s\n",
                        "Lípidos", totalLipidos, idealLipidos,
                        diffLip >= 0 ? String.format("(+%.1f g)", diffLip) : String.format("(%.1f g)", diffLip)));
                writer.write(String.format("%-20s: %6.1f g (Objetivo: %6.1f g) %s\n",
                        "Proteínas", totalProteinas, idealProteinas,
                        diffProt >= 0 ? String.format("(+%.1f g)", diffProt) : String.format("(%.1f g)", diffProt)));

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exportación exitosa");
                alert.setHeaderText("Datos exportados correctamente");
                alert.setContentText("El archivo se guardó en: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No se pudo guardar el archivo");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private boolean hayDatosParaExportar() {
        // Verificar si hay datos en las tablas de comidas
        for (AlimentoComida ac : tablaDesayuno.getItems()) {
            for (String grupo : todosLosGrupos) {
                if (ac.getAlimento(grupo) != null && !ac.getAlimento(grupo).isEmpty())
                    return true;
            }
        }
        for (AlimentoComida ac : tablaComida.getItems()) {
            for (String grupo : todosLosGrupos) {
                if (ac.getAlimento(grupo) != null && !ac.getAlimento(grupo).isEmpty())
                    return true;
            }
        }
        for (AlimentoComida ac : tablaCena.getItems()) {
            for (String grupo : todosLosGrupos) {
                if (ac.getAlimento(grupo) != null && !ac.getAlimento(grupo).isEmpty())
                    return true;
            }
        }

        // Verificar si hay platillos específicos seleccionados
        for (List<PlatilloSeleccionado> platillos : platillosEspecificosSeleccionados.values()) {
            for (PlatilloSeleccionado ps : platillos) {
                if (ps != null && !ps.nombre.isEmpty() && ps.porciones > 0)
                    return true;
            }
        }

        return false;
    }

    private void exportarComidaTXT(FileWriter writer, String nombreComida, TableView<AlimentoComida> tabla)
            throws IOException {
        writer.write(nombreComida + "\n");
        writer.write("-".repeat(80) + "\n");

        boolean tieneDatos = false;
        for (AlimentoComida alimentoComida : tabla.getItems()) {
            for (String grupo : todosLosGrupos) {
                String alimento = alimentoComida.getAlimento(grupo);
                if (alimento != null && !alimento.isEmpty()) {
                    writer.write(String.format("  • %-25s (%s)\n", alimento, grupo));
                    tieneDatos = true;
                }
            }
        }

        if (!tieneDatos) {
            writer.write("  Sin alimentos registrados\n");
        }
        writer.write("\n");
    }

    private void mostrarPlatillosEspecificosModal(ObservableList<String> platillos) {
        Stage modal = new Stage();
        modal.setTitle("Lista Completa de Platillos");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        Label titulo = new Label("PLATILLOS DISPONIBLES");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titulo.setTextFill(PRIMARY_COLOR);

        ListView<String> lista = new ListView<>(platillos);
        lista.setPrefHeight(400);
        lista.setPrefWidth(500);

        Button cerrar = new Button("Cerrar");
        cerrar.setStyle("-fx-background-color: " + toRGBCode(DANGER_COLOR) + "; -fx-text-fill: white;");
        cerrar.setOnAction(e -> modal.close());

        root.getChildren().addAll(titulo, lista, cerrar);

        Scene scene = new Scene(root, 550, 500);
        modal.setScene(scene);
        modal.show();
    }

    private static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    @Override
    public void start(Stage primaryStage) {
        cargarDatosExcel();
        cargarPlatillosCSV();

        VBox root = new VBox();
        root.setStyle("-fx-background-color: " + toRGBCode(BACKGROUND_COLOR) + ";");

        VBox header = crearHeader();
        ScrollPane centerPanel = crearPanelCentral();

        root.getChildren().addAll(header, centerPanel);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Recordatorio Alimenticio - Todos los Grupos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // CLASES INTERNAS - AGREGAR AL FINAL DEL ARCHIVO

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

    public static class NutrienteInfo {
        private String nutriente;
        private String cantidad;

        public NutrienteInfo(String nutriente, String cantidad) {
            this.nutriente = nutriente;
            this.cantidad = cantidad;
        }

        public String getNutriente() {
            return nutriente;
        }

        public void setNutriente(String nutriente) {
            this.nutriente = nutriente;
        }

        public String getCantidad() {
            return cantidad;
        }

        public void setCantidad(String cantidad) {
            this.cantidad = cantidad;
        }
    }

    private class ComboBoxSpinnerTableCell extends TableCell<AlimentoComida, String> {
        private HBox container;
        private ComboBox<String> comboBox;
        private Spinner<Integer> spinner;
        private String grupo;
        private boolean isUpdating = false;

        public ComboBoxSpinnerTableCell(String grupo) {
            this.grupo = grupo;

            // Configurar ComboBox
            comboBox = new ComboBox<>();
            comboBox.setMaxWidth(Double.MAX_VALUE);
            comboBox.setPromptText("Seleccionar alimento...");
            HBox.setHgrow(comboBox, Priority.ALWAYS);

            // Configurar Spinner con SpinnerValueFactory
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 1);
            spinner = new Spinner<>(valueFactory);
            spinner.setPrefWidth(70);
            spinner.setEditable(true);

            // Container
            container = new HBox(6);
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().addAll(comboBox, spinner);

            // Listeners para actualizar el modelo
            comboBox.setOnAction(e -> {
                if (!isUpdating && getIndex() >= 0) {
                    commitChanges();
                }
            });

            spinner.valueProperty().addListener((obs, oldV, newV) -> {
                if (!isUpdating && getIndex() >= 0 && oldV != null && !oldV.equals(newV)) {
                    commitChanges();
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || getIndex() < 0) {
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

                // Actualizar opciones del combo
                List<String> alimentos = datosExcel.getOrDefault(grupo, new ArrayList<>());
                ObservableList<String> items = FXCollections.observableArrayList();
                items.add(""); // Opción vacía
                items.addAll(alimentos);
                comboBox.setItems(items);

                // Establecer valores actuales
                String alimentoActual = row.getAlimento(grupo);
                int porcionActual = row.getPorcion(grupo);

                comboBox.setValue(alimentoActual != null ? alimentoActual : "");
                spinner.getValueFactory().setValue(porcionActual > 0 ? porcionActual : 1);

                setGraphic(container);
                setText(null);
            } finally {
                isUpdating = false;
            }
        }

        private void commitChanges() {
            int idx = getIndex();
            if (idx < 0 || idx >= getTableView().getItems().size())
                return;

            AlimentoComida rowItem = getTableView().getItems().get(idx);
            if (rowItem == null)
                return;

            String alimentoAnterior = rowItem.getAlimento(grupo);
            int porcionAnterior = rowItem.getPorcion(grupo);

            String alimentoNuevo = comboBox.getValue();
            Integer porcionNueva = spinner.getValue();

            if (alimentoNuevo == null)
                alimentoNuevo = "";
            if (porcionNueva == null)
                porcionNueva = 1;

            // Actualizar modelo
            rowItem.setAlimento(grupo, alimentoNuevo);
            rowItem.setPorcion(grupo, porcionNueva);

            // Recalcular totales
            recomputeTotalsFromAllTables();

            System.out.println("Cambio en " + grupo + ": " + alimentoAnterior +
                    " (x" + porcionAnterior + ") -> " + alimentoNuevo +
                    " (x" + porcionNueva + ")");
        }
    }

    private void recomputeTotalsFromAllTables() {
        double sumHc = 0.0;
        double sumLip = 0.0;
        double sumProt = 0.0;

        List<TableView<AlimentoComida>> tables = List.of(tablaDesayuno, tablaComida, tablaCena);
        for (TableView<AlimentoComida> table : tables) {
            if (table == null)
                continue;
            for (AlimentoComida row : table.getItems()) {
                if (row == null)
                    continue;
                for (String grupo : todosLosGrupos) {
                    String alimento = row.getAlimento(grupo);
                    if (alimento == null || alimento.isEmpty())
                        continue;

                    int porcion = row.getPorcion(grupo);

                    Map<String, Double> nut = nutrientesAlimentos.get(alimento);
                    if (nut == null)
                        nut = nutrientesPlatillos.get(alimento);
                    if (nut == null)
                        continue;

                    double hc = nut.getOrDefault("HC", 0.0) * porcion;
                    double lip = nut.getOrDefault("Lípidos", 0.0) * porcion;
                    double prot = nut.getOrDefault("Proteínas", 0.0) * porcion;

                    sumHc += hc;
                    sumLip += lip;
                    sumProt += prot;
                }
            }
        }

        totalHc = sumHc;
        totalLipidos = sumLip;
        totalProteinas = sumProt;

        // Actualizar tabla de nutrientes (gramos)
        if (tablaNutrientes != null && !tablaNutrientes.getItems().isEmpty()) {
            ObservableList<NutrienteInfo> items = tablaNutrientes.getItems();
            if (items.size() >= 3) {
                items.get(0).setCantidad(df.format(totalHc));
                items.get(1).setCantidad(df.format(totalLipidos));
                items.get(2).setCantidad(df.format(totalProteinas));
                tablaNutrientes.refresh();
            }
        }

        // Calcular porcentajes sobre el total de nutrientes
        if (tablaPorcentajes != null && !tablaPorcentajes.getItems().isEmpty()) {
            ObservableList<NutrienteInfo> pitems = tablaPorcentajes.getItems();
            double totalNutrientes = totalHc + totalLipidos + totalProteinas;

            double pctHc = 0.0;
            double pctLip = 0.0;
            double pctProt = 0.0;

            if (totalNutrientes > 0) {
                pctHc = (totalHc / totalNutrientes) * 100.0;
                pctLip = (totalLipidos / totalNutrientes) * 100.0;
                pctProt = (totalProteinas / totalNutrientes) * 100.0;
            }

            if (pitems.size() >= 3) {
                pitems.get(0).setCantidad(df.format(pctHc) + "%");
                pitems.get(1).setCantidad(df.format(pctLip) + "%");
                pitems.get(2).setCantidad(df.format(pctProt) + "%");
                tablaPorcentajes.refresh();
            }
        }
    }
}