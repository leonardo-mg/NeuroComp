package programa;

public class SemanticException extends Exception{
      
    public static final long serialVersionUID = 800L;
    
    public SemanticException(String mensaje){
        super(mensaje);
    }
}
