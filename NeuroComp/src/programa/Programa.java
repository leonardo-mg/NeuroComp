package programa;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.UIManager;
import vistas.Controlador;
import vistas.Ventana;

public class Programa {

  public static void main(String[] args) {
    apariencia();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int ancho = screenSize.width;
    int alto = screenSize.height;
    
    Ventana ventana = new Ventana();
    Controlador controlador = new Controlador(ventana);
    ventana.addEventos(controlador);
    ventana.setSize(ancho, alto);
    ventana.setLocationRelativeTo(null);
    ventana.ajustarSeparador(400);
    ventana.setVisible(true);
  }

  public static void apariencia() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
