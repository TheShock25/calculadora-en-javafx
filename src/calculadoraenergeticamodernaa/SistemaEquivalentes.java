package calculadoraenergeticamodernaa;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import java.text.DecimalFormat;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

public class SistemaEquivalentes extends Application {

    // Colores
    private static final Color PRIMARY_COLOR = Color.rgb(46, 125, 50);
    private static final Color SECONDARY_COLOR = Color.rgb(76, 175, 80);
    private static final Color INFO_COLOR = Color.rgb(33, 150, 243);
    private static final Color BACKGROUND_COLOR = Color.rgb(245, 245, 245);

    // Componentes
    private TableView<Equivalente> tablaEquivalentes;
    private TableView<Nutriente> tablaDistribucion;
    private TableView<Nutriente> tablaNutrientes;
    private Label totalKcalLabel, sumaKcalLabel;
    private TextField kcalDiaField;
    private TextField porcentajeHCField, porcentajeProteinasField, porcentajeLipidosField;

    private DecimalFormat df = new DecimalFormat("#.#");
    private double kcalObjetivo;
    private boolean isTabletMode;

    // Porcentajes
    private double porcentajeHC = 60.0;
    private double porcentajeLipidos = 15.0;
    private double porcentajeProteinas = 25.0;

    // Datos base de equivalentes
    private Object[][] datosBase = {
            { "Verduras", " ", 25, 2, 0, 4 },
            { "Frutas", " ", 60, 0, 0, 15 },
            { "Cereales y tubérculos", "Sin Grasa", 70, 2, 0, 15 },
            { "Cereales y tubérculos", "Con Grasa", 115, 2, 5, 15 },
            { "Leguminosas", " ", 120, 8, 1, 20 },
            { "AOA", "MBAG", 40, 7, 1, 0 },
            { "AOA", "BAG", 55, 7, 3, 0 },
            { "AOA", "MAG", 75, 7, 5, 0 },
            { "AOA", "AAG", 100, 7, 8, 0 },
            { "Leche", "Descremada", 95, 9, 2, 12 },
            { "Leche", "Semi", 110, 9, 4, 12 },
            { "Leche", "Entera", 150, 9, 8, 12 },
            { "Leche", "Con Azucar", 200, 8, 5, 30 },
            { "Aceite y grasa", "Sin proteina", 45, 0, 5, 0 },
            { "Aceite y grasa", "Con proteina", 70, 3, 5, 3 },
            { "Azucar", "Sin grasa", 40, 0, 0, 10 },
            { "Azucar", "Con grasa", 85, 0, 5, 10 }
    };

    private ObservableList<Equivalente> equivalentesData;

    public SistemaEquivalentes() {
        this(2000.0);
    }

    public SistemaEquivalentes(double kcalCalculadas) {
        this.kcalObjetivo = kcalCalculadas;
        this.isTabletMode = ResponsiveManager.isTabletMode();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Equivalentes Nutricionales");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");

        // Header
        VBox header = crearHeader();
        root.setTop(header);

        // Centro
        BorderPane centerPanel = crearPanelCentral();
        root.setCenter(centerPanel);

        // Footer con botones
        HBox footer = crearFooter();
        root.setBottom(footer);

        // Escena
        double width = isTabletMode ? Math.min(1100, ResponsiveManager.getScreenBounds().getWidth() - 40) : 1200;
        double height = isTabletMode ? Math.min(750, ResponsiveManager.getScreenBounds().getHeight() - 40) : 800;

        Scene scene = new Scene(root, width, height);

        primaryStage.setScene(scene);
        primaryStage.show();

        // Inicializar datos
        inicializarDatos();
        actualizarTablaDistribucion();
        calcularTotales();
    }

    private VBox crearHeader() {
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: linear-gradient(to right, #2E7D32, #4CAF50);");
        header.setPadding(new Insets(ResponsiveManager.getMargin(15, 20)));
        header.setSpacing(10);

        Label titulo = new Label("SISTEMA DE EQUIVALENTES");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD,
                ResponsiveManager.getFontSize(20, 24)));
        titulo.setTextFill(Color.WHITE);

        // Panel con labels de valores
        HBox valoresPanel = new HBox(20);
        valoresPanel.setAlignment(Pos.CENTER);

        totalKcalLabel = crearLabelHeader("Kcal al día", String.format("%.0f", kcalObjetivo));
        sumaKcalLabel = crearLabelHeader("Suma Kcal", "0");

        valoresPanel.getChildren().addAll(totalKcalLabel, sumaKcalLabel);

        // Panel de entrada de Kcal y porcentajes
        HBox inputPanel = new HBox(10);
        inputPanel.setAlignment(Pos.CENTER);

        // Subpanel para Kcal
        VBox kcalPanel = new VBox(5);
        kcalPanel.setAlignment(Pos.CENTER);

        Label kcalLabel = new Label("Kcal/día:");
        kcalLabel.setTextFill(Color.WHITE);
        kcalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        kcalDiaField = new TextField(String.format("%.0f", kcalObjetivo));
        kcalDiaField.setPrefWidth(80);
        kcalDiaField.setStyle("-fx-background-color: white;");

        kcalPanel.getChildren().addAll(kcalLabel, kcalDiaField);

        // Subpanel para HC
        VBox hcPanel = new VBox(5);
        hcPanel.setAlignment(Pos.CENTER);

        Label hcLabel = new Label("HC %:");
        hcLabel.setTextFill(Color.WHITE);
        hcLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        porcentajeHCField = new TextField(String.valueOf(porcentajeHC));
        porcentajeHCField.setPrefWidth(50);
        porcentajeHCField.setStyle("-fx-background-color: white;");

        hcPanel.getChildren().addAll(hcLabel, porcentajeHCField);

        // Subpanel para Proteínas
        VBox protPanel = new VBox(5);
        protPanel.setAlignment(Pos.CENTER);

        Label protLabel = new Label("Prot %:");
        protLabel.setTextFill(Color.WHITE);
        protLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        porcentajeProteinasField = new TextField(String.valueOf(porcentajeProteinas));
        porcentajeProteinasField.setPrefWidth(50);
        porcentajeProteinasField.setStyle("-fx-background-color: white;");

        protPanel.getChildren().addAll(protLabel, porcentajeProteinasField);

        // Subpanel para Lípidos
        VBox lipPanel = new VBox(5);
        lipPanel.setAlignment(Pos.CENTER);

        Label lipLabel = new Label("Lip %:");
        lipLabel.setTextFill(Color.WHITE);
        lipLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        porcentajeLipidosField = new TextField(String.valueOf(porcentajeLipidos));
        porcentajeLipidosField.setPrefWidth(50);
        porcentajeLipidosField.setStyle("-fx-background-color: white;");

        lipPanel.getChildren().addAll(lipLabel, porcentajeLipidosField);

        // Botón actualizar
        Button actualizarBtn = new Button("Actualizar");
        actualizarBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        actualizarBtn.setOnAction(e -> {
            if (validarYActualizarPorcentajes()) {
                actualizarTablaDistribucion();
                calcularTotales();
            }
        });

        inputPanel.getChildren().addAll(kcalPanel, hcPanel, protPanel, lipPanel, actualizarBtn);

        // Label de validación de porcentajes
        Label validacionLabel = new Label();
        validacionLabel.setTextFill(Color.YELLOW);
        validacionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));

        header.getChildren().addAll(titulo, valoresPanel, inputPanel, validacionLabel);

        return header;
    }

    // Método para validar y actualizar porcentajes
    private boolean validarYActualizarPorcentajes() {
        try {
            double hc = Double.parseDouble(porcentajeHCField.getText());
            double prot = Double.parseDouble(porcentajeProteinasField.getText());
            double lip = Double.parseDouble(porcentajeLipidosField.getText());

            double suma = hc + prot + lip;

            if (Math.abs(suma - 100.0) > 0.1) {
                mostrarError("La suma de porcentajes debe ser 100%. Actual: " + df.format(suma) + "%");
                // Restaurar valores anteriores
                porcentajeHCField.setText(String.valueOf(porcentajeHC));
                porcentajeProteinasField.setText(String.valueOf(porcentajeProteinas));
                porcentajeLipidosField.setText(String.valueOf(porcentajeLipidos));
                return false;
            }

            if (hc < 0 || prot < 0 || lip < 0) {
                mostrarError("Los porcentajes no pueden ser negativos");
                return false;
            }

            // Actualizar valores
            porcentajeHC = hc;
            porcentajeProteinas = prot;
            porcentajeLipidos = lip;

            return true;

        } catch (NumberFormatException e) {
            mostrarError("Por favor ingrese valores numéricos válidos para los porcentajes");
            // Restaurar valores anteriores
            porcentajeHCField.setText(String.valueOf(porcentajeHC));
            porcentajeProteinasField.setText(String.valueOf(porcentajeProteinas));
            porcentajeLipidosField.setText(String.valueOf(porcentajeLipidos));
            return false;
        }
    }

    // Método para mostrar errores
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error en porcentajes");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private Label crearLabelHeader(String titulo, String valor) {
        Label label = new Label(titulo + "\n" + valor);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setTextFill(Color.WHITE);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-padding: 10; " +
                "-fx-background-radius: 5;");
        label.setPrefWidth(150);

        return label;
    }

    private BorderPane crearPanelCentral() {
        BorderPane centerPanel = new BorderPane();
        centerPanel.setPadding(new Insets(ResponsiveManager.getMargin(10, 15)));

        // Tabla principal de equivalentes
        VBox tablaPanel = crearTablaEquivalentes();
        centerPanel.setCenter(tablaPanel);

        // Panel derecho con tablas de resumen
        VBox rightPanel = crearPanelDerecho();
        centerPanel.setRight(rightPanel);

        return centerPanel;
    }

    private VBox crearTablaEquivalentes() {
        VBox container = new VBox(10);
        container.setStyle("-fx-background-color: white; -fx-border-color: #BDBDBD; " +
                "-fx-border-width: 1; -fx-padding: 10;");

        Label titulo = new Label("Distribución por equivalentes");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setTextFill(PRIMARY_COLOR);

        tablaEquivalentes = new TableView<>();
        tablaEquivalentes.setEditable(true);

        // Columnas
        TableColumn<Equivalente, String> grupoCol = new TableColumn<>("Grupo");
        grupoCol.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        grupoCol.setPrefWidth(180);

        TableColumn<Equivalente, String> subgrupoCol = new TableColumn<>("Subgrupo");
        subgrupoCol.setCellValueFactory(new PropertyValueFactory<>("subgrupo"));
        subgrupoCol.setPrefWidth(150);

        TableColumn<Equivalente, Integer> porcionesCol = new TableColumn<>("Porciones");
        porcionesCol.setCellValueFactory(new PropertyValueFactory<>("porciones"));
        porcionesCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        porcionesCol.setOnEditCommit(event -> {
            Equivalente eq = event.getRowValue();
            eq.setPorciones(event.getNewValue());
            calcularTotales();
            tablaEquivalentes.refresh();
        });
        porcionesCol.setPrefWidth(100);

        TableColumn<Equivalente, Integer> energiaCol = new TableColumn<>("Energía");
        energiaCol.setCellValueFactory(new PropertyValueFactory<>("energiaTotal"));
        energiaCol.setPrefWidth(100);

        TableColumn<Equivalente, Integer> proteinasCol = new TableColumn<>("Proteínas");
        proteinasCol.setCellValueFactory(new PropertyValueFactory<>("proteinasTotal"));
        proteinasCol.setPrefWidth(100);

        TableColumn<Equivalente, Integer> lipidosCol = new TableColumn<>("Lípidos");
        lipidosCol.setCellValueFactory(new PropertyValueFactory<>("lipidosTotal"));
        lipidosCol.setPrefWidth(100);

        TableColumn<Equivalente, Integer> hcCol = new TableColumn<>("HC");
        hcCol.setCellValueFactory(new PropertyValueFactory<>("hcTotal"));
        hcCol.setPrefWidth(100);

        tablaEquivalentes.getColumns().addAll(
                grupoCol, subgrupoCol, porcionesCol, energiaCol,
                proteinasCol, lipidosCol, hcCol);

        // Datos
        equivalentesData = FXCollections.observableArrayList();
        tablaEquivalentes.setItems(equivalentesData);

        container.getChildren().addAll(titulo, tablaEquivalentes);
        VBox.setVgrow(tablaEquivalentes, Priority.ALWAYS);

        return container;
    }

    private VBox crearPanelDerecho() {
        VBox panel = new VBox(ResponsiveManager.getSpacing(10, 15));
        panel.setPrefWidth(ResponsiveManager.getWidth(300, 350));
        panel.setPadding(new Insets(10));

        // Tabla de distribución
        VBox distribucionPanel = crearTablaDistribucion();

        // Tabla de nutrientes
        VBox nutrientesPanel = crearTablaNutrientes();

        panel.getChildren().addAll(distribucionPanel, nutrientesPanel);

        return panel;
    }

    private VBox crearTablaDistribucion() {
        VBox container = new VBox(5);
        container.setStyle("-fx-background-color: white; -fx-border-color: " +
                toRGBCode(PRIMARY_COLOR) + "; -fx-border-width: 2; -fx-padding: 10;");

        Label titulo = new Label("Distribución de macronutrientes");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        titulo.setTextFill(PRIMARY_COLOR);

        tablaDistribucion = new TableView<>();

        TableColumn<Nutriente, String> macroCol = new TableColumn<>("Macro");
        macroCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        macroCol.setPrefWidth(100);

        TableColumn<Nutriente, String> porcentajeCol = new TableColumn<>("%");
        porcentajeCol.setCellValueFactory(new PropertyValueFactory<>("porcentaje"));
        porcentajeCol.setPrefWidth(80);

        TableColumn<Nutriente, String> kcalCol = new TableColumn<>("Kcal");
        kcalCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        kcalCol.setPrefWidth(80);

        TableColumn<Nutriente, String> gramosCol = new TableColumn<>("g");
        gramosCol.setCellValueFactory(new PropertyValueFactory<>("gramos"));
        gramosCol.setPrefWidth(60);

        tablaDistribucion.getColumns().addAll(macroCol, porcentajeCol, kcalCol, gramosCol);
        tablaDistribucion.setPrefHeight(150);

        container.getChildren().addAll(titulo, tablaDistribucion);

        return container;
    }

    private VBox crearTablaNutrientes() {
        VBox container = new VBox(5);
        container.setStyle("-fx-background-color: white; -fx-border-color: " +
                toRGBCode(PRIMARY_COLOR) + "; -fx-border-width: 2; -fx-padding: 10;");

        Label titulo = new Label("Nutrientes calculados");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        titulo.setTextFill(PRIMARY_COLOR);

        tablaNutrientes = new TableView<>();

        TableColumn<Nutriente, String> nutrienteCol = new TableColumn<>("Nutriente");
        nutrienteCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nutrienteCol.setPrefWidth(100);

        TableColumn<Nutriente, String> totalKcalCol = new TableColumn<>("Total Kcal");
        totalKcalCol.setCellValueFactory(new PropertyValueFactory<>("kcal"));
        totalKcalCol.setPrefWidth(80);

        TableColumn<Nutriente, String> totalGramosCol = new TableColumn<>("Total g");
        totalGramosCol.setCellValueFactory(new PropertyValueFactory<>("gramos"));
        totalGramosCol.setPrefWidth(80);

        TableColumn<Nutriente, String> adecuacionCol = new TableColumn<>("% adec.");
        adecuacionCol.setCellValueFactory(new PropertyValueFactory<>("adecuacion"));
        adecuacionCol.setPrefWidth(80);

        // Colorear según adecuación
        adecuacionCol.setCellFactory(column -> new TableCell<Nutriente, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    try {
                        double valor = Double.parseDouble(item.replace("%", ""));
                        if (valor == 100) {
                            setStyle("-fx-background-color: #4CAF50;");
                        } else if ((valor >= 95 && valor < 100) || (valor > 100 && valor <= 105)) {
                            setStyle("-fx-background-color: #FFEB3B;");
                        } else if (valor > 105) {
                            setStyle("-fx-background-color: #F44336;");
                        } else {
                            setStyle("");
                        }
                    } catch (Exception e) {
                        setStyle("");
                    }
                }
            }
        });

        tablaNutrientes.getColumns().addAll(
                nutrienteCol, totalKcalCol, totalGramosCol, adecuacionCol);
        tablaNutrientes.setPrefHeight(150);

        container.getChildren().addAll(titulo, tablaNutrientes);

        return container;
    }

    private HBox crearFooter() {
        HBox footer = new HBox(ResponsiveManager.getSpacing(10, 15));
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(ResponsiveManager.getMargin(15, 20)));

        Button r24hBtn = crearBoton("R24H", PRIMARY_COLOR);
        r24hBtn.setOnAction(e -> abrirRecordatorio());

        Button planBtn = crearBoton("Plan Alimenticio", PRIMARY_COLOR);
        planBtn.setOnAction(e -> abrirPlanAlimenticio());

        Button resetBtn = crearBoton("Reiniciar", Color.rgb(244, 67, 54));
        resetBtn.setOnAction(e -> reiniciarTabla());

        Button exportBtn = crearBoton("Exportar", SECONDARY_COLOR);
        exportBtn.setOnAction(e -> exportarDatos());

        footer.getChildren().addAll(r24hBtn, planBtn, resetBtn, exportBtn);

        return footer;
    }

    private Button crearBoton(String texto, Color color) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);

        String colorHex = toRGBCode(color);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 5; " +
                        "-fx-padding: 8 16; -fx-cursor: hand;",
                colorHex));

        return btn;
    }

    private void inicializarDatos() {
        for (Object[] datos : datosBase) {
            equivalentesData.add(new Equivalente(
                    (String) datos[0],
                    (String) datos[1],
                    0, // porciones inicial en 0
                    (Integer) datos[2],
                    (Integer) datos[3],
                    (Integer) datos[4],
                    (Integer) datos[5]));
        }
    }

    private void actualizarTablaDistribucion() {
        try {
            double kcalTotal = Double.parseDouble(kcalDiaField.getText());

            double kcalHC = kcalTotal * (porcentajeHC / 100.0);
            double gramosHC = kcalHC / 4.0;

            double kcalProteinas = kcalTotal * (porcentajeProteinas / 100.0);
            double gramosProteinas = kcalProteinas / 4.0;

            double kcalLipidos = kcalTotal * (porcentajeLipidos / 100.0);
            double gramosLipidos = kcalLipidos / 9.0;

            double gramosTotal = gramosHC + gramosProteinas + gramosLipidos;

            ObservableList<Nutriente> datos = FXCollections.observableArrayList(
                    new Nutriente("HC", df.format(porcentajeHC) + "%",
                            df.format(kcalHC), df.format(gramosHC), ""),
                    new Nutriente("Lípidos", df.format(porcentajeLipidos) + "%",
                            df.format(kcalLipidos), df.format(gramosLipidos), ""),
                    new Nutriente("Proteínas", df.format(porcentajeProteinas) + "%",
                            df.format(kcalProteinas), df.format(gramosProteinas), ""),
                    new Nutriente("Total", "", df.format(kcalTotal), df.format(gramosTotal), ""));

            tablaDistribucion.setItems(datos);

            // Actualizar label
            totalKcalLabel.setText("Kcal al día\n" + df.format(kcalTotal));

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

        // Actualizar tabla de nutrientes
        try {
            double kcalRef = Double.parseDouble(kcalDiaField.getText());

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
                    new Nutriente("Lípidos", "", df.format(kcalLipidos), df.format(totalLipidos),
                            df.format(adecLip) + "%"),
                    new Nutriente("Proteínas", "", df.format(kcalProteinas), df.format(totalProteinas),
                            df.format(adecProt) + "%"),
                    new Nutriente("Total", "", df.format(totalKcal),
                            df.format(totalProteinas + totalLipidos + totalHC), df.format(adecTotal) + "%"));

            tablaNutrientes.setItems(datosNutrientes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Actualizar suma label
        sumaKcalLabel.setText("Suma Kcal\n" + df.format(totalKcal));

        // Cambiar color según diferencia
        try {
            double kcalObj = Double.parseDouble(kcalDiaField.getText());
            double diferencia = Math.abs(totalKcal - kcalObj);

            String colorStyle;
            if (diferencia <= 50) {
                colorStyle = "-fx-background-color: #2ECC71;";
            } else if (diferencia <= 150) {
                colorStyle = "-fx-background-color: #F1C40F;";
            } else {
                colorStyle = "-fx-background-color: #E74C3C;";
            }

            sumaKcalLabel.setStyle(colorStyle + " -fx-padding: 10; -fx-background-radius: 5; " +
                    "-fx-text-fill: white; -fx-font-weight: bold;");
        } catch (Exception e) {
            // Ignorar
        }
    }

    private void reiniciarTabla() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar reinicio");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro de reiniciar todas las porciones a 0?");

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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exportar Datos");
        alert.setHeaderText("Datos del Sistema de Equivalentes");

        StringBuilder sb = new StringBuilder();
        sb.append("=== SISTEMA DE EQUIVALENTES ===\n\n");
        sb.append("Kcal objetivo: ").append(kcalDiaField.getText()).append("\n\n");

        sb.append("EQUIVALENTES SELECCIONADOS:\n");
        for (Equivalente eq : equivalentesData) {
            if (eq.getPorciones() > 0) {
                sb.append(String.format("%s - %s: %d porciones (E:%d, P:%d, L:%d, HC:%d)\n",
                        eq.getGrupo(), eq.getSubgrupo(), eq.getPorciones(),
                        eq.getEnergiaTotal(), eq.getProteinasTotal(),
                        eq.getLipidosTotal(), eq.getHcTotal()));
            }
        }

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(20);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void abrirRecordatorio() {
        try {
            Stage stage = new Stage();
            Recordatorio recordatorio = new Recordatorio(0, 0, 0);
            recordatorio.start(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("No se pudo abrir Recordatorio 24H");
            error.setContentText(ex.getMessage());
            error.showAndWait();
        }
    }

    private void abrirPlanAlimenticio() {
        try {
            java.util.List<java.util.Map<String, Object>> seleccion = new java.util.ArrayList<>();
            double kcalDiarias = 0;

            // Obtener Kcal diarias del campo
            try {
                kcalDiarias = Double.parseDouble(kcalDiaField.getText());
            } catch (Exception e) {
                kcalDiarias = this.kcalObjetivo; // valor por defecto
            }

            // Solo agregar equivalentes con porciones > 0
            for (Equivalente eq : equivalentesData) {
                if (eq.getPorciones() > 0) {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("grupo", eq.getGrupo());
                    m.put("subgrupo", eq.getSubgrupo());
                    m.put("porciones", eq.getPorciones());
                    seleccion.add(m);
                }
            }

            // DEBUG: print selection sent to PlanAlimenticio
            System.out.println("[DEBUG] Seleccion enviada a PlanAlimenticio:");
            System.out.println("[DEBUG] Kcal diarias: " + kcalDiarias);
            for (var map : seleccion)
                System.out.println("  -> " + map);

            // Validar que hay equivalentes seleccionados
            if (seleccion.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Sin equivalentes seleccionados");
                alert.setHeaderText("No hay equivalentes para mostrar");
                alert.setContentText(
                        "Por favor, agregue porciones a los equivalentes antes de abrir el Plan Alimenticio.");
                alert.showAndWait();
                return;
            }

            Stage stage = new Stage();
            PlanAlimenticio plan = new PlanAlimenticio(seleccion, kcalDiarias);
            plan.start(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("No se pudo abrir Plan Alimenticio");
            error.setContentText(ex.getMessage());
            error.showAndWait();
        }
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    // Clases auxiliares
    public static class Equivalente {
        private String grupo;
        private String subgrupo;
        private int porciones;
        private int energiaBase;
        private int proteinasBase;
        private int lipidosBase;
        private int hcBase;

        public Equivalente(String grupo, String subgrupo, int porciones,
                int energiaBase, int proteinasBase, int lipidosBase, int hcBase) {
            this.grupo = grupo;
            this.subgrupo = subgrupo;
            this.porciones = porciones;
            this.energiaBase = energiaBase;
            this.proteinasBase = proteinasBase;
            this.lipidosBase = lipidosBase;
            this.hcBase = hcBase;
        }

        // Getters y setters
        public String getGrupo() {
            return grupo;
        }

        public String getSubgrupo() {
            return subgrupo;
        }

        public int getPorciones() {
            return porciones;
        }

        public void setPorciones(int porciones) {
            this.porciones = porciones;
        }

        public int getEnergiaTotal() {
            return energiaBase * porciones;
        }

        public int getProteinasTotal() {
            return proteinasBase * porciones;
        }

        public int getLipidosTotal() {
            return lipidosBase * porciones;
        }

        public int getHcTotal() {
            return hcBase * porciones;
        }
    }

    public static class Nutriente {
        private String nombre;
        private String porcentaje;
        private String kcal;
        private String gramos;
        private String adecuacion;

        public Nutriente(String nombre, String porcentaje, String kcal,
                String gramos, String adecuacion) {
            this.nombre = nombre;
            this.porcentaje = porcentaje;
            this.kcal = kcal;
            this.gramos = gramos;
            this.adecuacion = adecuacion;
        }

        public String getNombre() {
            return nombre;
        }

        public String getPorcentaje() {
            return porcentaje;
        }

        public String getKcal() {
            return kcal;
        }

        public String getGramos() {
            return gramos;
        }

        public String getAdecuacion() {
            return adecuacion;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}