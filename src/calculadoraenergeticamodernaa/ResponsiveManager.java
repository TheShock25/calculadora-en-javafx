package calculadoraenergeticamodernaa;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * Gestor de diseño responsive para JavaFX
 */
public class ResponsiveManager {
    private static boolean isTabletMode = false;
    private static Rectangle2D screenBounds;
    
    static {
        detectarDispositivo();
    }
    
    private static void detectarDispositivo() {
        screenBounds = Screen.getPrimary().getVisualBounds();
        isTabletMode = screenBounds.getWidth() <= 1200;
        System.out.println("Responsive Manager FX - Modo Tablet: " + isTabletMode + 
                          ", Pantalla: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());
    }
    
    public static boolean isTabletMode() {
        return isTabletMode;
    }
    
    public static Rectangle2D getScreenBounds() {
        return screenBounds;
    }
    
    // Tamaños de fuente adaptables
    public static double getFontSize(double desktopSize, double tabletSize) {
        return isTabletMode ? tabletSize : desktopSize;
    }
    
    // Dimensiones adaptables
    public static double getWidth(double desktopWidth, double tabletWidth) {
        return isTabletMode ? tabletWidth : desktopWidth;
    }
    
    public static double getHeight(double desktopHeight, double tabletHeight) {
        return isTabletMode ? tabletHeight : desktopHeight;
    }
    
    // Márgenes adaptables
    public static double getMargin(double desktopMargin, double tabletMargin) {
        return isTabletMode ? tabletMargin : desktopMargin;
    }
    
    // Espaciado adaptable
    public static double getSpacing(double desktopSpacing, double tabletSpacing) {
        return isTabletMode ? tabletSpacing : desktopSpacing;
    }
}
