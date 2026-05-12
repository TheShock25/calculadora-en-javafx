package calculadoraenergeticamodernaa;

import javafx.application.Application;

public class MainLauncher {
    public static void main(String[] args) {
        try {
            // Iniciar directamente sin verificaciones complejas
            Application.launch(CalculadoraEnergeticaModernaa.class, args);
        } catch (Exception e) {
            // Fallback silencioso - si hay error, no mostrar nada
            // Porque el usuario solo quiere que funcione con doble click
            System.exit(1);
        }
    }
}