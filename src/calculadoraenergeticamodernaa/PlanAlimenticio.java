package calculadoraenergeticamodernaa;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Versión migrada (simplificada) de PlanAlimenticio usando JavaFX.
 * Contiene una estructura equivalente: encabezado, secciones en VBox,
 * tablas con datos de ejemplo y estilos consistentes.
 */
public class PlanAlimenticio extends Application {

    private static final Color PRIMARY_COLOR = Color.web("#2980b9");
    private static final Color BACKGROUND_COLOR = Color.web("#f5f6fa");
    private static final String FONT_FAMILY = "Segoe UI";

    private java.util.List<java.util.Map<String, Object>> selectedEquivalentes;
    // Map grupo -> lista de alimentos (loaded from SMAE Excel)
    private Map<String, List<String>> datosExcel = new HashMap<>();

    // Mapping names used in the Excel similar to RecordatorioFX
    private Map<String, List<String>> nombreExcel = new HashMap<>();

    // Nutrient maps: alimento -> {"HC":x, "Lípidos":y, "Proteínas":z}
    private Map<String, Map<String, Double>> nutrientesAlimentos = new HashMap<>();
    private Map<String, Map<String, Double>> nutrientesPlatillos = new HashMap<>();

    // UI state: map grupo -> list of ItemRow controls
    private Map<String, List<ItemRow>> grupoItemRows = new HashMap<>();
    // Persistent model: map grupo -> list of ItemModel (source of truth for
    // selection/porciones)
    private Map<String, List<ItemModel>> grupoModelRows = new HashMap<>();

    // UI fields for export and global platillos
    private TableView<Alimento> summaryTableField;
    private Label totalesLabelField;
    private List<String> listaPlatillosGlobal = new ArrayList<>();

    private double kcalDiariasObjetivo;

    public PlanAlimenticio() {
        this.selectedEquivalentes = null;
        this.kcalDiariasObjetivo = 2000.0; // valor por defecto
    }

    // NUEVO CONSTRUCTOR con Kcal diarias
    public PlanAlimenticio(java.util.List<java.util.Map<String,Object>> selectedEquivalentes, double kcalDiarias) {
        this.selectedEquivalentes = selectedEquivalentes;
        this.kcalDiariasObjetivo = kcalDiarias;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Plan Alimenticio - JavaFX (migrado) - " + String.format("%,.0f kcal", kcalDiariasObjetivo));

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

        // Encabezado
        Label title = new Label("Plan Alimenticio");
        title.setFont(Font.font(FONT_FAMILY, 20));
        title.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        Button verPlatillosBtn = new Button("Ver platillos específicos");
        verPlatillosBtn.setOnAction(e -> mostrarPlatillosEspecificos(listaPlatillosGlobal));

        Button exportBtn = new Button("Exportar a TXT");
        exportBtn.setOnAction(e -> exportarATXTPlan());

        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);

        HBox header = new HBox(10, title, spacerHeader, verPlatillosBtn, exportBtn);
        header.setPadding(new Insets(12));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        root.setTop(header);

        // Contenido principal (scrollable)
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setPrefWidth(800);

        // Sección: Parámetros básicos (simulada con GridPane)
        content.getChildren().add(createParametrosBasicosSection());

        // Sección: Tabla de alimentos (TableView)
        try {
            inicializarMapeoExcel();
            cargarDatosExcel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        content.getChildren().add(createTablaAlimentosSection());

        // Sección: Resumen/calorías
        content.getChildren().add(createResumenSection());

        scroll.setContent(content);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 900, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Region createParametrosBasicosSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: white; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 1); -fx-border-radius: 8; -fx-background-radius: 8;");

        Label h = new Label("Parámetros básicos");
        h.setFont(Font.font(FONT_FAMILY, 16));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(4));

        grid.add(new Label("Edad:"), 0, 0);
        grid.add(new Label("30 años"), 1, 0);

        grid.add(new Label("Sexo:"), 0, 1);
        grid.add(new Label("Masculino"), 1, 1);

        grid.add(new Label("Peso:"), 0, 2);
        grid.add(new Label("75 kg"), 1, 2);

        grid.add(new Label("Altura:"), 0, 3);
        grid.add(new Label("175 cm"), 1, 3);

        box.getChildren().addAll(h, grid);
        return box;
    }

    private Region createTablaAlimentosSection() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: white; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 1); -fx-border-radius: 8; -fx-background-radius: 8;");

        Label h = new Label("Alimentos / Plan");
        h.setFont(Font.font(FONT_FAMILY, 16));

        // If selectedEquivalentes provided, create a panel per grupo
        VBox gruposContainer = new VBox(8);
        gruposContainer.setId("gruposContainer");
        // Filter controls: subgrupo selector
        HBox filtroBox = new HBox(8);
        filtroBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> comboSubgrupo = new ComboBox<>();
        comboSubgrupo.setEditable(true);
        // fill with possible sheet names / grupos
        comboSubgrupo.getItems().addAll(datosExcel.keySet());
        comboSubgrupo.setPromptText("Ingrese o seleccione subgrupo...");
        Button mostrarBtn = new Button("Mostrar");
        Button mostrarTodosBtn = new Button("Mostrar todos");
        filtroBox.getChildren().addAll(new Label("Subgrupo:"), comboSubgrupo, mostrarBtn, mostrarTodosBtn);
        // Place filter box above gruposContainer
        VBox containerWrapper = new VBox(6, filtroBox, gruposContainer);
        // Ensure platillos CSV is loaded for 'Ver platillos' modal
        listaPlatillosGlobal = cargarPlatillosCSV();

        // function to (re)populate gruposContainer based on a collection of grupos
        java.util.function.Consumer<Collection<String>> populateGrupos = (grupos) -> {
            gruposContainer.getChildren().clear();
            for (String grupo : grupos) {
                List<String> alimentos = datosExcel.getOrDefault(grupo, List.of());
                VBox contenido = createGrupoAlimentosPanel(grupo, alimentos);
                TitledPane tp = new TitledPane(grupo + " (" + alimentos.size() + ")", contenido);
                tp.setCollapsible(true);
                gruposContainer.getChildren().add(tp);
            }
        };

        if (selectedEquivalentes != null && !selectedEquivalentes.isEmpty()) {
            // Use the 'grupo' field first if available; otherwise try to map subgrupo
            // reliably
            LinkedHashSet<String> gruposSeleccionados = new LinkedHashSet<>();
            for (var map : selectedEquivalentes) {
                String grupoFrom = (String) map.getOrDefault("grupo", "");
                String sub = (String) map.getOrDefault("subgrupo", "");

                // prefer explicit grupo match
                if (grupoFrom != null && !grupoFrom.isBlank()) {
                    for (String gKey : datosExcel.keySet()) {
                        if (gKey.equalsIgnoreCase(grupoFrom.trim())) {
                            gruposSeleccionados.add(gKey);
                            break;
                        }
                    }
                    if (gruposSeleccionados.contains(grupoFrom))
                        continue;
                }

                // try abbreviation or direct mapping
                Optional<String> mapped = mapSubgrupoToGroupKey(grupoFrom, sub);
                if (mapped.isPresent()) {
                    gruposSeleccionados.add(mapped.get());
                    continue;
                }

                // try exact matches against possible names
                if (sub != null && !sub.isBlank()) {
                    String trimmed = sub.trim();
                    boolean matched = false;
                    for (String grupoKey : nombreExcel.keySet()) {
                        for (String p : nombreExcel.getOrDefault(grupoKey, List.of(grupoKey))) {
                            if (p.equalsIgnoreCase(trimmed) || grupoKey.equalsIgnoreCase(trimmed)) {
                                gruposSeleccionados.add(grupoKey);
                                matched = true;
                                break;
                            }
                        }
                        if (matched)
                            break;
                    }
                    if (matched)
                        continue;

                    // fallback: contains match but only if unique
                    List<String> containsMatches = new ArrayList<>();
                    for (String grupoKey : nombreExcel.keySet()) {
                        for (String p : nombreExcel.getOrDefault(grupoKey, List.of(grupoKey))) {
                            if (p.toLowerCase().contains(trimmed.toLowerCase())
                                    || grupoKey.toLowerCase().contains(trimmed.toLowerCase())) {
                                containsMatches.add(grupoKey);
                                break;
                            }
                        }
                    }
                    if (containsMatches.size() == 1)
                        gruposSeleccionados.add(containsMatches.get(0));
                }
            }
            if (!gruposSeleccionados.isEmpty())
                populateGrupos.accept(gruposSeleccionados);
            else
                populateGrupos.accept(datosExcel.keySet());
        } else {
            populateGrupos.accept(datosExcel.keySet());
        }

        // filter actions
        mostrarBtn.setOnAction(e -> {
            String val = comboSubgrupo.getEditor().getText();
            if (val == null || val.trim().isEmpty())
                return;
            String trimmed = val.trim();
            // find grupos whose nombreExcel entries contain this subgrupo
            // (case-insensitive)
            List<String> matches = new ArrayList<>();
            for (String grupo : datosExcel.keySet()) {
                List<String> posibles = nombreExcel.getOrDefault(grupo, List.of(grupo));
                for (String p : posibles) {
                    if (p.equalsIgnoreCase(trimmed) || p.toLowerCase().contains(trimmed.toLowerCase())) {
                        matches.add(grupo);
                        break;
                    }
                }
            }
            if (!matches.isEmpty())
                populateGrupos.accept(matches);
        });

        mostrarTodosBtn.setOnAction(e -> populateGrupos.accept(datosExcel.keySet()));

        // Totals display
        totalesLabelField = new Label("Totales: Kcal: 0 | HC: 0.0g | Proteínas: 0.0g | Lípidos: 0.0g");

        // Summary table for selected items
        summaryTableField = new TableView<>();
        summaryTableField.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<Alimento, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().nombre));

        TableColumn<Alimento, String> colPorcion = new TableColumn<>("Porción");
        colPorcion.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().porcion));

        TableColumn<Alimento, String> colKcal = new TableColumn<>("Kcal");
        colKcal.setCellValueFactory(
                c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().kcal)));

        // NUEVA COLUMNA: Eliminar
        TableColumn<Alimento, Void> colEliminar = new TableColumn<>("Eliminar");
        colEliminar.setPrefWidth(80);
        colEliminar.setCellFactory(param -> new TableCell<Alimento, Void>() {
            private final Button deleteButton = new Button("X");

            {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteButton.setOnAction(event -> {
                    Alimento alimento = getTableView().getItems().get(getIndex());
                    summaryTableField.getItems().remove(alimento);
                    recomputeTotals();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        summaryTableField.getColumns().addAll(colNombre, colPorcion, colKcal, colEliminar);

        // totals state
        final double[] totalHC = { 0.0 };
        final double[] totalProt = { 0.0 };
        final double[] totalLip = { 0.0 };
        final double[] totalKcal = { 0.0 };

        Button agregarBtn = new Button("Agregar seleccionados");
        agregarBtn.setOnAction(e -> {
            // collect checked items from persistent models (so selections persist)
            List<ItemModel> selectedModels = new ArrayList<>();

            for (var entry : grupoModelRows.entrySet()) {
                for (ItemModel model : entry.getValue()) {
                    if (model.isSelected()) {
                        selectedModels.add(model);
                        String nombre = model.getNombre();
                        int por = model.getPorcion();

                        // Lookup nutrients: first check Excel map, then CSV platillos
                        Map<String, Double> nut = nutrientesAlimentos.getOrDefault(nombre,
                                nutrientesPlatillos.getOrDefault(nombre, Map.of()));
                        double hc = nut.getOrDefault("HC", 0.0) * por;
                        double prot = nut.getOrDefault("Proteínas", 0.0) * por;
                        double lip = nut.getOrDefault("Lípidos", 0.0) * por;
                        double kcal = hc * 4 + prot * 4 + lip * 9;

                        summaryTableField.getItems()
                                .add(new Alimento(nombre, por + " porciones", (int) Math.round(kcal)));
                    }
                }
            }

            // LIMPIAR SELECCIONES: Desmarcar checkboxes y resetear porciones a 1
            for (ItemModel model : selectedModels) {
                model.setSelected(false);
                model.setPorcion(1);
            }

            // recompute totals from summary table
            recomputeTotals();
        });
        // Agregar botón para eliminar todos los alimentos de la tabla
        Button eliminarTodosBtn = new Button("Eliminar todos");
        eliminarTodosBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        eliminarTodosBtn.setOnAction(e -> {
            summaryTableField.getItems().clear();
            recomputeTotals();
        });

        box.getChildren().addAll(h, containerWrapper, agregarBtn, eliminarTodosBtn, totalesLabelField,
                new Label("Resumen del plan:"), summaryTableField);
        return box;
    }

    private void exportarATXTPlan() {
        if (summaryTableField == null)
            return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar plan alimenticio");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto", "*.txt"));
        fc.setInitialFileName("plan_alimenticio.txt");
        java.io.File file = fc.showSaveDialog(null);
        if (file == null)
            return;
        try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
            fw.write("PLAN ALIMENTICIO\n\n");
            fw.write(totalesLabelField.getText() + "\n\n");
            for (Alimento a : summaryTableField.getItems()) {
                fw.write(String.format("%s - %s - %dkcal\n", a.nombre, a.porcion, a.kcal));
            }
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Exportado");
            info.setHeaderText("Plan exportado");
            info.setContentText("Archivo: " + file.getAbsolutePath());
            info.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Map well-known subgrupo abbreviations or values from SistemaEquivalentes
    // to the group keys used in nombreExcel.
    private Optional<String> mapSubgrupoToGroupKey(String grupo, String subgrupo) {
        if (subgrupo == null || subgrupo.isBlank())
            return Optional.empty();
        String s = subgrupo.trim();
        // common AOA abbreviations
        Map<String, String> abbrev = Map.of(
                "MBAG", "Alimentos de origen animal - MBAG",
                "BAG", "Alimentos de origen animal - BAG",
                "MAG", "Alimentos de origen animal - MAG",
                "AAG", "Alimentos de origen animal - AAG");
        if (abbrev.containsKey(s.toUpperCase()))
            return Optional.of(abbrev.get(s.toUpperCase()));

        // Leche variants
        if (s.equalsIgnoreCase("Descremada"))
            return Optional.of("Leche - Descremada");
        if (s.equalsIgnoreCase("Semi"))
            return Optional.of("Leche - Semi");
        if (s.equalsIgnoreCase("Entera"))
            return Optional.of("Leche - Entera");
        if (s.equalsIgnoreCase("Con Azucar"))
            return Optional.of("Leche - Con Azucar");

        // Cereales
        if (s.equalsIgnoreCase("Sin Grasa") || s.equalsIgnoreCase("SG"))
            return Optional.of("Cereales y tubérculos - Sin Grasa");
        if (s.equalsIgnoreCase("Con Grasa") || s.equalsIgnoreCase("CG"))
            return Optional.of("Cereales y tubérculos - Con Grasa");

        // Azucares
        if (s.equalsIgnoreCase("Sin Grasa"))
            return Optional.of("Azucar - Sin grasa");
        if (s.equalsIgnoreCase("Con Grasa"))
            return Optional.of("Azucar - Con grasa");

        // Aceites y grasas
        if (s.equalsIgnoreCase("Sin Proteina"))
            return Optional.of("Aceite y grasa - Sin proteina");
        if (s.equalsIgnoreCase("Con Proteina"))
            return Optional.of("Aceite y grasa - Con proteina");

        // Leguminosas
        if (s.equalsIgnoreCase("Leguminosas"))
            return Optional.of("Leguminosas");

        // Verduras y frutas
        if (s.equalsIgnoreCase("Verduras"))
            return Optional.of("Verduras");
        if (s.equalsIgnoreCase("Frutas"))
            return Optional.of("Frutas");

        // Try direct matching against nombreExcel possible values
        for (String grupoKey : nombreExcel.keySet()) {
            for (String p : nombreExcel.getOrDefault(grupoKey, List.of())) {
                if (p.equalsIgnoreCase(s) || p.toLowerCase().contains(s.toLowerCase()))
                    return Optional.of(grupoKey);
            }
            if (grupoKey.equalsIgnoreCase(s))
                return Optional.of(grupoKey);
        }
        return Optional.empty();
    }

    // Recompute totals from summaryTableField items (preferred over ad-hoc local
    // sums)
    private void recomputeTotals() {
        double totalHC = 0, totalProt = 0, totalLip = 0, totalKcal = 0;
        if (summaryTableField != null) {
            for (Alimento a : summaryTableField.getItems()) {
                String nombre = a.nombre;
                int por = 1;
                try {
                    String p = a.porcion.split("\\s+")[0];
                    por = Integer.parseInt(p);
                } catch (Exception ignored) {
                }
                Map<String, Double> nut = nutrientesAlimentos.getOrDefault(nombre,
                        nutrientesPlatillos.getOrDefault(nombre, Map.of()));
                double hc = nut.getOrDefault("HC", 0.0) * por;
                double prot = nut.getOrDefault("Proteínas", 0.0) * por;
                double lip = nut.getOrDefault("Lípidos", 0.0) * por;
                double kcal = hc * 4 + prot * 4 + lip * 9;
                totalHC += hc;
                totalProt += prot;
                totalLip += lip;
                totalKcal += kcal;
            }
        }
        totalesLabelField.setText(String.format("Totales: Kcal: %.0f | HC: %.1fg | Proteínas: %.1fg | Lípidos: %.1fg",
                totalKcal, totalHC, totalProt, totalLip));
    }

    // Load Platillos_mexicanos.csv and return list of platillo names
    private List<String> cargarPlatillosCSV() {
    List<String> lista = new ArrayList<>();
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
            return lista;
        }
        
        try (BufferedReader br = new BufferedReader(new java.io.InputStreamReader(is))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty())
                    continue;
                String[] partes = linea.split(",", -1);
                if (partes[0].toLowerCase().contains("platillo"))
                    continue;
                if (partes.length < 1)
                    continue;
                String nombrePlatillo = partes[0].trim();
                boolean esSubgrupo = true;
                for (int i = 2; i <= 4; i++) {
                    if (i < partes.length) {
                        try {
                            Double.parseDouble(partes[i].trim());
                            esSubgrupo = false;
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (esSubgrupo)
                    continue;
                if (!nombrePlatillo.isEmpty()) {
                    lista.add(nombrePlatillo);
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
                    Map<String, Double> nutrientes = new HashMap<>();
                    nutrientes.put("Proteínas", prote);
                    nutrientes.put("Lípidos", lip);
                    nutrientes.put("HC", hc);
                    nutrientesPlatillos.put(nombrePlatillo, nutrientes);
                }
            }
            System.out.println("✅ Archivo CSV cargado correctamente. " + lista.size() + " platillos encontrados.");
        }
    } catch (Exception e) {
        System.err.println("❌ Error al cargar archivo CSV: " + e.getMessage());
        e.printStackTrace();
    } finally {
        if (is != null) {
            try { is.close(); } catch (Exception e) {}
        }
    }
    return lista;
}

    private void mostrarPlatillosEspecificos(List<String> listaPlatillos) {
        Stage modal = new Stage();
        modal.setTitle("Platillos específicos");
        VBox root = new VBox(8);
        root.setPadding(new Insets(10));

        // Table with platillo name, porciones spinner and checkbox
        TableView<PlatilloRow> table = new TableView<>();
        table.setEditable(true);
        TableColumn<PlatilloRow, Boolean> selCol = new TableColumn<>("Sel");
        selCol.setCellValueFactory(c -> c.getValue().selectedProperty());
        selCol.setCellFactory(CheckBoxTableCell.forTableColumn(selCol));
        selCol.setPrefWidth(40);

        TableColumn<PlatilloRow, String> nombreCol = new TableColumn<>("Platillo");
        nombreCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().nombre));
        nombreCol.setPrefWidth(260);

        TableColumn<PlatilloRow, Integer> porCol = new TableColumn<>("Porciones");
        porCol.setCellValueFactory(c -> c.getValue().porcionProperty().asObject());
        porCol.setPrefWidth(100);
        porCol.setCellFactory(col -> new TableCell<PlatilloRow, Integer>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 10, 1);

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    PlatilloRow row = getTableView().getItems().get(getIndex());
                    spinner.getValueFactory().setValue(row.getPorcion());
                    setGraphic(spinner);
                    spinner.valueProperty().addListener((obs, ov, nv) -> row.setPorcion(nv));
                }
            }
        });

        table.getColumns().addAll(selCol, nombreCol, porCol);
        ObservableList<PlatilloRow> rows = FXCollections.observableArrayList();
        for (String p : listaPlatillos)
            rows.add(new PlatilloRow(p));
        table.setItems(rows);
        table.setPrefHeight(420);

        Button agregar = new Button("Agregar seleccionados");
        agregar.setOnAction(e -> {
    List<PlatilloRow> selectedRows = new ArrayList<>();
    for (PlatilloRow r : table.getItems()) {
        if (r.isSelected()) {
            selectedRows.add(r);
            String nombre = r.getNombre();
            int por = r.getPorcion();
            Map<String, Double> nut = nutrientesPlatillos.getOrDefault(nombre, Map.of());
            double hc = nut.getOrDefault("HC", 0.0) * por;
            double prot = nut.getOrDefault("Proteínas", 0.0) * por;
            double lip = nut.getOrDefault("Lípidos", 0.0) * por;
            double kcal = hc * 4 + prot * 4 + lip * 9;
            summaryTableField.getItems().add(new Alimento(nombre, por + " porciones", (int) Math.round(kcal)));
        }
    }
    
    // LIMPIAR SELECCIONES en platillos - CORREGIDO
    for (PlatilloRow r : selectedRows) {
        r.selectedProperty().set(false); // Usar la propiedad directamente
        r.setPorcion(1);
    }
    
    recomputeTotals();
    modal.close();
});

        root.getChildren().addAll(new Label("Platillos específicos (selecciona y ajusta porciones):"), table, agregar);
        Scene scene = new Scene(root, 420, 560);
        modal.setScene(scene);
        modal.initOwner(null);
        modal.show();
    }

    // Helper row for platillos modal
    private static class PlatilloRow {
        private final String nombre;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final IntegerProperty porcion = new SimpleIntegerProperty(1);

        PlatilloRow(String n) {
            nombre = n;
        }

        public String getNombre() {
            return nombre;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public IntegerProperty porcionProperty() {
            return porcion;
        }

        public int getPorcion() {
            return porcion.get();
        }

        public void setPorcion(int p) {
            porcion.set(p);
        }
    }

    // Create UI panel for a single grupo: list of foods with CheckBox and Spinner
    private VBox createGrupoAlimentosPanel(String grupo, List<String> alimentos) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(8));
        box.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-background-radius:6; -fx-border-radius:6;");
        // Compact: do not render inner title (Accordion shows the title). Use a
        // scrollable list.
        VBox list = new VBox(4);
        // Ensure persistent models exist for this grupo
        List<ItemModel> models = grupoModelRows.get(grupo);
        if (models == null) {
            models = new ArrayList<>();
            for (String nombre : alimentos)
                models.add(new ItemModel(nombre));
            grupoModelRows.put(grupo, models);
        }

        List<ItemRow> rows = new ArrayList<>();
        for (ItemModel model : models) {
            ItemRow r = new ItemRow(model);
            rows.add(r);
            list.getChildren().add(r.hbox);
        }
        grupoItemRows.put(grupo, rows);
        // Wrap in ScrollPane to limit vertical space
        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        double pref = Math.min(250, Math.max(100, alimentos.size() * 26));
        sp.setPrefViewportHeight(pref);
        sp.setPrefHeight(pref);

        box.getChildren().addAll(sp);
        return box;
    }

    // Load datosExcel from SMAE_5aed-2.0.xlsx similarly to RecordatorioFX
    private void inicializarMapeoExcel() {
        // Full mapping copied from RecordatorioFX to match groups/subgrupos
        nombreExcel.put("Verduras", List.of("Verduras"));
        nombreExcel.put("Frutas", List.of("Frutas"));
        nombreExcel.put("Cereales y tubérculos - Sin Grasa", List.of("Cereales SG"));
        nombreExcel.put("Cereales y tubérculos - Con Grasa", List.of("Cereales CG"));
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
                for (String grupo : nombreExcel.keySet()) {
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
                                        // read nutrients if present in expected columns
                                        org.apache.poi.ss.usermodel.Cell celdaHc = row.getCell(9);
                                        org.apache.poi.ss.usermodel.Cell celdaLip = row.getCell(10);
                                        org.apache.poi.ss.usermodel.Cell celdaProt = row.getCell(11);
                                        double hc = celdaHc != null && celdaHc.getCellType() == CellType.NUMERIC
                                                ? celdaHc.getNumericCellValue()
                                                : 0.0;
                                        double lip = celdaLip != null && celdaLip.getCellType() == CellType.NUMERIC
                                                ? celdaLip.getNumericCellValue()
                                                : 0.0;
                                        double prot = celdaProt != null && celdaProt.getCellType() == CellType.NUMERIC
                                                ? celdaProt.getNumericCellValue()
                                                : 0.0;
                                        Map<String, Double> nut = new HashMap<>();
                                        nut.put("HC", hc);
                                        nut.put("Lípidos", lip);
                                        nut.put("Proteínas", prot);
                                        nutrientesAlimentos.put(alimento, nut);
                                    }
                                }
                            }
                        }
                        datosExcel.put(grupo, alimentos);
                    }
                }
            }
            System.out.println("✅ Archivo Excel cargado correctamente");
        }
    } catch (Exception e) {
        System.err.println("❌ Error al cargar archivo Excel: " + e.getMessage());
        e.printStackTrace();
    } finally {
        if (is != null) {
            try { is.close(); } catch (Exception e) {}
        }
    }
}
    // UI helper for a single food row
    // Model backing each selectable food row (persists selection and porcion)
    private static class ItemModel {
        private final String nombre;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final IntegerProperty porcion = new SimpleIntegerProperty(1);

        ItemModel(String nombre) {
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public IntegerProperty porcionProperty() {
            return porcion;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean v) {
            selected.set(v);
        }

        public int getPorcion() {
            return porcion.get();
        }

        public void setPorcion(int v) {
            porcion.set(v);
        }
    }

    // UI helper for a single food row bound to a model
    private static class ItemRow {
        final ItemModel model;
        final CheckBox checkBox;
        final Spinner<Integer> spinner;
        final HBox hbox;

        ItemRow(ItemModel model) {
            this.model = model;
            this.checkBox = new CheckBox(model.getNombre());
            this.spinner = new Spinner<>(1, 10, model.getPorcion());
            this.spinner.setPrefWidth(80);
            // bind checkbox to model
            this.checkBox.selectedProperty().bindBidirectional(model.selectedProperty());
            // bind spinner value to model porcion
            this.spinner.getValueFactory().valueProperty().bindBidirectional(model.porcionProperty().asObject());
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox = new HBox(8, checkBox, spacer, spinner);
            hbox.setAlignment(Pos.CENTER_LEFT);
        }
    }

    private Region createResumenSection() {
        VBox box = new VBox(8);
    box.setPadding(new Insets(12));
    box.setStyle("-fx-background-color: white; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.03), 8, 0, 0, 1); -fx-border-radius: 8; -fx-background-radius: 8;");

    Label h = new Label("Resumen energético");
    h.setFont(Font.font(FONT_FAMILY, 16));

    HBox row = new HBox(16);
    row.setAlignment(Pos.CENTER_LEFT);

    VBox left = new VBox(6);
    left.getChildren().addAll(new Label("TMB estimado:"), new Label("Calorías diarias objetivo:"));

    VBox right = new VBox(6);
    // Usar las Kcal diarias recibidas del Sistema de Equivalentes
    right.getChildren().addAll(new Label(String.format("%,.0f kcal", kcalDiariasObjetivo)));

    row.getChildren().addAll(left, right);
    box.getChildren().addAll(h, row);
    return box;
}

    // Clase interna simple para la tabla
    public static class Alimento {
        public final String nombre;
        public final String porcion;
        public final int kcal;

        public Alimento(String n, String p, int k) {
            nombre = n;
            porcion = p;
            kcal = k;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
