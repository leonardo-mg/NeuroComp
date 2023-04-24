package programa;

import compilador.CompiladorConstants;
import compilador.CompiladorTokenManager;
import compilador.ParseException;
import compilador.SimpleCharStream;
import compilador.Token;
import compilador.TokenMgrError;
import java.io.InputStream;
import java.util.ArrayList;

public class Compilador implements CompiladorConstants {

  public SimpleCharStream in;
  public CompiladorTokenManager manager;
  public ArrayList<Token> tabla;
  public Token token;
  public Token oldToken;
  public int erroresLexicos;
  public int erroresSintacticos;
  public ArrayList<String> mensajesError;

  public Compilador(InputStream stream) {
    in = new SimpleCharStream(stream);
    manager = new CompiladorTokenManager(in);
    token = new Token();
    oldToken = new Token();
    erroresLexicos = 0;
    erroresSintacticos = 0;
    mensajesError = new ArrayList();
  }

  public void ReInit(InputStream stream) {
    in.ReInit(stream);
    manager.ReInit(in);
    token = new Token();
    oldToken = new Token();
    erroresLexicos = 0;
    erroresSintacticos = 0;
    mensajesError = new ArrayList();
  }

  public void analisisLexico() throws TokenMgrError {
    CompiladorTokenManager.tablaTokens.clear();
    token = manager.getNextToken();
    if (token.kind == ERROR) {
      erroresLexicos++;
      errorLexico(token);
    }
    while (token.kind != EOF) {
      token.next = manager.getNextToken();
      token = token.next;
      if (token.kind == ERROR) {
        erroresLexicos++;
        errorLexico(token);
      }
    }
    tabla = CompiladorTokenManager.tablaTokens;
    token = tabla.get(0);
    if (erroresLexicos > 0) {
      throw new TokenMgrError();
    }
  }

  public void analisisSintactico() throws ParseException {
    metodoPrincipal();
    if (erroresSintacticos > 0) {
      throw new ParseException();
    }
  }

  public void errorLexico(Token error) {
    String mensaje = "Se ha encontrado un error en la linea: " + error.beginLine + ", columna: "
      + error.beginColumn + "\nSe encontro: " + error.image;
    mensajesError.add(mensaje);
  }

  public void errorSintactico(Token error, String[] seEsperaba) {
    String mensaje = "Se ha encontrado un error en la linea: " + error.beginLine + ", columna: "
      + error.beginColumn + "\nSe encontro: " + error.image
      + "\nSe esperaba:\n";
    for (String e : seEsperaba) {
      mensaje += "------>" + e + "\n";
    }
    mensajesError.add(mensaje);
  }

  public void codigo() {
    principal:
    while (token != null) {
      switch (token.kind) {
        case ENTERO, DECIMAL -> {
          declaracionVariable();
        }
        case IDENTIFICADOR -> {
          asignacionVariable();
        }
        case IMPRIMIR -> {
          sentenciaImprimir();
        }
        case LEER -> {
          sentenciaLeer();
        }
        case SI -> {
          sentenciaSi();
        }
        case SINO -> {
          sentenciaSiNo();
        }
        case SELECTOR -> {
          sentenciaSelector();
        }
        case ROMPER -> {
          oldToken = token;
          token = token.next;
          if (token.kind != PUNTOCOMA) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{";"});
          }
        }
        case MIENTRAS -> {
          sentenciaMientras();
        }
        case PARA -> {
          sentenciaPara();
        }
        case LLAVEC -> {
          break principal;
        }
        case EOF -> {
          break principal;
        }
        default -> {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"entero", "decimal", "identificador", "imprimir", "leer", "si", "sino", "selector", "mientras", "para"});
          control();
        }

      }
      token = token.next;
    }
  }

  public void control() {
    while (token != null && token.kind != PUNTOCOMA && token.kind != LLAVEC && token.kind != EOF) {
      oldToken = token;
      token = token.next;
    }
    if (token.next == null || token.next.kind == EOF) {
      token = oldToken;
    }
  }

  public boolean estructuras() {
    return token.kind != ENTERO && token.kind != DECIMAL && token.kind != IDENTIFICADOR
      && token.kind != IMPRIMIR && token.kind != LEER && token.kind != SI && token.kind != SINO
      && token.kind != SELECTOR && token.kind != SELECCION && token.kind != MIENTRAS && token.kind != PARA;
  }

  public boolean tiposValores() {
    return token.kind != ENTEROS && token.kind != DECIMALES;
  }

  public boolean operadoresAritmeticos() {
    return token.kind != MAS && token.kind != MENOS && token.kind != DIVISION && token.kind != MULTIPLICACION && token.kind != MODULO;
  }

  public boolean operadoresRelacionales() {
    return token.kind != MENORQUE && token.kind != MENORIGUAL && token.kind != MAYORQUE && token.kind != MAYORIGUAL && token.kind != DIFERENTEDE && token.kind != IGUALQUE;
  }

  public void metodoPrincipal() {
    boolean band = true;
    if (token.kind != PRINCIPAL) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"principal"});
      control();
      band = false;
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != LLAVEA) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"{"});
        control();
      }
    }
    oldToken = token;
    token = token.next;
    codigo();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
    }
  }

  public void declaracionVariable() {
    oldToken = token;
    token = token.next;
    if (token.kind != IDENTIFICADOR) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"IDENTIFICADOR"});
      control();
      return;
    }
    oldToken = token;
    token = token.next;
    if (token.kind != IGUAL && token.kind != PUNTOCOMA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"=", ";"});
      control();
      return;
    }
    if (token.kind == IGUAL) {
      operacionAritmetica();
    }
    if (token.kind != PUNTOCOMA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{";"});
      control();
    }
  }

  public void asignacionVariable() {
    oldToken = token;
    token = token.next;
    if (token.kind != IGUAL) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"="});
      control();
      return;
    }
    operacionAritmetica();
  }

  public boolean operacionAritmetica() {
    boolean correcto = true;
    boolean band = true;
    oldToken = token;
    token = token.next;
    while (token.kind != PUNTOCOMA) {
      if (band) {
        if (tiposValores() && token.kind != IDENTIFICADOR) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"ENTEROS","DECIMALES","IDENTIFICADOR"});
          control();
          correcto = false;
        }
      } else {
        if (operadoresAritmeticos()) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"+", "-", "*", "/", "%", ";"});
          control();
          correcto = false;
        }
      }
      band = !band;
      if (!correcto) {
        break;
      }
      oldToken = token;
      token = token.next;
    }
    return correcto;
  }

  public void sentenciaImprimir() {
    oldToken = token;
    token = token.next;
    if (token.kind != DOSPUNTOS) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{":"});
      control();
      return;
    }
    boolean band = true;
    oldToken = token;
    token = token.next;
    while (token.kind != PUNTOCOMA) {
      if (band) {
        if (token.kind != CADENA && token.kind != IDENTIFICADOR) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"CADENA", "IDENTIFICADOR"});
          control();
          return;
        }
      } else {
        if (token.kind != CONCATENACION) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"<<"});
          control();
          return;
        }
      }
      band = !band;
      oldToken = token;
      token = token.next;
    }
    if (token.kind != PUNTOCOMA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{";"});
      control();
    }
  }

  public void sentenciaLeer() {
    oldToken = token;
    token = token.next;
    if (token.kind != DOSPUNTOS) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{":"});
      control();
      return;
    }
    oldToken = token;
    token = token.next;
    if (token.kind != IDENTIFICADOR) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"IDENTIFICADOR"});
      control();
      return;
    }
    oldToken = token;
    token = token.next;
    if (token.kind != PUNTOCOMA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{";"});
      control();
    }
  }

  public void sentenciaSi() {
    boolean band = true;
    oldToken = token;
    token = token.next;
    if (token.kind != PARENTESISA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"("});
      control();
      band = false;
    }
    if (band) {
      band = operacionCondicional();
    }
    if (band) {
      if (token.kind != PARENTESISC) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{")"});
        control();
        band = false;
      }
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != LLAVEA) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"{"});
        control();
        band = false;
      }
    }
    oldToken = token;
    token = token.next;
    codigo();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
      control();
    }
  }

  public void sentenciaSiNo() {
    oldToken = token;
    token = token.next;
    if (token.kind != LLAVEA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"{"});
      control();
    }
    oldToken = token;
    token = token.next;
    codigo();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
      control();
    }
  }

  public boolean operacionCondicional() {
    boolean correcto = true;
    boolean band = true;
    int cont = 1;
    oldToken = token;
    token = token.next;
    while (token.kind != PARENTESISC) {
      if (cont < 4) {
        if (band) {
          if (token.kind != ENTEROS && token.kind != DECIMALES && token.kind != IDENTIFICADOR) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{"ENTEROS", "DECIMALES", "IDENTIFICADOR"});
            control();
            correcto = false;
          }
        } else {
          if (operadoresRelacionales()) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{"<", "<=", ">", ">=", "==", "!="});
            control();
            correcto = false;
          }
        }
        band = !band;
        cont++;
      } else {
        if (token.kind != Y && token.kind != O) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"&&", "||"});
          control();
          correcto = false;
        }
        cont = 1;
      }
      oldToken = token;
      token = token.next;
      if (!correcto) {
        break;
      }
    }
    return correcto;
  }

  public void sentenciaSelector() {
    boolean band = true;
    oldToken = token;
    token = token.next;
    if (token.kind != PARENTESISA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"("});
      control();
      band = false;
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != IDENTIFICADOR) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"IDENTIFICADOR"});
        control();
        band = false;
      }
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != PARENTESISC) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{")"});
        control();
        band = false;
      }
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != LLAVEA) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"{"});
        control();
        band = false;
      }
    }
    oldToken = token;
    token = token.next;
    sentenciasSeleccion();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
      control();
    }
  }

  public void sentenciasSeleccion() {
    ciclo:
    while (token != null) {
      switch (token.kind) {
        case SELECCION -> {
          sentenciaSeleccion();
        }
        case AUTOMATICO -> {
          sentenciaAutomatico();
        }
        case LLAVEC -> {
          break ciclo;
        }
      }
      oldToken = token;
      token = token.next;
    }
  }

  public void sentenciaSeleccion() {
    boolean band = true;
    oldToken = token;
    token = token.next;
    if (token.kind != ENTEROS && token.kind != DECIMALES) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"ENTEROS", "DECIMALES"});
      control();
      band = false;
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != DOSPUNTOS) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{":"});
        control();
        band = false;
      }
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != LLAVEA) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"{"});
        control();
      }
    }
    oldToken = token;
    token = token.next;
    codigo();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
      control();
    }
  }

  public void sentenciaAutomatico() {
    boolean band = true;
    oldToken = token;
    token = token.next;
    if (token.kind != DOSPUNTOS) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{":"});
      control();
      band = false;
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != LLAVEA) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"{"});
        control();
      }
    }
    oldToken = token;
    token = token.next;
    codigo();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
      control();
    }
  }

  public void sentenciaMientras() {
    boolean band = true;
    oldToken = token;
    token = token.next;
    if (token.kind != PARENTESISA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"("});
      control();
      band = false;
    }
    if (band) {
      band = operacionCondicional();
    }
    if (band) {
      if (token.kind != PARENTESISC) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{")"});
        control();
        band = false;
      }
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != LLAVEA) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"{"});
        control();
      }
    }
    oldToken = token;
    token = token.next;
    codigo();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
      control();
    }
  }

  public void sentenciaPara() {
    boolean correcto = true;
    oldToken = token;
    token = token.next;
    if (token.kind != PARENTESISA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"("});
      control();
      correcto = false;
    }
    if (correcto) {
      correcto = condicionalPara();
    }
    if (correcto) {
      if (token.kind != PARENTESISC) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{")"});
        control();
        correcto = false;
      }
    }
    if (correcto) {
      oldToken = token;
      token = token.next;
      if (token.kind != LLAVEA) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"{"});
        control();
      }
    }
    oldToken = token;
    token = token.next;
    codigo();
    if (token.kind != LLAVEC) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"}"});
      control();
    }
  }

  public boolean condicionalPara() {
//    boolean correcto = true;
    oldToken = token;
    token = token.next;
    switch (token.kind) {
      case ENTERO, DECIMAL -> {
        boolean band = true;
        oldToken = token;
        token = token.next;
        if (token.kind != IDENTIFICADOR) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"IDENTIFICADOR"});
          control();
          band = false;
        }
        if (band) {
          oldToken = token;
          token = token.next;
          if (token.kind != IGUAL) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{"="});
            control();
            band = false;
          }
        }
        if (band) {
          oldToken = token;
          token = token.next;
          if (token.kind != ENTEROS && token.kind != DECIMALES && token.kind != IDENTIFICADOR) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{"ENTEROS", "DECIMALES", "IDENTIFICADOR"});
            control();
          }
        }
        oldToken = token;
        token = token.next;
        if (token.kind != PUNTOCOMA) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{";"});
          control();
        }
      }
      case IDENTIFICADOR -> {
        boolean band = true;
        oldToken = token;
        token = token.next;
        if (token.kind != IGUAL) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{"="});
          control();
          band = false;
        }
        if (band) {
          oldToken = token;
          token = token.next;
          if (token.kind != ENTEROS && token.kind != DECIMALES && token.kind != IDENTIFICADOR) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{"ENTEROS", "DECIMALES", "IDENTIFICADOR"});
            control();
          }
        }
        oldToken = token;
        token = token.next;
        if (token.kind != PUNTOCOMA) {
          erroresSintacticos++;
          errorSintactico(token, new String[]{";"});
          control();
        }
      }
      default -> {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"ENTEROS", "DECIMALES", "IDENTIFICADORES"});
        control();
      }
    }
    boolean band = true;
    oldToken = token;
    token = token.next;
    if (token.kind != IDENTIFICADOR) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"IDENTIFICADOR"});
      control();
      band = false;
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (operadoresRelacionales()) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"<", "<=", ">", ">=", "==", "!="});
        control();
        band = false;
      }
    }
    if (band) {
      oldToken = token;
      token = token.next;
      if (token.kind != ENTEROS && token.kind != DECIMALES && token.kind != IDENTIFICADOR) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"ENTEROS", "DECIMALES", "IDENTIFICADOR"});
        control();
      }
    }
    oldToken = token;
    token = token.next;
    if (token.kind != PUNTOCOMA) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{";"});
      control();
    }
    boolean correcto = true;
    oldToken = token;
    token = token.next;
    if (token.kind != IDENTIFICADOR) {
      erroresSintacticos++;
      errorSintactico(token, new String[]{"IDENTIFICADOR"});
      control();
      correcto = false;
    }
    if (correcto) {
      oldToken = token;
      token = token.next;
      if (token.kind != IGUAL) {
        erroresSintacticos++;
        errorSintactico(token, new String[]{"="});
        control();
        correcto = false;
      }
    }
    if (correcto) {
      band = true;
      oldToken = token;
      token = token.next;
      while (token.kind != PARENTESISC) {
        if (band) {
          if (tiposValores() && token.kind != IDENTIFICADOR) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{"ENTEROS", "DECIMALES", "IDENTIFICADOR"});
            control();
            correcto = false;
          }
        } else {
          if (operadoresAritmeticos()) {
            erroresSintacticos++;
            errorSintactico(token, new String[]{"+", "-", "*", "/", "%"});
            control();
            correcto = false;
          }
        }
        band = !band;
        if (!correcto) {
          break;
        }
        oldToken = token;
        token = token.next;
      }
    }
    return correcto;
  }
}
