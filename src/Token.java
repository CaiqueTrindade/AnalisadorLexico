import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Token {

    private String lexema;
    private int nLinha;
    private int tipo;
    private static final Map<Integer, String> idTokens;
    static {
        Map<Integer, String> temp = new HashMap<Integer, String>();;
        temp.put(0, "Palavra Reservada");
        temp.put(1, "Número Real");
        temp.put(2, "Número Inteiro");
        temp.put(3, "Identificador");
        temp.put(4, "Operador Lógico");
        temp.put(5, "Operador Lógico de Negação");
        temp.put(6, "Operador Relacional");
        temp.put(7, "Operador Relacional de Atribuição");
        temp.put(8, "Operador Aritmético");
        temp.put(9, "Operador Aritmético de incremento/decremento");
        temp.put(10, "Delimitador de Comentário");
        temp.put(11, "Cadeia de Caracteres");
        temp.put(12, ";");
        temp.put(13, ",");
        temp.put(14, "(");
        temp.put(15, ")");
        temp.put(16, "[");
        temp.put(17, "]");
        temp.put(18, "{");
        temp.put(19, "}");
        temp.put(20, ".");
        idTokens = Collections.unmodifiableMap(temp);
    }

    public Token(String lexema, int nLinha, int tipo) {
        this.lexema = lexema;
        this.nLinha = nLinha;
        this.tipo = tipo;

    }

    public String getLexema() {
        return lexema;
    }

    public int getnLinha() {
        return nLinha;
    }

    public int getTipo() {
        return tipo;
    }

    public String toString(){
        return nLinha + " " + lexema + " " + this.idToToken(tipo) + ";\n";
    }

    public static String idToToken(int id){
        return idTokens.get(id);
    }
}
