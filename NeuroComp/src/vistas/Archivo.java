package vistas;

import java.io.*;
import java.util.*;

public class Archivo {

  public static ArrayList<String> leerArchivo(InputStream flujo) {
    ArrayList<String> lineas = new ArrayList();
    try {
      InputStreamReader otro = new InputStreamReader(flujo);
      BufferedReader buffer = new BufferedReader(otro);
      String linea = buffer.readLine();
      while (linea != null) {
        lineas.add(linea);
        linea = buffer.readLine();
      }
      buffer.close();
      
    } catch (IOException ex) {
      System.out.println("Error de archivo" + ex);
      System.exit(-1);
    }
    return lineas;
  }
  
  public static ArrayList<String> leerArchivo(String archivo) {
    return leerArchivo(new File(archivo));
  }  

  public static ArrayList<String> leerArchivo(File archivo) {
    try {
      return leerArchivo(new FileInputStream(archivo));
    } catch (FileNotFoundException ex) {
      System.out.println("Error de archivo" + ex);
      System.exit(-1);
    }
    return null;
  }

  public static void grabarArchivo(String archivo, ArrayList<String> lineas) {
    try {
      FileWriter flujo = new FileWriter(archivo);
      BufferedWriter buffer = new BufferedWriter(flujo);
      for (String linea : lineas) {
        buffer.write(linea);
        buffer.newLine();
      }
      buffer.close();
      flujo.close();
    } catch (IOException error) {
      System.out.println("Error de archivo" + error);
      System.exit(-1);
    }
  }

}
