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

public class VentanaInfoParametros extends Application {

    // Paleta de colores oficial: Verdes y Blancos
    private static final Color PRIMARY_GREEN = Color.web("#2E7D32");       // Verde esmeralda principal
    private static final Color SECONDARY_GREEN = Color.web("#388E3C");     // Verde medio
    private static final Color DARK_FOREST = Color.web("#1B5E20");         // Verde bosque profundo
    private static final Color LIGHT_MINT = Color.web("#E8F5E9");          // Fondo verde menta suave
    private static final Color BORDER_GREEN = Color.web("#C8E6C9");        // Borde verde suave
    private static final Color TEXT_DARK = Color.web("#1C2D27");           // Texto oscuro
    private static final Color TEXT_MUTED = Color.web("#5C7669");          // Texto secundario

    private boolean isTabletMode;

    public VentanaInfoParametros() {
    }

    @Override
    public void start(Stage primaryStage) {
        this.isTabletMode = ResponsiveManager.isTabletMode();

        primaryStage.setTitle("NutriEnergia Pro - Referencia de Parametros Corporales");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F4F7F5;");

        // Header institucional
        VBox header = crearHeader();
        root.setTop(header);

        // Centro desplazable con las tablas de referencia y notas
        ScrollPane centerScroll = crearPanelContenido();
        root.setCenter(centerScroll);

        // Footer institucional
        HBox footer = crearFooter();
        root.setBottom(footer);

        double width = isTabletMode ? 
            Math.min(1080, ResponsiveManager.getScreenBounds().getWidth() - 50) : 1000;
        double height = isTabletMode ? 
            Math.min(850, ResponsiveManager.getScreenBounds().getHeight() - 50) : 750;

        Scene scene = new Scene(root, width, height);
        cargarCSS(scene);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void abrir() {
        if (WindowManager.enfocarSiAbierta("VentanaInfoParametros")) {
            return;
        }
        try {
            Stage stage = new Stage();
            WindowManager.registrarVentana("VentanaInfoParametros", stage);
            VentanaInfoParametros ventana = new VentanaInfoParametros();
            ventana.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        double margin = ResponsiveManager.getMargin(16, 22);
        header.setPadding(new Insets(margin, margin, margin + 4, margin));

        Label badge = new Label("GUIA CLINICA Y ESTANDARES OMS");
        badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        badge.setTextFill(Color.web("#E8F5E9"));
        badge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.18); " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 3 10;"
        );

        Label titulo = new Label("Referencia de Parametros Corporales");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 
            ResponsiveManager.getFontSize(22, 26)));
        titulo.setTextFill(Color.WHITE);

        Label subtitulo = new Label("Valores normativos, puntos de corte e interpretacion antropometrica internacional");
        subtitulo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 
            ResponsiveManager.getFontSize(12.5, 13.5)));
        subtitulo.setTextFill(Color.web("#C8E6C9"));

        header.getChildren().addAll(badge, titulo, subtitulo);
        header.setEffect(new DropShadow(10, 0, 3, Color.rgb(0, 0, 0, 0.15)));

        return header;
    }

    private ScrollPane crearPanelContenido() {
        VBox content = new VBox(22);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));
        content.setMaxWidth(960);

        // Tabla 1: Parámetros Básicos de Composición
        VBox seccion1 = crearSeccionTabla(
            "1. PARAMETROS BASICOS DE COMPOSICION CORPORAL",
            obtenerDatosTabla1()
        );

        // Tabla 2: Parámetros Avanzados y Morfología
        VBox seccion2 = crearSeccionTabla(
            "2. PARAMETROS AVANZADOS, FRACCIONAMIENTO Y PLIEGUES",
            obtenerDatosTabla2()
        );

        // Panel de Notas Explicativas
        VBox notasPanel = crearPanelNotas();

        content.getChildren().addAll(seccion1, seccion2, notasPanel);

        HBox wrapper = new HBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scroll;
    }

    private VBox crearSeccionTabla(String titulo, String[][] datos) {
        VBox seccion = new VBox(10);
        seccion.setAlignment(Pos.TOP_LEFT);
        seccion.setMaxWidth(960);

        // Título de la sección
        Label seccionTitulo = new Label(titulo);
        seccionTitulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13.5));
        seccionTitulo.setTextFill(PRIMARY_GREEN);
        seccionTitulo.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 6; -fx-padding: 4 12;");

        // Contenedor de la tabla con elevación
        VBox tableContainer = new VBox();
        tableContainer.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #E2E8E4; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-overflow: hidden;"
        );
        tableContainer.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        // Cabecera de la tabla
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setStyle(
            "-fx-background-color: #2E7D32; " +
            "-fx-background-radius: 10 10 0 0; " +
            "-fx-padding: 10 16;"
        );

        Label col1 = crearHeaderLabel("PARAMETRO", 0.22);
        Label col2 = crearHeaderLabel("HOMBRES", 0.24);
        Label col3 = crearHeaderLabel("MUJERES", 0.24);
        Label col4 = crearHeaderLabel("INTERPRETACION CLINICA", 0.30);

        headerRow.getChildren().addAll(col1, col2, col3, col4);
        tableContainer.getChildren().add(headerRow);

        // Filas de datos
        for (int i = 0; i < datos.length; i++) {
            String[] fila = datos[i];
            boolean esPar = (i % 2 == 0);

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                String.format("-fx-background-color: %s; -fx-padding: 10 16; -fx-border-color: #F0F4F1; -fx-border-width: 0 0 1 0;",
                    esPar ? "#FFFFFF" : "#F7FAF8")
            );

            // Efecto hover sobre cada fila
            final boolean finalEsPar = esPar;
            row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #E8F5E9; -fx-padding: 10 16; -fx-border-color: #C8E6C9; -fx-border-width: 0 0 1 0;"
            ));
            row.setOnMouseExited(e -> row.setStyle(
                String.format("-fx-background-color: %s; -fx-padding: 10 16; -fx-border-color: #F0F4F1; -fx-border-width: 0 0 1 0;",
                    finalEsPar ? "#FFFFFF" : "#F7FAF8")
            ));

            Label paramLabel = new Label(fila[0]);
            paramLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12.5));
            paramLabel.setTextFill(TEXT_DARK);
            paramLabel.setWrapText(true);
            HBox.setHgrow(paramLabel, Priority.ALWAYS);
            paramLabel.setMaxWidth(Double.MAX_VALUE);
            paramLabel.prefWidthProperty().bind(tableContainer.widthProperty().multiply(0.22));

            Label homLabel = new Label(fila[1]);
            homLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            homLabel.setTextFill(TEXT_DARK);
            homLabel.setWrapText(true);
            HBox.setHgrow(homLabel, Priority.ALWAYS);
            homLabel.setMaxWidth(Double.MAX_VALUE);
            homLabel.prefWidthProperty().bind(tableContainer.widthProperty().multiply(0.24));

            Label mujLabel = new Label(fila[2]);
            mujLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            mujLabel.setTextFill(TEXT_DARK);
            mujLabel.setWrapText(true);
            HBox.setHgrow(mujLabel, Priority.ALWAYS);
            mujLabel.setMaxWidth(Double.MAX_VALUE);
            mujLabel.prefWidthProperty().bind(tableContainer.widthProperty().multiply(0.24));

            Label interpLabel = new Label(fila[3]);
            interpLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            interpLabel.setTextFill(TEXT_MUTED);
            interpLabel.setWrapText(true);
            HBox.setHgrow(interpLabel, Priority.ALWAYS);
            interpLabel.setMaxWidth(Double.MAX_VALUE);
            interpLabel.prefWidthProperty().bind(tableContainer.widthProperty().multiply(0.30));

            row.getChildren().addAll(paramLabel, homLabel, mujLabel, interpLabel);
            tableContainer.getChildren().add(row);
        }

        seccion.getChildren().addAll(seccionTitulo, tableContainer);
        return seccion;
    }

    private Label crearHeaderLabel(String texto, double widthRatio) {
        Label label = new Label(texto);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11.5));
        label.setTextFill(Color.WHITE);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private String[][] obtenerDatosTabla1() {
        return new String[][] {
            {
                "IMC (kg/m²)", 
                "< 18.5: Bajo peso\n18.5 – 24.9: Normal\n25.0 – 29.9: Sobrepeso\n>= 30.0: Obesidad", 
                "Igual que hombres (Criterios OMS)", 
                "Util para cribado poblacional rápido, aunque no distingue entre compartimento graso y muscular."
            },
            {
                "Peso Ideal (Lorentz)", 
                "Talla (cm) - 100 - [(Talla - 150) / 4]", 
                "Talla (cm) - 100 - [(Talla - 150) / 2]", 
                "Estima el rango de peso corporal optimo asociado con menor prevalencia de patologias metabolicas."
            },
            {
                "% Grasa Corporal (Siri)", 
                "Optimo: 10 – 20%\nModerado: 21 – 24%\nAlto: >= 25%", 
                "Optimo: 18 – 28%\nModerado: 29 – 32%\nAlto: >= 33%", 
                "Valores elevados indican riesgo de sindrome metabolico. Valores <5% (H) o <12% (M) comprometen salud hormonal."
            },
            {
                "Masa Grasa (kg)", 
                "Peso total * (% Grasa / 100)", 
                "Peso total * (% Grasa / 100)", 
                "Indica los kilogramos absolutos de tejido adiposo almacenado en el organismo."
            },
            {
                "Masa Muscular (AMB)", 
                "Deficit: < 25 cm²\nNormal: 25 – 35 cm²\nAlto: > 35 cm²", 
                "Deficit: < 20 cm²\nNormal: 20 – 30 cm²\nAlto: > 30 cm²", 
                "Valores deficitarios indican riesgo de sarcopenia y desnutricion proteica somatica."
            }
        };
    }

    private String[][] obtenerDatosTabla2() {
        return new String[][] {
            {
                "Masa Osea (Rocha)", 
                "12 – 15% del peso corporal", 
                "10 – 12% del peso corporal", 
                "Porcentajes por debajo del rango sugieren disminucion en densidad mineral osea u osteoporosis."
            },
            {
                "Masa Residual (Wurch)", 
                "~24% del peso corporal total", 
                "~21% del peso corporal total", 
                "Corresponde a la masa visceral, organos toracoabdominales y componentes no oseos ni grasos."
            },
            {
                "LBM (Masa Magra)", 
                "75 – 85% del peso corporal", 
                "65 – 75% del peso corporal", 
                "Representa el peso corporal total exento de grasa. Valores bajos reflejan desnutricion proteico-calorica."
            },
            {
                "Circunferencia de Cintura", 
                "< 90 cm: Normal\n90 – 102 cm: Riesgo moderado\n> 102 cm: Riesgo alto", 
                "< 80 cm: Normal\n80 – 88 cm: Riesgo moderado\n> 88 cm: Riesgo alto", 
                "Predictor independiente de adiposidad visceral y riesgo de enfermedad cardiovascular y diabetes tipo 2."
            },
            {
                "ICC (Cintura / Cadera)", 
                "Normal: < 0.90\nRiesgo aumentado: >= 0.90", 
                "Normal: < 0.85\nRiesgo aumentado: >= 0.85", 
                "Determina el patron de distribucion de grasa corporal (androide vs ginoide)."
            },
            {
                "ICT (Cintura / Talla)", 
                "Normal: < 0.50\nRiesgo cardiometabolico: >= 0.50", 
                "Normal: < 0.50\nRiesgo cardiometabolico: >= 0.50", 
                "Indice de gran precision aplicable transversalmente en ninos, adolescentes y adultos."
            },
            {
                "Pliegue Tricipital (mm)", 
                "Normal: 6 – 12 mm\nModerado: 13 – 20 mm\nAlto: > 20 mm", 
                "Normal: 12 – 20 mm\nModerado: 21 – 30 mm\nAlto: > 30 mm", 
                "Util para evaluar las reservas de tejido adiposo subcutaneo periférico."
            },
            {
                "Sumatoria de 7 Pliegues", 
                "Atletas: < 50 mm\nNormal: 60 – 100 mm\nAlto: > 100 mm", 
                "Atletas: < 80 mm\nNormal: 90 – 150 mm\nAlto: > 150 mm", 
                "Utilizada en las ecuaciones de Jackson & Pollock para la determinacion de la densidad corporal."
            }
        };
    }

    private VBox crearPanelNotas() {
        VBox notasCard = new VBox(12);
        notasCard.setMaxWidth(960);
        notasCard.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #C8E6C9; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 18 20;"
        );
        notasCard.setEffect(new DropShadow(10, 0, 2, Color.rgb(0, 0, 0, 0.04)));

        Label tituloNotas = new Label("CONSIDERACIONES CLINICAS IMPORTANTES");
        tituloNotas.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        tituloNotas.setTextFill(DARK_FOREST);

        String[] puntos = {
            "Los valores y puntos de corte presentados corresponden a estandares internacionales de la Organizacion Mundial de la Salud (OMS) y consenso ISAK.",
            "Las interpretaciones antropometricas deben individualizarse considerando edad, nivel de entrenamiento y contexto clinico del paciente.",
            "El Indice de Masa Corporal (IMC) es una herramienta de cribado poblacional; debe complementarse siempre con pliegues y circunferencias.",
            "Los porcentajes optimos de grasa corporal varian significativamente segun la disciplina deportiva y el objetivo del plan nutricional.",
            "La masa osea se estima mediante la formula de Rocha a partir de los diametros biestiloideo, bicondileo y maleolar."
        };

        VBox listaPuntos = new VBox(8);
        for (String p : puntos) {
            HBox item = new HBox(8);
            item.setAlignment(Pos.TOP_LEFT);

            Label bullet = new Label("•");
            bullet.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            bullet.setTextFill(PRIMARY_GREEN);

            Label texto = new Label(p);
            texto.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            texto.setTextFill(TEXT_DARK);
            texto.setWrapText(true);

            item.getChildren().addAll(bullet, texto);
            listaPuntos.getChildren().add(item);
        }

        notasCard.getChildren().addAll(tituloNotas, listaPuntos);
        return notasCard;
    }

    private HBox crearFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E8E3; -fx-border-width: 1.5 0 0 0;");
        footer.setPadding(new Insets(12, 20, 14, 20));

        Label footerLabel = new Label("NutriEnergia Pro - Modulo de Consulta y Referencia Clinica");
        footerLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        footerLabel.setTextFill(TEXT_MUTED);

        footer.getChildren().add(footerLabel);
        return footer;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
