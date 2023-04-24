package vistas;

import compilador.ParseException;
import compilador.TokenMgrError;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import programa.AnalizadorSemantico;
import programa.Compilador;
import programa.SemanticException;
import programa.Traduccion;

/**
 *
 * @author Leonardo Montero
 */
public class Controlador implements ActionListener, CaretListener {

  private final Ventana ventana;
  private final Compilador comp;
  private final AnalizadorSemantico semantico;
  private File archivo;
  private Traduccion traduccion;

  public Controlador(Ventana ventana) {
    this.ventana = ventana;
    comp = new Compilador(new ByteArrayInputStream("".getBytes()));
    semantico = new AnalizadorSemantico();
    archivo = null;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JButton origen = (JButton) e.getSource();
    switch (origen.getName()) {
      case "abrir" -> {
        abrirArchivo();
      }
      case "analisis" -> {
        if (archivo == null) {
          guardarArchivoComo();
        }
        if (archivo != null) {
          try {
            guardarArchivo();
            InputStream in = new FileInputStream(archivo);
            ventana.getAreaTerminal().setText("");
            analisis(in);
          } catch (FileNotFoundException ex) {
            System.out.println("error de lectura");
          }
        }
      }
      case "guardar" -> {
        if (archivo == null) {
          guardarArchivoComo();
        }
        guardarArchivo();
      }
      case "ejecutar" -> {
        traduccion.ejecutar();
      }
      case "salir" -> {
        System.exit(0);
      }
    }
  }

  public void analisis(InputStream in) {
    if (!ventana.getAreaCodigo().getText().isEmpty()) {
      try {
        ventana.getBotonEjecutar().setEnabled(false);
        
        comp.ReInit(new ByteArrayInputStream(ventana.getAreaCodigo().getText().getBytes()));
        //Comienza el proceso de compilación
        ventana.getAreaTerminal().append("********Iniciando analisis********\n\n");
        //El primer proceso realizado es el analisis lexico
        ventana.getAreaTerminal().append("Analisis Lexico: \n");
        comp.analisisLexico();
        ventana.getAreaTerminal().append("Analisis Lexico exitoso\nErrores encontrados: 0\n\n");
        //El segundo proceso realizado es el analisis sintactico
        ventana.getAreaTerminal().append("Analisis Sintactico: \n");
        comp.analisisSintactico();
        ventana.getAreaTerminal().append("Analisis Sintactico exitoso\nErrores encontrados: 0\n\n");
        //El tercer proceso realizado es el analisis semantico
        ventana.getAreaTerminal().append("Analisis Semantico: \n");
        semantico.realizarAnalisis();
        ventana.getAreaTerminal().append("Analisis Semantico exitoso\nErrores encontrados: 0\n\n");
        traduccion = new Traduccion();
        traduccion.generarTraduccion();
        traduccion.compilar();
        ventana.getBotonEjecutar().setEnabled(true);
      } catch (TokenMgrError ex) {
        ventana.getAreaTerminal().append("Errores encontrados: " + comp.erroresLexicos + "\n\n");
        for(String er : comp.mensajesError){
          ventana.getAreaTerminal().append(er + "\n\n");
        }
        ventana.getAreaTerminal().append("Analisis Fallido");
      } catch (ParseException ex){
        ventana.getAreaTerminal().append("Errores encontrados: " + comp.erroresSintacticos + "\n\n");
        for(String er : comp.mensajesError){
          ventana.getAreaTerminal().append(er + "\n\n");
        }
        ventana.getAreaTerminal().append("Analisis Fallido");
      } catch (SemanticException ex){
        ventana.getAreaTerminal().append("Errores encontrados: " + semantico.totalErrores + "\n\n");
        for(String er : semantico.mensajesError){
          ventana.getAreaTerminal().append(er + "\n\n");
        }
        ventana.getAreaTerminal().append("Analisis Fallido");
      }
    }
  }

  public void abrirArchivo() {
    String ruta = getClass().getResource("/pruebas").getPath();
    ruta = ruta.replace("build/classes/pruebas", "src/pruebas");
    JFileChooser seleccion = new JFileChooser(ruta);
    int opcion = seleccion.showOpenDialog(ventana);
    if (opcion == JFileChooser.APPROVE_OPTION) {
      archivo = seleccion.getSelectedFile();
      ventana.getEtiquetaArchivo().setText(archivo.getName());
      ArrayList<String> lineas = Archivo.leerArchivo(archivo);
      ventana.getAreaCodigo().setText("");
      for (String l : lineas) {
        ventana.getAreaCodigo().append( l + "\n");
      }
    }
  }

  public void guardarArchivoComo() {
    String ruta = getClass().getResource("/pruebas").getPath();
    ruta = ruta.replace("build/classes/pruebas", "src/pruebas");
    JFileChooser seleccion = new JFileChooser(ruta);
    int opcion = seleccion.showSaveDialog(ventana);
    if (opcion == JFileChooser.APPROVE_OPTION) {
      File otro = seleccion.getSelectedFile();
      System.out.println(otro.getName());
      String nombre = otro.getName();
      if (!nombre.contains(".txt")) {
        nombre += ".txt";
      }
      File nuevo = new File(otro.getParent() + "/" + nombre);
      archivo = nuevo;
      try {
        System.out.println(archivo.createNewFile());
      } catch (IOException ex) {
        System.out.println("Error de lectura");
      }
      System.out.println(archivo.getPath());
      ventana.getEtiquetaArchivo().setText(archivo.getName());
    }
  }

  public void guardarArchivo() {
    ArrayList<String> lineas = new ArrayList();
    String[] codigo = ventana.getAreaCodigo().getText().split("\n");
    for (String l : codigo) {
      lineas.add(l);
    }
    Archivo.grabarArchivo(archivo.getPath(), lineas);
  }

  @Override
  public void caretUpdate(CaretEvent ce) {
    ventana.posicionCursor();
  }
}
