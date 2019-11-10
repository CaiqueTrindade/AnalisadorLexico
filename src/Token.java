import java.util.HashMap;

public class Token {

    private String lexema;
    private int nLinha;
    private int tipo;
    private HashMap<Integer, String> idTokens;

    public Token(String lexema, int nLinha, int tipo) {
        this.lexema = lexema;
        this.nLinha = nLinha;
        this.tipo = tipo;
        this.idTokens = new HashMap<Integer, String>();
        this.addIds();
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
        return nLinha + " " + lexema + " " + idTokens.get(tipo) + ";";
    }

    private void addIds(){
        idTokens.put(0, "Palavra Reservada");
        idTokens.put(1, "Número Real");
        idTokens.put(2, "Número Inteiro");
        idTokens.put(3, "Identificador");
        idTokens.put(4, "Operador Lógico");
        idTokens.put(5, "Operador Lógico de Negação");
        idTokens.put(6, "Operador Relacional");
        idTokens.put(7, "Operador Relacional de Atribuição");
        idTokens.put(8, "Operador Aritmético");
        idTokens.put(9, "Operador Aritmético de incremento");
        idTokens.put(10, "Delimitador de Comentário");
        idTokens.put(11, "Cadeia de Caracteres");
        idTokens.put(12, ";");
        idTokens.put(13, ",");
        idTokens.put(14, "(");
        idTokens.put(15, ")");
        idTokens.put(16, "[");
        idTokens.put(17, "]");
        idTokens.put(18, "{");
        idTokens.put(19, "}");
        idTokens.put(20, ".");
    }
}
