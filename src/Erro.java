import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Erro {

    private String lexema;
    private int nLinha;
    private int tipo;
    private static final Map<Integer, String> idTokens;
    static {
        Map<Integer, String> temp = new HashMap<Integer, String>();
        temp.put(1, "Número Real");
        temp.put(2, "Número Inteiro");
        temp.put(3, "Identificador");
        temp.put(10, "Delimitador de Comentário");
        temp.put(11, "Cadeia de Caracteres");
        idTokens = Collections.unmodifiableMap(temp);
    }

    public Erro(String lexema, int nLinha, int tipo) {
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
        return nLinha + " " + lexema + " " + "Mal formação de " +this.idToToken(tipo) + ";";
    }

    public static String idToToken(int id){
        return idTokens.get(id);
    }
}
