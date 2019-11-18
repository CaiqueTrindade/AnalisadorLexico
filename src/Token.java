import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que representa um token proveniente da análise léxica realizada pela classe AnalisadorLexico.
 * @author Caique Trindade, Felipe Damasceno e Solenir Figuerêdo
 */
public class Token {

    private String lexema;
    private int nLinha;
    private int tipo;                                  // tipo de token
    private static final Map<Integer, String> idTokens;// Hashmap estatico dos tipos de token
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
        temp.put(12, "Delimitador ;");
        temp.put(13, "Delimitador ,");
        temp.put(14, "Delimitador (");
        temp.put(15, "Delimitador )");
        temp.put(16, "Delimitador [");
        temp.put(17, "Delimitador ]");
        temp.put(18, "Delimitador {");
        temp.put(19, "Delimitador }");
        temp.put(20, "Delimitador .");
        idTokens = Collections.unmodifiableMap(temp);
    }

    /**
     * Construtor da classe
     * @param lexema
     * @param nLinha
     * @param tipo tipo de token
     */
    public Token(String lexema, int nLinha, int tipo) {
        this.lexema = lexema;
        this.nLinha = nLinha;
        this.tipo = tipo;

    }

    /**
     * retorna lexema
     * @return lexema
     */
    public String getLexema() {
        return lexema;
    }

    /**
     * retorna numero da linha atual
     * @return linha atual
     */
    public int getnLinha() {
        return nLinha;
    }

    /**
     * retorna tipo do token
     * @return tipo de token
     */
    public int getTipo() {
        return tipo;
    }
    /**
     * Método toString da classe.
     * @return String no padrão de saída da lista de tokens usado pelo Analisador Léxico.
     */
    public String toString(){
        return nLinha + " " + lexema + " " + this.idToToken(tipo) + ";\n";
    }

    /**
     * Método responsável por retornar o tipo do token de forma nominal.
     * @param id numero do tipo de token
     * @return token de forma nominal
     */
    public static String idToToken(int id){
        return idTokens.get(id);
    }
}
