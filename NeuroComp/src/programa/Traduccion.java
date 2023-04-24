package programa;

import compilador.CompiladorConstants;
import compilador.CompiladorTokenManager;
import compilador.Token;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import vistas.Archivo;

public class Traduccion implements CompiladorConstants {

  public ArrayList<Token> tablaTokens;
  public HashMap<String, String> operaciones;
  public HashMap<String, Integer> variableValor;
  public String codigoIntermedio;
  private Token token;
  private Token aux;
  private Conversion con;

  public Traduccion() {
    tablaTokens = CompiladorTokenManager.tablaTokens;
    operaciones = new HashMap();
    variableValor = new HashMap();
    codigoIntermedio = "";
    token = tablaTokens.get(0);
    con = new Conversion();
  }

  public void generarTraduccion() {
    while (token != null) {
      switch (token.kind) {
        case ENTERO:
        case DECIMAL:
          funcionDeclaracion();
          break;
        case IDENTIFICADOR:
          funcionAsignacion();
          break;
        case IMPRIMIR:
          funcionImpresion();
          break;
        case LEER:
          funcionLeer();
          break;
        case SI:
          funcionSI();
          break;
        case SINO:
          funcionSINO();
          break;
        case SELECTOR:
          funcionSelector();
          break;
        case SELECCION:
          funcionSeleccion();
          break;
        case AUTOMATICO:
          funcionAutomatico();
          break;
        case ROMPER:
          funcionRomper();
          break;
        case MIENTRAS:
          funcionMientras();
          break;
        case PARA:
          funcionPara();
          break;
      }
      token = token.next;
    }
    //System.out.println(codigoIntermedio);
    con.analizarCodigo();
  }

  public void funcionDeclaracion() {
    String funcion = "DECLARACION";
    String valores = token.image + "~" + token.next.image;
    codigoIntermedio += funcion + "~" + valores + "\n";
  }

  public void funcionAsignacion() {
    String funcion = "ASIGNACION";
    String valores = token.image + "=";
    while (token.next.kind != PUNTOCOMA) {
      token = token.next;
      if (token.kind == IDENTIFICADOR || token.kind == ENTEROS || token.kind == DECIMALES || token.kind == MAS || token.kind == MENOS || token.kind == MULTIPLICACION || token.kind == DIVISION || token.kind == MODULO) {
        valores += token.image;
      }
    }
    codigoIntermedio += funcion + "~" + valores + "\n";
    //System.out.println(funcion + " : " + valores);
  }

  public void funcionImpresion() {
    String funcion = "IMPRESION";
    String valores = "";
    while (token.next.kind != PUNTOCOMA) {
      token = token.next;
      if (token.kind == CADENA || token.kind == IDENTIFICADOR) {
        if (token.next.kind == PUNTOCOMA) {
          valores += token.image;
        } else {
          valores += token.image + "°";
        }

      }
    }
    codigoIntermedio += funcion + "~" + valores + "\n";
    //System.out.println(funcion + " : " + valores);
  }

  public void funcionLeer() {
    String funcion = "LECTURA";
    token = token.next.next;
    String valores = token.image;
    codigoIntermedio += funcion + "~" + valores + "\n";
    //System.out.println(funcion + " : " + valores);
  }

  public void funcionSI() {
    String funcion = "CONDICIONAL_SI";
    String valores = "CONDICION:";
    while (token.next.kind != LLAVEA) {
      token = token.next;
      if (token.kind == IDENTIFICADOR || token.kind == ENTEROS || token.kind == DECIMALES || token.kind == MENORQUE
        || token.kind == MAYORQUE || token.kind == MENORIGUAL || token.kind == MAYORIGUAL || token.kind == DIFERENTEDE
        || token.kind == IGUALQUE || token.kind == O || token.kind == Y) {
        valores += token.image;
      }
    }
    token = token.next.next;
    codigoIntermedio += funcion + "~" + valores + "\nEJECUTA\n";
    //System.out.println(funcion + " : " + valores + "\nEJECUTA:");
    busquedaRecursiva();
    codigoIntermedio += "FIN_SI\n";
    //System.out.println("FIN_SI");
  }

  public void funcionSINO() {
    String funcion = "CONDICIONAL_SINO";
    token = token.next.next;
    codigoIntermedio += funcion + "\nEJECUTA:\n";
    //System.out.println(funcion + "\nEJECUTA:");
    busquedaRecursiva();
    codigoIntermedio += "FIN_SINO\n";
    //System.out.println("FIN_SINO");
  }

  public void funcionSelector() {
    String funcion = "CONDICIONAL_SELECTOR";
    token = token.next.next;
    aux = token;
    String valores = "" + token.image;
    token = token.next.next.next;
    codigoIntermedio += funcion + "~" + valores + "\n";
    //System.out.println(funcion + " : " + valores);
    busquedaRecursiva();
    codigoIntermedio += "FIN_SELECTOR\n";
    //System.out.println("FIN_SELECTOR");
  }

  public void funcionSeleccion() {
    String funcion = "SELECCION";
    token = token.next;
    String valores = aux.image + "=" + token.image;
    codigoIntermedio += funcion + "~" + valores + "\nEJECUTA\n";
    //System.out.println(funcion + " : " + valores + "\nEJECUTA:");
  }

  public void funcionAutomatico() {
    String funcion = "AUTOMATICO";
    codigoIntermedio += funcion + "\nEJECUTA\n";
    //System.out.println(funcion + "\nEJECUTA: ");
  }

  public void funcionRomper() {
    String funcion = "DETENER";
    codigoIntermedio += funcion + "\n";
  }

  public void funcionMientras() {
    String funcion = "MIENTRAS";
    String valores = "CONDICION:";
    while (token.next.kind != LLAVEA) {
      token = token.next;
      if (token.kind == IDENTIFICADOR || token.kind == ENTEROS || token.kind == DECIMALES || token.kind == MENORQUE
        || token.kind == MAYORQUE || token.kind == MENORIGUAL || token.kind == MAYORIGUAL || token.kind == DIFERENTEDE
        || token.kind == IGUALQUE || token.kind == O || token.kind == Y) {
        valores += token.image;
      }
    }
    token = token.next.next;
    codigoIntermedio += funcion + "~" + valores + "\nREPITE:\n";
    //System.out.println(funcion + " : " + valores + "\nREPITE:");
    busquedaRecursiva();
    codigoIntermedio += "FIN_MIENTRAS\n";
    //System.out.println("FIN_MIENTRAS");
  }

  public void funcionPara() {
    String funcion = "PARA";
    String valores = "CONDICION:";
    while (token.next.kind != LLAVEA) {
      token = token.next;
      if (token.kind == IDENTIFICADOR || token.kind == ENTEROS || token.kind == DECIMALES || token.kind == MENORQUE
        || token.kind == MAYORQUE || token.kind == MENORIGUAL || token.kind == MAYORIGUAL || token.kind == DIFERENTEDE
        || token.kind == MAS || token.kind == MENOS || token.kind == MULTIPLICACION || token.kind == DIVISION
        || token.kind == IGUALQUE || token.kind == IGUAL || token.kind == ENTERO || token.kind == DECIMAL
        || token.kind == PUNTOCOMA) {
        valores += token.image;
      }
    }
    token = token.next.next;
    codigoIntermedio += funcion + "~" + valores + "\nREPITE\n";
    //System.out.println(funcion + " : " + valores + "\nREPITE:");
    busquedaRecursiva();
    codigoIntermedio += "FIN_PARA\n";
    //System.out.println("FIN_PARA");
  }

  public void busquedaRecursiva() {
    int cont = 1;
    while (cont != 0) {
      switch (token.kind) {
        case LLAVEA:
          cont++;
          break;
        case LLAVEC:
          cont--;
          break;
        case ENTERO:
        case DECIMAL:
          funcionDeclaracion();
        case IDENTIFICADOR:
          funcionAsignacion();
          break;
        case IMPRIMIR:
          funcionImpresion();
          break;
        case LEER:
          funcionLeer();
          break;
        case SI:
          funcionSI();
          break;
        case SINO:
          funcionSINO();
          break;
        case SELECTOR:
          funcionSelector();
          break;
        case SELECCION:
          funcionSeleccion();
          break;
        case AUTOMATICO:
          funcionAutomatico();
          break;
        case ROMPER:
          funcionRomper();
          break;
        case MIENTRAS:
          funcionMientras();
          break;
        case PARA:
          funcionPara();
          break;
      }
      if (cont != 0) {
        token = token.next;
      }
    }
  }

  public void ejecutar() {
    String ruta = this.getClass().getResource("").getPath().replace("programa/", "");
    lanzar(ruta);
  }

  public void compilar() {
    String ruta = this.getClass().getResource("").getPath().replace("build/classes/programa/", "executable/programa.java");
    //System.out.println("---->" + ruta);
    String osName = System.getProperty("os.name");
    if(!osName.equals("Linux")){
      ruta = ruta.replaceFirst("/", "");
    }
    try {
      List<String> arguments = new ArrayList<>();
      arguments.add("javac");
      arguments.add("-cp");
      arguments.add("./");
      arguments.add(ruta);

      // Crear el proceso
      ProcessBuilder processBuilder = new ProcessBuilder(arguments);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      // Leer la salida del proceso y mostrarla en la consola
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = process.getInputStream().read(buffer)) != -1) {
        System.out.write(buffer, 0, bytesRead);
      }

      // Esperar a que el proceso termine
      try {
        int exitCode = process.waitFor();
        if (exitCode == 0) {
          //System.out.println("Compilación exitosa.");
        } else {
          //System.err.println("La compilación ha fallado.");
        }
      } catch (InterruptedException e) {
        System.err.println("El proceso ha sido interrumpido.");
        Thread.currentThread().interrupt();
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  public void lanzar(String ruta) {
    crearEjecutable();
    String osName = System.getProperty("os.name");
    //System.out.println("El sistema operativo actual es: " + osName);
    //System.out.println(ruta);
    if (osName.equals("Linux")) {
      try {
        ruta = ruta.replace("build/classes/", "executable/ejecutable.sh");
        //System.out.println(ruta);
        String[] cmd = {"x-terminal-emulator", "-e", "bash -c " + ruta + "; exec bash"};
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        long pid = p.pid();
        Runtime.getRuntime().exec("kill -9 " + pid);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    } else {
      try {
        ruta = ruta.replace("build/classes/", "executable/ejecutable.bat");
//        System.out.println(ruta);
        ruta = ruta.replaceFirst("/", "");
        String[] cmd = {"cmd", "/c", "start", ruta};
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        p.destroy();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void crearEjecutable(){
    String ruta = getClass().getResource("").getPath().replace("build/classes/programa/", "executable/");
    ruta = ruta.replaceFirst("/", "");
    String osName = System.getProperty("os.name");
    if(osName.equals("Linux")){
      ruta += "ejecutable.sh";
    }else{
      String comando = "java -cp " + ruta + " programa";
      ruta += "ejecutable.bat";
      ArrayList<String> lineas = new ArrayList();
      lineas.add(comando);
      Archivo.grabarArchivo(ruta, lineas);
    }
   
  }
  class Conversion {

    String codigo;

    public Conversion() {
      codigo = """
               import java.util.Scanner;
               public class programa{
               public static void main(String[] args){
               try{
               Scanner scan = new Scanner(System.in);
               """;
    }

    public void analizarCodigo() {
      String[] lineas = codigoIntermedio.split("\n");
      for (int i = 0; i < lineas.length; i++) {
        String[] linea = lineas[i].split("~");
        revisarEstructura(linea);
      }
      codigo += """
                } catch(Throwable e){
                System.out.println("Se ha generado un error en tiempo de ejecucion");
                System.out.println("Causa: Posiblemente estas intentando introducir un valor invalido");
                }
                }
                }
                """;
      guardarArchivo();
    }

    public void guardarArchivo() {
      try {
        String ruta = getClass().getResource("").getPath().replace("build/classes/programa", "executable");
        //System.out.println(ruta);
        File archivo = new File(ruta + "/programa.java");
        archivo.createNewFile();
        ArrayList<String> lineas = new ArrayList();
        String[] l = codigo.split("\n");
        for (String ls : l) {
          lineas.add(ls);
          //System.out.println(ls);
        }
        Archivo.grabarArchivo(archivo.getPath(), lineas);
        //System.out.println("Grabado");
      } catch (IOException ex) {
        System.out.println("Error al procesar el archivo");
      }
    }

    public void revisarEstructura(String[] e) {
      switch (e[0]) {
        case "DECLARACION": {
          if ("entero".equals(e[1])) {
            codigo += "int " + e[2] + ";\n";
          } else {
            codigo += "float " + e[2] + ";\n";
          }
          break;
        }
        case "ASIGNACION": {
          if (e[1].split("=").length > 1) {
            codigo += e[1] + ";\n";
          }
          break;
        }
        case "IMPRESION": {
          String[] argumentos = e[1].split("°");
          codigo += "System.out.print(";
          for (int i = 0; i < argumentos.length; i++) {
            if (i == argumentos.length - 1) {
              codigo += argumentos[i];
            } else {
              codigo += argumentos[i] + " + ";
            }
          }
          codigo += ");\n";
          break;
        }
        case "LECTURA": {
          codigo += e[1] + " = scan.nextInt();\n";
          break;
        }
        case "CONDICIONAL_SI": {
          codigo += "if(";
          String[] condicion = e[1].split(":");
          codigo += condicion[1] + "){\n";
          break;
        }
        case "FIN_SI": {
          codigo += "}\n";
          break;
        }
        case "CONDICIONAL_SINO": {
          codigo += "else{\n";
          break;
        }
        case "FIN_SINO": {
          codigo += "}\n";
          break;
        }
        case "CONDICIONAL_SELECTOR": {
          codigo += "switch(" + e[1] + "){\n";
          break;
        }
        case "SELECCION": {
          codigo += "case ";
          String[] valor = e[1].split("=");
          codigo += valor[1] + ":\n";
          break;
        }
        case "AUTOMATICO": {
          codigo += "default:\n";
          break;
        }
        case "FIN_SELECTOR": {
          codigo += "}\n";
          break;
        }
        case "DETENER": {
          codigo += "break;\n";
          break;
        }
        case "MIENTRAS": {
          codigo += "while(";
          String[] condicion = e[1].split(":");
          codigo += condicion[1] + "){\n";
          break;
        }
        case "FIN_MIENTRAS": {
          codigo += "}\n";
          break;
        }
        case "PARA": {
          codigo += "for(";
          String[] condicion = e[1].split(":");
          if (condicion[1].contains("entero")) {
            codigo += condicion[1].replace("entero", "int ") + "){\n";
          } else {
            codigo += condicion[1].replace("decimal", "float ") + "){\n";
          }
          break;
        }
        case "FIN_PARA": {
          codigo += "}\n";
          break;
        }
      }
    }
  }
}
