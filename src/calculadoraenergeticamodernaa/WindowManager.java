package calculadoraenergeticamodernaa;

import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor centralizado del ciclo de vida e instancias de ventanas en NutriEnergia Pro.
 * 
 * Reglas implementadas:
 * 1. Instancia Unica: Si una ventana secundaria (Equivalentes, IMC, Somatotipo, Menu, etc.)
 *    ya se encuentra abierta, al intentar abrirla de nuevo se trae al frente con foco en lugar
 *    de duplicarla.
 * 2. Cierre Maestro: Si se cierra la ventana principal de la calculadora (GET/GEB), todas
 *    las ventanas secundarias abiertas se cierran inmediatamente y el programa finaliza por completo.
 */
public class WindowManager {

    private static final Map<String, Stage> ventanasActivas = new HashMap<>();
    private static Stage masterStage = null;

    /**
     * Registra la ventana principal (Calculadora de Gasto Energético).
     * Al cerrarse esta ventana, se cierra toda la aplicación.
     */
    public static void setMasterStage(Stage stage) {
        masterStage = stage;
        if (masterStage != null) {
            masterStage.setOnCloseRequest(event -> cerrarTodo());
        }
    }

    /**
     * Registra una ventana secundaria bajo una clave identificadora.
     */
    public static void registrarVentana(String clave, Stage stage) {
        if (stage == null) return;
        ventanasActivas.put(clave, stage);
        stage.setOnHidden(e -> ventanasActivas.remove(clave));
    }

    /**
     * Verifica si una ventana ya está abierta. Si está abierta, la desminimiza,
     * la trae al frente y le da el foco, retornando true.
     * Si no está abierta, retorna false.
     */
    public static boolean enfocarSiAbierta(String clave) {
        Stage stage = ventanasActivas.get(clave);
        if (stage != null && stage.isShowing()) {
            if (stage.isIconified()) {
                stage.setIconified(false);
            }
            stage.toFront();
            stage.requestFocus();
            return true;
        }
        ventanasActivas.remove(clave);
        return false;
    }

    /**
     * Cierra todas las ventanas abiertas y finaliza el proceso de la aplicación.
     */
    public static void cerrarTodo() {
        for (Stage stage : new HashMap<>(ventanasActivas).values()) {
            if (stage != null && stage.isShowing()) {
                try {
                    stage.close();
                } catch (Exception ignored) {}
            }
        }
        ventanasActivas.clear();

        if (masterStage != null && masterStage.isShowing()) {
            try {
                masterStage.close();
            } catch (Exception ignored) {}
        }

        Platform.exit();
        System.exit(0);
    }
}
