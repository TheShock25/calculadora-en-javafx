package calculadoraenergeticamodernaa;


public class JavaFXEmbeddedLauncher {
    public static void main(String[] args) {
        // Este main NO extiende Application
        try {
            // Forzar carga de módulos JavaFX
            System.setProperty("javafx.embed.singleThread", "true");
            
            // Lanzar la aplicación JavaFX
            CalculadoraEnergeticaModernaa.launchDirectly();
            
        } catch (Exception e) {
            // Fallback absoluto - intentar de todas formas
            try {
                CalculadoraEnergeticaModernaa.main(args);
            } catch (Exception e2) {
                // Silencio total - el JAR debe funcionar o fallar silenciosamente
            }
        }
    }
}