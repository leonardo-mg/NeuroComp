package programa;

import compilador.CompiladorConstants;
import compilador.CompiladorTokenManager;
import compilador.Token;
import java.util.ArrayList;
import java.util.HashMap;

public class AnalizadorSemantico implements CompiladorConstants {

  public static ArrayList<Token> tablaTokens;
  public ArrayList<Token> variables;
  public HashMap<String, Integer> declaradas;
  public HashMap<String, Referencia> repetidas;
  public ArrayList<String> mensajesError;
  public int totalErrores;

  public AnalizadorSemantico() {
    tablaTokens = CompiladorTokenManager.tablaTokens;
    variables = new ArrayList();
    declaradas = new HashMap();
    repetidas = new HashMap();
    mensajesError = new ArrayList();
  }

  private void obtenerValores() {
    tablaTokens = CompiladorTokenManager.tablaTokens;
    variables.clear();
    declaradas.clear();
    repetidas.clear();
    mensajesError.clear();
    totalErrores = 0;
    for (Token t : tablaTokens) {
      if (t.kind == IDENTIFICADOR) {
        variables.add(t);
      }
      if (t.kind == ENTERO || t.kind == DECIMAL) {
        declaradas.put(t.next.image, t.kind);
        this.addRepetidas(t.next);
      }
    }
  }

  public void addRepetidas(Token k) {
    if (repetidas.containsKey(k.image)) {
      Referencia r = repetidas.get(k.image);
      r.cantidad++;
      r.t = k;
      repetidas.replace(k.image, r);
    } else {
      repetidas.put(k.image, new Referencia(1,k));
    }
  }

  public void realizarAnalisis() throws SemanticException {
    obtenerValores();
    if (comprobacionDeUnicidad() > 0) {
      throw new SemanticException("");
    }
    if (comprobacionDeDeclaracion() > 0) {
      throw new SemanticException("");
    }
    if (comprobacionDeTipos() > 0) {
      throw new SemanticException("");
    }
  }

  private int comprobacionDeUnicidad() {
    int errores = 0;
    for (String clave : repetidas.keySet()) {
      int valor = repetidas.get(clave).cantidad;
      if (valor > 1) {
        Token error = repetidas.get(clave).t;
        mensajesError.add(mensajeError("unicidad", error));
        errores++;
      }
    }
    totalErrores += errores;
    return errores;
  }

  private int comprobacionDeDeclaracion() {
    int errores = 0;
    for (Token t : variables) {
      if (!declaradas.containsKey(t.image)) {
        mensajesError.add(mensajeError("declaracion", t));
        errores++;
      }
    }
    totalErrores += errores;
    return errores;
  }

  private int comprobacionDeTipos() {
    int errores = 0;
    for (Token t : variables) {
      if (t.next.kind == IGUAL) {
        int tipo = declaradas.get(t.image);
        Token otro = t.next.next;
        if (tipo == ENTERO && otro != null) {
          while (otro.kind != PUNTOCOMA) {
            if (otro.kind == IDENTIFICADOR) {
              if (declaradas.get(otro.image) != ENTERO) {
                mensajesError.add(mensajeError("tipos", otro));
                errores++;
              }
            } else if (otro.kind == DECIMALES) {
              mensajesError.add(mensajeError("tipos", otro));
              errores++;
            }
            otro = otro.next;
            if(otro == null){
              break;
            }
          }
        }
      }
    }
    totalErrores += errores;
    return errores;
  }
  
  public String mensajeError(String causa, Token error){
    if(causa.equals("unicidad")){
      return "Se ha encontrado un error en la linea: " + error.beginLine + ", columna: " + error.beginColumn + ".\nLa variable"
        + " \"" + error.image + "\" ya ha sido declarada.";
    } else if(causa.equals("declaracion")){
      return "Se ha encontrado un error en la linea: " + error.beginLine + ", columna: " + error.beginColumn + ".\nLa variable"
        + " \"" + error.image + "\" no ha sido declarada.";
    } else{
      return "Se ha encontrado un error en la linea: " + error.beginLine + ", columna: " + error.beginColumn + ".\nTipos de datos"
        + " incompatibles. La variable o valor: \"" + error.image + "\" no es de tipo entero.";
    }
  }

  
  class Referencia {
    int cantidad;
    Token t;
    
    public Referencia(int cantidad, Token t){
      this.cantidad = cantidad;
      this.t = t;
    }
  }
}
