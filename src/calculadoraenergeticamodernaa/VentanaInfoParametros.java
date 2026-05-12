package calculadoraenergeticamodernaa;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Migración simplificada de VentanaInfoParametros a JavaFX.
 * Muestra un diálogo/ventana con información de parámetros y ayuda.
 */
public class VentanaInfoParametros extends Application {

    private static final Color PRIMARY_COLOR = Color.web("#2980b9");
    private static final Color BACKGROUND_COLOR = Color.web("#f5f6fa");
    private static final String FONT_FAMILY = "Segoe UI";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Información de parámetros - JavaFX (migrado)");

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, null, null)));

        Label title = new Label("Información de parámetros");
        title.setFont(Font.font(FONT_FAMILY, 18));

        HBox header = new HBox(title);
        header.setPadding(new Insets(12));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        root.setTop(header);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);

        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        // Ejemplo de tarjetas descriptivas para cada parámetro
        content.getChildren().add(createCard("Edad", "Edad del usuario en años. Se usa para calcular TMB y requerimientos."));
        content.getChildren().add(createCard("Sexo", "Masculino/Femenino - influye en las fórmulas metabólicas."));
        content.getChildren().add(createCard("Peso", "Peso en kilogramos."));
        content.getChildren().add(createCard("Altura", "Altura en centímetros."));
        content.getChildren().add(createCard("Actividad", "Nivel de actividad física (sedentario, moderado, activo)."));

        scroll.setContent(content);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 600, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Region createCard(String title, String description) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.03), 6, 0, 0, 1);");

        Label h = new Label(title);
        h.setFont(Font.font(FONT_FAMILY, 14));
        Label desc = new Label(description);
        desc.setWrapText(true);
        desc.setFont(Font.font(FONT_FAMILY, 12));
        desc.setStyle("-fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(h, desc);
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
